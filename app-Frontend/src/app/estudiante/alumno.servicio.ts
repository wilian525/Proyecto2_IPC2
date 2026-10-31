import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_API } from '../configuracion/api';
import { RespuestaPagina } from '../modelos/gestion-colegio';
import {
  Alumno, EstadoCuenta, LibroAlumno, NotaAlumno,
  PaginaPagos, PrestamoAlumno
} from '../modelos/alumno';

@Injectable({ providedIn: 'root' })
export class AlumnoServicio {
  private readonly http = inject(HttpClient);

  obtenerPerfil(): Observable<Alumno> {
    return this.http.get<Alumno>(`${URL_API}/estudiantes/perfil`);
  }

  obtenerNotas(): Observable<NotaAlumno[]> {
    return this.http.get<NotaAlumno[]>(`${URL_API}/academico/mis-notas`);
  }

  obtenerEstadoCuenta(): Observable<EstadoCuenta> {
    return this.http.get<EstadoCuenta>(`${URL_API}/pagos/mi-estado-cuenta`);
  }

  obtenerHistorialPagos(pagina = 1): Observable<PaginaPagos> {
    return this.http.get<PaginaPagos>(`${URL_API}/pagos/mi-historial`, { params: { pagina, limite: 20 } });
  }

  obtenerCatalogo(pagina = 1): Observable<RespuestaPagina<LibroAlumno>> {
    return this.http.get<RespuestaPagina<LibroAlumno>>(`${URL_API}/biblioteca/catalogo`,
      { params: { pagina, limite: 9 } });
  }

  obtenerPendientes(): Observable<PrestamoAlumno[]> {
    return this.http.get<PrestamoAlumno[]>(`${URL_API}/biblioteca/mis-pendientes`);
  }

  obtenerMultas(): Observable<PrestamoAlumno[]> {
    return this.http.get<PrestamoAlumno[]>(`${URL_API}/biblioteca/mis-multas`);
  }
}
