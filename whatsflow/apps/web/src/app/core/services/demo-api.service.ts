import { Injectable, signal } from '@angular/core';
import { ApiService } from './api.service';
import { tap, catchError, of, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DemoApiService {
  readonly catalog = signal<any | null>(null);
  readonly loading = signal(false);

  constructor(private api: ApiService) {}

  loadCatalog() {
    this.loading.set(true);
    return this.api.get<any>('/v1/demo/catalog').pipe(
      tap((res) => {
        this.catalog.set(res.data);
        this.loading.set(false);
      }),
      catchError(() => {
        this.catalog.set(null);
        this.loading.set(false);
        return of(null);
      }),
    );
  }

  dashboard() {
    return this.api.get<any>('/v1/dashboard/summary').pipe(map((r) => r.data));
  }

  customers(page = 0, size = 50, q = '') {
    const params: Record<string, string | number> = { page, size };
    if (q) params['q'] = q;
    return this.api.get<any>('/v1/customers', params).pipe(map((r) => r.data));
  }

  campaigns() {
    return this.api.get<any>('/v1/campaigns', { page: 0, size: 50 }).pipe(map((r) => r.data));
  }

  conversations() {
    return this.api.get<any>('/v1/conversations', { page: 0, size: 150 }).pipe(map((r) => r.data));
  }

  messages(id: string) {
    return this.api.get<any>(`/v1/conversations/${id}/messages`, { page: 0, size: 100 }).pipe(map((r) => r.data));
  }

  forms() {
    return this.api.get<any>('/v1/forms').pipe(map((r) => r.data));
  }

  whatsapp() {
    return this.api.get<any>('/v1/whatsapp/account').pipe(map((r) => r.data));
  }

  module(name: string) {
    return this.api.get<any>(`/v1/demo/catalog/${name}`).pipe(map((r) => r.data));
  }

  runScenario(id: string) {
    return this.api.post<any>(`/v1/demo/scenarios/${id}/run`).pipe(map((r) => r.data));
  }

  aiSuggest(text: string) {
    return this.api.post<any>('/v1/ai/suggest-reply', { text }).pipe(map((r) => r.data));
  }
}
