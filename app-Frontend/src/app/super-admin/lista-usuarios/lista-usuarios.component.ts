import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Administrador } from '../../modelos/administracion';

@Component({
  selector: 'app-lista-administradores',
  imports: [DecimalPipe],
  templateUrl: './lista-usuarios.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class ListaAdministradoresComponent {
  @Input() datos: Administrador[] = [];
  @Input() cargando = false;
  @Input() procesando = false;
  @Input() pagina = 1;
  @Input() limite = 10;
  @Input() total = 0;
  @Output() editar = new EventEmitter<Administrador>();
  @Output() desactivar = new EventEmitter<Administrador>();
  @Output() activar = new EventEmitter<Administrador>();
  @Output() cambiarPagina = new EventEmitter<number>();

  get ultimaPagina(): number { return Math.max(1, Math.ceil(this.total / this.limite)); }
}
