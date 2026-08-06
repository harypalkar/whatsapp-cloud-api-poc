import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DemoApiService } from '../../core/services/demo-api.service';
import { unwrapPage, formatNum } from '../../core/utils/page.util';

@Component({
  selector: 'wf-campaigns',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './campaigns.component.html',
  styleUrl: './campaigns.component.scss',
})
export class CampaignsComponent implements OnInit {
  loading = signal(true);
  campaigns = signal<any[]>([]);
  catalogStats = signal<any>(null);

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.campaigns().subscribe({
      next: (data) => {
        this.campaigns.set(unwrapPage(data).content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.demo.module('reports').subscribe({
      next: (r) => this.catalogStats.set(r?.campaignReport || null),
    });
  }

  badgeClass(status: string): string {
    const s = (status || '').toUpperCase();
    if (s === 'COMPLETED' || s === 'RUNNING') return 'ok';
    if (s === 'SCHEDULED' || s === 'DRAFT') return 'warn';
    if (s === 'PAUSED' || s === 'CANCELLED') return 'danger';
    return 'neutral';
  }

  fmt = formatNum;

  placeholderStats(i: number) {
    const base = this.catalogStats();
    if (!base) {
      return { sent: 1200 + i * 340, delivered: 1100 + i * 300, read: 700 + i * 180 };
    }
    const factor = 1 / Math.max(this.campaigns().length, 1);
    return {
      sent: Math.round((base.sent || 0) * factor * (0.7 + (i % 5) * 0.08)),
      delivered: Math.round((base.delivered || 0) * factor * (0.7 + (i % 5) * 0.08)),
      read: Math.round((base.read || 0) * factor * (0.7 + (i % 5) * 0.08)),
    };
  }
}
