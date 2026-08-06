import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemoApiService } from '../../core/services/demo-api.service';

@Component({
  selector: 'wf-templates',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './templates.component.html',
  styleUrl: './templates.component.scss',
})
export class TemplatesComponent implements OnInit {
  loading = signal(true);
  templates = signal<any[]>([]);
  q = '';
  category = 'ALL';

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.module('templates').subscribe({
      next: (data) => {
        this.templates.set(Array.isArray(data) ? data : []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  filtered(): any[] {
    const q = this.q.trim().toLowerCase();
    return this.templates().filter((t) => {
      const catOk = this.category === 'ALL' || t.category === this.category;
      const qOk = !q || (t.name || '').toLowerCase().includes(q) || (t.body || '').toLowerCase().includes(q);
      return catOk && qOk;
    });
  }
}
