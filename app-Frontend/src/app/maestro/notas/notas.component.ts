import { ConfirmacionServicio } from '../../compartidos/confirmacion/confirmacion.servicio';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { MaestroServicio } from '../../servicios/maestro.servicio';
import { AlumnoNomina, AsignacionMaestro, NotaMaestro, ZonaMaestro } from '../../modelos/maestro';
import { explicarError } from '../../admin/mensajes-error';

@Component({selector:'app-notas-maestro',imports:[RouterLink,ReactiveFormsModule],templateUrl:'./notas.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class NotasMaestroComponent {
  private readonly confirmacion = inject(ConfirmacionServicio);
  private readonly servicio=inject(MaestroServicio);private readonly destruir=inject(DestroyRef);private readonly fb=inject(FormBuilder);
  readonly cursos=signal<AsignacionMaestro[]>([]);readonly alumnos=signal<AlumnoNomina[]>([]);
  readonly zonas=signal<ZonaMaestro[]>([]);readonly notas=signal<NotaMaestro[]>([]);
  readonly procesando=signal(false);readonly error=signal('');readonly mensaje=signal('');
  readonly formulario=this.fb.nonNullable.group({idAsignacion:[0,Validators.min(1)],idZona:[0,Validators.min(1)]});
  constructor(){this.servicio.misCursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.cursos.set(r),error:e=>this.error.set(explicarError(e,'consultar cursos'))});}
  curso():AsignacionMaestro|undefined{return this.cursos().find(c=>c.idAsignacion===this.formulario.controls.idAsignacion.value);}
  zona():ZonaMaestro|undefined{return this.zonas().find(z=>z.idZona===this.formulario.controls.idZona.value);}
  totalZona():number{return this.zonas().reduce((t,z)=>t+z.porcentaje,0);}
  seleccionar():void{const id=this.formulario.controls.idAsignacion.value;this.alumnos.set([]);this.zonas.set([]);this.notas.set([]);this.error.set('');this.mensaje.set('');this.formulario.controls.idZona.setValue(0);
    if(!id)return;
    forkJoin({alumnos:this.servicio.nomina(id),zonas:this.servicio.zonas(id),notas:this.servicio.notas(id)})
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.alumnos.set(r.alumnos);this.zonas.set(r.zonas);this.notas.set(r.notas);},error:e=>this.error.set(explicarError(e,'consultar notas y alumnos'))});
  }
  actual(idInscripcion:number):number|null{return this.notas().find(n=>n.idInscripcion===idInscripcion&&n.idZona===this.formulario.controls.idZona.value)?.valor??null;}
  async guardar(idInscripcion:number,valorEntrada:string): Promise<void> {
    const valor=Number(valorEntrada);const idZona=this.formulario.controls.idZona.value;
    if(!idZona||valorEntrada.trim()===''||!Number.isFinite(valor)||valor<0||valor>100){this.error.set('La nota debe estar entre 0 y 100.');return;}
    if(this.curso()?.estadoCiclo!=='ACTIVO'){this.error.set('No se pueden modificar notas de un ciclo inactivo.');return;}
    if(this.totalZona()<99.99||this.totalZona()>100.01){this.error.set('La suma de la zona debe ser exactamente 100%.');return;}
    if(this.procesando()||!await this.confirmacion.confirmar('¿Guardar esta calificación?'))return;
    this.procesando.set(true);this.error.set('');this.mensaje.set('');
    this.servicio.guardarNota(idInscripcion,idZona,valor).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next:r=>{this.procesando.set(false);this.mensaje.set(r.mensaje);this.servicio.notas(this.formulario.controls.idAsignacion.value).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:datos=>this.notas.set(datos),error:e=>this.error.set(explicarError(e,'actualizar notas'))});},
      error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'guardar la nota'));}
    });
  }
}
