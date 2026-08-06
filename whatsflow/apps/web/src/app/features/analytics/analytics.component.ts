import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DemoApiService } from '../../core/services/demo-api.service';

@Component({
  selector: 'wf-analytics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.scss',
})
export class AnalyticsComponent implements OnInit {
  loading = signal(true);
  data = signal<any>(null);

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.module('analytics').subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  max(arr: number[] | undefined): number {
    return Math.max(...(arr || [1]), 1);
  }

  pct(v: number, max: number): string {
    return `${Math.round((v / max) * 100)}%`;
  }

  linePoints(arr: number[] | undefined): string {
    const vals = arr || [];
    if (!vals.length) return '';
    const max = this.max(vals);
    return vals.map((v, i) => {
      const x = (i / Math.max(vals.length - 1, 1)) * 100;
      const y = 100 - (v / max) * 100;
      return `${x},${y}`;
    }).join(' ');
  }
}
