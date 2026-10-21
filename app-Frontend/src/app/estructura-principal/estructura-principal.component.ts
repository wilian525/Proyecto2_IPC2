import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AutenticacionServicio } from '../servicios/autenticacion.servicio';
import { SesionServicio } from '../servicios/sesion.servicio';

@Component({
  selector: 'app-estructura-principal',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './estructura-principal.component.html',
  styleUrl: './estructura-principal.component.css'
})
export class EstructuraPrincipalComponent {
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly sesion = inject(SesionServicio);
  private readonly router = inject(Router);

  readonly rol = this.sesion.obtenerRol() ?? 'USUARIO';
  readonly esAlumno = this.rol === 'ESTUDIANTE';
  readonly menuAbierto = signal(false);

  readonly opcionesAlumno = [
    { ruta: '/inicio', titulo: 'Resumen', icono: 'bi-grid-1x2' },
    { ruta: '/inicio/perfil', titulo: 'Mi perfil', icono: 'bi-person-vcard' },
    { ruta: '/inicio/notas', titulo: 'Mis notas', icono: 'bi-journal-check' },
    { ruta: '/inicio/cuenta', titulo: 'Estado de cuenta', icono: 'bi-wallet2' },
    { ruta: '/inicio/catalogo', titulo: 'Catálogo de libros', icono: 'bi-book' },
    { ruta: '/inicio/prestamos', titulo: 'Préstamos pendientes', icono: 'bi-bookmark-check' },
    { ruta: '/inicio/multas', titulo: 'Multas de biblioteca', icono: 'bi-receipt' }
  ];

  alternarMenu(): void { this.menuAbierto.update(actual => !actual); }
  cerrarMenu(): void { this.menuAbierto.set(false); }

  salir(): void {
    this.autenticacion.cerrarSesion();
    void this.router.navigateByUrl('/login');
  }
}
