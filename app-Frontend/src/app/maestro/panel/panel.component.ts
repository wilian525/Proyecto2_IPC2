import { Component } from '@angular/core';
import { TarjetaModuloComponent } from '../../admin/tarjeta-modulo/tarjeta-modulo.component';

@Component({selector: 'app-panel-maestro', imports: [TarjetaModuloComponent],
  templateUrl: './panel.component.html', styleUrl: '../../compartidos/estilos/estilos-administracion.css'})
export class PanelMaestroComponent {
  readonly modulos = [
    {ruta: '/inicio/maestro/cursos', titulo: 'Mis cursos y alumnos', descripcion: 'Consulta tus asignaciones y nóminas.', icono: 'bi-journals'},
    {ruta: '/inicio/maestro/asistencia', titulo: 'Asistencia diaria', descripcion: 'Registra asistencia en tus secciones.', icono: 'bi-calendar-check'},
    {ruta: '/inicio/maestro/zona', titulo: 'Ponderaciones', descripcion: 'Define la zona de tus cursos.', icono: 'bi-pie-chart'},
    {ruta: '/inicio/maestro/notas', titulo: 'Notas', descripcion: 'Ingresa o modifica notas del ciclo activo.', icono: 'bi-journal-check'},
    {ruta: '/inicio/maestro/rendimiento', titulo: 'Rendimiento académico', descripcion: 'Promedios y reporte PDF de tus cursos.', icono: 'bi-file-earmark-pdf'},
    {ruta: '/inicio/maestro/biblioteca', titulo: 'Mi biblioteca', descripcion: 'Consultar libros pendientes y multas de biblioteca.', icono: 'bi-book'}
  ];
}
