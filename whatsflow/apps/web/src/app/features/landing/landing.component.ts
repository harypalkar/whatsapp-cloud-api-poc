import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'wf-landing',
  standalone: true,
  imports: [RouterLink, NgFor, NgIf, FormsModule],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  lead = {
    name: '',
    phone: '',
    email: '',
    requirement: 'WhatsApp campaigns',
  };
  leadSent = false;

  reasons = [
    { title: 'Campaign control', desc: 'Schedule, segment, and track WhatsApp outreach from one workspace.' },
    { title: 'Guided Meta setup', desc: 'Embedded Signup walks your team through WABA and number linking.' },
    { title: 'Shared inbox', desc: 'Agents reply together with clear ownership and conversation history.' },
    { title: 'AI-assisted replies', desc: 'Suggestions grounded in your knowledge base, reviewed by humans.' },
    { title: 'India-ready billing', desc: 'Plans, GST fields, and company onboarding built for local ops.' },
    { title: 'Tenant security', desc: 'Each company stays isolated with encrypted WhatsApp credentials.' },
  ];

  services = [
    { title: 'WhatsApp campaigns', desc: 'Run template-based promotions and utility updates at scale.' },
    { title: 'Cloud API connect', desc: 'Link phone number ID, WABA, and verified display name securely.' },
    { title: 'Team inbox', desc: 'Handle customer chats with assignment and faster follow-ups.' },
    { title: 'AI Studio', desc: 'Draft replies and search your docs before sending.' },
    { title: 'Forms & automations', desc: 'Capture leads and trigger journeys from events you define.' },
    { title: 'Insights', desc: 'See sends, delivery trends, and conversation load over time.' },
  ];

  features = [
    'Template library',
    'Media library',
    'Multi-agent inbox',
    'Campaign scheduler',
    'Webhook intake',
    'Role-based access',
    'Onboarding wizard',
    'Plan upgrades',
    'AI suggestions',
    'Customer CRM list',
    'Export-ready reports',
    'Light / dark console',
  ];

  industries = [
    'Retail', 'Clinics', 'Education', 'Hospitality',
    'Finance ops', 'Logistics', 'SaaS teams', 'Local brands',
  ];

  outcomes = [
    {
      title: 'Faster go-live',
      quote: 'Company setup, plan selection, and WhatsApp connect happen in one guided flow.',
    },
    {
      title: 'Clearer operations',
      quote: 'Campaigns and inbox sit together so marketing and support stop working in silos.',
    },
    {
      title: 'Safer scale',
      quote: 'Tokens stay encrypted and each workspace stays separated as you add companies.',
    },
  ];

  plans: Array<{ name: string; price: string; unit: string; desc: string; featured?: boolean }> = [
    { name: 'Starter', price: '₹999', unit: '/mo', desc: '3 agents · 5k messages · core inbox' },
    { name: 'Growth', price: '₹4,999', unit: '/mo', desc: '10 agents · 50k messages · campaigns + AI', featured: true },
    { name: 'Enterprise', price: 'Custom', unit: '', desc: 'SLA · white-label · dedicated success' },
  ];

  requirements = [
    'WhatsApp campaigns',
    'Cloud API connect',
    'Team inbox',
    'AI Studio',
    'Custom / other',
  ];

  submitLead() {
    this.leadSent = true;
  }
}
