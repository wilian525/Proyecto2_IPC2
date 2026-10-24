import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SecretariaServicio } from '../../servicios/secretaria.servicio';
import { FacturaEscolar, PagoEscolar } from '../../modelos/secretaria';
import { fechaFormulario } from '../../modelos/gestion-colegio';
import { explicarError } from '../../admin/mensajes-error';
@Component({selector:'app-facturas-secretaria',imports:[RouterLink,ReactiveFormsModule,CurrencyPipe],templateUrl:'./facturas.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class FacturasSecretariaComponent {
  private readonly servicio=inject(SecretariaServicio);private readonly fb=inject(FormBuilder);private readonly destruir=inject(DestroyRef);
  readonly facturas=signal<FacturaEscolar[]>([]);readonly pagos=signal<PagoEscolar[]>([]);readonly pagina=signal(1);readonly total=signal(0);readonly error=signal('');readonly mensaje=signal('');readonly procesando=signal(false);
  readonly formulario=this.fb.nonNullable.group({numeroFactura:['',[Validators.required,Validators.maxLength(100)]],fecha:[new Date().toLocaleDateString('en-CA'),Validators.required],idPago:[0,[Validators.required,Validators.min(1)]]});
  constructor(){this.cargar();this.servicio.pagos(1,100).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.pagos.set(r.datos),error:e=>this.error.set(explicarError(e,'cargar pagos disponibles'))});}
  fecha(v:FacturaEscolar['fecha']):string{return fechaFormulario(v);}
  pagosSinFactura():PagoEscolar[]{return this.pagos().filter(p=>!this.facturas().some(f=>f.idPago===p.idPago));}
  cargar():void{this.servicio.facturas(this.pagina()).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.facturas.set(r.datos);this.total.set(r.total);},error:e=>this.error.set(explicarError(e,'consultar facturas'))});}
  emitir():void{this.formulario.markAllAsTouched();if(this.formulario.invalid||this.procesando())return;
    const d=this.formulario.getRawValue();if(!this.pagosSinFactura().some(p=>p.idPago===d.idPago)){this.error.set('Selecciona un pago existente que no tenga una factura emitida.');return;}
    if(!window.confirm(`¿Emitir la factura ${d.numeroFactura} para el pago #${d.idPago}?`))return;
    this.procesando.set(true);this.error.set('');this.mensaje.set('');
    this.servicio.emitirFactura(d).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>{this.procesando.set(false);this.mensaje.set(r.mensaje);this.formulario.patchValue({numeroFactura:'',idPago:0});this.cargar();},error:e=>{this.procesando.set(false);this.error.set(explicarError(e,'emitir la factura'));}});
  }
  descargarFactura(id:number):void {
    this.error.set('');
    this.servicio.facturaPdf(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next:archivo=>{
        const url=URL.createObjectURL(archivo);const a=document.createElement('a');
        a.href=url;a.download=`factura_${id}.pdf`;a.click();setTimeout(()=>URL.revokeObjectURL(url),1200);
        this.mensaje.set('Comprobante PDF generado correctamente.');
      },error:e=>this.error.set(explicarError(e,'descargar la factura'))
    });
  }
  anterior():void{if(this.pagina()>1){this.pagina.update(p=>p-1);this.cargar();}}
  siguiente():void{if(this.pagina()*10<this.total()){this.pagina.update(p=>p+1);this.cargar();}}
}
