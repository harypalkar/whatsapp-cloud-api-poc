import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DemoApiService } from '../../core/services/demo-api.service';

@Component({
  selector: 'wf-whatsapp',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './whatsapp.component.html',
  styleUrl: './whatsapp.component.scss',
})
export class WhatsappComponent implements OnInit {
  loading = signal(true);
  account = signal<any>(null);

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.whatsapp().subscribe({
      next: (d) => { this.account.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  connected(): boolean {
    const a = this.account();
    return !!a?.connected || (a?.status || '').toUpperCase() === 'CONNECTED';
  }
}
