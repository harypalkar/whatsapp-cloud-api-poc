import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly mode = signal<'light' | 'dark'>((localStorage.getItem('wf_theme') as 'light' | 'dark') || 'light');

  constructor() {
    effect(() => {
      const m = this.mode();
      document.body.classList.toggle('theme-dark', m === 'dark');
      document.body.classList.toggle('theme-light', m === 'light');
      localStorage.setItem('wf_theme', m);
    });
  }

  toggle() { this.mode.update(m => m === 'light' ? 'dark' : 'light'); }
}
