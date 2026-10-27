import { ConfirmacionServicio } from '../../compartidos/confirmacion/confirmacion.servicio';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MaestroServicio } from '../../servicios/maestro.servicio';
import { AsignacionMaestro, ZonaMaestro } from '../../modelos/maestro';
import { explicarError } from '../../admin/mensajes-error';

@Component({selector:'app-zona-maestro',imports:[RouterLink,ReactiveFormsModule],templateUrl:'./zona.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class ZonaMaestroComponent {
  private readonly confirmacion = inject(ConfirmacionServicio);
  private readonly servicio=inject(MaestroServicio);private readonly destruir=inject(DestroyRef);private readonly fb=inject(FormBuilder);
  readonly cursos=signal<AsignacionMaestro[]>([]);readonly zonas=signal<ZonaMaestro[]>([]);
  readonly procesando=signal(false);readonly error=signal('');readonly mensaje=signal('');
  readonly formulario=this.fb.nonNullable.group({idAsignacion:[0,Validators.min(1)],nombre:['',[Validators.required,Validators.maxLength(100)]],porcentaje:[0,[Validators.min(1),Validators.max(100)]]});
  constructor(){this.servicio.misCursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.cursos.set(r),error:e=>this.error.set(explicarError(e,'consultar cursos'))});}
  curso():AsignacionMaestro|undefined{return this.cursos().find(c=>c.idAsignacion===this.formulario.controls.idAsignacion.value);}
  porcentajeTotal():number{return this.zonas().reduce((s,z)=>s+z.porcentaje,0);}
  seleccionar():void{this.zonas.set([]);this.error.set('');this.mensaje.set('');const id=this.formulario.controls.idAsignacion.value;
    if(id)this.servicio.zonas(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.zonas.set(r),error:e=>this.error.set(explicarError(e,'consultar ponderaciones'))});}
  async guardar(): Promise<void> {this.formulario.markAllAsTouched();const d=this.formulario.getRawValue();
    if(this.formulario.invalid||this.procesando()||!this.curso())return;
    if(this.curso()?.estadoCiclo!=='ACTIVO'){this.error.set('Solo puedes definir zona en el ciclo activo.');return;}
    if(this.porcentajeTotal()+d.porcentaje>100){this.error.set('La suma de las ponderaciones no puede superar 100%.');return;}
    if(!await this.confirmacion.confirmar('¿Guardar esta ponderación?'))return;
    this.procesando.set(true);this.error.set('');this.mensaje.set('');
    this.servicio.definirZona(d.idAsignacion,d.nombre.trim(),d.porcentaje).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next:r=>{this.procesando.set(false);this.formulario.patchValue({nombre:'',porcentaje:0});this.seleccionar();this.mensaje.set(r.mensaje);},
      error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'definir la zona'));}
    });
  }
}
