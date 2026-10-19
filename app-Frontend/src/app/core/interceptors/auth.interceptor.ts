import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { API_URL } from '../config/api.config';
import { SessionService } from '../services/session.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(SessionService).obtenerToken();
  if (!token || !request.url.startsWith(`${API_URL}/`) || request.url.endsWith('/autenticacion/login')) {
    return next(request);
  }
  return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
