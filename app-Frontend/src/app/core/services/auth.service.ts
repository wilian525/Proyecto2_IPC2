import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../config/api.config';
import { LoginRequest, LoginResponse } from '../../models/auth.model';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);

  iniciarSesion(datos: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/autenticacion/login`, datos).pipe(
      tap((respuesta: LoginResponse) => this.session.guardarToken(respuesta.token))
    );
  }

  cerrarSesion(): void {
    // El servidor valida /logout, pero actualmente no revoca el JWT.
    // Cerramos la sesión local, que es el comportamiento implementado.
    this.session.cerrarSesion();
  }
}
