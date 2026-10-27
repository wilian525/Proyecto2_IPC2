import { ConfirmacionServicio } from '../../compartidos/confirmacion/confirmacion.servicio';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MaestroServicio } from '../../servicios/maestro.servicio';
import { AlumnoNomina, AsignacionMaestro, AsistenciaMaestro } from '../../modelos/maestro';
import { explicarError } from '../../admin/mensajes-error';
import { fechaFormulario } from '../../modelos/gestion-colegio';

@Component({selector:'app-asistencia-maestro',imports:[RouterLink,ReactiveFormsModule],
  templateUrl:'./asistencia.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class AsistenciaMaestroComponent {
  private readonly confirmacion = inject(ConfirmacionServicio);
  private readonly servicio=inject(MaestroServicio);
  private readonly destruir=inject(DestroyRef);
  private readonly fb=inject(FormBuilder);
  readonly cursos=signal<AsignacionMaestro[]>([]);
  readonly alumnos=signal<AlumnoNomina[]>([]);
  readonly registros=signal<AsistenciaMaestro[]>([]);
  readonly guardando=signal(false);
  readonly error=signal('');readonly mensaje=signal('');
  readonly formulario=this.fb.nonNullable.group({idAsignacion:[0,Validators.min(1)],fecha:[new Date().toLocaleDateString('sv-SE'),Validators.required]});
  readonly fechaMaxima = new Date().toLocaleDateString('sv-SE');
  readonly alumnoActual=signal(0);
  readonly estado=signal('PRESENTE');
  constructor(){this.servicio.misCursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.cursos.set(r),error:e=>this.error.set(explicarError(e,'consultar cursos'))});}
  curso():AsignacionMaestro|undefined{return this.cursos().find(c=>c.idAsignacion===this.formulario.controls.idAsignacion.value);}
  seleccionar():void{
    const id=this.formulario.controls.idAsignacion.value;this.alumnos.set([]);this.registros.set([]);this.error.set('');this.mensaje.set('');
    if(!id)return;
    this.servicio.nomina(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.alumnos.set(r),error:e=>this.error.set(explicarError(e,'consultar alumnos'))});
    this.servicio.asistencias(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.registros.set(r),error:e=>this.error.set(explicarError(e,'consultar asistencias'))});
  }
  existente(idInscripcion:number):string{
    const fecha=this.formulario.controls.fecha.value;
    return this.registros().find(r=>r.idInscripcion===idInscripcion&&fechaFormulario(r.fecha)===fecha)?.estado??'';
  }
  async guardar(idInscripcion:number): Promise<void> {
    const d=this.formulario.getRawValue();
    if(this.guardando()||!d.idAsignacion||!d.fecha)return;
    if(d.fecha>this.fechaMaxima){this.error.set('No puedes registrar asistencia para una fecha futura.');return;}
    if(this.existente(idInscripcion)){this.error.set('La asistencia ya está registrada para este alumno y fecha.');return;}
    if(!await this.confirmacion.confirmar('¿Registrar la asistencia de este estudiante?'))return;
    this.guardando.set(true);this.error.set('');this.mensaje.set('');
    this.servicio.registrarAsistencia(d.idAsignacion,{idInscripcion,fecha:d.fecha,estado:this.estado()})
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next:r=>{this.mensaje.set(r.mensaje);this.guardando.set(false);this.seleccionar();this.mensaje.set(r.mensaje);},
        error:e=>{this.error.set(explicarError(e,'registrar asistencia'));this.guardando.set(false);}
      });
  }
}
