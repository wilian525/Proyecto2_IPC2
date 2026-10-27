import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ConfirmacionComponent } from './compartidos/confirmacion/confirmacion.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ConfirmacionComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
