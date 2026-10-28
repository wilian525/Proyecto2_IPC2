import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { MaestroServicio } from '../../servicios/maestro.servicio';
import { PrestamoAlumno } from '../../modelos/alumno';
import { fechaFormulario } from '../../modelos/gestion-colegio';
import { explicarError } from '../../admin/mensajes-error';

@Component({
  selector: 'app-biblioteca-maestro',
  imports: [RouterLink],
  templateUrl: './biblioteca.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class BibliotecaMaestroComponent {
  private readonly servicio = inject(MaestroServicio);
  private readonly destruir = inject(DestroyRef);
  readonly pendientes = signal<PrestamoAlumno[]>([]);
  readonly multas = signal<PrestamoAlumno[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');

  constructor() { this.cargar(); }

  fecha(valor: string | number[] | null): string {
    if (!valor) return '—';
    const fecha = fechaFormulario(valor);
    const [anio, mes, dia] = fecha.split('-');
    return `${dia}/${mes}/${anio}`;
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    forkJoin({pendientes: this.servicio.misPrestamos(), multas: this.servicio.misMultas()})
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next: (r: { pendientes: PrestamoAlumno[]; multas: PrestamoAlumno[] }) => {this.pendientes.set(r.pendientes);this.multas.set(r.multas);this.cargando.set(false);},
        error: (e: unknown) => {this.cargando.set(false);this.error.set(explicarError(e, 'consultar mis préstamos y multas'));}
      });
  }
}
