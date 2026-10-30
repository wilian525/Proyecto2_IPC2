import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BibliotecaServicio } from '../../servicios/biblioteca.servicio';
import { ReporteMora } from '../../modelos/biblioteca';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-reporte-biblioteca',imports:[RouterLink],templateUrl:'./reportes.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class ReportesBibliotecarioComponent {
  private readonly servicio=inject(BibliotecaServicio);private readonly destruir=inject(DestroyRef);
  readonly pagina=signal(1);readonly total=signal(0);readonly limite=7;
  readonly filas=signal<ReporteMora[]>([]);readonly error=signal('');readonly mensaje=signal('');readonly cargando=signal(false);
  constructor(){this.consultar();}
  consultar():void{this.pagina.set(1);this.cargar();}
  private cargar():void{
    this.cargando.set(true);this.error.set('');
    this.servicio.reporte(this.pagina(),this.limite).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next:r=>{this.filas.set(r.datos);this.total.set(r.total);this.cargando.set(false);},
      error:e=>{this.cargando.set(false);this.error.set(explicarError(e,'consultar reporte de biblioteca'));}
    });
  }
  anterior():void{if(this.pagina()>1){this.pagina.update(p=>p-1);this.cargar();}}
  siguiente():void{if(this.pagina()*this.limite<this.total()){this.pagina.update(p=>p+1);this.cargar();}}
  pdf():void{this.error.set('');this.servicio.reportePdf().pipe(takeUntilDestroyed(this.destruir)).subscribe({next:archivo=>{const url=URL.createObjectURL(archivo);const enlace=document.createElement('a');enlace.href=url;enlace.download='prestamos_en_mora.pdf';enlace.click();setTimeout(()=>URL.revokeObjectURL(url),1000);this.mensaje.set('PDF generado correctamente.');},error:e=>this.error.set(explicarError(e,'exportar reporte PDF'))});}
}
