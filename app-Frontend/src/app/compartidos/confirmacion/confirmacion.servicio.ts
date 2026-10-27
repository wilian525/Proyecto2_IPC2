import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ConfirmacionServicio {
  readonly mensaje = signal('');
  private responder: ((aceptado: boolean) => void) | null = null;

  confirmar(texto: string): Promise<boolean> {
    if (this.responder) return Promise.resolve(false);
    this.mensaje.set(texto);
    return new Promise(resolve => { this.responder = resolve; });
  }

  resolver(aceptado: boolean): void {
    const respuesta = this.responder;
    this.responder = null;
    this.mensaje.set('');
    if (respuesta) respuesta(aceptado);
  }
}
