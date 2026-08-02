import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthResponse } from '../models/auth.model';

const ACCESS = 'wf_access';
const REFRESH = 'wf_refresh';
const TENANT = 'wf_tenant';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userEmail = signal<string | null>(localStorage.getItem('wf_email'));
  readonly isAuthenticated = computed(() => !!this.accessToken());
  private readonly accessTokenSig = signal<string | null>(localStorage.getItem(ACCESS));

  constructor(private http: HttpClient, private router: Router) {}

  accessToken() { return this.accessTokenSig(); }
  tenantId() { return localStorage.getItem(TENANT); }
  email() { return this.userEmail(); }

  login(email: string, password: string) {
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiBaseUrl}/v1/auth/login`, { email, password })
      .pipe(tap(res => this.persist(res.data)));
  }

  register(companyName: string, email: string, password: string, fullName: string) {
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiBaseUrl}/v1/auth/register`, {
      companyName, email, password, fullName
    }).pipe(tap(res => this.persist(res.data)));
  }

  refresh() {
    const refreshToken = localStorage.getItem(REFRESH);
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiBaseUrl}/v1/auth/refresh`, { refreshToken })
      .pipe(tap(res => this.persist(res.data)));
  }

  logout() {
    localStorage.removeItem(ACCESS);
    localStorage.removeItem(REFRESH);
    localStorage.removeItem(TENANT);
    localStorage.removeItem('wf_email');
    this.accessTokenSig.set(null);
    this.userEmail.set(null);
    this.router.navigateByUrl('/auth/login');
  }

  private persist(data: AuthResponse) {
    localStorage.setItem(ACCESS, data.accessToken);
    localStorage.setItem(REFRESH, data.refreshToken);
    localStorage.setItem(TENANT, data.tenantId);
    localStorage.setItem('wf_email', data.email);
    this.accessTokenSig.set(data.accessToken);
    this.userEmail.set(data.email);
  }
}
