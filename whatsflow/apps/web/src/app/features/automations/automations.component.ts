import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DemoApiService } from '../../core/services/demo-api.service';

@Component({
  selector: 'wf-automations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './automations.component.html',
  styleUrl: './automations.component.scss',
})
export class AutomationsComponent implements OnInit {
  loading = signal(true);
  workflows = signal<any[]>([]);
  scenarios = signal<any[]>([]);
  running = signal<string | null>(null);
  lastRun = signal<any | null>(null);

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.module('automations').subscribe({
      next: (data) => {
        this.workflows.set(Array.isArray(data) ? data : []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.demo.module('scenarios').subscribe({
      next: (data) => this.scenarios.set(Array.isArray(data) ? data : []),
    });
  }

  run(id: string) {
    this.running.set(id);
    this.lastRun.set(null);
    this.demo.runScenario(id).subscribe({
      next: (res) => { this.lastRun.set(res); this.running.set(null); },
      error: () => { this.running.set(null); this.lastRun.set({ message: 'Scenario run failed' }); },
    });
  }

  scenarioIdFor(wf: any): string {
    const match = this.scenarios().find((s) =>
      (s.name || '').toLowerCase() === (wf.name || '').toLowerCase()
      || (s.id || '') === (wf.id || '')
    );
    if (match?.id) return match.id;
    const fallback: Record<string, string> = {
      'hospital-appointment-reminder': 's1',
      'school-admission-campaign': 's2',
      'restaurant-offer': 's3',
      'property-inquiry': 's4',
      'insurance-renewal': 's5',
    };
    return fallback[wf.id] || 's1';
  }
}
