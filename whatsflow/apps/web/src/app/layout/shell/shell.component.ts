import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NgFor } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'wf-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgFor],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  navPrimary = [
    { path: '/app/dashboard', label: 'Dashboard' },
    { path: '/app/inbox', label: 'Live Chat' },
    { path: '/app/customers', label: 'Customers' },
  ];

  navGrow = [
    { path: '/app/campaigns', label: 'Campaigns' },
    { path: '/app/templates', label: 'Templates' },
    { path: '/app/automations', label: 'Automations' },
    { path: '/app/forms', label: 'Forms' },
    { path: '/app/ai', label: 'AI Studio' },
  ];

  navManage = [
    { path: '/app/media', label: 'Media' },
    { path: '/app/reports', label: 'Reports' },
    { path: '/app/analytics', label: 'Analytics' },
    { path: '/app/billing', label: 'Billing' },
    { path: '/app/settings', label: 'Settings' },
  ];

  constructor(public auth: AuthService, public theme: ThemeService) {}
}
