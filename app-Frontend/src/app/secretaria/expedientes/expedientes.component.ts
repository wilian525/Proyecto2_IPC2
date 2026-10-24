import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SecretariaServicio } from '../../servicios/secretaria.servicio';
import { EstudianteColegio, fechaFormulario, RespuestaPagina } from '../../modelos/gestion-colegio';
import { InscripcionEscolar } from '../../modelos/secretaria';
import { CicloEscolar, GradoEscolar } from '../../modelos/administracion';
import { SeccionColegio } from '../../modelos/gestion-colegio';
import { forkJoin } from 'rxjs';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-expedientes-secretaria',imports:[RouterLink],templateUrl:'./expedientes.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class ExpedientesSecretariaComponent {
  private readonly servicio=inject(SecretariaServicio);private readonly destruir=inject(DestroyRef);
  readonly estudiantes=signal<EstudianteColegio[]>([]);readonly seleccionado=signal<EstudianteColegio|null>(null);readonly inscripciones=signal<InscripcionEscolar[]>([]);
  readonly ciclos=signal<CicloEscolar[]>([]);readonly grados=signal<GradoEscolar[]>([]);readonly secciones=signal<SeccionColegio[]>([]);
  readonly error=signal('');readonly pagina=signal(1);readonly total=signal(0);readonly cargando=signal(false);
  constructor(){this.cargar();
    forkJoin({c:this.servicio.ciclos(),g:this.servicio.grados(),s:this.servicio.secciones()})
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({next:(r:{c:CicloEscolar[];g:GradoEscolar[];s:SeccionColegio[]})=>{this.ciclos.set(r.c);this.grados.set(r.g);this.secciones.set(r.s);},error:(e:unknown)=>this.error.set(explicarError(e,'cargar datos académicos'))});
  }
  ciclo(id:number):string{const c=this.ciclos().find(c=>c.idCiclo===id);return c?`${c.anioInicio} - ${c.anioFin}`:`Ciclo #${id}`;}
  grado(id:number):string{return this.grados().find(g=>g.idGrado===id)?.nombre??`Grado #${id}`;}
  seccion(id:number):string{return this.secciones().find(s=>s.idSeccion===id)?.nombre??`Sección #${id}`;}
  fecha(v:EstudianteColegio['fechaNacimiento']):string{const f=fechaFormulario(v);const partes=f.split('-');return partes.length===3?`${partes[2]}/${partes[1]}/${partes[0]}`:f;}
  cargar():void{this.cargando.set(true);this.servicio.estudiantes(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.estudiantes.set(r.datos);this.total.set(r.total);this.cargando.set(false);},error:e=>{this.error.set(explicarError(e,'consultar estudiantes'));this.cargando.set(false);}});}
  consultar(e:EstudianteColegio):void{this.error.set('');this.seleccionado.set(e);this.inscripciones.set([]);
    this.servicio.expediente(e.idEstudiante).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:ins=>this.inscripciones.set(ins),error:err=>this.error.set(explicarError(err,'consultar el expediente'))});
  }
  anterior():void{if(this.pagina()>1){this.pagina.update(p=>p-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(p=>p+1);this.cargar();}}
}
