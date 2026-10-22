import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CicloEscolar } from '../../modelos/administracion';
import { AdministracionServicio } from '../administracion.servicio';
import { FormularioCicloComponent } from '../formulario-ciclo/formulario-ciclo.component';

@Component({
  selector: 'app-ciclos',
  imports: [RouterLink, FormularioCicloComponent],
  templateUrl: './ciclos.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class CiclosComponent {
  private readonly servicio = inject(AdministracionServicio);
  private readonly destruir = inject(DestroyRef);

  readonly ciclos = signal<CicloEscolar[]>([]);
  readonly pagina = signal(1);
  readonly limite = 10;
  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.ciclos().length / this.limite)));
  readonly visibles = computed(() => this.ciclos().slice((this.pagina() - 1) * this.limite, this.pagina() * this.limite));
  readonly tieneCicloActivo = computed(() => this.ciclos().some(ciclo => ciclo.estado === 'ACTIVO'));
  readonly cargando = signal(true);
  readonly procesando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');

  constructor() {
    this.cargar();
  }

  cargar(): void {
    if (this.procesando()) return;
    this.cargando.set(true);
    this.error.set('');
    this.servicio.consultarCiclos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: respuesta => {
        this.ciclos.set(respuesta);
        this.pagina.set(Math.min(this.pagina(), this.totalPaginas()));
        this.cargando.set(false);
      },
      error: error => {
        this.error.set(this.mensajeError(error, 'consultar los años lectivos'));
        this.cargando.set(false);
      }
    });
  }

  crear(datos: { anioInicio: number; anioFin: number }): void {
    if (this.procesando()) return;
    if (this.tieneCicloActivo()) {
      this.error.set('Debes desactivar el año lectivo actual antes de registrar otro.');
      return;
    }
    this.procesando.set(true);
    this.error.set('');
    this.mensaje.set('');
    this.servicio.crearCiclo(datos).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: respuesta => this.finalizarOperacion(respuesta.mensaje),
      error: error => this.finalizarError(error, 'registrar el año lectivo')
    });
  }

  activar(ciclo: CicloEscolar): void {
    if (this.procesando() || this.cargando()) return;
    if (this.tieneCicloActivo()) {
      this.error.set('Debes desactivar el año lectivo actual antes de activar otro.');
      return;
    }
    if (!window.confirm(`¿Activar el año lectivo ${ciclo.anioInicio} - ${ciclo.anioFin}?`)) return;

    this.procesando.set(true);
    this.error.set('');
    this.mensaje.set('');
    this.servicio.activarCiclo(ciclo.idCiclo).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: respuesta => this.finalizarOperacion(respuesta.mensaje),
      error: error => this.finalizarError(error, 'activar el año lectivo')
    });
  }

  desactivar(ciclo: CicloEscolar): void {
    if (this.procesando() || this.cargando()) return;
    if (!window.confirm(`¿Desactivar el año lectivo ${ciclo.anioInicio} - ${ciclo.anioFin}?`)) return;

    this.procesando.set(true);
    this.error.set('');
    this.mensaje.set('');
    this.servicio.desactivarCiclo(ciclo.idCiclo).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: respuesta => this.finalizarOperacion(respuesta.mensaje),
      error: error => this.finalizarError(error, 'desactivar el año lectivo')
    });
  }

  private finalizarOperacion(mensaje: string): void {
    this.procesando.set(false);
    this.mensaje.set(mensaje || 'Operación realizada correctamente.');
    this.cargar();
  }

  private finalizarError(error: HttpErrorResponse, accion: string): void {
    this.procesando.set(false);
    this.error.set(this.mensajeError(error, accion));
  }

  private mensajeError(error: HttpErrorResponse, accion: string): string {
    const mensajeServidor = typeof error.error?.mensaje === 'string' ? error.error.mensaje : '';
    let detalle: string;

    switch (error.status) {
      case 0:
        detalle = 'No se pudo conectar con el servidor. Comprueba que Tomcat esté iniciado.';
        break;
      case 400:
        detalle = mensajeServidor || 'Los datos enviados no son válidos.';
        break;
      case 401:
        detalle = 'Tu sesión ha expirado. Vuelve a iniciar sesión.';
        break;
      case 403:
        detalle = 'Tu usuario no tiene permiso para realizar esta operación.';
        break;
      case 404:
        detalle = mensajeServidor || 'No se encontró el año lectivo solicitado o la ruta del servidor.';
        break;
      case 409:
        detalle = mensajeServidor || 'La operación entra en conflicto con el estado actual del año lectivo.';
        break;
      default:
        detalle = error.status >= 500
          ? 'El backend no pudo completar la operación. Revisa el registro de Tomcat.'
          : mensajeServidor || 'Ocurrió un error inesperado. Intenta nuevamente.';
    }

    return `No se pudo ${accion}: ${detalle}`;
  }
}
