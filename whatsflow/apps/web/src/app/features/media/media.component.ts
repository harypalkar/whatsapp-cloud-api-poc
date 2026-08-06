import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemoApiService } from '../../core/services/demo-api.service';

@Component({
  selector: 'wf-media',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './media.component.html',
  styleUrl: './media.component.scss',
})
export class MediaComponent implements OnInit {
  loading = signal(true);
  items = signal<any[]>([]);
  folder = 'ALL';
  type = 'ALL';

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.module('media').subscribe({
      next: (data) => {
        this.items.set(Array.isArray(data) ? data : []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  folders(): string[] {
    const set = new Set(this.items().map((i) => i.folder).filter(Boolean));
    return ['ALL', ...Array.from(set)];
  }

  filtered(): any[] {
    return this.items().filter((i) => {
      const fOk = this.folder === 'ALL' || i.folder === this.folder;
      const tOk = this.type === 'ALL' || i.type === this.type;
      return fOk && tOk;
    });
  }

  icon(type: string): string {
    const t = (type || '').toLowerCase();
    if (t === 'image') return 'IMG';
    if (t === 'video') return 'VID';
    if (t === 'pdf') return 'PDF';
    if (t === 'excel') return 'XLS';
    if (t === 'word') return 'DOC';
    return 'FILE';
  }
}
