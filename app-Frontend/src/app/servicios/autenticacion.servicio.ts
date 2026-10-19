import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { URL_API } from '../configuracion/api';
import { DatosIngreso, DatosRecuperacion, RespuestaIngreso, RespuestaRecuperacion } from '../modelos/autenticacion';
import { SesionServicio } from './sesion.servicio';

@Injectable({ providedIn: 'root' })
export class AutenticacionServicio {
  private readonly http = inject(HttpClient);
  private readonly sesion = inject(SesionServicio);

  iniciarSesion(datos: DatosIngreso): Observable<RespuestaIngreso> {
    return this.http.post<RespuestaIngreso>(`${URL_API}/autenticacion/login`, datos).pipe(
      tap((respuesta: RespuestaIngreso) => this.sesion.guardarToken(respuesta.token))
    );
  }

  recuperarContrasena(datos: DatosRecuperacion): Observable<RespuestaRecuperacion> {
    return this.http.post<RespuestaRecuperacion>(`${URL_API}/autenticacion/recuperar`, datos);
  }

  cerrarSesion(): void {
    this.sesion.cerrarSesion();
  }
}
