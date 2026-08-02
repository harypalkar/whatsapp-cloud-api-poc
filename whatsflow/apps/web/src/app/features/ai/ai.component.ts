import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'wf-ai',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai.component.html',
  styleUrl: './ai.component.scss',
})
export class AiComponent {}
