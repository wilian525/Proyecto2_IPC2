import { Injectable } from '@angular/core';
import { RolUsuario } from '../modelos/autenticacion';

interface DatosToken {
  sub?: string;
  rol?: string;
  exp?: number;
}

@Injectable({ providedIn: 'root' })
export class SesionServicio {
  private readonly nombreClave = 'res_token';

  guardarToken(token: string): void {
    if (!this.leerDatosToken(token)) {
      throw new Error('El servidor devolvió un token inválido.');
    }
    sessionStorage.setItem(this.nombreClave, token);
  }

  obtenerToken(): string | null {
    const token = sessionStorage.getItem(this.nombreClave);
    if (!token) return null;
    const datos = this.leerDatosToken(token);
    if (!datos?.exp || datos.exp * 1000 <= Date.now()) {
      this.cerrarSesion();
      return null;
    }
    return token;
  }

  estaAutenticado(): boolean {
    return this.obtenerToken() !== null;
  }

  obtenerRol(): RolUsuario | null {
    const token = this.obtenerToken();
    if (!token) return null;
    const rol = this.leerDatosToken(token)?.rol?.toUpperCase();
    const opciones: RolUsuario[] = [
      'SUPER_ADMIN', 'ADMIN', 'SECRETARIA',
      'BIBLIOTECARIO', 'MAESTRO', 'ESTUDIANTE', 'MANTENIMIENTO', 'LIMPIEZA'
    ];
    return opciones.find(opcion => opcion === rol) ?? null;
  }

  cerrarSesion(): void {
    sessionStorage.removeItem(this.nombreClave);
  }

  // Solo se lee el rol para la interfaz; el backend valida el token.
  private leerDatosToken(token: string): DatosToken | null {
    try {
      const partes = token.split('.');
      if (partes.length !== 3) return null;
      const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
      const texto = atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='));
      return JSON.parse(texto) as DatosToken;
    } catch {
      return null;
    }
  }
}
