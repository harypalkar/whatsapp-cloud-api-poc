import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CompanyProfile, OnboardingService, Plan } from '../../core/services/onboarding.service';

@Component({
  selector: 'wf-onboarding',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './onboarding.component.html',
  styleUrl: './onboarding.component.scss',
})
export class OnboardingComponent implements OnInit {
  readonly steps = [
    { key: 1, label: 'Business details' },
    { key: 2, label: 'Choose plan' },
    { key: 3, label: 'Meta signup' },
    { key: 4, label: 'Connect WhatsApp' },
    { key: 5, label: 'Success' },
  ];

  step = signal(1);
  loading = signal(false);
  error = signal('');
  plans = signal<Plan[]>([]);
  company = signal<CompanyProfile | null>(null);
  metaSession = signal<any>(null);
  whatsappPreview = signal<any>(null);

  form = {
    name: '',
    gstin: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    pincode: '',
    timezone: 'Asia/Kolkata',
    logoUrl: '',
  };

  waForm = {
    displayPhone: '+91 95126 18333',
    verifiedName: 'Altitude Labs',
  };

  selectedPlan = signal('GROWTH');
  progress = computed(() => (this.step() / this.steps.length) * 100);

  constructor(private onboarding: OnboardingService, private router: Router) {}

  ngOnInit(): void {
    this.bootstrap();
  }

  bootstrap() {
    this.loading.set(true);
    this.error.set('');
    this.onboarding.status().subscribe({
      next: (res) => {
        const data = res.data;
        const company = data.company as CompanyProfile;
        this.company.set(company);
        this.form.name = company.name || '';
        this.form.gstin = company.gstin || '';
        this.form.addressLine1 = company.addressLine1 || '';
        this.form.addressLine2 = company.addressLine2 || '';
        this.form.city = company.city || '';
        this.form.state = company.state || '';
        this.form.pincode = company.pincode || '';
        this.form.timezone = company.timezone || 'Asia/Kolkata';
        this.form.logoUrl = company.logoUrl || '';
        if (company.planCode) this.selectedPlan.set(company.planCode);
        if (data.whatsappAccount) this.whatsappPreview.set(data.whatsappAccount);

        if (company.onboardingCompleted) {
          this.router.navigateByUrl('/app/dashboard');
          return;
        }
        const resume = Math.min(Math.max(company.onboardingStep || 0, 0) + 1, 5);
        this.step.set(resume === 0 ? 1 : resume);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Failed to load onboarding');
      },
    });

    this.onboarding.listPlans().subscribe({
      next: (res) => this.plans.set(res.data || []),
      error: () => this.plans.set([]),
    });
  }

  saveBusiness() {
    this.loading.set(true);
    this.error.set('');
    this.onboarding.saveCompany({ ...this.form, name: this.form.name }).subscribe({
      next: (res) => {
        this.company.set(res.data);
        this.step.set(2);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Could not save business details');
      },
    });
  }

  choosePlan(code: string) {
    this.selectedPlan.set(code);
  }

  savePlan() {
    this.loading.set(true);
    this.error.set('');
    this.onboarding.selectPlan(this.selectedPlan()).subscribe({
      next: (res) => {
        this.company.set(res.data);
        this.step.set(3);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Could not select plan');
      },
    });
  }

  startMeta() {
    this.loading.set(true);
    this.error.set('');
    this.onboarding.startMeta().subscribe({
      next: (res) => {
        this.metaSession.set(res.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Could not start Meta signup');
      },
    });
  }

  completeMetaSimulated() {
    this.loading.set(true);
    this.error.set('');
    this.onboarding.completeMeta({
      phoneNumberId: '1226308087231072',
      wabaId: '1583394760167591',
      displayPhone: this.waForm.displayPhone,
      verifiedName: this.waForm.verifiedName || this.form.name,
      accessToken: 'mock-local-token',
    }).subscribe({
      next: (res) => {
        this.whatsappPreview.set(res.data);
        this.company.set(res.data.company);
        this.step.set(4);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Meta linking failed');
      },
    });
  }

  connectWhatsApp() {
    this.loading.set(true);
    this.error.set('');
    this.onboarding.connectWhatsApp({
      displayPhone: this.waForm.displayPhone,
      verifiedName: this.waForm.verifiedName,
    }).subscribe({
      next: (res) => {
        this.whatsappPreview.set(res.data);
        this.company.set(res.data.company);
        this.step.set(5);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'WhatsApp connect failed');
      },
    });
  }

  finish() {
    this.loading.set(true);
    this.error.set('');
    this.onboarding.finish().subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl('/app/dashboard');
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Could not finish onboarding');
      },
    });
  }

  back() {
    if (this.step() > 1) this.step.update((s) => s - 1);
  }

  skipToDashboard() {
    this.loading.set(true);
    this.error.set('');
    this.onboarding.skip().subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl('/app/dashboard');
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message || 'Could not skip onboarding');
      },
    });
  }
}
