import { ConfirmacionServicio } from '../../compartidos/confirmacion/confirmacion.servicio';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EstudianteColegio, fechaFormulario } from '../../modelos/gestion-colegio';
import { GestionColegioServicio } from '../gestion.servicio';
import { explicarError } from '../mensajes-error';

@Component({
  selector: 'app-estudiantes-administrador',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './estudiantes.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class EstudiantesAdministradorComponent {
  private readonly confirmacion = inject(ConfirmacionServicio);
  private readonly servicio = inject(GestionColegioServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly registros = signal<EstudianteColegio[]>([]);
  readonly total = signal(0);
  readonly pagina = signal(1);
  readonly cargando = signal(false);
  readonly procesando = signal(false);
  readonly seleccionado = signal<EstudianteColegio | null>(null);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    fechaNacimiento: ['', Validators.required],
    direccion: ['', Validators.required],
    telefono: ['', Validators.required],
    correo: ['', [Validators.required, Validators.email]],
    informacionMedica: ['Ninguna'],
    datosEncargados: ['', Validators.required],
    nombreUsuario: ['', Validators.required],
    contrasena: ['', Validators.minLength(8)]
  });
  constructor() { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.servicio.estudiantes(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.registros.set(r.datos); this.total.set(r.total); this.cargando.set(false); },
      error: e => { this.error.set(explicarError(e, 'consultar estudiantes')); this.cargando.set(false); }
    });
  }
  editar(e: EstudianteColegio): void {
    this.seleccionado.set(e);
    this.formulario.reset({
      nombre: e.nombre, apellido: e.apellido, fechaNacimiento: fechaFormulario(e.fechaNacimiento),
      direccion: e.direccion, telefono: e.telefono, correo: e.correo,
      informacionMedica: e.informacionMedica, datosEncargados: e.datosEncargados,
      nombreUsuario: '', contrasena: ''
    });
    this.formulario.controls.nombreUsuario.disable();
    this.formulario.controls.contrasena.disable();
    this.mensaje.set(''); this.error.set('');
  }
  limpiar(): void {
    this.seleccionado.set(null);
    this.formulario.controls.nombreUsuario.enable();
    this.formulario.controls.contrasena.enable();
    this.formulario.reset({ nombre:'', apellido:'', fechaNacimiento:'', direccion:'', telefono:'', correo:'', informacionMedica:'Ninguna', datosEncargados:'', nombreUsuario:'', contrasena:'' });
  }
  guardar(): void {
    this.formulario.markAllAsTouched();
    if (this.procesando() || this.formulario.invalid) return;
    const d = this.formulario.getRawValue();
    const actual = this.seleccionado();
    if (!actual && d.contrasena.length < 8) { this.error.set('La contraseña inicial debe tener al menos 8 caracteres.'); return; }
    const estudiante = {
      nombre: d.nombre.trim(), apellido: d.apellido.trim(), fechaNacimiento: d.fechaNacimiento,
      direccion: d.direccion.trim(), telefono: d.telefono.trim(), correo: d.correo.trim(),
      informacionMedica: d.informacionMedica.trim(), datosEncargados: d.datosEncargados.trim()
    };
    const peticion = actual ? this.servicio.modificarEstudiante(actual.idEstudiante, estudiante)
      : this.servicio.crearEstudiante(estudiante, { nombreUsuario: d.nombreUsuario.trim(), contrasena: d.contrasena, rol: 'ESTUDIANTE' });
    this.procesando.set(true); this.mensaje.set(''); this.error.set('');
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.limpiar(); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, actual ? 'modificar estudiante' : 'crear estudiante')); }
    });
  }
  async cambiarEstado(e: EstudianteColegio, estado: 'ACTIVO' | 'INACTIVO' | 'GRADUADO'): Promise<void> {
    if (this.procesando() || estado === e.estado) return;
    if (e.estado === 'GRADUADO') { this.error.set('No es posible reactivar a un estudiante graduado.'); return; }
    if (!await this.confirmacion.confirmar(`¿Cambiar a ${estado} el estado de ${e.nombre} ${e.apellido}?`)) return;
    this.procesando.set(true); this.mensaje.set(''); this.error.set('');
    this.servicio.cambiarEstadoEstudiante(e.idEstudiante, estado).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.cargar(); },
      error: error => { this.procesando.set(false); this.error.set(explicarError(error, 'cambiar el estado del estudiante')); }
    });
  }
  anterior(): void { if (this.pagina()>1) {this.pagina.update(p=>p-1);this.cargar();} }
  siguiente(): void { if (this.pagina()*10<this.total()) {this.pagina.update(p=>p+1);this.cargar();} }
}
