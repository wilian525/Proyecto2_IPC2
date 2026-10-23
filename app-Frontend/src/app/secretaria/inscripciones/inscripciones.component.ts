import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { SecretariaServicio } from '../../servicios/secretaria.servicio';
import { EstudianteColegio, SeccionColegio } from '../../modelos/gestion-colegio';
import { CicloEscolar, GradoEscolar } from '../../modelos/administracion';
import { InscripcionEscolar } from '../../modelos/secretaria';
import { explicarError } from '../../admin/mensajes-error';
@Component({ selector:'app-inscripciones-secretaria', imports:[RouterLink,ReactiveFormsModule], templateUrl:'./inscripciones.component.html', styleUrl:'../../compartidos/estilos/estilos-administracion.css' })
export class InscripcionesSecretariaComponent {
  private readonly servicio=inject(SecretariaServicio); private readonly fb=inject(FormBuilder); private readonly destruir=inject(DestroyRef);
  readonly estudiantes=signal<EstudianteColegio[]>([]); readonly grados=signal<GradoEscolar[]>([]); readonly secciones=signal<SeccionColegio[]>([]); readonly ciclos=signal<CicloEscolar[]>([]);
  readonly inscripciones=signal<InscripcionEscolar[]>([]); readonly total=signal(0); readonly pagina=signal(1);
  readonly cargando=signal(false); readonly procesando=signal(false); readonly mensaje=signal(''); readonly error=signal('');
  readonly formulario=this.fb.nonNullable.group({ idEstudiante:[0,[Validators.required,Validators.min(1)]], idCiclo:[0,[Validators.required,Validators.min(1)]], idGrado:[0,[Validators.required,Validators.min(1)]], idSeccion:[0,[Validators.required,Validators.min(1)]] });
  constructor(){this.catalogos();this.cargar();}
  private catalogos():void {
    forkJoin({estudiantes:this.servicio.estudiantes(1,100),grados:this.servicio.grados(),secciones:this.servicio.secciones(),ciclos:this.servicio.ciclos()})
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.estudiantes.set(r.estudiantes.datos);this.grados.set(r.grados);this.secciones.set(r.secciones);this.ciclos.set(r.ciclos);const actual=r.ciclos.find(c=>c.estado==='ACTIVO');if(actual)this.formulario.patchValue({idCiclo:actual.idCiclo});},error:e=>this.error.set(explicarError(e,'cargar estudiantes y opciones de inscripción'))});
  }
  seccionesDelGrado():SeccionColegio[]{return this.secciones().filter(s=>s.idGrado===this.formulario.controls.idGrado.value);}
  cambiarGrado():void{this.formulario.controls.idSeccion.setValue(0);}
  descripcion(id:number):string{const e=this.estudiantes().find(v=>v.idEstudiante===id);return e?`${e.nombre} ${e.apellido}`:`Estudiante #${id}`;}
  grado(id:number):string{return this.grados().find(g=>g.idGrado===id)?.nombre??`#${id}`;}
  seccion(id:number):string{return this.secciones().find(s=>s.idSeccion===id)?.nombre??`#${id}`;}
  ciclo(id:number):string{const c=this.ciclos().find(x=>x.idCiclo===id);return c?`${c.anioInicio}-${c.anioFin}`:`#${id}`;}
  cargar():void{this.cargando.set(true);this.servicio.inscripciones(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.inscripciones.set(r.datos);this.total.set(r.total);this.cargando.set(false);},error:e=>{this.error.set(explicarError(e,'consultar inscripciones'));this.cargando.set(false);}});}
  registrar():void{
    this.formulario.markAllAsTouched();if(this.formulario.invalid||this.procesando())return;
    const d=this.formulario.getRawValue();
    const estudiante=this.estudiantes().find(e=>e.idEstudiante===d.idEstudiante);
    if(!estudiante||estudiante.estado!=='ACTIVO'){this.error.set('Solo puedes inscribir estudiantes activos.');return;}
    if(!this.ciclos().some(c=>c.idCiclo===d.idCiclo&&c.estado==='ACTIVO')){this.error.set('Selecciona el ciclo escolar activo.');return;}
    if(!this.seccionesDelGrado().some(s=>s.idSeccion===d.idSeccion)){this.error.set('La sección elegida no pertenece al grado seleccionado.');return;}
    if(!window.confirm(`¿Inscribir a ${estudiante.nombre} ${estudiante.apellido} en ${this.grado(d.idGrado)}, sección ${this.seccion(d.idSeccion)}, ciclo ${this.ciclo(d.idCiclo)}?`))return;
    this.procesando.set(true);this.error.set('');this.mensaje.set('');
    this.servicio.inscribir(d).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.procesando.set(false);this.mensaje.set(r.mensaje);this.formulario.patchValue({idEstudiante:0,idGrado:0,idSeccion:0});this.cargar();},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'registrar la inscripción'));}});
  }
  anterior():void{if(this.pagina()>1){this.pagina.update(p=>p-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(p=>p+1);this.cargar();}}
}
