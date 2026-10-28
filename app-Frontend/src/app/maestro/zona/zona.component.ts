import { ConfirmacionServicio } from '../../compartidos/confirmacion/confirmacion.servicio';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MaestroServicio } from '../../servicios/maestro.servicio';
import { AsignacionMaestro, ZonaMaestro } from '../../modelos/maestro';
import { explicarError } from '../../admin/mensajes-error';

@Component({selector:'app-zona-maestro',imports:[RouterLink,ReactiveFormsModule],templateUrl:'./zona.component.html',styleUrl:'../../compartidos/estilos/estilos-administracion.css'})
export class ZonaMaestroComponent {
  private readonly confirmacion = inject(ConfirmacionServicio);
  private readonly servicio=inject(MaestroServicio);private readonly destruir=inject(DestroyRef);private readonly fb=inject(FormBuilder);
  readonly cursos=signal<AsignacionMaestro[]>([]);readonly zonas=signal<ZonaMaestro[]>([]);
  readonly procesando=signal(false);readonly error=signal('');readonly mensaje=signal('');
  readonly editando=signal<number|null>(null);
  readonly formulario=this.fb.nonNullable.group({idAsignacion:[0,Validators.min(1)],nombre:['',[Validators.required,Validators.maxLength(100)]],porcentaje:[0,[Validators.min(1),Validators.max(100)]]});
  constructor(){this.servicio.misCursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.cursos.set(r),error:e=>this.error.set(explicarError(e,'consultar cursos'))});}
  curso():AsignacionMaestro|undefined{return this.cursos().find(c=>c.idAsignacion===this.formulario.controls.idAsignacion.value);}
  porcentajeTotal():number{return this.zonas().reduce((s,z)=>s+z.porcentaje,0);}
  seleccionar():void{this.editando.set(null);this.zonas.set([]);this.error.set('');this.mensaje.set('');const id=this.formulario.controls.idAsignacion.value;
    if(id)this.servicio.zonas(id).pipe(takeUntilDestroyed(this.destruir)).subscribe({next:r=>this.zonas.set(r),error:e=>this.error.set(explicarError(e,'consultar ponderaciones'))});}
  editar(zona: ZonaMaestro): void {
    if (this.curso()?.estadoCiclo !== 'ACTIVO') return;
    this.editando.set(zona.idZona);
    this.formulario.patchValue({ nombre: zona.nombre, porcentaje: zona.porcentaje });
    this.error.set(''); this.mensaje.set('');
  }
  cancelarEdicion(): void {
    this.editando.set(null);
    this.formulario.patchValue({ nombre: '', porcentaje: 0 });
  }
  async guardar(): Promise<void> {
    this.formulario.markAllAsTouched();
    const datos = this.formulario.getRawValue();
    const actual = this.editando();
    if (this.formulario.invalid || this.procesando() || !this.curso()) return;
    if (this.curso()?.estadoCiclo !== 'ACTIVO') {
      this.error.set('Solo puedes definir zona en el ciclo activo.'); return;
    }
    const total = this.porcentajeTotal() - (this.zonas().find(z => z.idZona === actual)?.porcentaje ?? 0);
    if (total + datos.porcentaje > 100) {
      this.error.set('La suma de las ponderaciones no puede superar 100%.'); return;
    }
    if (!await this.confirmacion.confirmar(actual === null ? '¿Guardar esta ponderación?' : '¿Actualizar esta ponderación?')) return;
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    const peticion = actual === null
      ? this.servicio.definirZona(datos.idAsignacion, datos.nombre.trim(), datos.porcentaje)
      : this.servicio.actualizarZona(datos.idAsignacion, actual, datos.nombre.trim(), datos.porcentaje);
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => {
        this.procesando.set(false);
        this.cancelarEdicion();
        this.seleccionar();
        this.mensaje.set(r.mensaje);
      },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, actual === null ? 'definir la zona' : 'actualizar la zona')); }
    });
  }
}
