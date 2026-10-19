export interface LoginRequest {
  nombreUsuario: string;
  contrasena: string;
}

export interface LoginResponse {
  token: string;
}

export interface ApiError {
  codigo: string;
  mensaje: string;
}

export type RolUsuario =
  | 'SUPER_ADMIN'
  | 'ADMIN'
  | 'SECRETARIA'
  | 'BIBLIOTECARIO'
  | 'MAESTRO'
  | 'ESTUDIANTE';
