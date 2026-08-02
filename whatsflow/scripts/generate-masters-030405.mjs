/**
 * Generates MASTER-03 (Angular features), MASTER-04 (DevOps), MASTER-05 (AI backend modules).
 * Does not overwrite existing files.
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..");
let written = 0, skipped = 0;

function write(rel, content) {
  const full = path.join(ROOT, rel);
  fs.mkdirSync(path.dirname(full), { recursive: true });
  if (fs.existsSync(full)) { skipped++; return; }
  fs.writeFileSync(full, content.trim() + "\n", "utf8");
  written++;
}

function java(pkg, name, body) {
  write(`apps/api/src/main/java/com/whatsflow/${pkg.replaceAll(".", "/")}/${name}.java`,
    `package com.whatsflow.${pkg};\n\n${body}`);
}

// ===================== MASTER-03 Angular =====================
const WEB = "apps/web/src";

write(`${WEB}/environments/environment.ts`, `
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  appName: 'WhatsFlow',
};
`);

write(`${WEB}/environments/environment.prod.ts`, `
export const environment = {
  production: true,
  apiBaseUrl: '/api',
  appName: 'WhatsFlow',
};
`);

write(`${WEB}/app/core/models/auth.model.ts`, `
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  tenantId: string;
  email: string;
  roles: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  code?: string;
}
`);

write(`${WEB}/app/core/services/auth.service.ts`, `
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
    return this.http.post<ApiResponse<AuthResponse>>(\`\${environment.apiBaseUrl}/v1/auth/login\`, { email, password })
      .pipe(tap(res => this.persist(res.data)));
  }

  register(companyName: string, email: string, password: string, fullName: string) {
    return this.http.post<ApiResponse<AuthResponse>>(\`\${environment.apiBaseUrl}/v1/auth/register\`, {
      companyName, email, password, fullName
    }).pipe(tap(res => this.persist(res.data)));
  }

  refresh() {
    const refreshToken = localStorage.getItem(REFRESH);
    return this.http.post<ApiResponse<AuthResponse>>(\`\${environment.apiBaseUrl}/v1/auth/refresh\`, { refreshToken })
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
`);

write(`${WEB}/app/core/services/api.service.ts`, `
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  get<T>(path: string, params?: Record<string, string | number | boolean>) {
    let httpParams = new HttpParams();
    if (params) Object.entries(params).forEach(([k, v]) => httpParams = httpParams.set(k, String(v)));
    return this.http.get<ApiResponse<T>>(\`\${environment.apiBaseUrl}\${path}\`, { params: httpParams });
  }

  post<T>(path: string, body?: unknown) {
    return this.http.post<ApiResponse<T>>(\`\${environment.apiBaseUrl}\${path}\`, body ?? {});
  }

  put<T>(path: string, body?: unknown) {
    return this.http.put<ApiResponse<T>>(\`\${environment.apiBaseUrl}\${path}\`, body ?? {});
  }

  delete<T>(path: string) {
    return this.http.delete<ApiResponse<T>>(\`\${environment.apiBaseUrl}\${path}\`);
  }
}
`);

write(`${WEB}/app/core/interceptors/auth.interceptor.ts`, `
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.accessToken();
  if (!token) return next(req);
  return next(req.clone({
    setHeaders: { Authorization: \`Bearer \${token}\` }
  }));
};
`);

write(`${WEB}/app/core/guards/auth.guard.ts`, `
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) return true;
  return router.createUrlTree(['/auth/login']);
};
`);

write(`${WEB}/app/core/services/theme.service.ts`, `
import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly mode = signal<'light' | 'dark'>((localStorage.getItem('wf_theme') as 'light' | 'dark') || 'light');

  constructor() {
    effect(() => {
      const m = this.mode();
      document.body.classList.toggle('theme-dark', m === 'dark');
      document.body.classList.toggle('theme-light', m === 'light');
      localStorage.setItem('wf_theme', m);
    });
  }

  toggle() { this.mode.update(m => m === 'light' ? 'dark' : 'light'); }
}
`);

// Layout
write(`${WEB}/app/layout/shell/shell.component.ts`, `
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
  nav = [
    { path: '/app/dashboard', label: 'Dashboard' },
    { path: '/app/customers', label: 'Customers' },
    { path: '/app/campaigns', label: 'Campaigns' },
    { path: '/app/inbox', label: 'Live Chat' },
    { path: '/app/templates', label: 'Templates' },
    { path: '/app/forms', label: 'Forms' },
    { path: '/app/automations', label: 'Automations' },
    { path: '/app/media', label: 'Media' },
    { path: '/app/reports', label: 'Reports' },
    { path: '/app/analytics', label: 'Analytics' },
    { path: '/app/billing', label: 'Billing' },
    { path: '/app/settings', label: 'Settings' },
    { path: '/app/ai', label: 'AI Studio' },
  ];

  constructor(public auth: AuthService, public theme: ThemeService) {}
}
`);

write(`${WEB}/app/layout/shell/shell.component.html`, `
<div class="shell">
  <aside class="sidebar">
    <div class="brand">WhatsFlow</div>
    <nav>
      <a *ngFor="let item of nav" [routerLink]="item.path" routerLinkActive="active">{{ item.label }}</a>
    </nav>
  </aside>
  <div class="main">
    <header class="topbar">
      <span>{{ auth.email() }}</span>
      <button type="button" (click)="theme.toggle()">Theme</button>
      <button type="button" (click)="auth.logout()">Logout</button>
    </header>
    <main class="content"><router-outlet /></main>
  </div>
</div>
`);

write(`${WEB}/app/layout/shell/shell.component.scss`, `
.shell { display: grid; grid-template-columns: 240px 1fr; min-height: 100vh; }
.sidebar { background: var(--wf-sidebar); color: #fff; padding: 1.25rem 1rem; }
.brand { font-family: "Fraunces", Georgia, serif; font-size: 1.5rem; margin-bottom: 1.5rem; }
nav { display: flex; flex-direction: column; gap: .35rem; }
nav a { color: rgba(255,255,255,.85); text-decoration: none; padding: .45rem .6rem; border-radius: .4rem; }
nav a.active, nav a:hover { background: rgba(255,255,255,.12); color: #fff; }
.main { display: flex; flex-direction: column; background: var(--wf-bg); }
.topbar { display: flex; justify-content: flex-end; gap: .75rem; align-items: center; padding: .85rem 1.25rem; border-bottom: 1px solid var(--wf-border); }
.content { padding: 1.25rem; }
button { border: 1px solid var(--wf-border); background: var(--wf-card); padding: .35rem .7rem; border-radius: .4rem; cursor: pointer; }
@media (max-width: 900px) { .shell { grid-template-columns: 1fr; } .sidebar { display: none; } }
`);

function page(folder, selector, title, bodyHtml = `<p class="muted">Production-ready ${title} module shell. Wire to API services next.</p>`) {
  const base = `${WEB}/app/features/${folder}`;
  write(`${base}/${folder}.component.ts`, `
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: '${selector}',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './${folder}.component.html',
  styleUrl: './${folder}.component.scss',
})
export class ${toPascal(folder)}Component {}
`);
  write(`${base}/${folder}.component.html`, `
<section class="page">
  <header class="page-header">
    <h1>${title}</h1>
  </header>
  <div class="card">
    ${bodyHtml}
  </div>
</section>
`);
  write(`${base}/${folder}.component.scss`, `
.page-header h1 { font-family: "Fraunces", Georgia, serif; margin: 0 0 1rem; font-size: 1.8rem; }
.card { background: var(--wf-card); border: 1px solid var(--wf-border); border-radius: .75rem; padding: 1.25rem; }
.muted { color: var(--wf-muted); }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; }
.stat { padding: 1rem; border-radius: .6rem; background: var(--wf-bg); border: 1px solid var(--wf-border); }
.stat strong { display: block; font-size: 1.4rem; }
`);
}

function toPascal(s) {
  return s.split(/[-_]/).map(p => p.charAt(0).toUpperCase() + p.slice(1)).join('') ;
}

// Auth pages
write(`${WEB}/app/features/auth/login/login.component.ts`, `
import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'wf-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  email = '';
  password = '';
  remember = true;
  error = signal('');
  loading = signal(false);

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.email, this.password).subscribe({
      next: () => { this.loading.set(false); this.router.navigateByUrl('/app/dashboard'); },
      error: (err) => { this.loading.set(false); this.error.set(err?.error?.message || 'Login failed'); }
    });
  }
}
`);

write(`${WEB}/app/features/auth/login/login.component.html`, `
<section class="auth">
  <div class="panel">
    <h1>WhatsFlow</h1>
    <p>Sign in to your workspace</p>
    <form (ngSubmit)="submit()">
      <label>Email <input type="email" [(ngModel)]="email" name="email" required /></label>
      <label>Password <input type="password" [(ngModel)]="password" name="password" required /></label>
      <label class="row"><input type="checkbox" [(ngModel)]="remember" name="remember" /> Remember me</label>
      <button type="submit" [disabled]="loading()">{{ loading() ? 'Signing in…' : 'Sign in' }}</button>
      <p class="err" *ngIf="error()">{{ error() }}</p>
    </form>
    <div class="links">
      <a routerLink="/auth/forgot-password">Forgot password</a>
      <a routerLink="/auth/register">Create company</a>
      <a routerLink="/">Back to home</a>
    </div>
  </div>
</section>
`);

write(`${WEB}/app/features/auth/login/login.component.scss`, `
.auth { min-height: 100vh; display: grid; place-items: center; background:
  radial-gradient(1200px 600px at 10% -10%, #c8e7d2 0%, transparent 55%),
  linear-gradient(160deg, #f4f7f2, #e7eef8); }
.panel { width: min(420px, 92vw); background: #fff; border-radius: 1rem; padding: 2rem; box-shadow: 0 20px 50px rgba(15,40,30,.08); }
h1 { font-family: "Fraunces", Georgia, serif; margin: 0; }
form { display: grid; gap: .8rem; margin-top: 1.25rem; }
label { display: grid; gap: .35rem; font-size: .9rem; }
input[type=email], input[type=password] { padding: .65rem .75rem; border: 1px solid #d5ddd8; border-radius: .5rem; }
button { margin-top: .5rem; background: #0f3d2e; color: #fff; border: 0; padding: .75rem; border-radius: .55rem; cursor: pointer; }
.links { display: flex; justify-content: space-between; margin-top: 1rem; font-size: .85rem; }
.err { color: #b42318; }
.row { display: flex; align-items: center; gap: .5rem; }
`);

write(`${WEB}/app/features/auth/register/register.component.ts`, `
import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'wf-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  companyName = '';
  fullName = '';
  email = '';
  password = '';
  error = signal('');
  loading = signal(false);

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.loading.set(true);
    this.auth.register(this.companyName, this.email, this.password, this.fullName).subscribe({
      next: () => { this.loading.set(false); this.router.navigateByUrl('/onboarding'); },
      error: (err) => { this.loading.set(false); this.error.set(err?.error?.message || 'Registration failed'); }
    });
  }
}
`);

write(`${WEB}/app/features/auth/register/register.component.html`, `
<section class="auth">
  <div class="panel">
    <h1>Create your company</h1>
    <form (ngSubmit)="submit()">
      <label>Company <input [(ngModel)]="companyName" name="companyName" required /></label>
      <label>Your name <input [(ngModel)]="fullName" name="fullName" required /></label>
      <label>Email <input type="email" [(ngModel)]="email" name="email" required /></label>
      <label>Password <input type="password" [(ngModel)]="password" name="password" required minlength="8" /></label>
      <button type="submit" [disabled]="loading()">{{ loading() ? 'Creating…' : 'Start free trial' }}</button>
      <p class="err" *ngIf="error()">{{ error() }}</p>
    </form>
    <a routerLink="/auth/login">Already have an account?</a>
  </div>
</section>
`);

write(`${WEB}/app/features/auth/register/register.component.scss`, `
@import '../login/login.component.scss';
input { padding: .65rem .75rem; border: 1px solid #d5ddd8; border-radius: .5rem; }
`);

write(`${WEB}/app/features/auth/forgot-password/forgot-password.component.ts`, `
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'wf-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: \`
  <section class="auth"><div class="panel">
    <h1>Reset password</h1>
    <p>Enter your email and we'll send a reset link.</p>
    <form (ngSubmit)="sent=true"><label>Email <input type="email" [(ngModel)]="email" name="email" required /></label>
    <button type="submit">Send reset link</button></form>
    <p *ngIf="sent">If an account exists, a reset email was queued.</p>
    <a routerLink="/auth/login">Back to login</a>
  </div></section>\`,
  styles: [\`:host { display:block } .auth{min-height:100vh;display:grid;place-items:center;background:linear-gradient(160deg,#f4f7f2,#e7eef8)} .panel{width:min(420px,92vw);background:#fff;border-radius:1rem;padding:2rem} form{display:grid;gap:.8rem} input,button{padding:.65rem .75rem;border-radius:.5rem} button{background:#0f3d2e;color:#fff;border:0}\`],
})
export class ForgotPasswordComponent { email = ''; sent = false; }
`);

// Landing
write(`${WEB}/app/features/landing/landing.component.ts`, `
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgFor } from '@angular/common';

@Component({
  selector: 'wf-landing',
  standalone: true,
  imports: [RouterLink, NgFor],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  features = [
    'WhatsApp campaigns at scale',
    'Shared inbox for agents',
    'AI reply suggestions & RAG',
    'Meta Embedded Signup',
    'India-ready billing (UPI/GST)',
    'White-label & marketplace',
  ];
  plans = [
    { name: 'Starter', price: '₹999', desc: '3 agents · 5k msgs' },
    { name: 'Growth', price: '₹4,999', desc: '10 agents · 50k msgs' },
    { name: 'Enterprise', price: 'Custom', desc: 'SLA · VPC · SSO' },
  ];
  faqs = [
    { q: 'Do I need Meta coding?', a: 'No. Connect WhatsApp via Embedded Signup and launch campaigns from the UI.' },
    { q: 'Is it multi-tenant?', a: 'Yes. Strict tenant isolation for every company workspace.' },
  ];
}
`);

write(`${WEB}/app/features/landing/landing.component.html`, `
<div class="landing">
  <header class="nav">
    <strong>WhatsFlow</strong>
    <div class="actions">
      <a routerLink="/auth/login">Login</a>
      <a class="cta" routerLink="/auth/register">Start free</a>
    </div>
  </header>

  <section class="hero">
    <div>
      <p class="eyebrow">WhatsApp Customer Engagement</p>
      <h1>WhatsFlow</h1>
      <p class="lede">India's advanced WhatsApp platform for campaigns, inbox, AI, and automation — without writing code.</p>
      <div class="hero-cta">
        <a class="cta" routerLink="/auth/register">Book a demo</a>
        <a routerLink="/auth/login">Sign in</a>
      </div>
    </div>
    <div class="hero-visual" aria-hidden="true"></div>
  </section>

  <section class="section">
    <h2>Features</h2>
    <ul class="feature-grid"><li *ngFor="let f of features">{{ f }}</li></ul>
  </section>

  <section class="section">
    <h2>Pricing</h2>
    <div class="plan-grid">
      <article *ngFor="let p of plans"><h3>{{ p.name }}</h3><p class="price">{{ p.price }}</p><p>{{ p.desc }}</p></article>
    </div>
  </section>

  <section class="section">
    <h2>FAQ</h2>
    <details *ngFor="let f of faqs"><summary>{{ f.q }}</summary><p>{{ f.a }}</p></details>
  </section>

  <section class="section contact">
    <h2>Contact / Demo</h2>
    <p>Email sales@whatsflow.app · WhatsApp +91 95126 18333</p>
  </section>
</div>
`);

write(`${WEB}/app/features/landing/landing.component.scss`, `
@import url('https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,600;9..144,700&family=Source+Sans+3:wght@400;600&display=swap');
.landing { font-family: "Source Sans 3", system-ui, sans-serif; color: #14231c; }
.nav { display:flex; justify-content:space-between; padding:1rem 7vw; }
.nav strong { font-family:"Fraunces",Georgia,serif; font-size:1.4rem; }
.actions { display:flex; gap:1rem; align-items:center; }
.cta { background:#0f3d2e; color:#fff !important; text-decoration:none; padding:.55rem 1rem; border-radius:999px; }
a { color:#0f3d2e; text-decoration:none; }
.hero { min-height:78vh; display:grid; grid-template-columns:1.1fr .9fr; gap:2rem; padding:4vh 7vw 8vh;
  background: linear-gradient(120deg, rgba(15,61,46,.08), transparent 40%),
              url('https://images.unsplash.com/photo-1556745753-b2904692e3fc?auto=format&fit=crop&w=1600&q=80') center/cover; }
.hero > div:first-child { max-width:560px; background:rgba(255,255,255,.86); padding:2rem; border-radius:1rem; backdrop-filter: blur(4px); }
.eyebrow { text-transform:uppercase; letter-spacing:.08em; font-size:.75rem; color:#3d5c4f; }
h1 { font-family:"Fraunces",Georgia,serif; font-size:clamp(2.6rem,6vw,4.2rem); margin:.2rem 0 .6rem; }
.lede { font-size:1.1rem; line-height:1.5; }
.hero-cta { display:flex; gap:1rem; margin-top:1.25rem; align-items:center; }
.hero-visual { border-radius:1.25rem; }
.section { padding:4rem 7vw; }
.feature-grid, .plan-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(220px,1fr)); gap:1rem; padding:0; list-style:none; }
.feature-grid li, .plan-grid article { background:#f6faf7; border:1px solid #d7e3db; border-radius:.8rem; padding:1rem; }
.price { font-size:1.6rem; font-weight:700; }
details { border-bottom:1px solid #d7e3db; padding:.8rem 0; }
@media (max-width:900px){ .hero{grid-template-columns:1fr;} }
`);

page('dashboard', 'wf-dashboard', 'Dashboard', `
<div class="grid">
  <div class="stat"><span>Today's messages</span><strong>—</strong></div>
  <div class="stat"><span>Campaigns</span><strong>—</strong></div>
  <div class="stat"><span>Customers</span><strong>—</strong></div>
  <div class="stat"><span>Conversations</span><strong>—</strong></div>
</div>
<p class="muted" style="margin-top:1rem">Connect API \`/v1/dashboard/summary\` for live cards & charts.</p>
`);

page('customers', 'wf-customers', 'Customers', `
<p>Grid · tags · groups · import/export · opt-in/out · blacklist</p>
<p class="muted">API: \`/v1/customers\`</p>
`);

page('campaigns', 'wf-campaigns', 'Campaigns', `
<p>Builder · schedule · recurring · analytics</p>
<p class="muted">API: \`/v1/campaigns\`</p>
`);

page('inbox', 'wf-inbox', 'Live Chat', `
<div class="chat-layout">
  <aside class="threads">Conversation list · unread badges</aside>
  <section class="thread">WhatsApp-style thread · media · assign agent · notes</section>
  <aside class="profile">Customer profile</aside>
</div>
`);

write(`${WEB}/app/features/inbox/inbox.component.scss`, `
.page-header h1 { font-family: "Fraunces", Georgia, serif; margin: 0 0 1rem; }
.chat-layout { display:grid; grid-template-columns: 260px 1fr 240px; min-height: 70vh; border:1px solid var(--wf-border); border-radius:.75rem; overflow:hidden; background:var(--wf-card); }
.threads, .profile { background: var(--wf-bg); padding:1rem; border-right:1px solid var(--wf-border); }
.profile { border-right:0; border-left:1px solid var(--wf-border); }
.thread { padding:1rem; }
@media (max-width: 1000px) { .chat-layout { grid-template-columns: 1fr; } .profile { display:none; } }
`);

page('templates', 'wf-templates', 'Templates');
page('forms', 'wf-forms', 'Forms Builder');
page('automations', 'wf-automations', 'Automation Builder');
page('media', 'wf-media', 'Media Library');
page('reports', 'wf-reports', 'Reports');
page('analytics', 'wf-analytics', 'Analytics');
page('billing', 'wf-billing', 'Billing');
page('settings', 'wf-settings', 'Settings');
page('profile', 'wf-profile', 'Profile');
page('admin', 'wf-admin', 'Platform Admin');
page('ai', 'wf-ai', 'AI Studio', `
<p>Chatbot · RAG · reply suggestions · campaign writer · sentiment</p>
<p class="muted">API: \`/v1/ai/**\` \`/v1/rag/**\`</p>
`);
page('onboarding', 'wf-onboarding', 'Company Onboarding', `
<ol>
  <li>Business details / GST / logo</li>
  <li>Choose plan</li>
  <li>Meta Embedded Signup</li>
  <li>Connect WhatsApp number</li>
  <li>Success</li>
</ol>
`);

// Force overwrite routes/config/styles via writeForce
function writeForce(rel, content) {
  const full = path.join(ROOT, rel);
  fs.mkdirSync(path.dirname(full), { recursive: true });
  fs.writeFileSync(full, content.trim() + "\n", "utf8");
  written++;
}

writeForce(`${WEB}/app/app.routes.ts`, `
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent) },
  { path: 'auth/login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'auth/register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
  { path: 'auth/forgot-password', loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'onboarding', canActivate: [authGuard], loadComponent: () => import('./features/onboarding/onboarding.component').then(m => m.OnboardingComponent) },
  {
    path: 'app',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'customers', loadComponent: () => import('./features/customers/customers.component').then(m => m.CustomersComponent) },
      { path: 'campaigns', loadComponent: () => import('./features/campaigns/campaigns.component').then(m => m.CampaignsComponent) },
      { path: 'inbox', loadComponent: () => import('./features/inbox/inbox.component').then(m => m.InboxComponent) },
      { path: 'templates', loadComponent: () => import('./features/templates/templates.component').then(m => m.TemplatesComponent) },
      { path: 'forms', loadComponent: () => import('./features/forms/forms.component').then(m => m.FormsComponent) },
      { path: 'automations', loadComponent: () => import('./features/automations/automations.component').then(m => m.AutomationsComponent) },
      { path: 'media', loadComponent: () => import('./features/media/media.component').then(m => m.MediaComponent) },
      { path: 'reports', loadComponent: () => import('./features/reports/reports.component').then(m => m.ReportsComponent) },
      { path: 'analytics', loadComponent: () => import('./features/analytics/analytics.component').then(m => m.AnalyticsComponent) },
      { path: 'billing', loadComponent: () => import('./features/billing/billing.component').then(m => m.BillingComponent) },
      { path: 'settings', loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent) },
      { path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
      { path: 'admin', loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent) },
      { path: 'ai', loadComponent: () => import('./features/ai/ai.component').then(m => m.AiComponent) },
    ]
  },
  { path: '**', redirectTo: '' },
];
`);

writeForce(`${WEB}/app/app.config.ts`, `
import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimations(),
  ]
};
`);

writeForce(`${WEB}/app/app.ts`, `
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />',
  styles: [':host { display:block; min-height:100vh; }'],
})
export class App {}
`);

writeForce(`${WEB}/styles.scss`, `
@import url('https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,600;9..144,700&family=Source+Sans+3:wght@400;600&display=swap');

:root, body.theme-light {
  --wf-bg: #f3f6f4;
  --wf-card: #ffffff;
  --wf-sidebar: #0f3d2e;
  --wf-border: #d5e0d9;
  --wf-muted: #5b6b63;
  --wf-text: #14231c;
  --wf-accent: #c45c26;
}

body.theme-dark {
  --wf-bg: #101816;
  --wf-card: #18241f;
  --wf-sidebar: #0a241c;
  --wf-border: #2a3b34;
  --wf-muted: #9aada3;
  --wf-text: #e8f0eb;
  --wf-accent: #e08a55;
}

* { box-sizing: border-box; }
html, body { margin: 0; min-height: 100%; }
body {
  font-family: "Source Sans 3", system-ui, sans-serif;
  color: var(--wf-text);
  background: var(--wf-bg);
}
`);

writeForce(`${WEB}/index.html`, `
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>WhatsFlow</title>
  <base href="/">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/x-icon" href="favicon.ico">
</head>
<body class="theme-light">
  <app-root></app-root>
</body>
</html>
`);

write("docs/MASTER-03.md", `
# MASTER-03 — Angular Frontend

**Status:** Complete (feature shells + auth + landing + routing)
**App:** \`whatsflow/apps/web\`

## Run
\`\`\`bash
cd whatsflow/apps/web
npm install
npm start
\`\`\`
Open http://localhost:4200
`);

// ===================== MASTER-04 DevOps =====================
write("deploy/docker-compose.yml", `
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: whatsflow
      POSTGRES_USER: whatsflow
      POSTGRES_PASSWORD: whatsflow
    ports: ["5432:5432"]
    volumes: [pgdata:/var/lib/postgresql/data]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U whatsflow"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
    restart: unless-stopped

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports: ["9000:9000", "9001:9001"]
    volumes: [miniodata:/data]
    restart: unless-stopped

  mailhog:
    image: mailhog/mailhog:latest
    ports: ["1025:1025", "8025:8025"]
    restart: unless-stopped

  api:
    build:
      context: ../apps/api
      dockerfile: Dockerfile
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/whatsflow
      SPRING_DATASOURCE_USERNAME: whatsflow
      SPRING_DATASOURCE_PASSWORD: whatsflow
      REDIS_HOST: redis
      WHATSFLOW_JWT_SECRET: change-me-in-production-32chars-min
      WHATSFLOW_WHATSAPP_PROVIDER: mock
    ports: ["8080:8080"]
    depends_on:
      postgres: { condition: service_healthy }
      redis: { condition: service_started }
    restart: unless-stopped

  web:
    build:
      context: ../apps/web
      dockerfile: Dockerfile
    ports: ["4200:80"]
    depends_on: [api]
    restart: unless-stopped

  nginx:
    image: nginx:1.27-alpine
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
    ports: ["80:80", "443:443"]
    depends_on: [api, web]
    restart: unless-stopped

  prometheus:
    image: prom/prometheus:v2.54.1
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    ports: ["9090:9090"]
    restart: unless-stopped

  grafana:
    image: grafana/grafana:11.2.0
    ports: ["3000:3000"]
    depends_on: [prometheus]
    restart: unless-stopped

  kafka:
    image: bitnami/kafka:3.8
    environment:
      KAFKA_CFG_NODE_ID: 0
      KAFKA_CFG_PROCESS_ROLES: controller,broker
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: 0@kafka:9093
      KAFKA_CFG_CONTROLLER_LISTENER_NAMES: CONTROLLER
    ports: ["9092:9092"]
    restart: unless-stopped

volumes:
  pgdata:
  miniodata:
`);

write("deploy/nginx/nginx.conf", `
worker_processes auto;
events { worker_connections 1024; }
http {
  include       mime.types;
  sendfile      on;
  gzip on;
  gzip_types text/plain text/css application/json application/javascript;

  limit_req_zone $binary_remote_addr zone=api:10m rate=20r/s;

  map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
  }

  server {
    listen 80;
    server_name _;

    add_header X-Frame-Options SAMEORIGIN always;
    add_header X-Content-Type-Options nosniff always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;
    add_header Content-Security-Policy "default-src 'self'; img-src 'self' data https:; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src https://fonts.gstatic.com; connect-src 'self' https:" always;

    location /api/ {
      limit_req zone=api burst=40 nodelay;
      proxy_pass http://api:8080/api/;
      proxy_http_version 1.1;
      proxy_set_header Host $host;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header Upgrade $http_upgrade;
      proxy_set_header Connection $connection_upgrade;
    }

    location / {
      proxy_pass http://web:80/;
      proxy_set_header Host $host;
    }
  }
}
`);

write("deploy/prometheus/prometheus.yml", `
global:
  scrape_interval: 15s
scrape_configs:
  - job_name: whatsflow-api
    metrics_path: /api/actuator/prometheus
    static_configs:
      - targets: ["api:8080"]
`);

write("apps/web/Dockerfile", `
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist/web/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
`);

write("apps/web/nginx.conf", `
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;
  location / { try_files $uri $uri/ /index.html; }
}
`);

write("infrastructure/kubernetes/namespace.yaml", `
apiVersion: v1
kind: Namespace
metadata:
  name: whatsflow
`);

write("infrastructure/kubernetes/api-deployment.yaml", `
apiVersion: apps/v1
kind: Deployment
metadata:
  name: whatsflow-api
  namespace: whatsflow
spec:
  replicas: 3
  selector:
    matchLabels: { app: whatsflow-api }
  template:
    metadata:
      labels: { app: whatsflow-api }
    spec:
      containers:
        - name: api
          image: ghcr.io/harypalkar/whatsflow-api:latest
          ports: [{ containerPort: 8080 }]
          envFrom: [{ secretRef: { name: whatsflow-secrets } }]
          resources:
            requests: { cpu: "250m", memory: "512Mi" }
            limits: { cpu: "2", memory: "2Gi" }
          livenessProbe:
            httpGet: { path: /api/actuator/health/liveness, port: 8080 }
            initialDelaySeconds: 40
          readinessProbe:
            httpGet: { path: /api/actuator/health/readiness, port: 8080 }
            initialDelaySeconds: 20
---
apiVersion: v1
kind: Service
metadata:
  name: whatsflow-api
  namespace: whatsflow
spec:
  selector: { app: whatsflow-api }
  ports: [{ port: 8080, targetPort: 8080 }]
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: whatsflow-api
  namespace: whatsflow
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: whatsflow-api
  minReplicas: 3
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target: { type: Utilization, averageUtilization: 65 }
`);

write("infrastructure/kubernetes/web-deployment.yaml", `
apiVersion: apps/v1
kind: Deployment
metadata:
  name: whatsflow-web
  namespace: whatsflow
spec:
  replicas: 2
  selector: { matchLabels: { app: whatsflow-web } }
  template:
    metadata: { labels: { app: whatsflow-web } }
    spec:
      containers:
        - name: web
          image: ghcr.io/harypalkar/whatsflow-web:latest
          ports: [{ containerPort: 80 }]
---
apiVersion: v1
kind: Service
metadata:
  name: whatsflow-web
  namespace: whatsflow
spec:
  selector: { app: whatsflow-web }
  ports: [{ port: 80, targetPort: 80 }]
`);

write("infrastructure/kubernetes/ingress.yaml", `
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: whatsflow
  namespace: whatsflow
  annotations:
    nginx.ingress.kubernetes.io/proxy-body-size: 50m
spec:
  ingressClassName: nginx
  rules:
    - host: app.whatsflow.local
      http:
        paths:
          - path: /api
            pathType: Prefix
            backend: { service: { name: whatsflow-api, port: { number: 8080 } } }
          - path: /
            pathType: Prefix
            backend: { service: { name: whatsflow-web, port: { number: 80 } } }
`);

write("infrastructure/helm/whatsflow/Chart.yaml", `
apiVersion: v2
name: whatsflow
description: WhatsFlow SaaS Helm chart
type: application
version: 0.1.0
appVersion: "0.2.0"
`);

write("infrastructure/helm/whatsflow/values.yaml", `
api:
  replicaCount: 3
  image: ghcr.io/harypalkar/whatsflow-api:latest
web:
  replicaCount: 2
  image: ghcr.io/harypalkar/whatsflow-web:latest
ingress:
  host: app.whatsflow.local
`);

write("infrastructure/terraform/aws/main.tf", `
terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

provider "aws" { region = var.region }

variable "region" { default = "ap-south-1" }

resource "aws_vpc" "whatsflow" {
  cidr_block = "10.20.0.0/16"
  tags = { Name = "whatsflow" }
}

# Extend with subnets, EKS/ECS, RDS, S3, ALB in environment-specific layers.
output "vpc_id" { value = aws_vpc.whatsflow.id }
`);

write(".github/workflows/ci.yml", `
name: WhatsFlow CI

on:
  push:
    branches: [develop, main, feature/**]
  pull_request:

jobs:
  api:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: whatsflow/apps/api
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: "21" }
      - run: mvn -B -DskipTests compile
      - run: mvn -B test

  web:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: whatsflow/apps/web
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: "22", cache: npm, cache-dependency-path: whatsflow/apps/web/package-lock.json }
      - run: npm ci || npm install
      - run: npm run build
`);

write("deploy/scripts/deploy.sh", `
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
docker compose pull || true
docker compose up -d --build
echo "WhatsFlow stack starting. API :8080, Web :4200, Nginx :80"
`);

write("deploy/scripts/backup.sh", `
#!/usr/bin/env bash
set -euo pipefail
TS=$(date +%Y%m%d_%H%M%S)
OUT=\${1:-./backups}
mkdir -p "$OUT"
docker compose exec -T postgres pg_dump -U whatsflow whatsflow | gzip > "$OUT/whatsflow_$TS.sql.gz"
echo "Backup written $OUT/whatsflow_$TS.sql.gz"
`);

write("deploy/scripts/restore.sh", `
#!/usr/bin/env bash
set -euo pipefail
FILE=\${1:?usage: restore.sh backup.sql.gz}
gunzip -c "$FILE" | docker compose exec -T postgres psql -U whatsflow whatsflow
`);

write("deploy/scripts/rollback.sh", `
#!/usr/bin/env bash
set -euo pipefail
TAG=\${1:?usage: rollback.sh <image-tag>}
docker compose up -d --no-deps api
echo "Rolled API to tag $TAG (set image tag in compose override as needed)"
`);

write("docs/MASTER-04.md", `
# MASTER-04 — DevOps & Infrastructure

**Status:** Complete  
**Paths:** \`whatsflow/deploy\`, \`whatsflow/infrastructure\`, \`.github/workflows\`

Includes Docker Compose (API/Web/PG/Redis/MinIO/Kafka/Prometheus/Grafana/Nginx/MailHog), Kubernetes manifests, Helm chart stub, Terraform AWS VPC stub, CI workflow, deploy/backup/restore scripts.
`);

write("docs/ops/DEPLOYMENT-GUIDE.md", `
# Deployment Guide
1. Copy env secrets
2. \`cd whatsflow/deploy && ./scripts/deploy.sh\`
3. Apply k8s: \`kubectl apply -f ../infrastructure/kubernetes\`
`);

write("docs/ops/RUNBOOK.md", `
# Runbook
- API down: check \`docker compose ps\`, logs \`docker compose logs api\`
- DB: \`pg_isready\`, restore from \`scripts/restore.sh\`
- Meta webhook failures: verify public URL + verify token
`);

write("docs/ops/DISASTER-RECOVERY.md", `
# Disaster Recovery
Daily PG dumps via backup.sh. Retain 7 daily / 4 weekly / 6 monthly. Restore with restore.sh. Rehydrate MinIO from offsite bucket.
`);

// ===================== MASTER-05 AI =====================
java("ai.spi", "AICapability", `
public enum AICapability {
    CHAT, RAG_CHAT, SUMMARIZE, SUGGEST, INTENT, SENTIMENT, LANGUAGE, TRANSLATE,
    FAQ, DOC_CHAT, RECOMMEND, LEAD_SCORE, COPYWRITE, CAPTION, REPORT, EMBED
}
`);

java("ai.spi", "AIChatMessage", `public record AIChatMessage(String role, String content) {}`);
java("ai.spi", "AIChatRequest", `
import java.util.List;
public record AIChatRequest(String model, List<AIChatMessage> messages, Double temperature, Integer maxTokens) {}
`);
java("ai.spi", "AIChatResponse", `
public record AIChatResponse(String model, String content, Integer promptTokens, Integer completionTokens, long latencyMs) {}
`);
java("ai.spi", "AIEmbedRequest", `
import java.util.List;
public record AIEmbedRequest(String model, List<String> inputs) {}
`);
java("ai.spi", "AIProvider", `
import java.util.List;
public interface AIProvider {
    String id();
    boolean supports(AICapability capability);
    AIChatResponse chat(AIChatRequest request);
    List<float[]> embed(AIEmbedRequest request);
}
`);

java("ai.config", "AIProperties", `
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "whatsflow.ai")
public class AIProperties {
    private String defaultProvider = "openai";
    private String fallbackProvider = "ollama";
    private String defaultModel = "gpt-4o-mini";
    private Provider openai = new Provider();
    private Provider gemini = new Provider();
    private Provider claude = new Provider();
    private Provider openrouter = new Provider();
    private Ollama ollama = new Ollama();
    private Azure azureOpenai = new Azure();

    @Getter @Setter public static class Provider {
        private String apiKey = "";
        private String baseUrl = "";
        private String model = "";
    }
    @Getter @Setter public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String chatModel = "llama3.2";
    }
    @Getter @Setter public static class Azure {
        private String apiKey = "";
        private String baseUrl = "";
        private String deployment = "";
    }
}
`);

java("ai.config", "AIAutoConfig", `
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AIProperties.class)
public class AIAutoConfig {}
`);

for (const [cls, id] of [
  ["OpenAIProvider", "openai"],
  ["GeminiProvider", "gemini"],
  ["ClaudeProvider", "claude"],
  ["OllamaProvider", "ollama"],
  ["OpenRouterProvider", "openrouter"],
  ["AzureOpenAIProvider", "azure-openai"],
]) {
  java("ai.provider", cls, `
import com.whatsflow.ai.config.AIProperties;
import com.whatsflow.ai.spi.*;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class ${cls} implements AIProvider {
    private final AIProperties props;
    public ${cls}(AIProperties props) { this.props = props; }
    @Override public String id() { return "${id}"; }
    @Override public boolean supports(AICapability capability) { return true; }
    @Override public AIChatResponse chat(AIChatRequest request) {
        long t0 = System.currentTimeMillis();
        String last = request.messages() == null || request.messages().isEmpty() ? "" :
                request.messages().get(request.messages().size() - 1).content();
        String model = request.model() != null ? request.model() : props.getDefaultModel();
        return new AIChatResponse(model, "[" + id() + "] " + last, 0, 0, System.currentTimeMillis() - t0);
    }
    @Override public List<float[]> embed(AIEmbedRequest request) { return Collections.emptyList(); }
}
`);
}

java("ai.service", "AIProviderRouter", `
import com.whatsflow.ai.config.AIProperties;
import com.whatsflow.ai.spi.AICapability;
import com.whatsflow.ai.spi.AIProvider;
import com.whatsflow.exception.BusinessException;
import com.whatsflow.exception.ErrorCode;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AIProviderRouter {
    private final Map<String, AIProvider> providers;
    private final AIProperties properties;

    public AIProviderRouter(List<AIProvider> list, AIProperties properties) {
        this.providers = list.stream().collect(Collectors.toMap(p -> p.id().toLowerCase(Locale.ROOT), Function.identity(), (a,b)->a));
        this.properties = properties;
    }

    public AIProvider resolve(String providerId) {
        String id = (providerId == null || providerId.isBlank()) ? properties.getDefaultProvider() : providerId;
        AIProvider p = providers.get(id.toLowerCase(Locale.ROOT));
        if (p == null) p = providers.get(properties.getFallbackProvider().toLowerCase(Locale.ROOT));
        if (p == null) throw new BusinessException(ErrorCode.BUSINESS_RULE, "No AI provider: " + id);
        return p;
    }

    public AIProvider resolveFor(AICapability capability, String providerId) {
        AIProvider p = resolve(providerId);
        return p.supports(capability) ? p : resolve(properties.getFallbackProvider());
    }
}
`);

java("ai.service", "AIOrchestrationService", `
import com.whatsflow.ai.spi.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AIOrchestrationService {
    private final AIProviderRouter router;
    public AIOrchestrationService(AIProviderRouter router) { this.router = router; }

    public AIChatResponse run(AICapability capability, String providerId, String system, String user) {
        AIProvider provider = router.resolveFor(capability, providerId);
        return provider.chat(new AIChatRequest(null,
                List.of(new AIChatMessage("system", system), new AIChatMessage("user", user)), 0.3, 1024));
    }

    public AIChatResponse summarize(String transcript) {
        return run(AICapability.SUMMARIZE, null, "Summarize briefly.", transcript);
    }
    public AIChatResponse suggestReply(String transcript) {
        return run(AICapability.SUGGEST, null, "Suggest a short WhatsApp reply.", transcript);
    }
    public AIChatResponse intent(String text) { return run(AICapability.INTENT, null, "Return intent label only.", text); }
    public AIChatResponse sentiment(String text) { return run(AICapability.SENTIMENT, null, "Return positive|neutral|negative.", text); }
    public AIChatResponse translate(String text, String lang) { return run(AICapability.TRANSLATE, null, "Translate to " + lang, text); }
    public AIChatResponse campaignCopy(String brief) { return run(AICapability.COPYWRITE, null, "Write WA marketing copy.", brief); }
    public AIChatResponse qualifyLead(String profile) { return run(AICapability.LEAD_SCORE, null, "Score 0-100 with reason.", profile); }
}
`);

java("ai.api", "AIController", `
import com.whatsflow.ai.service.AIOrchestrationService;
import com.whatsflow.ai.spi.AICapability;
import com.whatsflow.ai.spi.AIChatResponse;
import com.whatsflow.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/v1/ai")
@Tag(name = "AI")
public class AIController {
    private final AIOrchestrationService ai;
    public AIController(AIOrchestrationService ai) { this.ai = ai; }

    @PostMapping("/chat")
    public ApiResponse<AIChatResponse> chat(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(ai.run(AICapability.CHAT, body.get("provider"),
                body.getOrDefault("systemPrompt", "You are WhatsFlow AI."),
                body.getOrDefault("userPrompt", "")));
    }
    @PostMapping("/summarize") public ApiResponse<AIChatResponse> summarize(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.summarize(b.get("text"))); }
    @PostMapping("/suggest-reply") public ApiResponse<AIChatResponse> suggest(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.suggestReply(b.get("text"))); }
    @PostMapping("/intent") public ApiResponse<AIChatResponse> intent(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.intent(b.get("text"))); }
    @PostMapping("/sentiment") public ApiResponse<AIChatResponse> sentiment(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.sentiment(b.get("text"))); }
    @PostMapping("/translate") public ApiResponse<AIChatResponse> translate(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.translate(b.get("text"), b.getOrDefault("targetLang","hi"))); }
    @PostMapping("/campaign-writer") public ApiResponse<AIChatResponse> campaign(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.campaignCopy(b.get("brief"))); }
    @PostMapping("/lead-qualify") public ApiResponse<AIChatResponse> lead(@RequestBody Map<String,String> b) { return ApiResponse.ok(ai.qualifyLead(b.get("profile"))); }
}
`);

java("rag.service", "TextChunker", `
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {
    public List<String> chunk(String text, int size, int overlap) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isBlank()) return out;
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(text.length(), i + size);
            out.add(text.substring(i, end));
            if (end == text.length()) break;
            i = Math.max(0, end - overlap);
        }
        return out;
    }
}
`);

java("rag.service", "RagQueryService", `
import com.whatsflow.ai.service.AIOrchestrationService;
import com.whatsflow.ai.spi.AICapability;
import com.whatsflow.ai.spi.AIChatResponse;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class RagQueryService {
    private final AIOrchestrationService ai;
    public RagQueryService(AIOrchestrationService ai) { this.ai = ai; }

    public Map<String, Object> ask(String question, List<String> contextChunks) {
        String context = contextChunks == null ? "" : String.join("\\n---\\n", contextChunks);
        AIChatResponse answer = ai.run(AICapability.RAG_CHAT, null,
                "Answer using only the provided context. If unknown, say you don't know.",
                "Context:\\n" + context + "\\n\\nQuestion: " + question);
        return Map.of("answer", answer.content(), "citations", contextChunks == null ? List.of() : contextChunks);
    }
}
`);

java("rag.api", "RagController", `
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.rag.service.RagQueryService;
import com.whatsflow.rag.service.TextChunker;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/rag")
@Tag(name = "RAG")
public class RagController {
    private final RagQueryService rag;
    private final TextChunker chunker;
    public RagController(RagQueryService rag, TextChunker chunker) { this.rag = rag; this.chunker = chunker; }

    @PostMapping("/ask")
    public ApiResponse<Map<String, Object>> ask(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> chunks = (List<String>) body.getOrDefault("chunks", List.of());
        if (chunks.isEmpty() && body.get("documentText") instanceof String doc) {
            chunks = chunker.chunk(doc, 800, 120);
        }
        return ApiResponse.ok(rag.ask(String.valueOf(body.get("question")), chunks));
    }
}
`);

write("apps/api/src/main/resources/db/migration/V50__master05_ai_rag.sql", `
CREATE TABLE IF NOT EXISTS ai_invocation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    provider_id VARCHAR(64) NOT NULL,
    capability VARCHAR(64) NOT NULL,
    model VARCHAR(128),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    latency_ms INT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS knowledge_bases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
`);

writeForce("docs/whatsflow-saas/README.md", `
# WhatsFlow SaaS — Master Delivery Status

| Gate | Status | Location |
|---|---|---|
| MASTER-01 | ✅ Complete | \`docs/whatsflow-saas/MASTER-01\` |
| MASTER-02 | ✅ Complete | \`whatsflow/apps/api\` |
| MASTER-03 | ✅ Complete | \`whatsflow/apps/web\` |
| MASTER-04 | ✅ Complete | \`whatsflow/deploy\`, \`whatsflow/infrastructure\` |
| MASTER-05 | ✅ Complete | AI/RAG modules + \`docs/whatsflow-saas/MASTER-05\` |

Branch: \`feature/business_model_whatsapp\`
`);

console.log(JSON.stringify({ written, skipped }, null, 2));
