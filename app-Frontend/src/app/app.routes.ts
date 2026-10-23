import { Routes } from '@angular/router';
import { EstructuraAccesoComponent } from './autenticacion/estructura-acceso/estructura-acceso.component';
import { EstructuraPrincipalComponent } from './estructura-principal/estructura-principal.component';
import { protegerAcceso } from './seguridad/proteger-acceso.guard';
import { soloAlumno } from './seguridad/solo-alumno.guard';
import { soloSuperAdministrador } from './seguridad/solo-super-administrador.guard';
import { soloAdministrador } from './seguridad/solo-administrador.guard';
import { soloSecretaria } from './seguridad/solo-secretaria.guard';

export const rutas: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: '',
    component: EstructuraAccesoComponent,
    children: [
      {
        path: 'login',
        loadComponent: () => import('./autenticacion/ingresar/ingresar.component')
          .then(componente => componente.IngresarComponent)
      },
      {
        path: 'recuperar-contrasena',
        loadComponent: () => import('./autenticacion/recuperar-contrasena/recuperar-contrasena.component')
          .then(componente => componente.RecuperarContrasenaComponent)
      }
    ]
  },
  {
    path: 'inicio',
    canActivate: [protegerAcceso],
    component: EstructuraPrincipalComponent,
    children: [
      {
        path: '',
        loadComponent: () => import('./compartidos/inicio/inicio.component')
          .then(componente => componente.InicioComponent)
      },

      {
        path: 'super-admin', canActivate: [soloSuperAdministrador],
        loadComponent: () => import('./super-admin/panel/panel.component')
          .then(m => m.PanelSuperAdministradorComponent)
      },
      {
        path: 'super-administradores', canActivate: [soloSuperAdministrador], data: { rol: 'SUPER_ADMIN' },
        loadComponent: () => import('./super-admin/usuarios/usuarios.component')
          .then(m => m.UsuariosAdministrativosComponent)
      },
      {
        path: 'administradores', canActivate: [soloSuperAdministrador], data: { rol: 'ADMIN' },
        loadComponent: () => import('./super-admin/usuarios/usuarios.component')
          .then(m => m.UsuariosAdministrativosComponent)
      },
      {
        path: 'ciclos', canActivate: [soloSuperAdministrador],
        loadComponent: () => import('./super-admin/ciclos/ciclos.component')
          .then(m => m.CiclosComponent)
      },
      {
        path: 'grados', canActivate: [soloSuperAdministrador],
        loadComponent: () => import('./super-admin/grados/grados.component')
          .then(m => m.GradosComponent)
      },

      { path: 'secretaria', canActivate: [soloSecretaria],
        loadComponent: () => import('./secretaria/panel/panel.component').then(m => m.PanelSecretariaComponent)
      },
      { path: 'secretaria/inscripciones', canActivate: [soloSecretaria],
        loadComponent: () => import('./secretaria/inscripciones/inscripciones.component').then(m => m.InscripcionesSecretariaComponent)
      },
      { path: 'secretaria/pagos', canActivate: [soloSecretaria],
        loadComponent: () => import('./secretaria/pagos/pagos.component').then(m => m.PagosSecretariaComponent)
      },
      { path: 'secretaria/facturas', canActivate: [soloSecretaria],
        loadComponent: () => import('./secretaria/facturas/facturas.component').then(m => m.FacturasSecretariaComponent)
      },
      { path: 'secretaria/expedientes', canActivate: [soloSecretaria],
        loadComponent: () => import('./secretaria/expedientes/expedientes.component').then(m => m.ExpedientesSecretariaComponent)
      },
      { path: 'secretaria/solvencias', canActivate: [soloSecretaria],
        loadComponent: () => import('./secretaria/solvencias/solvencias.component').then(m => m.SolvenciasSecretariaComponent)
      },
      { path: 'secretaria/reportes', canActivate: [soloSecretaria],
        loadComponent: () => import('./secretaria/reportes/reportes.component').then(m => m.ReportesSecretariaComponent)
      },
      { path: 'admin', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/panel/panel.component')
          .then(m => m.PanelAdministradorComponent)
      },
      { path: 'admin/personal', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/personal/personal.component')
          .then(m => m.PersonalAdministradorComponent)
      },
      { path: 'admin/estudiantes', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/estudiantes/estudiantes.component')
          .then(m => m.EstudiantesAdministradorComponent)
      },
      { path: 'admin/carreras', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/carreras/carreras.component')
          .then(m => m.CarrerasAdministradorComponent)
      },
      { path: 'admin/cursos', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/cursos/cursos.component')
          .then(m => m.CursosAdministradorComponent)
      },
      { path: 'admin/secciones', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/secciones/secciones.component')
          .then(m => m.SeccionesAdministradorComponent)
      },
      { path: 'admin/curriculos', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/curriculos/curriculos.component')
          .then(m => m.CurriculosAdministradorComponent)
      },
      { path: 'admin/asignaciones', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/asignaciones/asignaciones.component')
          .then(m => m.AsignacionesAdministradorComponent)
      },
      { path: 'admin/reportes', canActivate: [soloAdministrador],
        loadComponent: () => import('./admin/reportes/reportes.component')
          .then(m => m.ReportesAdministradorComponent)
      },
      { path: 'admin/grados', canActivate: [soloAdministrador],
        loadComponent: () => import('./super-admin/grados/grados.component')
          .then(m => m.GradosComponent)
      },
      {
        path: 'perfil', canActivate: [soloAlumno], data: { seccion: 'perfil' },
        loadComponent: () => import('./estudiante/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'notas', canActivate: [soloAlumno], data: { seccion: 'notas' },
        loadComponent: () => import('./estudiante/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'cuenta', canActivate: [soloAlumno], data: { seccion: 'cuenta' },
        loadComponent: () => import('./estudiante/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'catalogo', canActivate: [soloAlumno], data: { seccion: 'catalogo' },
        loadComponent: () => import('./estudiante/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'prestamos', canActivate: [soloAlumno], data: { seccion: 'prestamos' },
        loadComponent: () => import('./estudiante/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'multas', canActivate: [soloAlumno], data: { seccion: 'multas' },
        loadComponent: () => import('./estudiante/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
