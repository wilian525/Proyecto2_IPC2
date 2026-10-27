import { ConfirmacionServicio } from '../../compartidos/confirmacion/confirmacion.servicio';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CurriculoColegio, CursoColegio } from '../../modelos/gestion-colegio';
import { GradoEscolar, CicloEscolar } from '../../modelos/administracion';
import { AdministracionServicio } from '../../super-admin/administracion.servicio';
import { GestionColegioServicio } from '../gestion.servicio';
import { explicarError } from '../mensajes-error';

@Component({
  selector: 'app-curriculos-administrador',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './curriculos.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class CurriculosAdministradorComponent {
  private readonly confirmacion = inject(ConfirmacionServicio);
  private readonly servicio = inject(GestionColegioServicio);
  private readonly administracion = inject(AdministracionServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly curriculos = signal<CurriculoColegio[]>([]);
  readonly pagina = signal(1);
  readonly paginas = computed(() => Math.max(1, Math.ceil(this.curriculos().length / 10)));
  readonly visibles = computed(() => this.curriculos().slice((this.pagina()-1)*10, this.pagina()*10));
  readonly grados = signal<GradoEscolar[]>([]);
  readonly ciclos = signal<CicloEscolar[]>([]);
  readonly cursos = signal<CursoColegio[]>([]);
  readonly asignados = signal<number[]>([]);
  readonly seleccionado = signal<CurriculoColegio | null>(null);
  readonly cargando = signal(false);
  readonly procesando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly formulario = this.fb.nonNullable.group({ idGrado: [0, Validators.min(1)], idCiclo: [0, Validators.min(1)] });

  constructor() {
    this.administracion.consultarGrados().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: d => this.grados.set(d), error: e => this.error.set(explicarError(e, 'consultar grados')) });
    this.administracion.consultarCiclos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: d => this.ciclos.set(d), error: e => this.error.set(explicarError(e, 'consultar años lectivos')) });
    this.servicio.cursos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: d => this.cursos.set(d), error: e => this.error.set(explicarError(e, 'consultar cursos')) });
    this.cargar();
  }
  cargar(): void {
    this.cargando.set(true);
    this.servicio.curriculos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: d => { this.curriculos.set(d); this.pagina.set(Math.min(this.pagina(), this.paginas())); this.cargando.set(false); },
      error: e => { this.error.set(explicarError(e, 'consultar currículos')); this.cargando.set(false); }
    });
  }
  nombreGrado(id: number): string { return this.grados().find(g => g.idGrado === id)?.nombre ?? `Grado #${id}`; }
  nombreCiclo(id: number): string { const c = this.ciclos().find(x => x.idCiclo === id); return c ? `${c.anioInicio} - ${c.anioFin}` : `Ciclo #${id}`; }
  seleccionar(c: CurriculoColegio): void {
    this.seleccionado.set(c); this.asignados.set([]); this.error.set(''); this.mensaje.set('');
    this.formulario.patchValue({ idGrado: c.idGrado, idCiclo: c.idCiclo });
    this.servicio.cursosCurriculo(c.idGrado, c.idCiclo).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: cursos => this.asignados.set(cursos.map(x=>x.idCurso)),
      error: e => this.error.set(explicarError(e, 'consultar los cursos del currículo'))
    });
  }
  limpiar(): void { this.seleccionado.set(null); this.formulario.reset({ idGrado: 0, idCiclo: 0 }); this.asignados.set([]); }
  crear(): void {
    this.formulario.markAllAsTouched();
    if (this.procesando() || this.formulario.invalid) return;
    const ciclo = this.ciclos().find(c => c.idCiclo === this.formulario.getRawValue().idCiclo);
    if (!ciclo || ciclo.estado !== 'ACTIVO') { this.error.set('Solo puedes crear currículos para un año lectivo activo.'); return; }
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    this.servicio.crearCurriculo(this.formulario.getRawValue()).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje + '. Ahora selecciona el currículo y establece sus cursos.'); this.limpiar(); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, 'crear el currículo')); }
    });
  }
  alternarCurso(id: number, marcado: boolean): void {
    this.asignados.update(actual => marcado ? [...actual.filter(x=>x!==id), id] : actual.filter(x=>x!==id));
  }
  async guardarCursos(): Promise<void> {
    const actual = this.seleccionado();
    if (!actual || this.procesando()) return;
    if (this.ciclos().find(c => c.idCiclo === actual.idCiclo)?.estado !== 'ACTIVO') {
      this.error.set('Este currículo pertenece a un ciclo inactivo. Solo puede consultarse.'); return;
    }
    if (!this.asignados().length) { this.error.set('Selecciona al menos un curso para el currículo.'); return; }
    if (!await this.confirmacion.confirmar(`¿Guardar ${this.asignados().length} cursos en el currículo de ${this.nombreGrado(actual.idGrado)}?`)) return;
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    this.servicio.establecerCursos(actual.idCurriculo, this.asignados()).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.procesando.set(false); this.mensaje.set(r.mensaje); this.cargar(); },
      error: e => { this.procesando.set(false); this.error.set(explicarError(e, 'guardar los cursos del currículo')); }
    });
  }
}
