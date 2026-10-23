import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SecretariaServicio } from '../../servicios/secretaria.servicio';
import { EstudianteColegio, fechaFormulario } from '../../modelos/gestion-colegio';
import { PagoEscolar, InscripcionEscolar } from '../../modelos/secretaria';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-pagos-secretaria', imports:[RouterLink,ReactiveFormsModule,CurrencyPipe],templateUrl:'./pagos.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class PagosSecretariaComponent {
  private readonly servicio=inject(SecretariaServicio);private readonly fb=inject(FormBuilder);private readonly destruir=inject(DestroyRef);
  readonly estudiantes=signal<EstudianteColegio[]>([]);readonly inscripciones=signal<InscripcionEscolar[]>([]);readonly pagos=signal<PagoEscolar[]>([]);
  readonly pagina=signal(1);readonly total=signal(0);readonly cargando=signal(false);readonly procesando=signal(false);readonly error=signal('');readonly mensaje=signal('');
  readonly formulario=this.fb.nonNullable.group({idEstudiante:[0,[Validators.required,Validators.min(1)]],concepto:['INSCRIPCION' as PagoEscolar['concepto'],Validators.required],monto:[500,[Validators.required,Validators.min(0.01)]],fecha:[new Date().toLocaleDateString('en-CA'),Validators.required]});
  constructor(){forkJoin({estudiantes:this.servicio.estudiantes(1,100),inscripciones:this.servicio.inscripciones(1,100)}).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.estudiantes.set(r.estudiantes.datos);this.inscripciones.set(r.inscripciones.datos);},error:e=>this.error.set(explicarError(e,'cargar estudiantes inscritos'))});this.cargar();}
  inscrito(id:number):boolean{return this.inscripciones().some(i=>i.idEstudiante===id);}
  nombre(id:number):string{const e=this.estudiantes().find(s=>s.idEstudiante===id);return e?`${e.nombre} ${e.apellido}`:`Estudiante #${id}`;}
  fecha(v:PagoEscolar['fecha']):string{return fechaFormulario(v);}
  cambiarConcepto():void{const c=this.formulario.controls.concepto.value;if(c==='INSCRIPCION')this.formulario.patchValue({monto:500});if(c==='MENSUALIDAD')this.formulario.patchValue({monto:300});if(c==='ACTIVIDAD')this.formulario.patchValue({monto:0});}
  cargar():void{this.cargando.set(true);this.servicio.pagos(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.pagos.set(r.datos);this.total.set(r.total);this.cargando.set(false);},error:e=>{this.error.set(explicarError(e,'consultar pagos'));this.cargando.set(false);}});}
  guardar():void{
    this.formulario.markAllAsTouched();if(this.formulario.invalid||this.procesando())return;
    const d=this.formulario.getRawValue();
    if(!this.inscripciones().some(i=>i.idEstudiante===d.idEstudiante)){this.error.set('El estudiante no tiene inscripción registrada. Primero realiza la inscripción escolar.');return;}
    if(d.concepto==='INSCRIPCION'&&d.monto!==500||d.concepto==='MENSUALIDAD'&&d.monto!==300){this.error.set('Según las tarifas actuales del backend: inscripción Q 500.00 y mensualidad Q 300.00.');return;}
    if(!window.confirm(`¿Registrar el pago de ${d.concepto} por Q ${Number(d.monto).toFixed(2)} a nombre de ${this.nombre(d.idEstudiante)}?`))return;
    this.procesando.set(true);this.error.set('');this.mensaje.set('');
    this.servicio.registrarPago(d).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.procesando.set(false);this.mensaje.set(r.mensaje);this.cargar();},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'registrar el pago'));}});
  }
  anterior():void{if(this.pagina()>1){this.pagina.update(x=>x-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(x=>x+1);this.cargar();}}
}
