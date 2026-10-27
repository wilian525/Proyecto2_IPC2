import { Component, HostListener, inject } from '@angular/core';
import { ConfirmacionServicio } from './confirmacion.servicio';

@Component({
  selector: 'app-confirmacion',
  templateUrl: './confirmacion.component.html',
  styleUrl: './confirmacion.component.css'
})
export class ConfirmacionComponent {
  readonly confirmacion = inject(ConfirmacionServicio);

  @HostListener('document:keydown.escape')
  cancelarConEscape(): void {
    if (this.confirmacion.mensaje()) this.confirmacion.resolver(false);
  }
}
