import { ConfirmacionServicio } from '../../compartidos/confirmacion/confirmacion.servicio';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { BibliotecaServicio } from '../../servicios/biblioteca.servicio';
import { LibroBiblioteca, LectorBiblioteca, PrestamoBiblioteca } from '../../modelos/biblioteca';
import { fechaFormulario } from '../../modelos/gestion-colegio';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-prestamos-bibliotecario',imports:[RouterLink,ReactiveFormsModule],templateUrl:'./prestamos.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class PrestamosBibliotecarioComponent {
  private readonly confirmacion = inject(ConfirmacionServicio);
  private readonly servicio=inject(BibliotecaServicio);private readonly fb=inject(FormBuilder);private readonly destruir=inject(DestroyRef);
  readonly libros=signal<LibroBiblioteca[]>([]);readonly lectores=signal<LectorBiblioteca[]>([]);readonly prestamos=signal<PrestamoBiblioteca[]>([]);
  readonly pagina=signal(1);readonly total=signal(0);readonly error=signal('');readonly mensaje=signal('');readonly procesando=signal(false);
  readonly hoy=this.fechaLocal();
  private fechaLocal():string { const d=new Date();return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
  readonly formulario=this.fb.nonNullable.group({idLibro:[0,[Validators.min(1)]],idUsuario:[0,[Validators.min(1)]],fechaDevolucionEsperada:[this.hoy,Validators.required]});
  constructor(){this.catalogos();this.cargar();}
  catalogos():void{forkJoin({libros:this.servicio.libros(1,200),lectores:this.servicio.lectores()}).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.libros.set(r.libros.datos);this.lectores.set(r.lectores);},error:e=>this.error.set(explicarError(e,'cargar libros y lectores'))});}
  cargar():void{this.servicio.prestamos(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.prestamos.set(r.datos);this.total.set(r.total);},error:e=>this.error.set(explicarError(e,'consultar préstamos'))});}
  libro(id:number):string{return this.libros().find(l=>l.idLibro===id)?.titulo??`Libro #${id}`;}
  lector(id:number):string{return this.lectores().find(l=>l.idUsuario===id)?.nombreUsuario??`Usuario #${id}`;}
  fecha(d:unknown):string{return fechaFormulario(d as string|number[]);}
  prestar():void{const d=this.formulario.getRawValue();if(this.formulario.invalid||!d.idLibro||!d.idUsuario||d.fechaDevolucionEsperada<=this.hoy){this.error.set('Selecciona libro, lector y una fecha de devolución posterior a hoy.');return;}
    this.procesando.set(true);this.error.set('');this.servicio.prestar({...d,fechaPrestamo:this.hoy}).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.mensaje.set(r.mensaje);this.procesando.set(false);this.formulario.patchValue({idLibro:0,idUsuario:0});this.catalogos();this.cargar();},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'registrar préstamo'));}});}
  async devolver(p:PrestamoBiblioteca): Promise<void> {if(!await this.confirmacion.confirmar(`¿Confirmas la devolución de ${this.libro(p.idLibro)}?`))return;
    this.servicio.devolver(p.idPrestamo).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.mensaje.set(r.mensaje);this.catalogos();this.cargar();},error:e=>this.error.set(explicarError(e,'registrar devolución'))});}
  anterior():void{if(this.pagina()>1){this.pagina.update(x=>x-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(x=>x+1);this.cargar();}}
}
