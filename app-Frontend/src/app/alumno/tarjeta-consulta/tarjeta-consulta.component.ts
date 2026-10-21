import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-tarjeta-consulta',
  imports: [RouterLink],
  templateUrl: './tarjeta-consulta.component.html',
  styleUrl: './tarjeta-consulta.component.css'
})
export class TarjetaConsultaComponent {
  @Input({ required: true }) titulo = '';
  @Input({ required: true }) descripcion = '';
  @Input({ required: true }) ruta = '';
  @Input({ required: true }) icono = '';
  @Input() color: 'rosa' | 'verde' | 'violeta' = 'rosa';
}
