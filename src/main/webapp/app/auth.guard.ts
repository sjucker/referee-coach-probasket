import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';
import {PATH_LOGIN, PATH_OVERVIEW} from "./app.routes";

export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated()) {
        return true;
    }
    return router.createUrlTree([PATH_LOGIN]);
};

export const redirectIfAuthenticatedGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated()) {
        return router.createUrlTree([PATH_OVERVIEW]);
    }
    return true;
};
