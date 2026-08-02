import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { OnboardingService } from '../../../core/services/onboarding.service';

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

  constructor(
    private auth: AuthService,
    private onboarding: OnboardingService,
    private router: Router,
  ) {}

  submit() {
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.email, this.password).subscribe({
      next: () => {
        this.onboarding.status().subscribe({
          next: (res) => {
            this.loading.set(false);
            const done = !!res.data?.completed || !!res.data?.company?.onboardingCompleted;
            this.router.navigateByUrl(done ? '/app/dashboard' : '/onboarding');
          },
          error: () => {
            this.loading.set(false);
            this.router.navigateByUrl('/onboarding');
          },
        });
      },
      error: (err) => { this.loading.set(false); this.error.set(err?.error?.message || 'Login failed'); }
    });
  }
}
