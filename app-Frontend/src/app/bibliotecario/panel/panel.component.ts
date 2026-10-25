import { Component } from '@angular/core';
import { TarjetaModuloComponent } from '../../admin/tarjeta-modulo/tarjeta-modulo.component';
@Component({selector:'app-panel-bibliotecario',imports:[TarjetaModuloComponent],templateUrl:'./panel.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class PanelBibliotecarioComponent {
  readonly modulos=[
    {ruta:'/inicio/bibliotecario/libros',titulo:'Libros',descripcion:'Registrar, consultar y gestionar libros.',icono:'bi-book'},
    {ruta:'/inicio/bibliotecario/prestamos',titulo:'Préstamos y devoluciones',descripcion:'Prestar libros y registrar devoluciones.',icono:'bi-journal-arrow-up'},
    {ruta:'/inicio/bibliotecario/atrasos',titulo:'Atrasos y avisos',descripcion:'Controlar retrasos y multas.',icono:'bi-clock-history'},
    {ruta:'/inicio/bibliotecario/reportes',titulo:'Reporte de mora',descripcion:'Consultar y exportar PDF con JasperReports.',icono:'bi-file-earmark-pdf'}
  ];
}
