import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MaestroServicio } from '../../servicios/maestro.servicio';
import { AlumnoNomina, AsignacionMaestro } from '../../modelos/maestro';
import { explicarError } from '../../admin/mensajes-error';

@Component({ selector: 'app-cursos-maestro', imports: [RouterLink],
  templateUrl: './cursos.component.html', styleUrl: '../../compartidos/estilos/estilos-administracion.css' })
export class CursosMaestroComponent {
  private readonly servicio = inject(MaestroServicio);
  private readonly destruir = inject(DestroyRef);
  readonly cursos = signal<AsignacionMaestro[]>([]);
  readonly nomina = signal<AlumnoNomina[]>([]);
  readonly seleccionado = signal<AsignacionMaestro | null>(null);
  readonly cargando = signal(false);
  readonly error = signal('');
  constructor() { this.cargar(); }
  cargar(): void {
    this.cargando.set(true); this.error.set('');
    this.servicio.misCursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: datos => { this.cursos.set(datos); this.cargando.set(false); },
      error: e => { this.error.set(explicarError(e, 'consultar mis cursos')); this.cargando.set(false); }
    });
  }
  verNomina(curso: AsignacionMaestro): void {
    this.seleccionado.set(curso); this.nomina.set([]); this.error.set('');
    this.servicio.nomina(curso.idAsignacion).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: datos => this.nomina.set(datos), error: e => this.error.set(explicarError(e, 'consultar la nómina'))
    });
  }
}
