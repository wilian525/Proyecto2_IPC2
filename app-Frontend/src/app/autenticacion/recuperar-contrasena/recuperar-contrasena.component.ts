import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { DatosRecuperacion, ErrorServidor } from '../../modelos/autenticacion';
import { PresentacionAccesoComponent } from '../presentacion-acceso/presentacion-acceso.component';

@Component({
  selector: 'app-recuperar-contrasena',
  imports: [FormsModule, RouterLink, PresentacionAccesoComponent],
  templateUrl: './recuperar-contrasena.component.html',
  styleUrl: './recuperar-contrasena.component.css'
})
export class RecuperarContrasenaComponent {
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly cambios = inject(ChangeDetectorRef);

  nombreUsuario = '';
  nombre = '';
  apellido = '';
  fechaVerificacion = '';
  nuevaContrasena = '';
  confirmarContrasena = '';
  mostrarContrasena = false;
  mostrarConfirmacion = false;
  cargando = false;
  completado = false;
  mensajeError = '';
  mensajeExito = '';

  recuperar(): void {
    if (this.cargando || this.completado) return;
    this.mensajeError = '';

    if (!this.nombreUsuario.trim() || !this.nombre.trim() || !this.apellido.trim() ||
        !this.fechaVerificacion || !this.nuevaContrasena) {
      this.mensajeError = 'Completa todos los campos obligatorios.';
      return;
    }
    if (this.nuevaContrasena.length < 8) {
      this.mensajeError = 'La nueva contraseña debe tener al menos 8 caracteres.';
      return;
    }
    if (this.nuevaContrasena !== this.confirmarContrasena) {
      this.mensajeError = 'Las contraseñas no coinciden.';
      return;
    }

    const datos: DatosRecuperacion = {
      nombreUsuario: this.nombreUsuario.trim(),
      nombre: this.nombre.trim(),
      apellido: this.apellido.trim(),
      fechaVerificacion: this.fechaVerificacion,
      nuevaContrasena: this.nuevaContrasena
    };

    this.cargando = true;
    this.autenticacion.recuperarContrasena(datos).subscribe({
      next: respuesta => {
        this.cargando = false;
        this.completado = true;
        this.mensajeExito = respuesta.mensaje || 'Contraseña actualizada correctamente.';
        this.nuevaContrasena = '';
        this.confirmarContrasena = '';
        this.cambios.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.cargando = false;
        const detalle = error.error as ErrorServidor | null;
        if (error.status === 0) {
          this.mensajeError = 'No se pudo conectar con el backend. Verifica que Tomcat esté iniciado.';
        } else if (error.status === 401) {
          this.mensajeError = 'No se pudo verificar tu identidad. Revisa el usuario, nombre, apellido y fecha.';
        } else if (error.status === 409) {
          this.mensajeError = detalle?.mensaje || 'Elige una contraseña distinta a la anterior.';
        } else if (error.status === 400) {
          this.mensajeError = detalle?.mensaje || 'Revisa la información proporcionada.';
        } else {
          this.mensajeError = 'No fue posible actualizar la contraseña. Intenta nuevamente.';
        }
        this.cambios.markForCheck();
      }
    });
  }
}
