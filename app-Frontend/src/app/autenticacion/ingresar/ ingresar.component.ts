import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { SesionServicio } from '../../servicios/sesion.servicio';
import { DatosIngreso, ErrorServidor } from '../../modelos/autenticacion';
import { FormularioIngresoComponent } from '../formulario-ingreso/formulario-ingreso.component';
import { PresentacionAccesoComponent } from '../presentacion-acceso/presentacion-acceso.component';

@Component({
  selector: 'app-ingresar',
  imports: [FormularioIngresoComponent, PresentacionAccesoComponent],
  templateUrl: './ingresar.component.html',
  styleUrl: './ingresar.component.css'
})
export class IngresarComponent {
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly sesion = inject(SesionServicio);
  private readonly router = inject(Router);
  private readonly cambios = inject(ChangeDetectorRef);

  cargando = false;
  mensajeError = '';

  constructor() {
    if (this.sesion.estaAutenticado()) void this.router.navigateByUrl('/inicio');
  }

  iniciarSesion(datos: DatosIngreso): void {
    if (this.cargando) return;
    this.cargando = true;
    this.mensajeError = '';

    this.autenticacion.iniciarSesion(datos).subscribe({
      next: () => {
        this.cargando = false;
        this.cambios.markForCheck();
        void this.router.navigateByUrl('/inicio');
      },
      error: (error: HttpErrorResponse) => {
        this.cargando = false;
        const detalle = error.error as ErrorServidor | null;
        if (error.status === 0) {
          this.mensajeError = 'No se pudo conectar con el backend. Verifica que Tomcat esté iniciado.';
        } else if (error.status === 401) {
          this.mensajeError = detalle?.mensaje || 'Usuario o contraseña incorrectos.';
        } else {
          this.mensajeError = detalle?.mensaje || 'Ocurrió un error al iniciar sesión.';
        }
        this.cambios.markForCheck();
      }
    });
  }
}
