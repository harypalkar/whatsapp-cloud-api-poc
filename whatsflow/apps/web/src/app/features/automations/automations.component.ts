import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'wf-automations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './automations.component.html',
  styleUrl: './automations.component.scss',
})
export class AutomationsComponent {}
