import { HttpErrorResponse } from '@angular/common/http';

// Mensajes seguros y comprensibles; el backend sigue siendo la fuente de las validaciones.
export function explicarError(error: unknown, accion: string): string {
  if (!(error instanceof HttpErrorResponse)) return `No se pudo ${accion}. Intenta nuevamente.`;
  const servidor = typeof error.error?.mensaje === 'string' ? error.error.mensaje.trim() : '';
  let detalle = '';
  switch (error.status) {
    case 0: detalle = 'No hay conexión con el backend. Comprueba Tomcat y la URL de la API.'; break;
    case 400: detalle = servidor || 'Los datos enviados no son válidos.'; break;
    case 401: detalle = 'La sesión no es válida o ha expirado. Inicia sesión nuevamente.'; break;
    case 403: detalle = 'Tu cuenta no tiene permisos para esta operación.'; break;
    case 404: detalle = servidor || 'No se encontró el registro o la ruta solicitada.'; break;
    case 409: detalle = servidor || 'La operación entra en conflicto con los registros existentes.'; break;
    default: detalle = error.status >= 500
      ? 'Ocurrió un problema en el servidor. Consulta los registros de Tomcat.'
      : servidor || 'Ocurrió un error inesperado.';
  }
  return `No se pudo ${accion}: ${detalle}`;
}
