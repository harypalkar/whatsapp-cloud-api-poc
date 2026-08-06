import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemoApiService } from '../../core/services/demo-api.service';
import { parseAttrs, unwrapPage, formatNum } from '../../core/utils/page.util';

@Component({
  selector: 'wf-customers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customers.component.html',
  styleUrl: './customers.component.scss',
})
export class CustomersComponent implements OnInit {
  loading = signal(true);
  rows = signal<any[]>([]);
  page = signal(0);
  size = 20;
  totalElements = signal(0);
  totalPages = signal(0);
  q = '';
  private searchTimer: any;

  constructor(private demo: DemoApiService) {}

  ngOnInit() { this.load(); }

  load(page = this.page()) {
    this.loading.set(true);
    this.demo.customers(page, this.size, this.q.trim()).subscribe({
      next: (data) => {
        const p = unwrapPage(data);
        this.rows.set(p.content);
        this.page.set(p.page);
        this.totalElements.set(p.totalElements);
        this.totalPages.set(p.totalPages);
        this.loading.set(false);
      },
      error: () => { this.rows.set([]); this.loading.set(false); },
    });
  }

  onSearch() {
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.load(0), 300);
  }

  prev() { if (this.page() > 0) this.load(this.page() - 1); }
  next() { if (this.page() + 1 < this.totalPages()) this.load(this.page() + 1); }

  city(c: any): string {
    const a = parseAttrs(c.attributesJson);
    return a['city'] || a['state'] || '—';
  }

  tag(c: any): string {
    const a = parseAttrs(c.attributesJson);
    return a['tag'] || a['group'] || '—';
  }

  fmt = formatNum;
}
