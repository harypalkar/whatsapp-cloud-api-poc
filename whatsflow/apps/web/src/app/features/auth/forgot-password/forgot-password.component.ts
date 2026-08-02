import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'wf-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <section class="auth"><div class="panel">
    <h1>Reset password</h1>
    <p>Enter your email and we'll send a reset link.</p>
    <form (ngSubmit)="sent=true"><label>Email <input type="email" [(ngModel)]="email" name="email" required /></label>
    <button type="submit">Send reset link</button></form>
    <p *ngIf="sent">If an account exists, a reset email was queued.</p>
    <a routerLink="/auth/login">Back to login</a>
  </div></section>`,
  styles: [`:host { display:block } .auth{min-height:100vh;display:grid;place-items:center;background:linear-gradient(160deg,#f4f7f2,#e7eef8)} .panel{width:min(420px,92vw);background:#fff;border-radius:1rem;padding:2rem} form{display:grid;gap:.8rem} input,button{padding:.65rem .75rem;border-radius:.5rem} button{background:#0f3d2e;color:#fff;border:0}`],
})
export class ForgotPasswordComponent { email = ''; sent = false; }
