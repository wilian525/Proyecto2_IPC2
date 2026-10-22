import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_API } from '../configuracion/api';
import {
  CicloEscolar, DatosAdministrador, GradoEscolar, NivelEscolar,
  PaginaAdministradores, RespuestaOperacion, RolAdministrativo
} from '../modelos/administracion';

@Injectable({ providedIn: 'root' })
export class AdministracionServicio {
  private readonly http = inject(HttpClient);
  private readonly base = `${URL_API}/administracion`;

  consultarAdministradores(rol: RolAdministrativo, pagina = 1, limite = 10): Observable<PaginaAdministradores> {
    const params = new HttpParams().set('rol', rol).set('pagina', pagina).set('limite', limite);
    return this.http.get<PaginaAdministradores>(`${this.base}/usuarios-administrativos`, { params });
  }

  crearAdministrador(rol: RolAdministrativo, datos: DatosAdministrador): Observable<RespuestaOperacion> {
    return this.http.post<RespuestaOperacion>(`${this.base}/${this.segmento(rol)}/completo`, datos);
  }

  modificarAdministrador(rol: RolAdministrativo, id: number, datos: DatosAdministrador): Observable<RespuestaOperacion> {
    return this.http.put<RespuestaOperacion>(`${this.base}/${this.segmento(rol)}/completo/${id}`, datos);
  }

  desactivarAdministrador(rol: RolAdministrativo, id: number): Observable<RespuestaOperacion> {
    return this.http.delete<RespuestaOperacion>(`${this.base}/${this.segmento(rol)}/${id}`);
  }

  activarAdministrador(rol: RolAdministrativo, id: number): Observable<RespuestaOperacion> {
    return this.http.put<RespuestaOperacion>(`${this.base}/${this.segmento(rol)}/${id}/activar`, {});
  }

  consultarCiclos(): Observable<CicloEscolar[]> {
    return this.http.get<CicloEscolar[]>(`${this.base}/ciclos`);
  }

  crearCiclo(datos: Pick<CicloEscolar, 'anioInicio' | 'anioFin'>): Observable<RespuestaOperacion> {
    return this.http.post<RespuestaOperacion>(`${this.base}/ciclos`, datos);
  }

  activarCiclo(id: number): Observable<RespuestaOperacion> {
    return this.http.put<RespuestaOperacion>(`${this.base}/ciclos/${id}/activar`, {});
  }

  desactivarCiclo(id: number): Observable<RespuestaOperacion> {
    return this.http.delete<RespuestaOperacion>(`${this.base}/ciclos/${id}`);
  }

  consultarNiveles(): Observable<NivelEscolar[]> {
    return this.http.get<NivelEscolar[]>(`${this.base}/niveles`);
  }

  consultarGrados(): Observable<GradoEscolar[]> {
    return this.http.get<GradoEscolar[]>(`${this.base}/grados`);
  }

  crearGrado(datos: Omit<GradoEscolar, 'idGrado'>): Observable<RespuestaOperacion> {
    return this.http.post<RespuestaOperacion>(`${this.base}/grados`, datos);
  }

  modificarGrado(id: number, datos: Omit<GradoEscolar, 'idGrado'>): Observable<RespuestaOperacion> {
    return this.http.put<RespuestaOperacion>(`${this.base}/grados/${id}`, datos);
  }

  private segmento(rol: RolAdministrativo): string {
    return rol === 'SUPER_ADMIN' ? 'super-admin' : 'admin';
  }
}
