import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DemoApiService } from '../../core/services/demo-api.service';
import { formatInr } from '../../core/utils/page.util';

@Component({
  selector: 'wf-billing',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './billing.component.html',
  styleUrl: './billing.component.scss',
})
export class BillingComponent implements OnInit {
  loading = signal(true);
  data = signal<any>(null);

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.module('billing').subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  inr = formatInr;
}
