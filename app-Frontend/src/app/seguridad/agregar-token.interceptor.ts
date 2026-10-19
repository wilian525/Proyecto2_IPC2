import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { URL_API } from '../configuracion/api';
import { SesionServicio } from '../servicios/sesion.servicio';

export const agregarToken: HttpInterceptorFn = (solicitud, siguiente) => {
  const token = inject(SesionServicio).obtenerToken();
  const urlPublica = /\/autenticacion\/(login|recuperar)$/.test(solicitud.url);
  if (!token || !solicitud.url.startsWith(`${URL_API}/`) || urlPublica) {
    return siguiente(solicitud);
  }
  return siguiente(solicitud.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
