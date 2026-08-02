import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/models/auth.model';

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  constructor(private http: HttpClient) {}

  ask(message: string): Observable<string> {
    return this.http
      .post<ApiResponse<{ reply: string }>>(`${environment.apiBaseUrl}/v1/public/chatbot`, { message })
      .pipe(
        map((res) => res.data?.reply || this.localReply(message)),
        catchError(() => of(this.localReply(message))),
      );
  }

  localReply(message: string): string {
    const lower = (message || '').toLowerCase();
    if (lower.includes('price') || lower.includes('plan') || lower.includes('cost') || lower.includes('pricing')) {
      return 'WhatsFlow plans: Starter ₹999/mo, Growth ₹4,999/mo, and custom Enterprise. Start free from Sign up and choose a plan in onboarding.';
    }
    if (lower.includes('whatsapp') || lower.includes('meta') || lower.includes('waba') || lower.includes('connect')) {
      return 'Connect WhatsApp via Meta Embedded Signup during onboarding. Phone number ID, WABA, and tokens stay secured per company.';
    }
    if (lower.includes('inbox') || lower.includes('live chat') || lower.includes('agent')) {
      return 'The shared Live Chat inbox lets agents reply together. Open it from the console after onboarding.';
    }
    if (lower.includes('campaign') || lower.includes('bulk') || lower.includes('template')) {
      return 'Create template campaigns from Campaigns after WhatsApp is connected and templates are approved in Meta.';
    }
    if (lower.includes('ai') || lower.includes('rag') || lower.includes('suggest')) {
      return 'AI Studio can suggest replies and use your knowledge base. Find it under AI Studio in the console.';
    }
    if (lower.includes('onboard') || lower.includes('start') || lower.includes('register') || lower.includes('sign up')) {
      return 'Click Start free, create your company, then finish onboarding: business details, plan, Meta signup, WhatsApp connect, success.';
    }
    if (lower.includes('hello') || lower.includes('hi') || lower.includes('hey') || !lower.trim()) {
      return 'Hi — I am the WhatsFlow assistant. Ask about plans, WhatsApp setup, campaigns, inbox, or AI Studio.';
    }
    return 'I can help with WhatsFlow plans, WhatsApp/Meta connect, campaigns, inbox, and AI Studio. What would you like to know?';
  }
}
