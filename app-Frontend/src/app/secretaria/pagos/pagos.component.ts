import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SecretariaServicio } from '../../servicios/secretaria.servicio';
import { EstudianteColegio, fechaFormulario, RespuestaPagina } from '../../modelos/gestion-colegio';
import { CicloEscolar } from '../../modelos/administracion';
import { CuotaEscolar, PagoEscolar, InscripcionEscolar } from '../../modelos/secretaria';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-pagos-secretaria',imports:[RouterLink,ReactiveFormsModule,CurrencyPipe],templateUrl:'./pagos.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class PagosSecretariaComponent {
  private readonly servicio=inject(SecretariaServicio);
  private readonly fb=inject(FormBuilder);
  private readonly destruir=inject(DestroyRef);
  readonly estudiantes=signal<EstudianteColegio[]>([]);
  readonly inscripciones=signal<InscripcionEscolar[]>([]);
  readonly ciclos=signal<CicloEscolar[]>([]);
  readonly cuotas=signal<CuotaEscolar[]>([]);
  readonly pagos=signal<PagoEscolar[]>([]);
  readonly pagina=signal(1);readonly total=signal(0);
  readonly procesando=signal(false);readonly error=signal('');readonly mensaje=signal('');
  readonly formulario=this.fb.nonNullable.group({
    idEstudiante:[0,[Validators.required,Validators.min(1)]],idInscripcion:[0],idCuota:[0],
    concepto:['INSCRIPCION' as PagoEscolar['concepto'],Validators.required],
    monto:[500,[Validators.required,Validators.min(0.01)]],fecha:[new Date().toLocaleDateString('en-CA'),Validators.required]
  });
  readonly cuotaFormulario=this.fb.nonNullable.group({
    idInscripcion:[0,[Validators.required,Validators.min(1)]],
    mes:[new Date().getMonth()+1,[Validators.min(1),Validators.max(12)]],
    anio:[new Date().getFullYear(),[Validators.required,Validators.min(2000)]],monto:[300]
  });
  readonly conciliacion=this.fb.nonNullable.group({idPago:[0,[Validators.min(1)]],idInscripcion:[0,[Validators.min(1)]],idCuota:[0]});
  constructor(){
    forkJoin({alumnos:this.servicio.estudiantes(1,100),ins:this.servicio.inscripciones(1,100),ciclos:this.servicio.ciclos()})
    .pipe(takeUntilDestroyed(this.destruir)).subscribe({next:(r:{alumnos:RespuestaPagina<EstudianteColegio>;ins:RespuestaPagina<InscripcionEscolar>;ciclos:CicloEscolar[]})=>{
      this.estudiantes.set(r.alumnos.datos);this.inscripciones.set(r.ins.datos);this.ciclos.set(r.ciclos);
    },error:(e:unknown)=>this.error.set(explicarError(e,'cargar estudiantes y ciclos'))});
    this.formulario.controls.idEstudiante.valueChanges.pipe(takeUntilDestroyed(this.destruir)).subscribe(id=>{
      const i=this.inscripcionesActivas(id)[0];
      this.formulario.patchValue({idInscripcion:i?.idInscripcion??0,idCuota:0},{emitEvent:false});
      this.cuotaFormulario.patchValue({idInscripcion:i?.idInscripcion??0},{emitEvent:false});
      this.conciliacion.patchValue({idInscripcion:i?.idInscripcion??0,idCuota:0},{emitEvent:false});
      this.cuotas.set([]);if(id>0)this.cargarCuotas(id);
    });
    this.cargar();
  }
  inscripcionesActivas(id:number):InscripcionEscolar[]{return this.inscripciones().filter(i=>i.idEstudiante===id&&this.ciclos().some(c=>c.idCiclo===i.idCiclo&&c.estado==='ACTIVO'));}
  inscrito(id:number):boolean{return this.inscripcionesActivas(id).length>0;}
  nombre(id:number):string{const e=this.estudiantes().find(x=>x.idEstudiante===id);return e?`${e.nombre} ${e.apellido}`:`Estudiante #${id}`;}
  fecha(v:PagoEscolar['fecha']):string{return fechaFormulario(v);}
  periodo(c:CuotaEscolar):string{return `${String(c.mes).padStart(2,'0')}/${c.anio}`;}
  cuotaSeleccionada():CuotaEscolar|undefined{return this.cuotas().find(c=>c.idCuota===this.formulario.controls.idCuota.value);}
  cuotasSinPagar():CuotaEscolar[]{return this.cuotas().filter(c=>c.idPago==null&&this.inscripcionesActivas(this.formulario.controls.idEstudiante.value).some(i=>i.idInscripcion===c.idInscripcion));}
  concepto():PagoEscolar['concepto']{return this.formulario.controls.concepto.value;}
  cambiarConcepto():void{const c=this.concepto();this.formulario.patchValue({monto:c==='INSCRIPCION'?500:c==='MENSUALIDAD'?300:0,idCuota:0});}
  seleccionarCuota():void{const c=this.cuotaSeleccionada();if(c)this.formulario.patchValue({monto:c.monto,idInscripcion:c.idInscripcion});}
  cargarCuotas(id:number):void{this.servicio.cuotas(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.cuotas.set(r),error:e=>this.error.set(explicarError(e,'consultar cuotas'))});}
  cargar():void{this.servicio.pagos(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.pagos.set(r.datos);this.total.set(r.total);},error:e=>this.error.set(explicarError(e,'consultar pagos'))});}
  guardar():void{
    this.formulario.markAllAsTouched();if(this.formulario.invalid||this.procesando())return;
    const d=this.formulario.getRawValue();
    if(!this.inscripcionesActivas(d.idEstudiante).some(i=>i.idInscripcion===d.idInscripcion)){
      this.error.set('Selecciona una inscripción del ciclo activo para el cobro.');return;
    }
    if(d.concepto==='MENSUALIDAD'&&!this.cuotasSinPagar().some(c=>c.idCuota===d.idCuota)){
      this.error.set('Registra primero la cuota del mes y selecciónala. No se permiten cobros duplicados.');return;
    }
    if(d.concepto==='INSCRIPCION'&&d.monto!==500||d.concepto==='MENSUALIDAD'&&d.monto!==300){this.error.set('La tarifa no coincide con la cuota del backend.');return;}
    if(!window.confirm(`¿Registrar ${d.concepto} por Q ${d.monto.toFixed(2)} de ${this.nombre(d.idEstudiante)}?`))return;
    this.procesando.set(true);this.error.set('');this.mensaje.set('');
    this.servicio.registrarPago({...d,idCuota:d.concepto==='MENSUALIDAD'?d.idCuota:null,idInscripcion:d.idInscripcion})
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.procesando.set(false);this.mensaje.set(r.mensaje);this.cargar();this.cargarCuotas(d.idEstudiante);},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'registrar el pago'));}});
  }
  registrarCuota():void{
    this.cuotaFormulario.markAllAsTouched();if(this.cuotaFormulario.invalid||this.procesando())return;
    const d=this.cuotaFormulario.getRawValue();
    if(!this.inscripcionesActivas(this.formulario.controls.idEstudiante.value).some(i=>i.idInscripcion===d.idInscripcion)){
      this.error.set('La cuota debe pertenecer a una inscripción del ciclo activo.');return;
    }
    this.procesando.set(true);this.error.set('');this.mensaje.set('');
    this.servicio.crearCuota(d).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.procesando.set(false);this.mensaje.set(r.mensaje);this.cargarCuotas(this.formulario.controls.idEstudiante.value);},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'registrar la cuota'));}});
  }
  conciliar():void{
    const d=this.conciliacion.getRawValue();
    const pago=this.pagos().find(p=>p.idPago===d.idPago);
    if(!pago||!this.inscripciones().some(i=>i.idInscripcion===d.idInscripcion&&i.idEstudiante===pago.idEstudiante)){
      this.error.set('El pago debe pertenecer al estudiante de la inscripción seleccionada.');return;
    }
    if(pago.concepto==='MENSUALIDAD'&&!this.cuotas().some(c=>c.idCuota===d.idCuota&&c.idPago==null)){
      this.error.set('Para conciliar una mensualidad, selecciona su mes y año previamente registrados.');return;
    }
    if(!window.confirm('¿Vincular el pago histórico a la obligación seleccionada? Esta operación no registra otro cobro.'))return;
    this.procesando.set(true);this.error.set('');
    this.servicio.conciliarPago({idPago:d.idPago,idInscripcion:d.idInscripcion,idCuota:pago.concepto==='MENSUALIDAD'?d.idCuota:null})
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.procesando.set(false);this.mensaje.set(r.mensaje);this.cargarCuotas(pago.idEstudiante);},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'conciliar el pago histórico'));}});
  }
  anterior():void{if(this.pagina()>1){this.pagina.update(p=>p-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(p=>p+1);this.cargar();}}
}
