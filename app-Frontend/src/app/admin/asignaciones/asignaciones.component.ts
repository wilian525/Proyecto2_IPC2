import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AsignacionColegio, CursoColegio, SeccionColegio, EmpleadoColegio } from '../../modelos/gestion-colegio';
import { CicloEscolar } from '../../modelos/administracion';
import { GestionColegioServicio } from '../gestion.servicio';
import { AdministracionServicio } from '../../super-admin/administracion.servicio';
import { explicarError } from '../mensajes-error';

@Component({
  selector: 'app-asignaciones-administrador',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './asignaciones.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class AsignacionesAdministradorComponent {
  private readonly servicio = inject(GestionColegioServicio);
  private readonly administracion = inject(AdministracionServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly maestros = signal<EmpleadoColegio[]>([]);
  readonly cursos = signal<CursoColegio[]>([]);
  readonly secciones = signal<SeccionColegio[]>([]);
  readonly ciclos = signal<CicloEscolar[]>([]);
  readonly asignaciones = signal<AsignacionColegio[]>([]);
  readonly procesando = signal(false);
  readonly consultando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly formulario = this.fb.nonNullable.group({
    idEmpleado: [0, Validators.min(1)], idCurso: [0, Validators.min(1)],
    idSeccion: [0, Validators.min(1)], idCiclo: [0, Validators.min(1)]
  });
  constructor() {
    this.servicio.empleados(1, 100).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => this.maestros.set(r.datos.filter(e => e.puesto.toUpperCase()==='MAESTRO' && e.estado==='ACTIVO')),
      error: e => this.error.set(explicarError(e, 'consultar maestros'))
    });
    this.servicio.cursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => this.cursos.set(r), error: e => this.error.set(explicarError(e, 'consultar cursos')) });
    this.servicio.secciones().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => this.secciones.set(r), error: e => this.error.set(explicarError(e, 'consultar secciones')) });
    this.administracion.consultarCiclos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => this.ciclos.set(r), error: e => this.error.set(explicarError(e, 'consultar años lectivos')) });
  }
  consultar(): void {
    const id = this.formulario.controls.idEmpleado.value;
    if (!id) { this.error.set('Selecciona un maestro para consultar sus asignaciones.'); return; }
    this.consultando.set(true); this.error.set('');
    this.servicio.asignacionesMaestro(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: datos => { this.asignaciones.set(datos); this.consultando.set(false); },
      error: e => { this.error.set(explicarError(e, 'consultar asignaciones')); this.consultando.set(false); }
    });
  }
  nombreCurso(id: number): string { return this.cursos().find(c=>c.idCurso===id)?.nombre ?? `Curso #${id}`; }
  nombreSeccion(id: number): string { return this.secciones().find(s=>s.idSeccion===id)?.nombre ?? `Sección #${id}`; }
  guardar(): void {
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid || this.procesando()) return;
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    this.servicio.asignarMaestro(this.formulario.getRawValue()).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.consultar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, 'asignar el maestro')); }
    });
  }
}
