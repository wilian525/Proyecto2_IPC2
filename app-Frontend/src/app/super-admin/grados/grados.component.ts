import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { GradoEscolar, NivelEscolar } from '../../modelos/administracion';
import { AdministracionServicio } from '../administracion.servicio';
import { FormularioGradoComponent } from '../formulario-grado/formulario-grado.component';

@Component({
  selector: 'app-grados',
  imports: [RouterLink, FormularioGradoComponent],
  templateUrl: './grados.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class GradosComponent {
  private readonly servicio = inject(AdministracionServicio);
  private readonly destruir = inject(DestroyRef);
  readonly grados = signal<GradoEscolar[]>([]);
  readonly niveles = signal<NivelEscolar[]>([]);
  readonly seleccionado = signal<GradoEscolar | null>(null);
  readonly pagina = signal(1);
  readonly limite = 10;
  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.grados().length / this.limite)));
  readonly visibles = computed(() => this.grados().slice((this.pagina() - 1) * this.limite, this.pagina() * this.limite));
  readonly cargando = signal(true);
  readonly procesando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');

  constructor() { this.cargar(); this.cargarNiveles(); }

  cargarNiveles(): void {
    this.servicio.consultarNiveles().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: n => this.niveles.set(n), error: () => this.error.set('No se pudieron consultar los niveles académicos.')
    });
  }

  cargar(): void {
    this.cargando.set(true); this.error.set('');
    this.servicio.consultarGrados().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: g => { this.grados.set(g); this.pagina.set(Math.min(this.pagina(), this.totalPaginas())); this.cargando.set(false); },
      error: e => { this.error.set(this.errorHttp(e)); this.cargando.set(false); }
    });
  }

  nombreNivel(id: number): string {
    return this.niveles().find(n => n.idNivel === id)?.nombre ?? `Nivel #${id}`;
  }

  seleccionar(g: GradoEscolar): void { this.seleccionado.set(g); this.mensaje.set(''); }
  cancelar(): void { this.seleccionado.set(null); }

  guardar(datos: Omit<GradoEscolar, 'idGrado'>): void {
    const actual = this.seleccionado();
    const peticion = actual ? this.servicio.modificarGrado(actual.idGrado, datos) : this.servicio.crearGrado(datos);
    this.procesando.set(true); this.error.set(''); this.mensaje.set('');
    peticion.pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => { this.mensaje.set(r.mensaje); this.procesando.set(false); this.cancelar(); this.cargar(); },
      error: e => { this.error.set(this.errorHttp(e)); this.procesando.set(false); }
    });
  }

  private errorHttp(e: HttpErrorResponse): string {
    return e.status === 0 ? 'El backend no está disponible.' :
      (typeof e.error?.mensaje === 'string' ? e.error.mensaje : 'No se pudo realizar la operación.');
  }
}
