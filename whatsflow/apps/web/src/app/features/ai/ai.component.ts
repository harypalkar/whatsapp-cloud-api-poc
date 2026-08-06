import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemoApiService } from '../../core/services/demo-api.service';

@Component({
  selector: 'wf-ai',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai.component.html',
  styleUrl: './ai.component.scss',
})
export class AiComponent implements OnInit {
  loading = signal(true);
  catalog = signal<any>(null);
  input = 'Patient asking for Saturday OPD slot and insurance coverage.';
  suggesting = signal(false);
  suggestion = signal<any | null>(null);

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.module('ai').subscribe({
      next: (d) => { this.catalog.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  suggest() {
    const text = this.input.trim();
    if (!text) return;
    this.suggesting.set(true);
    this.suggestion.set(null);
    this.demo.aiSuggest(text).subscribe({
      next: (res) => { this.suggestion.set(res); this.suggesting.set(false); },
      error: () => {
        this.suggestion.set({ content: 'Could not generate a suggestion. Try again.' });
        this.suggesting.set(false);
      },
    });
  }

  usePreset(s: string) {
    this.input = s;
  }
}
