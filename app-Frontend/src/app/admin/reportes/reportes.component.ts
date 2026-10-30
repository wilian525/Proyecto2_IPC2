import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { GestionColegioServicio } from '../gestion.servicio';
import { explicarError } from '../mensajes-error';
import { AdministracionServicio } from '../../super-admin/administracion.servicio';
import { CicloEscolar, GradoEscolar } from '../../modelos/administracion';
import { EstudianteColegio, SeccionColegio, fechaFormulario, FechaServidor } from '../../modelos/gestion-colegio';

interface DetalleIngreso { concepto: string; total: number; }
interface DatosIngresos { detalle: DetalleIngreso[]; totalIngresos: number; }
type TipoReporte = 'ingresos' | 'solventes-morosos' | 'rendimiento' | 'historial-pagos' | 'prestamos-mora';

@Component({
  selector: 'app-reportes-administrador',
  imports: [RouterLink, ReactiveFormsModule, CurrencyPipe],
  templateUrl: './reportes.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class ReportesAdministradorComponent {
  private readonly servicio = inject(GestionColegioServicio);
  private readonly administracion = inject(AdministracionServicio);
  private readonly destruir = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  readonly datos = signal<unknown>(null);
  readonly pagina = signal(1);
  readonly paginaEstudiantes = signal(1);
  readonly total = signal(0);
  readonly totalEstudiantes = signal(0);
  readonly limite = 7;
  readonly grados = signal<GradoEscolar[]>([]);
  readonly secciones = signal<SeccionColegio[]>([]);
  readonly ciclos = signal<CicloEscolar[]>([]);
  readonly estudiantes = signal<EstudianteColegio[]>([]);
  readonly cargando = signal(false);
  readonly procesando = signal(false);
  readonly error = signal('');
  readonly mensaje = signal('');
  readonly formulario = this.fb.nonNullable.group({
    tipo: ['ingresos' as TipoReporte],
    fechaInicio: [''], fechaFin: [''],
    grado: [0], seccion: [0], ciclo: [0],
    idEstudiante: [0], mensualidades: [0]
  });
  readonly opciones: { valor: TipoReporte; titulo: string }[] = [
    {valor:'ingresos', titulo:'Ingresos por concepto'},
    {valor:'solventes-morosos', titulo:'Alumnos solventes y morosos'},
    {valor:'rendimiento', titulo:'Rendimiento por grado y sección'},
    {valor:'historial-pagos', titulo:'Historial de pagos de estudiante'},
    {valor:'prestamos-mora', titulo:'Préstamos de biblioteca en mora'}
  ];
  constructor() {
    this.administracion.consultarGrados().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: d => this.grados.set(d), error: e => this.error.set(explicarError(e, 'consultar grados para reportes')) });
    this.administracion.consultarCiclos().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: d => this.ciclos.set(d), error: e => this.error.set(explicarError(e, 'consultar ciclos para reportes')) });
    this.servicio.secciones().pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: d => this.secciones.set(d), error: e => this.error.set(explicarError(e, 'consultar secciones para reportes')) });
    this.servicio.estudiantes(1, 100).pipe(takeUntilDestroyed(this.destruir)).subscribe({
      next: r => this.estudiantes.set(r.datos), error: e => this.error.set(explicarError(e, 'consultar estudiantes para reportes')) });
    this.consultar();
  }
  tipo(): TipoReporte { return this.formulario.controls.tipo.value; }
  private parametros(): { ruta: string; parametros: Record<string,string|number> } | null {
    const d = this.formulario.getRawValue();
    if (!Number.isInteger(d.mensualidades) || d.mensualidades < 0 || d.mensualidades > 12) {
      this.error.set('Las mensualidades a evaluar deben estar entre 0 y 12.'); return null;
    }
    if (d.fechaInicio && d.fechaFin && d.fechaInicio > d.fechaFin) {
      this.error.set('La fecha inicial no puede ser posterior a la fecha final.'); return null;
    }
    const rango = { fechaInicio: d.fechaInicio, fechaFin: d.fechaFin };
    if (d.tipo === 'ingresos') return { ruta: 'ingresos', parametros: rango };
    if (d.tipo === 'prestamos-mora') return { ruta: 'prestamos-mora', parametros: {} };
    if (d.tipo === 'historial-pagos') {
      if (d.idEstudiante < 1) { this.error.set('Selecciona un estudiante válido para consultar sus pagos.'); return null; }
      return { ruta: `historial-pagos/${d.idEstudiante}`, parametros: { ...rango, mensualidades: d.mensualidades } };
    }
    if (d.grado < 1 || d.seccion < 1 || d.ciclo < 1) {
      this.error.set('Indica los IDs válidos de grado, sección y ciclo para generar este reporte.'); return null;
    }
    return { ruta: d.tipo, parametros: {
      grado: d.grado, seccion: d.seccion, ciclo: d.ciclo,
      ...(d.tipo === 'solventes-morosos' ? { mensualidades: d.mensualidades } : {})
    }};
  }
  consultar(): void {
    this.pagina.set(1);
    this.paginaEstudiantes.set(1);
    this.cargar();
  }
  private cargar(): void {
    const filtros = this.parametros(); if (!filtros) return;
    const parametros = { ...filtros.parametros, limite: this.limite,
      ...(this.tipo() === 'rendimiento' ? { paginaCursos: this.pagina(), paginaEstudiantes: this.paginaEstudiantes() }
        : { pagina: this.pagina() }) };
    this.cargando.set(true); this.datos.set(null); this.error.set(''); this.mensaje.set('');
    this.servicio.consultarReporte(filtros.ruta, parametros)
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next: valor => {
          this.datos.set(valor);
          const resultado = valor as Record<string, unknown>;
          this.total.set(Number(this.tipo() === 'rendimiento' ? resultado['totalCursos'] : resultado['total']) || 0);
          this.totalEstudiantes.set(Number(resultado['totalEstudiantes']) || 0);
          this.cargando.set(false);
        },
        error: e => { this.cargando.set(false); this.error.set(explicarError(e, 'consultar el reporte')); }
      });
  }
  cambiarReporte(): void {
    this.datos.set(null); this.error.set(''); this.mensaje.set('');
    this.pagina.set(1); this.paginaEstudiantes.set(1); this.total.set(0); this.totalEstudiantes.set(0);
  }
  anterior(): void { if (this.pagina() > 1) { this.pagina.update(p => p - 1); this.cargar(); } }
  siguiente(): void { if (this.pagina() * this.limite < this.total()) { this.pagina.update(p => p + 1); this.cargar(); } }
  anterioresEstudiantes(): void { if (this.paginaEstudiantes() > 1) { this.paginaEstudiantes.update(p => p - 1); this.cargar(); } }
  siguientesEstudiantes(): void { if (this.paginaEstudiantes() * this.limite < this.totalEstudiantes()) { this.paginaEstudiantes.update(p => p + 1); this.cargar(); } }
  fechaPago(valor: FechaServidor): string {
    const fecha = fechaFormulario(valor);
    if (!fecha) return '';
    const partes = fecha.split('-');
    return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : fecha;
  }
  descargarPdf(): void {
    const filtros = this.parametros(); if (!filtros || this.procesando()) return;
    this.procesando.set(true); this.error.set('');
    this.servicio.descargarReporte(filtros.ruta, filtros.parametros)
      .pipe(takeUntilDestroyed(this.destruir)).subscribe({
        next: archivo => {
          this.procesando.set(false);
          const url = URL.createObjectURL(archivo);
          const enlace = document.createElement('a'); enlace.href = url;
          enlace.download = `${this.tipo().replaceAll('-', '_')}.pdf`; enlace.click();
          setTimeout(() => URL.revokeObjectURL(url), 1000);
          this.mensaje.set('PDF generado correctamente.');
        },
        error: e => { this.procesando.set(false); this.error.set(explicarError(e, 'descargar el PDF')); }
      });
  }
  ingresos(): DatosIngresos { return this.datos() as DatosIngresos; }
  filas(): Record<string,unknown>[] { return (this.registro()['datos'] ?? []) as Record<string,unknown>[]; }
  registro(): Record<string, unknown> { return (this.datos() ?? {}) as Record<string,unknown>; }
  texto(valor: unknown): string { return typeof valor === 'string' || typeof valor === 'number' ? String(valor) : JSON.stringify(valor ?? ''); }
}
