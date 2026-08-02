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
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.message || err?.message || 'Registration failed';
        this.error.set(typeof msg === 'string' ? msg : 'Registration failed');
      }
    });
  }
}
