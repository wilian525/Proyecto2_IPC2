import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Alumno } from '../../modelos/alumno';
import { AlumnoServicio } from '../alumno.servicio';
import { TarjetaConsultaComponent } from '../tarjeta-consulta/tarjeta-consulta.component';

@Component({
  selector: 'app-panel-alumno',
  imports: [RouterLink, TarjetaConsultaComponent],
  templateUrl: './panel-alumno.component.html',
  styleUrl: './panel-alumno.component.css'
})
export class PanelAlumnoComponent {
  private readonly servicio = inject(AlumnoServicio);
  private readonly destruir = inject(DestroyRef);

  readonly perfil = signal<Alumno | null>(null);
  readonly cargando = signal(true);
  readonly error = signal('');

  constructor() { this.cargarPerfil(); }

  cargarPerfil(): void {
    this.cargando.set(true);
    this.error.set('');
    this.servicio.obtenerPerfil().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: alumno => { this.perfil.set(alumno); this.cargando.set(false); },
      error: (respuesta: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(respuesta.error?.mensaje ?? 'No se pudo cargar tu información. Revisa la conexión con el servidor.');
      }
    });
  }
}
