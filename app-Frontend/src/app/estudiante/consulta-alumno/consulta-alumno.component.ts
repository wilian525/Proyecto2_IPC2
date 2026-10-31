import { Component, DestroyRef, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  Alumno, EstadoCuenta, LibroAlumno, NotaAlumno,
  PaginaPagos, PrestamoAlumno
} from '../../modelos/alumno';
import { AlumnoServicio } from '../alumno.servicio';
import { fechaFormulario, FechaServidor } from '../../modelos/gestion-colegio';

export type SeccionAlumno = 'perfil' | 'notas' | 'cuenta' | 'catalogo' | 'prestamos' | 'multas';

@Component({
  selector: 'app-consulta-alumno',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './consulta-alumno.component.html',
  styleUrl: './consulta-alumno.component.css'
})
export class ConsultaAlumnoComponent {
  private readonly servicio = inject(AlumnoServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly ruta = inject(ActivatedRoute);

  readonly seccion = this.ruta.snapshot.data['seccion'] as SeccionAlumno;
  readonly cargando = signal(true);
  readonly mensajeError = signal('');
  readonly perfil = signal<Alumno | null>(null);
  readonly notas = signal<NotaAlumno[]>([]);
  readonly estadoCuenta = signal<EstadoCuenta | null>(null);
  readonly historial = signal<PaginaPagos | null>(null);
  readonly cargandoMasPagos = signal(false);
  readonly libros = signal<LibroAlumno[]>([]);
  readonly paginaLibros = signal(1);
  readonly totalLibros = signal(0);
  readonly prestamos = signal<PrestamoAlumno[]>([]);
  readonly multas = signal<PrestamoAlumno[]>([]);

  readonly titulos: Record<SeccionAlumno, string> = {
    perfil: 'Mi perfil', notas: 'Mi historial de notas', cuenta: 'Estado de cuenta',
    catalogo: 'Catálogo de libros', prestamos: 'Préstamos pendientes', multas: 'Multas de biblioteca'
  };

  fecha(valor: FechaServidor | null | undefined): string {
    if (!valor) return '—';
    const texto = fechaFormulario(valor);
    const [anio, mes, dia] = texto.split('-');
    return dia && mes && anio ? `${dia}/${mes}/${anio}` : texto;
  }

  constructor() { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.mensajeError.set('');
    switch (this.seccion) {
      case 'perfil':
        this.servicio.obtenerPerfil().pipe(takeUntilDestroyed(this.destruir)).subscribe({
          next: datos => { this.perfil.set(datos); this.finalizar(); }, error: err => this.error(err)
        });
        break;
      case 'notas':
        this.servicio.obtenerNotas().pipe(takeUntilDestroyed(this.destruir)).subscribe({
          next: datos => { this.notas.set(datos); this.finalizar(); }, error: err => this.error(err)
        });
        break;
      case 'cuenta':
        this.servicio.obtenerEstadoCuenta().pipe(takeUntilDestroyed(this.destruir)).subscribe({
          next: datos => { this.estadoCuenta.set(datos); this.finalizar(); }, error: err => this.error(err)
        });
        this.servicio.obtenerHistorialPagos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
          next: datos => this.historial.set(datos),
          error: () => this.mensajeError.set('No fue posible cargar el historial de pagos. El saldo se muestra por separado.')
        });
        break;
      case 'catalogo':
        this.servicio.obtenerCatalogo(this.paginaLibros()).pipe(takeUntilDestroyed(this.destruir)).subscribe({
          next: r => {
            if (!r.datos.length && this.paginaLibros() > 1) {
              this.paginaLibros.set(Math.max(1, Math.ceil(r.total / 9)));
              this.cargar();
              return;
            }
            this.libros.set(r.datos);
            this.totalLibros.set(r.total);
            this.finalizar();
          },
          error: err => this.error(err)
        });
        break;
      case 'prestamos':
        this.servicio.obtenerPendientes().pipe(takeUntilDestroyed(this.destruir)).subscribe({
          next: datos => { this.prestamos.set(datos); this.finalizar(); }, error: err => this.error(err)
        });
        break;
      case 'multas':
        this.servicio.obtenerMultas().pipe(takeUntilDestroyed(this.destruir)).subscribe({
          next: datos => { this.multas.set(datos); this.finalizar(); }, error: err => this.error(err)
        });
        break;
    }
  }

  anteriorLibros(): void {
    if (this.paginaLibros() > 1) {
      this.paginaLibros.update(p => p - 1);
      this.cargar();
    }
  }

  siguienteLibros(): void {
    if (this.paginaLibros() * 9 < this.totalLibros()) {
      this.paginaLibros.update(p => p + 1);
      this.cargar();
    }
  }

  cargarMasPagos(): void {
    const actual = this.historial();
    if (!actual || this.cargandoMasPagos() || actual.datos.length >= actual.total) return;
    this.cargandoMasPagos.set(true);
    this.servicio.obtenerHistorialPagos(actual.pagina + 1)
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next: pagina => {
          this.historial.set({ ...pagina, datos: [...actual.datos, ...pagina.datos] });
          this.cargandoMasPagos.set(false);
        },
        error: () => {
          this.mensajeError.set('No fue posible cargar más pagos.');
          this.cargandoMasPagos.set(false);
        }
      });
  }

  private finalizar(): void { this.cargando.set(false); }

  private error(err: HttpErrorResponse): void {
    this.cargando.set(false);
    this.mensajeError.set(
      err.status === 0 ? 'No se pudo conectar al servidor.' :
      err.status === 403 ? 'No tienes permiso para consultar esta información.' :
      (typeof err.error?.mensaje === 'string' ? err.error.mensaje : 'No se pudo realizar la consulta.')
    );
  }
}
