import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Never attach a stale token to public auth calls (causes 401 after API restart).
  if (req.url.includes('/v1/auth/')) {
    return next(req);
  }

  const auth = inject(AuthService);
  const token = auth.accessToken();
  if (!token) return next(req);
  return next(req.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  }));
};
