import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { URL_API } from '../configuracion/api';
import { RespuestaAccion, RespuestaPagina } from '../modelos/gestion-colegio';
import { LibroBiblioteca, LectorBiblioteca, PrestamoBiblioteca, ReporteMora } from '../modelos/biblioteca';
@Injectable({providedIn:'root'})
export class BibliotecaServicio {
  private readonly http=inject(HttpClient);
  private readonly url=`${URL_API}/biblioteca`;
  libros(pagina=1,limite=10):Observable<RespuestaPagina<LibroBiblioteca>> { return this.http.get<RespuestaPagina<LibroBiblioteca>>(`${this.url}/libros`,{params:{pagina,limite}}); }
  lectores():Observable<LectorBiblioteca[]> { return this.http.get<LectorBiblioteca[]>(`${this.url}/lectores`); }
  crearLibro(libro:Omit<LibroBiblioteca,'idLibro'|'estado'>):Observable<RespuestaAccion> { return this.http.post<RespuestaAccion>(`${this.url}/libros`,libro); }
  editarLibro(id:number,libro:Omit<LibroBiblioteca,'idLibro'|'estado'>):Observable<RespuestaAccion> { return this.http.put<RespuestaAccion>(`${this.url}/libros/${id}`,libro); }
  estadoLibro(id:number,estado:string):Observable<RespuestaAccion> { return this.http.put<RespuestaAccion>(`${this.url}/libros/${id}/estado`,{estado}); }
  eliminarLibro(id:number):Observable<RespuestaAccion> { return this.http.delete<RespuestaAccion>(`${this.url}/libros/${id}`); }
  prestamos(pagina=1):Observable<RespuestaPagina<PrestamoBiblioteca>> { return this.http.get<RespuestaPagina<PrestamoBiblioteca>>(`${this.url}/prestamos`,{params:{pagina,limite:10}}); }
  prestar(datos:{idLibro:number;idUsuario:number;fechaPrestamo:string;fechaDevolucionEsperada:string}):Observable<RespuestaAccion> { return this.http.post<RespuestaAccion>(`${this.url}/prestamos`,datos); }
  devolver(id:number):Observable<RespuestaAccion> { return this.http.put<RespuestaAccion>(`${this.url}/prestamos/${id}/devolucion`,{fechaDevolucion:new Date().toLocaleDateString('en-CA')}); }
  atrasos(pagina=1):Observable<RespuestaPagina<PrestamoBiblioteca>> { return this.http.get<RespuestaPagina<PrestamoBiblioteca>>(`${this.url}/atrasos`,{params:{pagina,limite:10}}); }
  aviso(id:number):Observable<RespuestaAccion> { return this.http.put<RespuestaAccion>(`${this.url}/prestamos/${id}/aviso`,{}); }
  reporte():Observable<ReporteMora[]> { return this.http.get<ReporteMora[]>(`${URL_API}/reportes/prestamos-mora`); }
  reportePdf():Observable<Blob> { return this.http.get(`${URL_API}/reportes/prestamos-mora/pdf`,{responseType:'blob'}); }
}
