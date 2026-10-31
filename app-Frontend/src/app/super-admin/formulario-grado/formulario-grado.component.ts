import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { GradoEscolar, NivelEscolar } from '../../modelos/administracion';
import { CarreraColegio } from '../../modelos/gestion-colegio';

@Component({
  selector: 'app-formulario-grado',
  imports: [ReactiveFormsModule],
  templateUrl: './formulario-grado.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class FormularioGradoComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);
  @Input() grado: GradoEscolar | null = null;
  @Input() niveles: NivelEscolar[] = [];
  @Input() carreras: CarreraColegio[] = [];
  @Input() procesando = false;
  @Output() guardar = new EventEmitter<Omit<GradoEscolar, 'idGrado'>>();
  @Output() cancelar = new EventEmitter<void>();

  readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    idNivel: [0, [Validators.required, Validators.min(1)]],
    idCarrera: ['']
  });
  intentado = false;

  esDiversificado(): boolean {
    const id = Number(this.formulario.controls.idNivel.value);
    return this.niveles.some(n => n.idNivel === id && n.nombre.toUpperCase() === 'DIVERSIFICADO');
  }

  carrerasDisponibles(): CarreraColegio[] {
    const id = Number(this.formulario.controls.idNivel.value);
    return this.carreras.filter(c => c.idNivel === id &&
      (c.estado === 'ACTIVO' || c.idCarrera === Number(this.formulario.controls.idCarrera.value)));
  }

  ngOnChanges(cambios: SimpleChanges): void {
    if (!cambios['grado']) return;
    this.formulario.reset({
      nombre: this.grado?.nombre ?? '', idNivel: this.grado?.idNivel ?? 0,
      idCarrera: this.grado?.idCarrera != null ? String(this.grado.idCarrera) : ''
    });
    this.intentado = false;
  }

  enviar(): void {
    this.intentado = true;
    if (this.formulario.invalid) { this.formulario.markAllAsTouched(); return; }
    const d = this.formulario.getRawValue();
    const carreraTexto = String(d.idCarrera ?? '').trim();
    const idCarrera = carreraTexto === '' ? null : Number(carreraTexto);
    if (this.esDiversificado() && (!idCarrera || !Number.isInteger(idCarrera) || idCarrera <= 0)) return;
    this.guardar.emit({ nombre: d.nombre.trim(), idNivel: Number(d.idNivel),
      idCarrera: this.esDiversificado() ? idCarrera : null });
  }
}
