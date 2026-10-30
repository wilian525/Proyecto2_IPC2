import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_API } from '../configuracion/api';
import {
  EmpleadoColegio, EstudianteColegio, CarreraColegio, CursoColegio,
  SeccionColegio, CurriculoColegio, AsignacionColegio, UsuarioNuevo,
  RespuestaAccion, RespuestaPagina
} from '../modelos/gestion-colegio';

@Injectable({ providedIn: 'root' })
export class GestionColegioServicio {
  private readonly http = inject(HttpClient);
  private readonly api = URL_API;
  private pagina(pagina: number, limite: number) {
    return new HttpParams().set('pagina', pagina).set('limite', limite);
  }

  empleados(pagina: number, limite = 10): Observable<RespuestaPagina<EmpleadoColegio>> {
    return this.http.get<RespuestaPagina<EmpleadoColegio>>(`${this.api}/empleados`, { params: this.pagina(pagina, limite) });
  }
  personal(pagina: number): Observable<RespuestaPagina<EmpleadoColegio>> {
    return this.http.get<RespuestaPagina<EmpleadoColegio>>(`${this.api}/empleados`, {
      params: this.pagina(pagina, 7).set('personal', true)
    });
  }
  crearEmpleado(empleado: Partial<EmpleadoColegio>, usuario: UsuarioNuevo): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.api}/empleados`, { empleado, usuario });
  }
  modificarEmpleado(id: number, empleado: Partial<EmpleadoColegio>): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.api}/empleados/${id}`, empleado);
  }
  desactivarEmpleado(id: number): Observable<RespuestaAccion> {
    return this.http.delete<RespuestaAccion>(`${this.api}/empleados/${id}`);
  }

  activarEmpleado(id: number): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.api}/empleados/${id}/activar`, {});
  }

  estudiantes(pagina: number, limite = 10): Observable<RespuestaPagina<EstudianteColegio>> {
    return this.http.get<RespuestaPagina<EstudianteColegio>>(`${this.api}/estudiantes`, { params: this.pagina(pagina, limite) });
  }
  crearEstudiante(estudiante: Partial<EstudianteColegio>, usuario: UsuarioNuevo): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.api}/estudiantes`, { estudiante, usuario });
  }
  modificarEstudiante(id: number, estudiante: Partial<EstudianteColegio>): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.api}/estudiantes/${id}`, estudiante);
  }
  cambiarEstadoEstudiante(id: number, estado: string): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.api}/estudiantes/${id}/estado`, { estado });
  }

  carreras(): Observable<CarreraColegio[]> { return this.http.get<CarreraColegio[]>(`${this.api}/academico/carreras`); }
  crearCarrera(datos: Pick<CarreraColegio, 'nombre' | 'idNivel'>): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.api}/academico/carreras`, datos);
  }
  modificarCarrera(id: number, datos: Pick<CarreraColegio, 'nombre' | 'idNivel'>): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.api}/academico/carreras/${id}`, datos);
  }
  desactivarCarrera(id: number): Observable<RespuestaAccion> { return this.http.delete<RespuestaAccion>(`${this.api}/academico/carreras/${id}`); }
  activarCarrera(id: number): Observable<RespuestaAccion> { return this.http.put<RespuestaAccion>(`${this.api}/academico/carreras/${id}/activar`, {}); }

  cursos(): Observable<CursoColegio[]> { return this.http.get<CursoColegio[]>(`${this.api}/academico/cursos`); }
  crearCurso(datos: Pick<CursoColegio, 'nombre' | 'descripcion'>): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.api}/academico/cursos`, datos);
  }
  modificarCurso(id: number, datos: Pick<CursoColegio, 'nombre' | 'descripcion'>): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.api}/academico/cursos/${id}`, datos);
  }
  secciones(): Observable<SeccionColegio[]> { return this.http.get<SeccionColegio[]>(`${this.api}/academico/secciones`); }
  crearSeccion(datos: Pick<SeccionColegio, 'nombre' | 'idGrado'>): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.api}/academico/secciones`, datos);
  }
  modificarSeccion(id: number, datos: Pick<SeccionColegio, 'nombre' | 'idGrado'>): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.api}/academico/secciones/${id}`, datos);
  }

  curriculos(): Observable<CurriculoColegio[]> { return this.http.get<CurriculoColegio[]>(`${this.api}/academico/curriculos/registrados`); }
  crearCurriculo(datos: Pick<CurriculoColegio, 'idGrado' | 'idCiclo'>): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.api}/academico/curriculos`, datos);
  }
  cursosCurriculo(grado: number, ciclo: number): Observable<CursoColegio[]> {
    return this.http.get<CursoColegio[]>(`${this.api}/academico/curriculos`, { params: new HttpParams().set('grado', grado).set('ciclo', ciclo) });
  }
  establecerCursos(id: number, cursos: number[]): Observable<RespuestaAccion> {
    return this.http.put<RespuestaAccion>(`${this.api}/academico/curriculos/${id}/cursos`, cursos);
  }
  asignarMaestro(datos: Omit<AsignacionColegio, 'idAsignacion'>): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.api}/academico/asignaciones`, datos);
  }
  asignacionesMaestro(id: number): Observable<AsignacionColegio[]> {
    return this.http.get<AsignacionColegio[]>(`${this.api}/academico/maestros/${id}/asignaciones`);
  }
  // Los reportes ya se generan en el backend; solo se consumen aquí.
  consultarReporte(ruta: string, parametros: Record<string, string | number>): Observable<unknown> {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(parametros)) {
      if (valor !== '' && valor !== null && valor !== undefined) params = params.set(clave, valor);
    }
    return this.http.get<unknown>(`${this.api}/reportes/${ruta}`, { params });
  }
  descargarReporte(ruta: string, parametros: Record<string, string | number>): Observable<Blob> {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(parametros)) {
      if (valor !== '' && valor !== null && valor !== undefined) params = params.set(clave, valor);
    }
    return this.http.get(`${this.api}/reportes/${ruta}/pdf`, { params, responseType: 'blob' });
  }

}
