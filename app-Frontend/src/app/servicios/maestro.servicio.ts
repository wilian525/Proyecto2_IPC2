import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_API } from '../configuracion/api';
import { RespuestaAccion } from '../modelos/gestion-colegio';
import { AlumnoNomina, AsignacionMaestro, AsistenciaMaestro, NotaMaestro, RendimientoMaestro, ZonaMaestro } from '../modelos/maestro';

@Injectable({ providedIn: 'root' })
export class MaestroServicio {
  private readonly http = inject(HttpClient);
  private readonly url = `${URL_API}/academico`;

  misCursos(): Observable<AsignacionMaestro[]> {
    return this.http.get<AsignacionMaestro[]>(`${this.url}/mis-asignaciones`);
  }
  nomina(id: number): Observable<AlumnoNomina[]> {
    return this.http.get<AlumnoNomina[]>(`${this.url}/asignaciones/${id}/nomina`);
  }
  asistencias(id: number): Observable<AsistenciaMaestro[]> {
    return this.http.get<AsistenciaMaestro[]>(`${this.url}/asignaciones/${id}/asistencias`);
  }
  registrarAsistencia(id: number, datos: Pick<AsistenciaMaestro, 'idInscripcion' | 'fecha' | 'estado'>): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.url}/asignaciones/${id}/asistencias`, datos);
  }
  zonas(id: number): Observable<ZonaMaestro[]> {
    return this.http.get<ZonaMaestro[]>(`${this.url}/asignaciones/${id}/zonas`);
  }
  definirZona(id: number, nombre: string, porcentaje: number): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.url}/asignaciones/${id}/zonas`, {nombre, porcentaje});
  }
  notas(id: number): Observable<NotaMaestro[]> {
    return this.http.get<NotaMaestro[]>(`${this.url}/asignaciones/${id}/notas`);
  }
  guardarNota(idInscripcion: number, idZona: number, valor: number): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.url}/notas`, {idInscripcion, idZona, valor});
  }
  rendimiento(id: number): Observable<RendimientoMaestro> {
    return this.http.get<RendimientoMaestro>(`${URL_API}/reportes/mi-rendimiento/${id}`);
  }
  rendimientoPdf(id: number): Observable<Blob> {
    return this.http.get(`${URL_API}/reportes/mi-rendimiento/${id}/pdf`, {responseType: 'blob'});
  }
}
