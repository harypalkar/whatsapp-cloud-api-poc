import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, catchError, of } from 'rxjs';
import { OnboardingService } from '../services/onboarding.service';

/** Blocks /app until company onboarding is finished. */
export const onboardingGuard: CanActivateFn = () => {
  const onboarding = inject(OnboardingService);
  const router = inject(Router);
  return onboarding.status().pipe(
    map((res) => {
      const done = !!res.data?.completed || !!res.data?.company?.onboardingCompleted;
      if (done) return true;
      return router.createUrlTree(['/onboarding']);
    }),
    catchError(() => of(router.createUrlTree(['/onboarding']))),
  );
};
