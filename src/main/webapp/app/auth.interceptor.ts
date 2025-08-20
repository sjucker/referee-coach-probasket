import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';
import {Router} from '@angular/router';
import {catchError} from 'rxjs/operators';
import {throwError} from 'rxjs';
import {PATH_LOGIN} from './app.routes';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    const token = auth.token();
    const request = token
        ? req.clone({setHeaders: {Authorization: `Bearer ${token}`}})
        : req;

    return next(request).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401) {
                const isAuthEndpoint = request.url.includes('/api/auth/');
                // Only redirect for protected endpoints; avoid interfering with login failures
                if (!isAuthEndpoint) {
                    auth.logout();
                    const requestedUrl = router.url;
                    router.navigate([PATH_LOGIN], {queryParams: {requestedUrl}}).catch(err => console.error(err));
                }
            }
            return throwError(() => error);
        })
    );
};
