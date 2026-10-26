import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SesionServicio } from '../servicios/sesion.servicio';

export const soloMaestro: CanActivateFn = () => {
  const sesion = inject(SesionServicio);
  const router = inject(Router);
  if (!sesion.estaAutenticado()) return router.createUrlTree(['/login']);
  return sesion.obtenerRol() === 'MAESTRO' ? true : router.createUrlTree(['/inicio']);
};
