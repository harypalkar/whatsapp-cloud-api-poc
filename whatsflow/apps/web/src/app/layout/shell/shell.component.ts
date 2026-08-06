import { Component, HostListener, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';
import { DemoApiService } from '../../core/services/demo-api.service';

@Component({
  selector: 'wf-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgFor, NgIf],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit {
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
    { path: '/app/whatsapp', label: 'WhatsApp' },
    { path: '/app/media', label: 'Media' },
    { path: '/app/reports', label: 'Reports' },
    { path: '/app/analytics', label: 'Analytics' },
    { path: '/app/billing', label: 'Billing' },
    { path: '/app/admin', label: 'Admin' },
    { path: '/app/settings', label: 'Settings' },
    { path: '/app/profile', label: 'Profile' },
  ];

  notifications = signal<any[]>([]);
  notifOpen = signal(false);
  toast = signal<string | null>(null);

  constructor(
    public auth: AuthService,
    public theme: ThemeService,
    private demo: DemoApiService,
  ) {}

  ngOnInit() {
    this.demo.module('notifications').subscribe({
      next: (data) => {
        const list = Array.isArray(data) ? data : [];
        this.notifications.set(list);
        const unread = list.find((n: any) => !n.read);
        if (unread) {
          this.toast.set(unread.title);
          setTimeout(() => this.toast.set(null), 4200);
        }
      },
    });
  }

  unreadCount(): number {
    return this.notifications().filter((n) => !n.read).length;
  }

  toggleNotif(event: Event) {
    event.stopPropagation();
    this.notifOpen.update((v) => !v);
  }

  @HostListener('document:click')
  closeNotif() {
    this.notifOpen.set(false);
  }
}
