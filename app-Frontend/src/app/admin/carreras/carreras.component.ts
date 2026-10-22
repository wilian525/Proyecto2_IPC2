import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CarreraColegio } from '../../modelos/gestion-colegio';
import { AdministracionServicio } from '../../super-admin/administracion.servicio';
import { GestionColegioServicio } from '../gestion.servicio';
import { explicarError } from '../mensajes-error';

@Component({
  selector: 'app-carreras-administrador',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './carreras.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class CarrerasAdministradorComponent {
  private readonly servicio = inject(GestionColegioServicio);
  private readonly administracion = inject(AdministracionServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly carreras = signal<CarreraColegio[]>([]);
  readonly pagina = signal(1);
  readonly paginas = computed(() => Math.max(1, Math.ceil(this.carreras().length / 10)));
  readonly visibles = computed(() => this.carreras().slice((this.pagina()-1)*10, this.pagina()*10));
  readonly nivelDiversificado = signal(0);
  readonly seleccionada = signal<CarreraColegio | null>(null);
  readonly cargando = signal(false);
  readonly procesando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly formulario = this.fb.nonNullable.group({ nombre: ['', [Validators.required, Validators.maxLength(100)]] });

  constructor() {
    this.administracion.consultarNiveles().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: niveles => this.nivelDiversificado.set(niveles.find(n => n.nombre.toUpperCase() === 'DIVERSIFICADO')?.idNivel ?? 0),
      error: e => this.error.set(explicarError(e, 'consultar los niveles'))
    });
    this.cargar();
  }
  cargar(): void {
    this.cargando.set(true);
    this.servicio.carreras().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: datos => { this.carreras.set(datos); this.pagina.set(Math.min(this.pagina(), this.paginas())); this.cargando.set(false); },
      error: e => { this.error.set(explicarError(e, 'consultar las carreras')); this.cargando.set(false); }
    });
  }
  editar(carrera: CarreraColegio): void {
    this.seleccionada.set(carrera);
    this.formulario.patchValue({ nombre: carrera.nombre });
    this.mensaje.set(''); this.error.set('');
  }
  limpiar(): void { this.seleccionada.set(null); this.formulario.reset(); }
  guardar(): void {
    this.formulario.markAllAsTouched();
    if (this.procesando() || this.formulario.invalid) return;
    if (!this.nivelDiversificado()) { this.error.set('No se encontró el nivel Diversificado en el servidor.'); return; }
    const datos = { nombre: this.formulario.getRawValue().nombre.trim(), idNivel: this.nivelDiversificado() };
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    const actual = this.seleccionada();
    const peticion = actual ? this.servicio.modificarCarrera(actual.idCarrera, datos) : this.servicio.crearCarrera(datos);
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.limpiar(); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, actual ? 'modificar la carrera' : 'crear la carrera')); }
    });
  }
  cambiarEstado(carrera: CarreraColegio): void {
    if (this.procesando()) return;
    const activar = carrera.estado === 'INACTIVO';
    if (!window.confirm(`¿${activar ? 'Activar' : 'Desactivar'} la carrera ${carrera.nombre}?`)) return;
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    const peticion = activar ? this.servicio.activarCarrera(carrera.idCarrera) : this.servicio.desactivarCarrera(carrera.idCarrera);
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, activar ? 'activar la carrera' : 'desactivar la carrera')); }
    });
  }
}
