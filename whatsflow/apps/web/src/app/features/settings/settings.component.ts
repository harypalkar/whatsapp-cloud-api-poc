import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemoApiService } from '../../core/services/demo-api.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'wf-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss',
})
export class SettingsComponent implements OnInit {
  loading = signal(true);
  data = signal<any>(null);

  constructor(private demo: DemoApiService, public theme: ThemeService) {}

  ngOnInit() {
    this.demo.module('settings').subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
