import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'wf-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  stats = [
    { label: "Today's messages", value: '1,284', hint: '+12% vs yesterday' },
    { label: 'Active campaigns', value: '6', hint: '2 scheduled' },
    { label: 'Customers', value: '18.4k', hint: 'Synced contacts' },
    { label: 'Open chats', value: '37', hint: 'Inbox waiting' },
  ];
}
