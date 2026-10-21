import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter,withDebugTracing } from '@angular/router';
import { rutas } from './app.routes';
import { agregarToken } from './seguridad/agregar-token.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(rutas, withDebugTracing()),
    provideHttpClient(withInterceptors([agregarToken]))
  ]
};
