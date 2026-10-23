import { Component } from '@angular/core';
import { TarjetaModuloComponent } from '../../admin/tarjeta-modulo/tarjeta-modulo.component';
@Component({ selector:'app-panel-secretaria', imports:[TarjetaModuloComponent], templateUrl:'./panel.component.html', styleUrl:'../../compartidos/estilos/estilos-administracion.css' })
export class PanelSecretariaComponent {
  readonly modulos = [
    {ruta:'/inicio/secretaria/inscripciones', titulo:'Inscripciones', descripcion:'Inscribir alumnos activos en un grado, sección y ciclo activo.', icono:'bi-journal-check'},
    {ruta:'/inicio/secretaria/pagos', titulo:'Pagos del colegio', descripcion:'Registrar inscripción, mensualidades y actividades.', icono:'bi-cash-coin'},
    {ruta:'/inicio/secretaria/facturas', titulo:'Facturas', descripcion:'Emitir y consultar comprobantes asociados a pagos.', icono:'bi-receipt'},
    {ruta:'/inicio/secretaria/expedientes', titulo:'Expedientes', descripcion:'Consultar datos personales e historial de inscripción.', icono:'bi-person-vcard'},
    {ruta:'/inicio/secretaria/solvencias', titulo:'Solvencias', descripcion:'Consultar saldos y verificar solvencia de estudiantes inscritos.', icono:'bi-patch-check'},
    {ruta:'/inicio/secretaria/reportes', titulo:'Reportes de Secretaría', descripcion:'Solventes, morosos e historial de pagos en tabla y PDF.', icono:'bi-file-earmark-pdf'}
  ];
}
