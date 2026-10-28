import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AutenticacionServicio } from '../servicios/autenticacion.servicio';
import { SesionServicio } from '../servicios/sesion.servicio';

@Component({
  selector: 'app-estructura-principal',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './estructura-principal.component.html',
  styleUrl: './estructura-principal.component.css'
})
export class EstructuraPrincipalComponent {
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly sesion = inject(SesionServicio);
  private readonly router = inject(Router);

  readonly rol = this.sesion.obtenerRol() ?? 'USUARIO';
  readonly esAlumno = this.rol === 'ESTUDIANTE';
  readonly esSuperAdministrador = this.rol === 'SUPER_ADMIN';
  readonly esAdministrador = this.rol === 'ADMIN';
  readonly esSecretaria = this.rol === 'SECRETARIA';
  readonly esBibliotecario = this.rol === 'BIBLIOTECARIO';
  readonly esMaestro = this.rol === 'MAESTRO';
  readonly menuAbierto = signal(false);

  readonly opcionesAlumno = [
    { ruta: '/inicio', titulo: 'Resumen', icono: 'bi-grid-1x2' },
    { ruta: '/inicio/perfil', titulo: 'Mi perfil', icono: 'bi-person-vcard' },
    { ruta: '/inicio/notas', titulo: 'Mis notas', icono: 'bi-journal-check' },
    { ruta: '/inicio/cuenta', titulo: 'Estado de cuenta', icono: 'bi-wallet2' },
    { ruta: '/inicio/catalogo', titulo: 'Catálogo de libros', icono: 'bi-book' },
    { ruta: '/inicio/prestamos', titulo: 'Préstamos pendientes', icono: 'bi-bookmark-check' },
    { ruta: '/inicio/multas', titulo: 'Multas de biblioteca', icono: 'bi-receipt' }
  ];

  readonly opcionesSuperAdministrador = [
    { ruta: '/inicio', titulo: 'Resumen', icono: 'bi-grid-1x2' },
    { ruta: '/inicio/super-admin', titulo: 'Panel de gestión', icono: 'bi-speedometer2' },
    { ruta: '/inicio/super-administradores', titulo: 'Super Administradores', icono: 'bi-shield-lock' },
    { ruta: '/inicio/administradores', titulo: 'Administradores', icono: 'bi-people' },
    { ruta: '/inicio/ciclos', titulo: 'Años lectivos', icono: 'bi-calendar3' },
    { ruta: '/inicio/grados', titulo: 'Grados por defecto', icono: 'bi-mortarboard' },
    { ruta: '/inicio/admin', titulo: 'Administración', icono: 'bi-building' },
    { ruta: '/inicio/secretaria', titulo: 'Secretaría', icono: 'bi-cash-coin' },
    { ruta: '/inicio/bibliotecario', titulo: 'Biblioteca', icono: 'bi-book' },
    { ruta: '/inicio/admin/reportes', titulo: 'Área docente / reportes', icono: 'bi-journal-check' },
    { ruta: '/inicio/admin/estudiantes', titulo: 'Estudiantes', icono: 'bi-person-vcard' }
  ];

  readonly opcionesAdministrador = [
    { ruta: '/inicio', titulo: 'Resumen', icono: 'bi-grid-1x2' },
    { ruta: '/inicio/admin', titulo: 'Panel de gestión', icono: 'bi-speedometer2' },
    { ruta: '/inicio/admin/personal', titulo: 'Personal', icono: 'bi-people' },
    { ruta: '/inicio/admin/estudiantes', titulo: 'Estudiantes', icono: 'bi-person-vcard' },
    { ruta: '/inicio/admin/carreras', titulo: 'Carreras', icono: 'bi-mortarboard' },
    { ruta: '/inicio/admin/cursos', titulo: 'Cursos', icono: 'bi-journals' },
    { ruta: '/inicio/admin/grados', titulo: 'Grados', icono: 'bi-diagram-3' },
    { ruta: '/inicio/admin/secciones', titulo: 'Secciones', icono: 'bi-grid' },
    { ruta: '/inicio/admin/curriculos', titulo: 'Currículos', icono: 'bi-journal-bookmark' },
    { ruta: '/inicio/admin/asignaciones', titulo: 'Asignaciones', icono: 'bi-person-video3' },
    { ruta: '/inicio/admin/reportes', titulo: 'Ingresos', icono: 'bi-file-earmark-pdf' }
  ];

  readonly opcionesSecretaria = [
    { ruta: '/inicio', titulo: 'Resumen', icono: 'bi-grid-1x2' },
    { ruta: '/inicio/secretaria', titulo: 'Panel de Secretaría', icono: 'bi-speedometer2' },
    { ruta: '/inicio/secretaria/inscripciones', titulo: 'Inscripciones', icono: 'bi-journal-check' },
    { ruta: '/inicio/secretaria/pagos', titulo: 'Pagos', icono: 'bi-cash-coin' },
    { ruta: '/inicio/secretaria/facturas', titulo: 'Facturas', icono: 'bi-receipt' },
    { ruta: '/inicio/secretaria/expedientes', titulo: 'Expedientes', icono: 'bi-person-vcard' },
    { ruta: '/inicio/secretaria/solvencias', titulo: 'Solvencias', icono: 'bi-patch-check' },
    { ruta: '/inicio/secretaria/reportes', titulo: 'Reportes', icono: 'bi-file-earmark-pdf' }
  ];

  readonly opcionesMaestro = [
    { ruta: '/inicio', titulo: 'Resumen', icono: 'bi-grid-1x2' },
    { ruta: '/inicio/maestro', titulo: 'Panel del Maestro', icono: 'bi-speedometer2' },
    { ruta: '/inicio/maestro/cursos', titulo: 'Mis cursos', icono: 'bi-journals' },
    { ruta: '/inicio/maestro/asistencia', titulo: 'Asistencia', icono: 'bi-calendar-check' },
    { ruta: '/inicio/maestro/zona', titulo: 'Zona', icono: 'bi-pie-chart' },
    { ruta: '/inicio/maestro/notas', titulo: 'Notas', icono: 'bi-journal-check' },
    { ruta: '/inicio/maestro/rendimiento', titulo: 'Rendimiento', icono: 'bi-file-earmark-pdf' },
    { ruta: '/inicio/maestro/biblioteca', titulo: 'Mi biblioteca', icono: 'bi-book' }
  ];

  readonly opcionesBibliotecario = [
    { ruta: '/inicio', titulo: 'Resumen', icono: 'bi-grid-1x2' },
    { ruta: '/inicio/bibliotecario', titulo: 'Panel de biblioteca', icono: 'bi-speedometer2' },
    { ruta: '/inicio/bibliotecario/libros', titulo: 'Libros', icono: 'bi-book' },
    { ruta: '/inicio/bibliotecario/prestamos', titulo: 'Préstamos', icono: 'bi-journal-arrow-up' },
    { ruta: '/inicio/bibliotecario/atrasos', titulo: 'Atrasos', icono: 'bi-clock-history' },
    { ruta: '/inicio/bibliotecario/reportes', titulo: 'Reporte en mora', icono: 'bi-file-earmark-pdf' }
  ];

  alternarMenu(): void { this.menuAbierto.update(actual => !actual); }
  cerrarMenu(): void { this.menuAbierto.set(false); }

  salir(): void {
    this.autenticacion.cerrarSesion();
    void this.router.navigateByUrl('/login');
  }
}
