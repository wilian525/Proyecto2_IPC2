import { Component, EventEmitter, Input, OnChanges, Output, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Administrador, DatosAdministrador, RolAdministrativo } from '../../modelos/administracion';

@Component({
  selector: 'app-formulario-administrativo',
  imports: [ReactiveFormsModule],
  templateUrl: './formulario-usuario.component.html',
  styleUrl: '../../compartidos/estilos/estilos-administracion.css'
})
export class FormularioUsuarioComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);
  @Input({ required: true }) rol: RolAdministrativo = 'ADMIN';
  @Input() usuario: Administrador | null = null;
  @Input() procesando = false;
  @Output() guardar = new EventEmitter<DatosAdministrador>();
  @Output() cancelar = new EventEmitter<void>();

  readonly formulario = this.fb.nonNullable.group({
    nombreUsuario: ['', [Validators.required, Validators.maxLength(100)]],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    fechaContratacion: ['', Validators.required],
    salario: [0, [Validators.required, Validators.min(0)]],
    contrasena: ['']
  });
  intentado = false;

  ngOnChanges(): void {
    const u = this.usuario;
    this.formulario.reset({
      nombreUsuario: u?.nombreUsuario ?? '',
      nombre: u?.nombre ?? '',
      apellido: u?.apellido ?? '',
      fechaContratacion: u?.fechaContratacion ?? '',
      salario: u?.salario ?? 0,
      contrasena: ''
    });
    this.intentado = false;
  }

  enviar(): void {
    this.intentado = true;
    const d = this.formulario.getRawValue();
    if (this.formulario.invalid || (!this.usuario && d.contrasena.length < 8)) {
      this.formulario.markAllAsTouched();
      return;
    }
    const datos: DatosAdministrador = {
      usuario: { nombreUsuario: d.nombreUsuario.trim() },
      empleado: {
        nombre: d.nombre.trim(), apellido: d.apellido.trim(),
        fechaContratacion: d.fechaContratacion, salario: Number(d.salario)
      }
    };
    if (!this.usuario) datos.usuario.contrasena = d.contrasena;
    this.guardar.emit(datos);
  }

  invalido(campo: string): boolean {
    const control = this.formulario.get(campo);
    return Boolean(control?.invalid && (control.touched || this.intentado));
  }
}
