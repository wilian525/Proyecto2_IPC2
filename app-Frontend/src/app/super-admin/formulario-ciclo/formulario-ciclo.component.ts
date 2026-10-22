import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-formulario-ciclo',
  imports: [ReactiveFormsModule],
  templateUrl: './formulario-ciclo.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class FormularioCicloComponent {
  private readonly fb = inject(FormBuilder);
  @Input() procesando = false;
  @Output() crear = new EventEmitter<{ anioInicio: number; anioFin: number }>();
  readonly formulario = this.fb.nonNullable.group({
    anioInicio: [new Date().getFullYear(), [Validators.required, Validators.min(2000), Validators.max(2100)]],
    anioFin: [new Date().getFullYear(), [Validators.required, Validators.min(2000), Validators.max(2101)]]
  });
  intentado = false;

  enviar(): void {
    this.intentado = true;
    const d = this.formulario.getRawValue();
    if (this.formulario.invalid || d.anioFin < d.anioInicio) return;
    this.crear.emit({ anioInicio: Number(d.anioInicio), anioFin: Number(d.anioFin) });
  }
}
