import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SecretariaServicio } from '../../servicios/secretaria.servicio';
import { EstudianteColegio, SeccionColegio } from '../../modelos/gestion-colegio';
import { CicloEscolar, GradoEscolar } from '../../modelos/administracion';
import { explicarError } from '../../admin/mensajes-error';
type TipoReporte = 'solventes-morosos' | 'historial-pagos';
@Component({selector:'app-reportes-secretaria',imports:[RouterLink,ReactiveFormsModule,CurrencyPipe],templateUrl:'./reportes.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class ReportesSecretariaComponent {
  private readonly servicio=inject(SecretariaServicio);private readonly fb=inject(FormBuilder);private readonly destruir=inject(DestroyRef);
  readonly estudiantes=signal<EstudianteColegio[]>([]);readonly grados=signal<GradoEscolar[]>([]);readonly secciones=signal<SeccionColegio[]>([]);readonly ciclos=signal<CicloEscolar[]>([]);
  readonly datos=signal<unknown>(null);readonly error=signal('');readonly mensaje=signal('');readonly cargando=signal(false);readonly procesando=signal(false);
  readonly formulario=this.fb.nonNullable.group({tipo:['solventes-morosos' as TipoReporte],grado:[0],seccion:[0],ciclo:[0],idEstudiante:[0],mensualidades:[0,[Validators.min(0),Validators.max(12)]],fechaInicio:[''],fechaFin:['']});
  constructor(){forkJoin({estudiantes:this.servicio.estudiantes(1,100),grados:this.servicio.grados(),secciones:this.servicio.secciones(),ciclos:this.servicio.ciclos()}).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.estudiantes.set(r.estudiantes.datos);this.grados.set(r.grados);this.secciones.set(r.secciones);this.ciclos.set(r.ciclos);},error:e=>this.error.set(explicarError(e,'cargar filtros de reportes'))});}
  tipo():TipoReporte{return this.formulario.controls.tipo.value;}
  seccionesGrado():SeccionColegio[]{return this.secciones().filter(s=>s.idGrado===this.formulario.controls.grado.value);}
  actualizarTipo():void{this.datos.set(null);this.error.set('');this.mensaje.set('');}
  private consulta():{ruta:string;parametros:Record<string,string|number>}|null{
    const d=this.formulario.getRawValue();
    if(!Number.isInteger(d.mensualidades)||d.mensualidades<0||d.mensualidades>12){this.error.set('Las mensualidades a evaluar deben estar entre 0 y 12.');return null;}
    if(d.fechaInicio&&d.fechaFin&&d.fechaInicio>d.fechaFin){this.error.set('La fecha inicial no puede ser posterior a la final.');return null;}
    if(d.tipo==='historial-pagos'){
      if(d.idEstudiante<=0){this.error.set('Selecciona un estudiante para consultar su historial.');return null;}
      return {ruta:`historial-pagos/${d.idEstudiante}`,parametros:{mensualidades:d.mensualidades,fechaInicio:d.fechaInicio,fechaFin:d.fechaFin}};
    }
    if(d.grado<=0||d.seccion<=0||d.ciclo<=0){this.error.set('Selecciona grado, sección y ciclo.');return null;}
    if(!this.seccionesGrado().some(s=>s.idSeccion===d.seccion)){this.error.set('La sección no corresponde al grado indicado.');return null;}
    return {ruta:'solventes-morosos',parametros:{grado:d.grado,seccion:d.seccion,ciclo:d.ciclo,mensualidades:d.mensualidades}};
  }
  consultar():void{const c=this.consulta();if(!c||this.cargando())return;this.error.set('');this.datos.set(null);this.cargando.set(true);
    this.servicio.reporte(c.ruta,c.parametros).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.datos.set(r);this.cargando.set(false);},error:e=>{this.error.set(explicarError(e,'consultar reporte'));this.cargando.set(false);}});
  }
  descargar():void{const c=this.consulta();if(!c||this.procesando())return;this.error.set('');this.procesando.set(true);
    this.servicio.reportePdf(c.ruta,c.parametros).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:archivo=>{this.procesando.set(false);const url=URL.createObjectURL(archivo);const a=document.createElement('a');a.href=url;a.download=`reporte_secretaria_${this.tipo()}.pdf`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);this.mensaje.set('PDF generado correctamente.');},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'descargar el PDF'));}});
  }
  filas():Record<string,unknown>[]{return Array.isArray(this.datos())?this.datos() as Record<string,unknown>[]:[];}
  historial():Record<string,unknown>{return (this.datos()??{}) as Record<string,unknown>;}
}
