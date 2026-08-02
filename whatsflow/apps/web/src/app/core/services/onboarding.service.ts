import { Injectable } from '@angular/core';
import { ApiService } from './api.service';

export interface CompanyProfile {
  id: string;
  name: string;
  slug: string;
  status: string;
  timezone: string;
  gstin?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  pincode?: string;
  logoUrl?: string;
  planCode?: string;
  onboardingStep: number;
  onboardingCompleted: boolean;
  whatsappConnected: boolean;
}

export interface Plan {
  code: string;
  name: string;
  maxAgents: number;
  maxMessagesMonth: number;
  priceMonthly: number;
  currency: string;
  description: string;
}

@Injectable({ providedIn: 'root' })
export class OnboardingService {
  constructor(private api: ApiService) {}

  status() {
    return this.api.get<any>('/v1/onboarding/status');
  }

  getCompany() {
    return this.api.get<CompanyProfile>('/v1/company');
  }

  saveCompany(body: Partial<CompanyProfile> & { name: string }) {
    return this.api.put<CompanyProfile>('/v1/company', body);
  }

  listPlans() {
    return this.api.get<Plan[]>('/v1/billing/plans');
  }

  selectPlan(planCode: string) {
    return this.api.post<CompanyProfile>('/v1/onboarding/plan', { planCode });
  }

  startMeta() {
    return this.api.post<any>('/v1/meta/embedded-signup/start');
  }

  completeMeta(body: Record<string, string>) {
    return this.api.post<any>('/v1/onboarding/meta/complete', body);
  }

  connectWhatsApp(body: Record<string, string> = {}) {
    return this.api.post<any>('/v1/onboarding/whatsapp/connect', body);
  }

  finish() {
    return this.api.post<CompanyProfile>('/v1/onboarding/finish');
  }

  skip() {
    return this.api.post<CompanyProfile>('/v1/onboarding/skip');
  }
}
