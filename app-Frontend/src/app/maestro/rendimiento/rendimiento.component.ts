import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MaestroServicio } from '../../servicios/maestro.servicio';
import { AsignacionMaestro, RendimientoMaestro } from '../../modelos/maestro';
import { explicarError } from '../../admin/mensajes-error';

@Component({selector:'app-rendimiento-maestro',imports:[RouterLink,ReactiveFormsModule],templateUrl:'./rendimiento.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class RendimientoMaestroComponent {
  private readonly servicio=inject(MaestroServicio);private readonly destruir=inject(DestroyRef);private readonly fb=inject(FormBuilder);
  readonly cursos=signal<AsignacionMaestro[]>([]);readonly resultado=signal<RendimientoMaestro|null>(null);
  readonly procesando=signal(false);readonly error=signal('');readonly mensaje=signal('');
  readonly formulario=this.fb.nonNullable.group({idAsignacion:[0]});
  constructor(){this.servicio.misCursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.cursos.set(r),error:e=>this.error.set(explicarError(e,'consultar cursos'))});}
  consultar():void{const id=this.formulario.controls.idAsignacion.value;if(!id){this.error.set('Selecciona un curso asignado.');return;}
    this.error.set('');this.mensaje.set('');this.resultado.set(null);
    this.servicio.rendimiento(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.resultado.set(r),error:e=>this.error.set(explicarError(e,'consultar rendimiento'))});
  }
  descargar():void{const id=this.formulario.controls.idAsignacion.value;if(!id||this.procesando())return;
    this.procesando.set(true);this.error.set('');
    this.servicio.rendimientoPdf(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next:archivo=>{this.procesando.set(false);const url=URL.createObjectURL(archivo);const enlace=document.createElement('a');enlace.href=url;enlace.download='mi_rendimiento.pdf';enlace.click();setTimeout(()=>URL.revokeObjectURL(url),1000);this.mensaje.set('PDF generado correctamente.');},
      error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'descargar el reporte PDF'));}
    });
  }
}
