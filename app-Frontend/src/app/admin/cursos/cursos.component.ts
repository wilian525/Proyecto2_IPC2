import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CursoColegio } from '../../modelos/gestion-colegio';
import { GestionColegioServicio } from '../gestion.servicio';
import { explicarError } from '../mensajes-error';

@Component({
  selector: 'app-admin-cursos',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './cursos.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class CursosAdministradorComponent {
  private readonly servicio = inject(GestionColegioServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly registros = signal<CursoColegio[]>([]);
  readonly pagina = signal(1);
  readonly paginas = computed(() => Math.max(1, Math.ceil(this.registros().length / 10)));
  readonly visibles = computed(() => this.registros().slice((this.pagina()-1)*10, this.pagina()*10));
  readonly seleccionado = signal<CursoColegio | null>(null);
  readonly cargando = signal(false);
  readonly procesando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: ['', [Validators.maxLength(250)]]
  });
  constructor() {
    this.cargar();
  }
  cargar(): void {
    this.cargando.set(true);
    this.servicio.cursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: datos => { this.registros.set(datos); this.pagina.set(Math.min(this.pagina(), this.paginas())); this.cargando.set(false); },
      error: e => { this.cargando.set(false); this.error.set(explicarError(e, 'consultar cursos')); }
    });
  }
  editar(registro: CursoColegio): void {
    this.seleccionado.set(registro);
    this.formulario.patchValue({ nombre: registro.nombre, descripcion: registro.descripcion });
    this.error.set(''); this.mensaje.set('');
  }
  limpiar(): void { this.seleccionado.set(null); this.formulario.reset(); }
  guardar(): void {
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid || this.procesando()) return;
    const form = this.formulario.getRawValue();
    const datos = { nombre: form.nombre.trim(), descripcion: form.descripcion };
    const actual = this.seleccionado();
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    const peticion = actual
      ? this.servicio.modificarCurso(actual.idCurso, datos)
      : this.servicio.crearCurso(datos);
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.limpiar(); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, actual ? 'modificar curso' : 'crear curso')); }
    });
  }
}
