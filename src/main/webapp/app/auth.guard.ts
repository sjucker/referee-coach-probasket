import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';
import {PATH_LOGIN, PATH_OVERVIEW} from "./app.routes";

export const authGuard: CanActivateFn = (_route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated()) {
        return true;
    }
    // redirect to login and remember the originally requested URL
    return router.createUrlTree([PATH_LOGIN], {queryParams: {requestedUrl: state.url}});
};

export const redirectIfAuthenticatedGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated()) {
        return router.createUrlTree([PATH_OVERVIEW]);
    }
    return true;
};

export const adminGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated() && auth.isAdmin()) {
        return true;
    }
    return router.createUrlTree([PATH_OVERVIEW]);
};
