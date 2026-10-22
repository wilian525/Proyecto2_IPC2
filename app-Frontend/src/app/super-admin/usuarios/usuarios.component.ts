import { Component, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Administrador, DatosAdministrador, RolAdministrativo } from '../../modelos/administracion';
import { AdministracionServicio } from '../administracion.servicio';
import { FormularioUsuarioComponent } from '../formulario-usuario/formulario-usuario.component';
import { ListaAdministradoresComponent } from '../lista-usuarios/lista-usuarios.component';

@Component({
  selector: 'app-usuarios-administrativos',
  imports: [RouterLink, FormularioUsuarioComponent, ListaAdministradoresComponent],
  templateUrl: './usuarios.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class UsuariosAdministrativosComponent {
  private readonly servicio = inject(AdministracionServicio);
  private readonly ruta = inject(ActivatedRoute);
  private readonly destruir = inject(DestroyRef);
  readonly rol = this.ruta.snapshot.data['rol'] as RolAdministrativo;
  readonly titulo = this.rol === 'SUPER_ADMIN' ? 'Super Administradores' : 'Administradores';
  readonly usuarios = signal<Administrador[]>([]);
  readonly pagina = signal(1);
  readonly total = signal(0);
  readonly limite = 10;
  readonly cargando = signal(false);
  readonly procesando = signal(false);
  readonly seleccionado = signal<Administrador | null>(null);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly formularioVisible = signal(false);

  constructor() { this.cargar(); }

  cargar(): void {
    this.cargando.set(true); this.error.set('');
    this.servicio.consultarAdministradores(this.rol, this.pagina(), this.limite)
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next: r => { this.usuarios.set(r.datos); this.total.set(r.total); this.cargando.set(false); },
        error: e => { this.error.set(this.errorHttp(e)); this.cargando.set(false); }
      });
  }

  nuevaCuenta(): void { this.seleccionado.set(null); this.formularioVisible.set(true); this.mensaje.set(''); }
  editar(u: Administrador): void {
    if (u.idEmpleado === null) {
      this.error.set('Este usuario no tiene empleado asociado. Vincúlalo antes de modificar datos personales.'); return;
    }
    this.seleccionado.set(u); this.formularioVisible.set(true); this.mensaje.set('');
  }
  cancelar(): void { this.formularioVisible.set(false); this.seleccionado.set(null); }

  guardar(datos: DatosAdministrador): void {
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    const elegido = this.seleccionado();
    const peticion = elegido
      ? this.servicio.modificarAdministrador(this.rol, elegido.idUsuario, datos)
      : this.servicio.crearAdministrador(this.rol, datos);
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.cancelar(); this.pagina.set(1); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(this.errorHttp(e)); }
    });
  }

  activar(usuario: Administrador): void {
    if (this.procesando() || usuario.estado !== 'INACTIVO') return;
    if (!window.confirm(`¿Confirmas la reactivación de ${usuario.nombreUsuario}?`)) return;
    this.error.set(''); this.mensaje.set(''); this.procesando.set(true);
    this.servicio.activarAdministrador(this.rol, usuario.idUsuario)
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.cargar(); },
        error: e => { this.procesando.set(false); this.error.set(this.errorHttp(e)); }
      });
  }

  desactivar(usuario: Administrador): void {
    if (this.procesando() || usuario.estado !== 'ACTIVO') return;
    if (!window.confirm(`¿Confirmas la desactivación de ${usuario.nombreUsuario}?`)) return;
    this.error.set(''); this.mensaje.set(''); this.procesando.set(true);
    this.servicio.desactivarAdministrador(this.rol, usuario.idUsuario)
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.cargar(); },
        error: e => { this.procesando.set(false); this.error.set(this.errorHttp(e)); }
      });
  }

  cambiarPagina(p: number): void {
    if (p < 1 || p > Math.max(1, Math.ceil(this.total() / this.limite))) return;
    this.pagina.set(p); this.cargar();
  }
  private errorHttp(e: HttpErrorResponse): string {
    return e.status === 0 ? 'No se pudo conectar con el backend.' :
      e.status === 403 ? 'No tienes permisos para esta acción.' :
      (typeof e.error?.mensaje === 'string' ? e.error.mensaje : 'No fue posible completar la operación.');
  }
}
