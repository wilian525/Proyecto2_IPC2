import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SeccionColegio } from '../../modelos/gestion-colegio';
import { GestionColegioServicio } from '../gestion.servicio';
import { explicarError } from '../mensajes-error';
import { AdministracionServicio } from '../../super-admin/administracion.servicio';
import { GradoEscolar } from '../../modelos/administracion';

@Component({
  selector: 'app-admin-secciones',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './secciones.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class SeccionesAdministradorComponent {
  private readonly servicio = inject(GestionColegioServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  private readonly administracion = inject(AdministracionServicio);
  readonly grados = signal<GradoEscolar[]>([]);
  gradoNombre(id: number): string { return this.grados().find(g => g.idGrado === id)?.nombre ?? `Grado #${id}`; }
  readonly registros = signal<SeccionColegio[]>([]);
  readonly pagina = signal(1);
  readonly paginas = computed(() => Math.max(1, Math.ceil(this.registros().length / 10)));
  readonly visibles = computed(() => this.registros().slice((this.pagina()-1)*10, this.pagina()*10));
  readonly seleccionado = signal<SeccionColegio | null>(null);
  readonly cargando = signal(false);
  readonly procesando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    idGrado: [0, [Validators.min(1)]]
  });
  constructor() {
    this.administracion.consultarGrados().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: datos => this.grados.set(datos), error: e => this.error.set(explicarError(e, 'consultar grados'))
    });
    this.cargar();
  }
  cargar(): void {
    this.cargando.set(true);
    this.servicio.secciones().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: datos => { this.registros.set(datos); this.pagina.set(Math.min(this.pagina(), this.paginas())); this.cargando.set(false); },
      error: e => { this.cargando.set(false); this.error.set(explicarError(e, 'consultar secciones')); }
    });
  }
  editar(registro: SeccionColegio): void {
    this.seleccionado.set(registro);
    this.formulario.patchValue({ nombre: registro.nombre, idGrado: registro.idGrado });
    this.error.set(''); this.mensaje.set('');
  }
  limpiar(): void { this.seleccionado.set(null); this.formulario.reset(); }
  guardar(): void {
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid || this.procesando()) return;
    const form = this.formulario.getRawValue();
    const datos = { nombre: form.nombre.trim(), idGrado: form.idGrado };
    const actual = this.seleccionado();
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    const peticion = actual
      ? this.servicio.modificarSeccion(actual.idSeccion, datos)
      : this.servicio.crearSeccion(datos);
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.limpiar(); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, actual ? 'modificar seccion' : 'crear seccion')); }
    });
  }
}
