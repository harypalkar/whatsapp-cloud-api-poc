import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { onboardingGuard } from './core/guards/onboarding.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent) },
  { path: 'auth/login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'auth/register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
  { path: 'auth/forgot-password', loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'onboarding', canActivate: [authGuard], loadComponent: () => import('./features/onboarding/onboarding.component').then(m => m.OnboardingComponent) },
  {
    path: 'app',
    canActivate: [authGuard, onboardingGuard],
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
      { path: 'whatsapp', loadComponent: () => import('./features/whatsapp/whatsapp.component').then(m => m.WhatsappComponent) },
      { path: 'settings', loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent) },
      { path: 'profile', loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
      { path: 'admin', loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent) },
      { path: 'ai', loadComponent: () => import('./features/ai/ai.component').then(m => m.AiComponent) },
    ]
  },
  { path: '**', redirectTo: '' },
];
