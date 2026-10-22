import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-tarjeta-modulo',
  imports: [RouterLink],
  template: `<a class="tarjeta-accion" [routerLink]="ruta">
    <i class="bi {{ icono }}"></i><div><strong>{{ titulo }}</strong><p>{{ descripcion }}</p></div>
  </a>`,
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class TarjetaModuloComponent {
  @Input({ required: true }) ruta = '';
  @Input({ required: true }) titulo = '';
  @Input({ required: true }) descripcion = '';
  @Input() icono = 'bi-folder';
}
