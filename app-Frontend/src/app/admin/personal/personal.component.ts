import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EmpleadoColegio, fechaFormulario } from '../../modelos/gestion-colegio';
import { GestionColegioServicio } from '../gestion.servicio';
import { explicarError } from '../mensajes-error';

@Component({
  selector: 'app-personal-administrador',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './personal.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class PersonalAdministradorComponent {
  private readonly servicio = inject(GestionColegioServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly empleados = signal<EmpleadoColegio[]>([]);
  readonly total = signal(0);
  readonly pagina = signal(1);
  readonly cargando = signal(false);
  readonly procesando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly seleccionado = signal<EmpleadoColegio | null>(null);
  readonly roles = ['SECRETARIA', 'MAESTRO', 'BIBLIOTECARIO', 'MANTENIMIENTO', 'LIMPIEZA'];
  readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    puesto: ['SECRETARIA', Validators.required],
    fechaContratacion: ['', Validators.required],
    salario: [0, [Validators.required, Validators.min(0)]],
    nombreUsuario: ['', [Validators.required, Validators.minLength(3)]],
    contrasena: ['', Validators.minLength(8)]
  });
  constructor() { this.cargar(); }
  cargar(): void {
    this.cargando.set(true);
    this.servicio.empleados(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => {
        this.empleados.set(r.datos.filter(e => this.roles.includes(e.puesto.toUpperCase())));
        this.total.set(r.total); this.cargando.set(false);
      },
      error: e => { this.error.set(explicarError(e, 'consultar empleados')); this.cargando.set(false); }
    });
  }
  editar(e: EmpleadoColegio): void {
    this.seleccionado.set(e);
    this.formulario.reset({
      nombre: e.nombre, apellido: e.apellido, puesto: e.puesto,
      fechaContratacion: fechaFormulario(e.fechaContratacion), salario: e.salario,
      nombreUsuario: '', contrasena: ''
    });
    // El puesto determina el rol al crear; cambiarlo luego requeriría actualizar USUARIO también.
    this.formulario.controls.puesto.disable();
    this.formulario.controls.nombreUsuario.disable();
    this.formulario.controls.contrasena.disable();
    this.error.set(''); this.mensaje.set('');
  }
  limpiar(): void {
    this.seleccionado.set(null);
    this.formulario.controls.puesto.enable();
    this.formulario.controls.nombreUsuario.enable();
    this.formulario.controls.contrasena.enable();
    this.formulario.reset({ nombre: '', apellido: '', puesto: 'SECRETARIA', fechaContratacion: '', salario: 0, nombreUsuario: '', contrasena: '' });
  }
  guardar(): void {
    this.formulario.markAllAsTouched();
    if (this.procesando() || this.formulario.invalid) return;
    const d = this.formulario.getRawValue();
    const actual = this.seleccionado();
    if (!actual && (!d.nombreUsuario.trim() || d.contrasena.length < 8)) {
      this.error.set('Para crear una cuenta necesitas usuario y contraseña de al menos 8 caracteres.'); return;
    }
    const empleado = {
      nombre: d.nombre.trim(), apellido: d.apellido.trim(), puesto: d.puesto,
      fechaContratacion: d.fechaContratacion, salario: Number(d.salario)
    };
    const peticion = actual ? this.servicio.modificarEmpleado(actual.idEmpleado, empleado)
      : this.servicio.crearEmpleado(empleado, { nombreUsuario: d.nombreUsuario.trim(), contrasena: d.contrasena, rol: d.puesto });
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.limpiar(); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, actual ? 'modificar el empleado' : 'registrar el empleado y su cuenta')); }
    });
  }
  desactivar(e: EmpleadoColegio): void {
    if (this.procesando() || e.estado !== 'ACTIVO') return;
    if (!window.confirm(`¿Desactivar a ${e.nombre} ${e.apellido}? Se bloqueará también su cuenta asociada.`)) return;
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    this.servicio.desactivarEmpleado(e.idEmpleado).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.cargar(); },
      error: error => { this.procesando.set(false); this.error.set(explicarError(error, 'desactivar al empleado')); }
    });
  }
  activar(e: EmpleadoColegio): void {
    if (this.procesando() || !window.confirm(`¿Reactivar a ${e.nombre} ${e.apellido} y su cuenta de acceso?`)) return;
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    this.servicio.activarEmpleado(e.idEmpleado).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.cargar(); },
      error: err => { this.procesando.set(false); this.error.set(explicarError(err, 'activar al empleado')); }
    });
  }
  anterior(): void { if (this.pagina() > 1) { this.pagina.update(p => p-1); this.cargar(); } }
  siguiente(): void { if (this.pagina()*10 < this.total()) { this.pagina.update(p => p+1); this.cargar(); } }
}
