import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_API } from '../configuracion/api';
import { RespuestaAccion, RespuestaPagina, EstudianteColegio, SeccionColegio } from '../modelos/gestion-colegio';
import { GradoEscolar, CicloEscolar } from '../modelos/administracion';
import { InscripcionEscolar, PagoEscolar, FacturaEscolar, EstadoCuentaEscolar, SolvenciaEscolar } from '../modelos/secretaria';
@Injectable({ providedIn: 'root' })
export class SecretariaServicio {
  private readonly http = inject(HttpClient);
  private readonly url = URL_API;
  private parametros(pagina: number, limite = 10): HttpParams { return new HttpParams().set('pagina', pagina).set('limite', limite); }
  estudiantes(pagina = 1, limite = 10): Observable<RespuestaPagina<EstudianteColegio>> {
    return this.http.get<RespuestaPagina<EstudianteColegio>>(`${this.url}/estudiantes`, { params: this.parametros(pagina, limite) });
  }
  estudiante(id: number): Observable<EstudianteColegio> { return this.http.get<EstudianteColegio>(`${this.url}/estudiantes/${id}`); }
  expediente(id: number): Observable<InscripcionEscolar[]> { return this.http.get<InscripcionEscolar[]>(`${this.url}/estudiantes/${id}/expediente`); }
  ciclos(): Observable<CicloEscolar[]> { return this.http.get<CicloEscolar[]>(`${this.url}/administracion/ciclos`); }
  grados(): Observable<GradoEscolar[]> { return this.http.get<GradoEscolar[]>(`${this.url}/administracion/grados`); }
  secciones(): Observable<SeccionColegio[]> { return this.http.get<SeccionColegio[]>(`${this.url}/academico/secciones`); }
  inscripciones(pagina = 1, limite = 10): Observable<RespuestaPagina<InscripcionEscolar>> {
    return this.http.get<RespuestaPagina<InscripcionEscolar>>(`${this.url}/academico/inscripciones`, { params: this.parametros(pagina, limite) });
  }
  inscribir(datos: Omit<InscripcionEscolar, 'idInscripcion'>): Observable<RespuestaAccion> {
    return this.http.post<RespuestaAccion>(`${this.url}/academico/inscripciones`, datos);
  }
  pagos(pagina = 1, limite = 10): Observable<RespuestaPagina<PagoEscolar>> {
    return this.http.get<RespuestaPagina<PagoEscolar>>(`${this.url}/pagos`, { params: this.parametros(pagina, limite) });
  }
  historial(id: number, pagina = 1, limite = 10): Observable<RespuestaPagina<PagoEscolar>> {
    return this.http.get<RespuestaPagina<PagoEscolar>>(`${this.url}/pagos/estudiantes/${id}/historial`, { params: this.parametros(pagina, limite) });
  }
  registrarPago(datos: Pick<PagoEscolar, 'idEstudiante' | 'concepto' | 'monto' | 'fecha'>): Observable<RespuestaAccion> {
    // El backend identifica a la Secretaria por el token. No se envía idEmpleado.
    return this.http.post<RespuestaAccion>(`${this.url}/pagos`, datos);
  }
  estadoCuenta(id: number, mensualidades: number): Observable<EstadoCuentaEscolar> {
    return this.http.get<EstadoCuentaEscolar>(`${this.url}/pagos/estudiantes/${id}/estado-cuenta`, { params: new HttpParams().set('mensualidades', mensualidades) });
  }
  solvencia(id: number, mensualidades: number): Observable<SolvenciaEscolar> {
    return this.http.get<SolvenciaEscolar>(`${this.url}/pagos/estudiantes/${id}/solvencia`, { params: new HttpParams().set('mensualidades', mensualidades) });
  }
  facturas(pagina = 1, limite = 10): Observable<RespuestaPagina<FacturaEscolar>> {
    return this.http.get<RespuestaPagina<FacturaEscolar>>(`${this.url}/pagos/facturas`, { params: this.parametros(pagina, limite) });
  }
  emitirFactura(datos: Pick<FacturaEscolar, 'numeroFactura' | 'fecha' | 'idPago'>): Observable<RespuestaAccion> {
    // montoTotal lo obtiene el backend del pago real.
    return this.http.post<RespuestaAccion>(`${this.url}/pagos/facturas`, datos);
  }
  reporte(ruta: string, parametros: Record<string,string|number>): Observable<unknown> {
    return this.http.get<unknown>(`${this.url}/reportes/${ruta}`, { params: this.filtros(parametros) });
  }
  reportePdf(ruta: string, parametros: Record<string,string|number>): Observable<Blob> {
    return this.http.get(`${this.url}/reportes/${ruta}/pdf`, { params: this.filtros(parametros), responseType:'blob' });
  }
  private filtros(datos: Record<string,string|number>): HttpParams {
    let params = new HttpParams();
    for (const [nombre, valor] of Object.entries(datos)) if (valor !== '' && valor !== null) params = params.set(nombre,valor);
    return params;
  }
}
