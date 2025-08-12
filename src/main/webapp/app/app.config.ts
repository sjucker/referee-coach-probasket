import {ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection} from '@angular/core';
import {provideRouter, withHashLocation} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';

import {routes} from './app.routes';
import {authInterceptor} from './auth.interceptor';
import {provideLuxonDateAdapter} from "@angular/material-luxon-adapter";

export const appConfig: ApplicationConfig = {
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideZoneChangeDetection({eventCoalescing: true}),
        provideRouter(routes, withHashLocation()),
        provideHttpClient(withInterceptors([authInterceptor])),
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
