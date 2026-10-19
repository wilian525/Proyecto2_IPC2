import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoginRequest } from '../../../../models/auth.model';

@Component({
  selector: 'app-login-form',
  imports: [FormsModule],
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.css'
})
export class LoginFormComponent {
  @Input() cargando = false;
  @Input() mensajeError = '';
  @Output() ingresar = new EventEmitter<LoginRequest>();

  nombreUsuario = '';
  contrasena = '';
  mostrarContrasena = false;

  enviar(): void {
    if (this.cargando || !this.nombreUsuario.trim() || !this.contrasena) return;
    this.ingresar.emit({
      nombreUsuario: this.nombreUsuario.trim(),
      contrasena: this.contrasena
    });
  }
}
