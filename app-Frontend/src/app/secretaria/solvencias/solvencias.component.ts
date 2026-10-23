import { Component, DestroyRef, inject, signal, ViewEncapsulation } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SecretariaServicio } from '../../servicios/secretaria.servicio';
import { EstudianteColegio } from '../../modelos/gestion-colegio';
import { InscripcionEscolar, EstadoCuentaEscolar } from '../../modelos/secretaria';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-solvencias-secretaria',imports:[RouterLink,ReactiveFormsModule,CurrencyPipe],templateUrl:'./solvencias.component.html',styleUrl:'./solvencias.component.css',encapsulation:ViewEncapsulation.None})
export class SolvenciasSecretariaComponent {
  private readonly servicio=inject(SecretariaServicio);private readonly fb=inject(FormBuilder);private readonly destruir=inject(DestroyRef);
  readonly estudiantes=signal<EstudianteColegio[]>([]);readonly inscripciones=signal<InscripcionEscolar[]>([]);
  readonly resultado=signal<EstadoCuentaEscolar|null>(null);readonly solvente=signal(false);readonly consultando=signal(false);readonly error=signal('');
  readonly fechaEmision=new Date().toLocaleDateString('es-GT');
  readonly formulario=this.fb.nonNullable.group({idEstudiante:[0,[Validators.required,Validators.min(1)]],mensualidades:[0,[Validators.required,Validators.min(0),Validators.max(12)]]});
  constructor(){forkJoin({alumnos:this.servicio.estudiantes(1,100),ins:this.servicio.inscripciones(1,100)}).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.estudiantes.set(r.alumnos.datos);this.inscripciones.set(r.ins.datos);},error:e=>this.error.set(explicarError(e,'cargar datos para solvencias'))});}
  inscrito(id:number):boolean{return this.inscripciones().some(i=>i.idEstudiante===id);}
  estudiante():EstudianteColegio|null{return this.estudiantes().find(e=>e.idEstudiante===this.formulario.controls.idEstudiante.value)??null;}
  consultar():void{this.formulario.markAllAsTouched();this.resultado.set(null);this.solvente.set(false);if(this.formulario.invalid||this.consultando())return;
    const d=this.formulario.getRawValue();if(!this.inscrito(d.idEstudiante)){this.error.set('No puedes emitir solvencia: el estudiante aún no tiene una inscripción registrada.');return;}
    this.consultando.set(true);this.error.set('');forkJoin({saldo:this.servicio.estadoCuenta(d.idEstudiante,d.mensualidades),solvencia:this.servicio.solvencia(d.idEstudiante,d.mensualidades)}).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.resultado.set(r.saldo);this.solvente.set(r.solvencia.solvente);this.consultando.set(false);},error:e=>{this.consultando.set(false);this.error.set(explicarError(e,'consultar la solvencia'));}});
  }
  imprimir():void{if(this.solvente()&&this.resultado())window.print();}
}
