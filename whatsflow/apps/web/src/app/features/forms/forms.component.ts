import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DemoApiService } from '../../core/services/demo-api.service';

@Component({
  selector: 'wf-forms',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './forms.component.html',
  styleUrl: './forms.component.scss',
})
export class FormsComponent implements OnInit {
  loading = signal(true);
  forms = signal<any[]>([]);
  copied = signal<string | null>(null);

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.forms().subscribe({
      next: (data) => {
        this.forms.set(Array.isArray(data) ? data : data?.content || []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  publicUrl(token: string): string {
    return `/f/${token}`;
  }

  responses(f: any): number | string {
    try {
      const s = JSON.parse(f.schemaJson || '{}');
      return s.responses ?? '—';
    } catch {
      return '—';
    }
  }

  copy(token: string) {
    const url = `${location.origin}${this.publicUrl(token)}`;
    navigator.clipboard?.writeText(url).then(() => {
      this.copied.set(token);
      setTimeout(() => this.copied.set(null), 1500);
    });
  }
}
