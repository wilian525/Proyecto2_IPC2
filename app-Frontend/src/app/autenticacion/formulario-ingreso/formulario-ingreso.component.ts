import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatosIngreso } from '../../modelos/autenticacion';

@Component({
  selector: 'app-formulario-ingreso',
  imports: [FormsModule, RouterLink],
  templateUrl: './formulario-ingreso.component.html',
  styleUrl: './formulario-ingreso.component.css'
})
export class FormularioIngresoComponent {
  @Input() cargando = false;
  @Input() mensajeError = '';
  @Output() ingresar = new EventEmitter<DatosIngreso>();

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
