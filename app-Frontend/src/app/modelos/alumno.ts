export interface Alumno {
  idEstudiante: number;
  idUsuario: number;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  direccion: string | null;
  telefono: string | null;
  correo: string | null;
  informacionMedica: string | null;
  datosEncargados: string | null;
  estado: string;
}

export interface NotaAlumno {
  idNota: number;
  idInscripcion: number;
  idZona: number;
  valor: number;
}

export interface EstadoCuenta {
  idEstudiante: number;
  saldoPendiente: number;
}

export interface PagoAlumno {
  idPago: number;
  idEstudiante: number;
  idEmpleado: number;
  concepto: string;
  monto: number;
  fecha: string;
}

export interface PaginaPagos {
  datos: PagoAlumno[];
  pagina: number;
  limite: number;
  total: number;
}

export interface LibroAlumno {
  idLibro: number;
  titulo: string;
  autor: string;
  editorial: string;
  cantidadDisponible: number;
  estado: string;
}

export interface PrestamoAlumno {
  idPrestamo: number;
  idLibro: number;
  idUsuario: number;
  fechaPrestamo: string;
  fechaDevolucionEsperada: string;
  fechaDevolucion: string | null;
  diasAtraso: number;
  montoMulta: number;
}
