import { Injectable } from '@angular/core';
import { RolUsuario } from '../../models/auth.model';

interface JwtPayload {
  sub?: string;
  rol?: string;
  exp?: number;
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly claveToken = 'res_token';

  guardarToken(token: string): void {
    if (!this.leerPayload(token)) {
      throw new Error('El servidor devolvió un token inválido');
    }
    sessionStorage.setItem(this.claveToken, token);
  }

  obtenerToken(): string | null {
    const token = sessionStorage.getItem(this.claveToken);
    if (!token) return null;
    const payload = this.leerPayload(token);
    if (!payload?.exp || payload.exp * 1000 <= Date.now()) {
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
    const rol = this.leerPayload(token)?.rol?.toUpperCase();
    const roles: RolUsuario[] = [
      'SUPER_ADMIN', 'ADMIN', 'SECRETARIA',
      'BIBLIOTECARIO', 'MAESTRO', 'ESTUDIANTE'
    ];
    return roles.find((valor) => valor === rol) ?? null;
  }

  cerrarSesion(): void {
    sessionStorage.removeItem(this.claveToken);
  }

  // Leer el rol sirve para la interfaz; el backend valida realmente el JWT.
  private leerPayload(token: string): JwtPayload | null {
    try {
      const partes = token.split('.');
      if (partes.length !== 3) return null;
      const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
      const texto = atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='));
      return JSON.parse(texto) as JwtPayload;
    } catch {
      return null;
    }
  }
}
