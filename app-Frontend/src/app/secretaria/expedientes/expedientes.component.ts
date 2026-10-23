import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SecretariaServicio } from '../../servicios/secretaria.servicio';
import { EstudianteColegio, fechaFormulario } from '../../modelos/gestion-colegio';
import { InscripcionEscolar } from '../../modelos/secretaria';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-expedientes-secretaria',imports:[RouterLink],templateUrl:'./expedientes.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class ExpedientesSecretariaComponent {
  private readonly servicio=inject(SecretariaServicio);private readonly destruir=inject(DestroyRef);
  readonly estudiantes=signal<EstudianteColegio[]>([]);readonly seleccionado=signal<EstudianteColegio|null>(null);readonly inscripciones=signal<InscripcionEscolar[]>([]);
  readonly error=signal('');readonly pagina=signal(1);readonly total=signal(0);readonly cargando=signal(false);
  constructor(){this.cargar();}
  fecha(v:EstudianteColegio['fechaNacimiento']):string{return fechaFormulario(v);}
  cargar():void{this.cargando.set(true);this.servicio.estudiantes(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.estudiantes.set(r.datos);this.total.set(r.total);this.cargando.set(false);},error:e=>{this.error.set(explicarError(e,'consultar estudiantes'));this.cargando.set(false);}});}
  consultar(e:EstudianteColegio):void{this.error.set('');this.seleccionado.set(e);this.inscripciones.set([]);
    this.servicio.expediente(e.idEstudiante).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:ins=>this.inscripciones.set(ins),error:err=>this.error.set(explicarError(err,'consultar el expediente'))});
  }
  anterior():void{if(this.pagina()>1){this.pagina.update(p=>p-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(p=>p+1);this.cargar();}}
}
