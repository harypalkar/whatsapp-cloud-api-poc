import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DemoApiService } from '../../core/services/demo-api.service';
import { formatInr, formatNum } from '../../core/utils/page.util';

@Component({
  selector: 'wf-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  loading = signal(true);
  data = signal<any>(null);
  notifications = signal<any[]>([]);
  bellOpen = signal(false);

  unreadNotifs = computed(() => this.notifications().filter((n) => !n.read).length);

  chartMax = computed(() => {
    const vals: number[] = this.data()?.chartMessages ?? [];
    return Math.max(...vals, 1);
  });

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.dashboard().subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.demo.module('notifications').subscribe({
      next: (n) => this.notifications.set(Array.isArray(n) ? n : []),
    });
  }

  fmt = formatNum;
  inr = formatInr;

  barHeight(v: number): string {
    return `${Math.round((v / this.chartMax()) * 100)}%`;
  }

  actionLink(label: string): string {
    const map: Record<string, string> = {
      'New campaign': '/app/campaigns',
      'Open inbox': '/app/inbox',
      'Import customers': '/app/customers',
      'Run demo scenario': '/app/automations',
    };
    return map[label] || '/app/dashboard';
  }

  toggleBell() { this.bellOpen.update((v) => !v); }
}
