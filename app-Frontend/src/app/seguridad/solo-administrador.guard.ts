import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SesionServicio } from '../servicios/sesion.servicio';

export const soloAdministrador: CanActivateFn = () => {
  const sesion = inject(SesionServicio);
  const router = inject(Router);
  if (!sesion.estaAutenticado()) return router.createUrlTree(['/login']);
  return sesion.obtenerRol() === 'ADMIN' ? true : router.createUrlTree(['/inicio']);
};
