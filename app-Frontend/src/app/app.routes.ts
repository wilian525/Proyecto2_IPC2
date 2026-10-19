import { Routes } from '@angular/router';
import { EstructuraAccesoComponent } from './autenticacion/estructura-acceso/estructura-acceso.component';
import { protegerAcceso } from './seguridad/proteger-acceso.guard';

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
    loadComponent: () => import('./inicio/inicio.component')
      .then(componente => componente.InicioComponent)
  },
  { path: '**', redirectTo: 'login' }
];
