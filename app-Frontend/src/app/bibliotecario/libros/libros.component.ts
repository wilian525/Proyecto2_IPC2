import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BibliotecaServicio } from '../../servicios/biblioteca.servicio';
import { LibroBiblioteca } from '../../modelos/biblioteca';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-libros-bibliotecario',imports:[RouterLink,ReactiveFormsModule],templateUrl:'./libros.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class LibrosBibliotecarioComponent {
  private readonly servicio=inject(BibliotecaServicio);private readonly fb=inject(FormBuilder);private readonly destruir=inject(DestroyRef);
  readonly libros=signal<LibroBiblioteca[]>([]);readonly pagina=signal(1);readonly total=signal(0);
  readonly editando=signal(0);readonly error=signal('');readonly mensaje=signal('');readonly procesando=signal(false);
  readonly formulario=this.fb.nonNullable.group({titulo:['',[Validators.required,Validators.maxLength(150)]],autor:['',[Validators.required,Validators.maxLength(150)]],editorial:[''],cantidadDisponible:[1,[Validators.required,Validators.min(0)]]});
  constructor(){this.cargar();}
  cargar():void {this.servicio.libros(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.libros.set(r.datos);this.total.set(r.total);},error:e=>this.error.set(explicarError(e,'consultar libros'))});}
  editar(l:LibroBiblioteca):void {this.editando.set(l.idLibro);this.formulario.patchValue({titulo:l.titulo,autor:l.autor,editorial:l.editorial??'',cantidadDisponible:l.cantidadDisponible});this.error.set('');}
  limpiar():void {this.editando.set(0);this.formulario.reset({titulo:'',autor:'',editorial:'',cantidadDisponible:1});}
  guardar():void {this.formulario.markAllAsTouched();if(this.formulario.invalid||this.procesando())return;
    this.procesando.set(true);this.error.set('');this.mensaje.set('');
    const operacion=this.editando()?this.servicio.editarLibro(this.editando(),this.formulario.getRawValue()):this.servicio.crearLibro(this.formulario.getRawValue());
    operacion.pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.procesando.set(false);this.mensaje.set(r.mensaje);this.limpiar();this.cargar();},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'guardar libro'));}});
  }
  estado(l:LibroBiblioteca):void {const nuevo=l.estado==='ACTIVO'?'INACTIVO':'ACTIVO';if(!window.confirm(`¿Cambiar ${l.titulo} a ${nuevo}?`))return;
    this.servicio.estadoLibro(l.idLibro,nuevo).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.mensaje.set(r.mensaje);this.cargar();},error:e=>this.error.set(explicarError(e,'cambiar estado del libro'))});}
  eliminar(l:LibroBiblioteca):void {if(!window.confirm(`¿Eliminar ${l.titulo}? No podrás hacerlo si tiene préstamos registrados.`))return;
    this.servicio.eliminarLibro(l.idLibro).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.mensaje.set(r.mensaje);this.cargar();},error:e=>this.error.set(explicarError(e,'eliminar libro'))});}
  anterior():void{if(this.pagina()>1){this.pagina.update(x=>x-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(x=>x+1);this.cargar();}}
}
