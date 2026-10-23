import { FechaServidor } from './gestion-colegio';
export interface InscripcionEscolar { idInscripcion: number; idEstudiante: number; idCiclo: number; idGrado: number; idSeccion: number; }
export interface PagoEscolar { idPago: number; idEstudiante: number; idEmpleado: number; concepto: 'INSCRIPCION' | 'MENSUALIDAD' | 'ACTIVIDAD'; monto: number; fecha: FechaServidor; }
export interface FacturaEscolar { idFactura: number; numeroFactura: string; fecha: FechaServidor; montoTotal: number; idPago: number; }
export interface EstadoCuentaEscolar { idEstudiante: number; saldoPendiente: number; }
export interface SolvenciaEscolar { idEstudiante: number; solvente: boolean; }
