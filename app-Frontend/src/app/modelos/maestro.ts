import { FechaServidor } from './gestion-colegio';

export interface AsignacionMaestro {
  idAsignacion: number; idCurso: number; idSeccion: number; idCiclo: number;
  curso: string; grado: string; seccion: string; ciclo: string; estadoCiclo: string;
}
export interface AlumnoNomina { idInscripcion: number; idEstudiante: number; nombre: string; apellido: string; }
export interface AsistenciaMaestro { idAsistencia: number; idAsignacion: number; idInscripcion: number; fecha: FechaServidor; estado: string; }
export interface ZonaMaestro { idZona: number; nombre: string; porcentaje: number; idAsignacion: number; }
export interface NotaMaestro { idNota: number; idInscripcion: number; idZona: number; valor: number; }
export interface PromedioCurso { idCurso: number; curso: string; promedio: number; estudiantesEvaluados: number; }
export interface PromedioEstudiante { idEstudiante: number; estudiante: string; promedioGeneral: number; }
export interface RendimientoMaestro {
  grado: string; seccion: string; promediosCursos: PromedioCurso[];
  cursoMayorDificultad: PromedioCurso | null; mejoresPromedios: PromedioEstudiante[];
}
