
import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../../core/services/auth.service';
import { SessionService } from '../../../../core/services/session.service';
import { LoginRequest } from '../../../../models/auth.model';
import { LoginFormComponent } from '../../components/login-form/login-form.component';

@Component({
  selector: 'app-login',
  imports: [LoginFormComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  cargando = false;
  mensajeError = '';

  constructor() {
    if (this.session.estaAutenticado()) {
      void this.router.navigateByUrl('/inicio');
    }
  }

  iniciarSesion(datos: LoginRequest): void {
    this.cargando = true;
    this.mensajeError = '';

    this.authService.iniciarSesion(datos).subscribe({
      next: () => {
        this.cargando = false;
        this.cdr.markForCheck();
        void this.router.navigateByUrl('/inicio');
      },
      error: (error: HttpErrorResponse) => {
        this.cargando = false;

        if (error.status === 0) {
          this.mensajeError = 'No se pudo conectar con el backend. Verifica que Tomcat esté iniciado.';
        } else if (error.status === 401) {
          this.mensajeError = 'Usuario o contraseña incorrectos.';
        } else {
          this.mensajeError = error.error?.mensaje || 'Ocurrió un error al iniciar sesión.';
        }

        this.cdr.markForCheck();
      }
    });
  }
}
