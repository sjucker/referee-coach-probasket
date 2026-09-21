import {
    ApplicationConfig,
    inject,
    provideAppInitializer,
    provideBrowserGlobalErrorListeners,
    provideZoneChangeDetection
} from '@angular/core';
import {provideRouter, withHashLocation} from '@angular/router';
import {provideHttpClient, withInterceptors, withXhr} from '@angular/common/http';

import {routes} from './app.routes';
import {authInterceptor} from './auth.interceptor';
import {AuthService} from './auth.service';
import {provideLuxonDateAdapter} from "@angular/material-luxon-adapter";

export const appConfig: ApplicationConfig = {
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideZoneChangeDetection({eventCoalescing: true}),
        provideRouter(routes, withHashLocation()),
        provideHttpClient(withXhr(), withInterceptors([authInterceptor])),
        // load the auth configuration before the first render, so the login page never shows the wrong hint
        provideAppInitializer(() => inject(AuthService).loadAuthConfig()),
        provideLuxonDateAdapter({
            parse: {
                dateInput: 'dd.MM.yyyy',
            },
            display: {
                dateInput: 'dd.MM.yyyy',
                monthYearLabel: 'MMM yyyy',
                dateA11yLabel: 'dd.MM.yyyy',
                monthYearA11yLabel: 'MMMM yyyy',
            },
        })
    ]
};
