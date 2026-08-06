import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DemoApiService } from '../../core/services/demo-api.service';
import { formatInr, formatNum } from '../../core/utils/page.util';

@Component({
  selector: 'wf-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss',
})
export class ReportsComponent implements OnInit {
  loading = signal(true);
  data = signal<any>(null);
  toast = signal('');

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.module('reports').subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  fmt = formatNum;
  inr = formatInr;

  exportAs(kind: 'PDF' | 'Excel') {
    this.toast.set(`${kind} export queued (demo)`);
    setTimeout(() => this.toast.set(''), 2000);
  }
}
