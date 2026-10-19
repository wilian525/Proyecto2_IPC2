import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SesionServicio } from '../servicios/sesion.servicio';

export const protegerAcceso: CanActivateFn = () => {
  const sesion = inject(SesionServicio);
  const router = inject(Router);
  return sesion.estaAutenticado() ? true : router.createUrlTree(['/login']);
};
