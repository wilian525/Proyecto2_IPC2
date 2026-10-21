import { Routes } from '@angular/router';
import { EstructuraAccesoComponent } from './autenticacion/estructura-acceso/estructura-acceso.component';
import { EstructuraPrincipalComponent } from './estructura-principal/estructura-principal.component';
import { protegerAcceso } from './seguridad/proteger-acceso.guard';
import { soloAlumno } from './seguridad/solo-alumno.guard';
import { soloSuperAdministrador } from './seguridad/solo-super-administrador.guard';

export const rutas: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: '',
    component: EstructuraAccesoComponent,
    children: [
      {
        path: 'login',
        loadComponent: () => import('./autenticacion/ingresar/ ingresar.component')
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
        loadComponent: () => import('./inicio/inicio.component')
          .then(componente => componente.InicioComponent)
      },

      {
        path: 'super-admin', canActivate: [soloSuperAdministrador],
        loadComponent: () => import('./super-administrador/panel/panel.component')
          .then(m => m.PanelSuperAdministradorComponent)
      },
      {
        path: 'super-administradores', canActivate: [soloSuperAdministrador], data: { rol: 'SUPER_ADMIN' },
        loadComponent: () => import('./super-administrador/usuarios/usuarios.component')
          .then(m => m.UsuariosAdministrativosComponent)
      },
      {
        path: 'administradores', canActivate: [soloSuperAdministrador], data: { rol: 'ADMIN' },
        loadComponent: () => import('./super-administrador/usuarios/usuarios.component')
          .then(m => m.UsuariosAdministrativosComponent)
      },
      {
        path: 'ciclos', canActivate: [soloSuperAdministrador],
        loadComponent: () => import('./super-administrador/ciclos/ciclos.component')
          .then(m => m.CiclosComponent)
      },
      {
        path: 'grados', canActivate: [soloSuperAdministrador],
        loadComponent: () => import('./super-administrador/grados/grados.component')
          .then(m => m.GradosComponent)
      },
      {
        path: 'perfil', canActivate: [soloAlumno], data: { seccion: 'perfil' },
        loadComponent: () => import('./alumno/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'notas', canActivate: [soloAlumno], data: { seccion: 'notas' },
        loadComponent: () => import('./alumno/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'cuenta', canActivate: [soloAlumno], data: { seccion: 'cuenta' },
        loadComponent: () => import('./alumno/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'catalogo', canActivate: [soloAlumno], data: { seccion: 'catalogo' },
        loadComponent: () => import('./alumno/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'prestamos', canActivate: [soloAlumno], data: { seccion: 'prestamos' },
        loadComponent: () => import('./alumno/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      },
      {
        path: 'multas', canActivate: [soloAlumno], data: { seccion: 'multas' },
        loadComponent: () => import('./alumno/consulta-alumno/consulta-alumno.component')
          .then(componente => componente.ConsultaAlumnoComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
