import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'wf-media',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './media.component.html',
  styleUrl: './media.component.scss',
})
export class MediaComponent {}
