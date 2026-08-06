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
    'Hospitals', 'Schools', 'Real Estate', 'Retail',
    'Restaurants', 'Travel', 'Insurance', 'Finance',
  ];

  testimonials = [
    {
      quote: 'Our OPD reminders and health camps finally run from one console. Demo Mode helped the board see it live.',
      name: 'Dr Meera Krishnan',
      role: 'Medical Director, Metro Care Hospitals',
    },
    {
      quote: 'Admission campaigns, parent inbox, and forms landed in the same week we evaluated WhatsFlow.',
      name: 'Anil Deshmukh',
      role: 'Principal, Horizon Public School',
    },
    {
      quote: 'Property enquiries no longer get lost in WhatsApp personal chats. Agents assign and close properly.',
      name: 'Sneha Kapoor',
      role: 'Sales Head, Skyline Realty',
    },
  ];

  videos = [
    { title: 'Product tour', meta: '3:40 · Workspace walkthrough', poster: 'Tour' },
    { title: 'Demo Mode', meta: '2:15 · ABC Hospital seed', poster: 'Demo' },
    { title: 'Inbox & AI', meta: '4:05 · Agent workflow', poster: 'Inbox' },
  ];

  faqs = [
    { q: 'Can I try WhatsFlow without Meta credentials?', a: 'Yes. Use Demo Login (demo@whatsflow.ai / Demo@123) to explore ABC Hospital with seeded customers, campaigns, and inbox.' },
    { q: 'Does onboarding support GST and plans?', a: 'Yes. The wizard covers business details, GST, subscription, Meta Embedded Signup placeholder, and WhatsApp connect.' },
    { q: 'Is the inbox WhatsApp-style?', a: 'Yes. Conversation list, delivery/read states, media previews, notes, and agent assignment are included for demos.' },
    { q: 'Can I run demo scenarios?', a: 'Open Automations and run Hospital Appointment, School Admission, Restaurant Offer, Property Inquiry, or Insurance Renewal.' },
  ];

  plans: Array<{ name: string; price: string; unit: string; desc: string; featured?: boolean }> = [
    { name: 'Starter', price: '₹999', unit: '/mo', desc: '3 agents · 5k messages · core inbox' },
    { name: 'Growth', price: '₹4,999', unit: '/mo', desc: '10 agents · 50k messages · campaigns + AI' },
    { name: 'Professional', price: '₹14,999', unit: '/mo', desc: '50 agents · 250k messages · automations · priority support', featured: true },
    { name: 'Enterprise', price: 'Custom', unit: '', desc: 'SLA · white-label · dedicated success' },
  ];

  requirements = [
    'WhatsApp campaigns',
    'Cloud API connect',
    'Team inbox',
    'AI Studio',
    'Book a demo',
    'Custom / other',
  ];

  submitLead() {
    this.leadSent = true;
  }
}
