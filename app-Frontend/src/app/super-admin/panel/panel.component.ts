import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AdministracionServicio } from '../administracion.servicio';

@Component({
  selector: 'app-panel-super-administrador',
  imports: [RouterLink],
  templateUrl: './panel.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class PanelSuperAdministradorComponent {
  private readonly servicio = inject(AdministracionServicio);
  private readonly destruir = inject(DestroyRef);
  readonly superAdministradores = signal<number | null>(null);
  readonly administradores = signal<number | null>(null);
  readonly ciclos = signal<number | null>(null);
  readonly grados = signal<number | null>(null);
  readonly error = signal('');

  constructor() { this.cargarResumen(); }

  cargarResumen(): void {
    this.error.set('');
    this.servicio.consultarAdministradores('SUPER_ADMIN', 1, 1)
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next: r => this.superAdministradores.set(r.total),
        error: () => this.error.set('No se pudieron consultar los usuarios. Comprueba que instalaste la actualización del backend.')
      });
    this.servicio.consultarAdministradores('ADMIN', 1, 1)
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({ next:r=>this.administradores.set(r.total), error:()=>{} });
    this.servicio.consultarCiclos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => this.ciclos.set(r.length), error:()=>{}
    });
    this.servicio.consultarGrados().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => this.grados.set(r.length), error:()=>{}
    });
  }
}
