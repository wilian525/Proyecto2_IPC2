import { Routes } from '@angular/router';
import { EstructuraAccesoComponent } from './autenticacion/estructura-acceso/estructura-acceso.component';
import { EstructuraPrincipalComponent } from './estructura-principal/estructura-principal.component';
import { protegerAcceso } from './seguridad/proteger-acceso.guard';
import { soloAlumno } from './seguridad/solo-alumno.guard';

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
