import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DemoApiService } from '../../core/services/demo-api.service';
import { formatInr, formatNum } from '../../core/utils/page.util';

@Component({
  selector: 'wf-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminComponent implements OnInit {
  loading = signal(true);
  data = signal<any>(null);

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.module('admin').subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  fmt = formatNum;
  inr = formatInr;

  subEntries(subs: any): { key: string; value: number }[] {
    if (!subs) return [];
    return Object.entries(subs).map(([key, value]) => ({ key, value: Number(value) || 0 }));
  }
}
