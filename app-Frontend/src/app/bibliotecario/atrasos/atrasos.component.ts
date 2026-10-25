import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { BibliotecaServicio } from '../../servicios/biblioteca.servicio';
import { PrestamoBiblioteca, LibroBiblioteca, LectorBiblioteca } from '../../modelos/biblioteca';
import { fechaFormulario } from '../../modelos/gestion-colegio';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-atrasos-bibliotecario',imports:[RouterLink],templateUrl:'./atrasos.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class AtrasosBibliotecarioComponent {
  private readonly servicio=inject(BibliotecaServicio);private readonly destruir=inject(DestroyRef);
  readonly atrasos=signal<PrestamoBiblioteca[]>([]);readonly libros=signal<LibroBiblioteca[]>([]);readonly lectores=signal<LectorBiblioteca[]>([]);readonly pagina=signal(1);readonly total=signal(0);readonly mensaje=signal('');readonly error=signal('');
  constructor(){this.cargar();forkJoin({libros:this.servicio.libros(1,200),lectores:this.servicio.lectores()}).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.libros.set(r.libros.datos);this.lectores.set(r.lectores);},error:e=>this.error.set(explicarError(e,'cargar catálogo'))});}
  cargar():void{this.servicio.atrasos(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.atrasos.set(r.datos);this.total.set(r.total);},error:e=>this.error.set(explicarError(e,'consultar atrasos'))});}
  libro(id:number):string{return this.libros().find(l=>l.idLibro===id)?.titulo??`Libro #${id}`;}
  lector(id:number):string{return this.lectores().find(l=>l.idUsuario===id)?.nombreUsuario??`Usuario #${id}`;}
  fecha(d:unknown):string{return d?fechaFormulario(d as string|number[]):'Sin aviso';}
  avisar(p:PrestamoBiblioteca):void{if(!window.confirm('¿Registrar aviso de devolución pendiente?'))return;
    this.servicio.aviso(p.idPrestamo).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.mensaje.set(r.mensaje);this.cargar();},error:e=>this.error.set(explicarError(e,'registrar aviso'))});}
  anterior():void{if(this.pagina()>1){this.pagina.update(x=>x-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(x=>x+1);this.cargar();}}
}
