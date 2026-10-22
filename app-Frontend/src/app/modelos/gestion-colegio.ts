export type FechaServidor = string | number[];
export interface RespuestaPagina<T> { datos: T[]; total: number; pagina: number; limite: number; }
export interface RespuestaAccion { mensaje: string; }
export interface EmpleadoColegio {
  idEmpleado: number; nombre: string; apellido: string; puesto: string;
  fechaContratacion: FechaServidor; salario: number; estado: string;
}
export interface EstudianteColegio {
  idEstudiante: number; nombre: string; apellido: string; fechaNacimiento: FechaServidor;
  direccion: string; telefono: string; correo: string; informacionMedica: string;
  datosEncargados: string; estado: 'ACTIVO' | 'INACTIVO' | 'GRADUADO'; idUsuario: number;
}
export interface CarreraColegio { idCarrera: number; nombre: string; idNivel: number; estado: string; }
export interface CursoColegio { idCurso: number; nombre: string; descripcion: string; }
export interface SeccionColegio { idSeccion: number; nombre: string; idGrado: number; }
export interface CurriculoColegio { idCurriculo: number; idGrado: number; idCiclo: number; }
export interface AsignacionColegio {
  idAsignacion: number; idEmpleado: number; idCurso: number; idSeccion: number; idCiclo: number;
}
export interface UsuarioNuevo { nombreUsuario: string; contrasena: string; rol: string; }
export interface ReporteIngreso { concepto?: string; total?: number; monto?: number; cantidad?: number; [campo: string]: unknown; }
export function fechaFormulario(fecha: FechaServidor | undefined | null): string {
  if (Array.isArray(fecha)) {
    const [anio, mes, dia] = fecha;
    return `${anio}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
  }
  return typeof fecha === 'string' ? fecha.slice(0, 10) : '';
}
