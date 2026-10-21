import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AutenticacionServicio } from '../servicios/autenticacion.servicio';
import { SesionServicio } from '../servicios/sesion.servicio';
import { PanelAlumnoComponent } from '../alumno/panel-alumno/panel-alumno.component';

@Component({
  selector: 'app-inicio',
  imports: [PanelAlumnoComponent],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.css'
})
export class InicioComponent {
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly router = inject(Router);
  private readonly sesion = inject(SesionServicio);

  readonly rol = this.sesion.obtenerRol() ?? 'USUARIO';

  salir(): void {
    this.autenticacion.cerrarSesion();
    void this.router.navigateByUrl('/login');
  }
}