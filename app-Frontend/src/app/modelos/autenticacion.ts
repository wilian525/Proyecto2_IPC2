export interface DatosIngreso {
  nombreUsuario: string;
  contrasena: string;
}

export interface RespuestaIngreso {
  token: string;
}

export interface DatosRecuperacion {
  nombreUsuario: string;
  nombre: string;
  apellido: string;
  fechaVerificacion: string; // yyyy-MM-dd
  nuevaContrasena: string;
}

export interface RespuestaRecuperacion {
  mensaje: string;
}

export interface ErrorServidor {
  codigo?: string;
  mensaje?: string;
}

export type RolUsuario = 'SUPER_ADMIN' | 'ADMIN' | 'SECRETARIA' |
  'BIBLIOTECARIO' | 'MAESTRO' | 'ESTUDIANTE' | 'MANTENIMIENTO' | 'LIMPIEZA';
