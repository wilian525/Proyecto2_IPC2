import { FechaServidor } from './gestion-colegio';
export interface LibroBiblioteca { idLibro:number; titulo:string; autor:string; editorial:string; cantidadDisponible:number; estado:string; }
export interface LectorBiblioteca { idUsuario:number; nombreUsuario:string; nombreCompleto:string; rol:string; }
export interface PrestamoBiblioteca {
  idPrestamo:number; idLibro:number; idUsuario:number; fechaPrestamo:FechaServidor;
  fechaDevolucionEsperada:FechaServidor; fechaDevolucion:FechaServidor|null;
  diasAtraso:number; montoMulta:number; fechaAviso:FechaServidor|null;
}
export interface ReporteMora { idPrestamo:number; estudiante:string; libro:string; diasAtraso:number; montoMulta:number; [campo:string]:unknown; }
