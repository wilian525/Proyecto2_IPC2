import { Component } from '@angular/core';
import { TarjetaModuloComponent } from '../tarjeta-modulo/tarjeta-modulo.component';

@Component({
  selector: 'app-panel-administrador',
  imports: [TarjetaModuloComponent],
  templateUrl: './panel.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class PanelAdministradorComponent {
  readonly modulos = [
    { ruta: '/inicio/admin/personal', titulo: 'Personal del colegio', descripcion: 'Crear cuentas de secretaria, maestro, biblioteca y mantenimiento; actualizar empleados.', icono: 'bi-people' },
    { ruta: '/inicio/admin/estudiantes', titulo: 'Estudiantes', descripcion: 'Registrar, editar y administrar el estado de los alumnos.', icono: 'bi-person-vcard' },
    { ruta: '/inicio/admin/carreras', titulo: 'Carreras', descripcion: 'Administrar carreras de Diversificado, activar y desactivar.', icono: 'bi-mortarboard' },
    { ruta: '/inicio/admin/cursos', titulo: 'Cursos', descripcion: 'Crear y actualizar el catálogo de cursos.', icono: 'bi-journals' },
    { ruta: '/inicio/admin/secciones', titulo: 'Secciones', descripcion: 'Gestionar secciones y su grado correspondiente.', icono: 'bi-grid' },
    { ruta: '/inicio/admin/grados', titulo: 'Grados', descripcion: 'Consultar y configurar grados; incluir Diversificado.', icono: 'bi-diagram-3' },
    { ruta: '/inicio/admin/curriculos', titulo: 'Currículos', descripcion: 'Definir los cursos de cada grado y ciclo.', icono: 'bi-journal-bookmark' },
    { ruta: '/inicio/admin/asignaciones', titulo: 'Asignaciones docentes', descripcion: 'Asignar maestros a cursos y secciones.', icono: 'bi-person-video3' },
    { ruta: '/inicio/admin/reportes', titulo: 'Reporte de ingresos', descripcion: 'Consultar y exportar a PDF ingresos por concepto.', icono: 'bi-file-earmark-pdf' }
  ];
}
