export type RolAdministrativo = 'ADMIN' | 'SUPER_ADMIN';

export interface Administrador {
  idUsuario: number;
  nombreUsuario: string;
  rol: RolAdministrativo;
  estado: string;
  idEmpleado: number | null;
  nombre: string | null;
  apellido: string | null;
  puesto: string | null;
  fechaContratacion: string | null;
  salario: number | null;
}

export interface DatosAdministrador {
  usuario: { nombreUsuario: string; contrasena?: string };
  empleado: {
    nombre: string;
    apellido: string;
    fechaContratacion: string;
    salario: number;
  };
}

export interface PaginaAdministradores {
  datos: Administrador[];
  total: number;
  pagina: number;
  limite: number;
}

export interface CicloEscolar {
  idCiclo: number;
  anioInicio: number;
  anioFin: number;
  estado: string;
}

export interface GradoEscolar {
  idGrado: number;
  nombre: string;
  idNivel: number;
  idCarrera: number | null;
}

export interface NivelEscolar {
  idNivel: number;
  nombre: string;
}

export interface RespuestaOperacion { mensaje: string; }
