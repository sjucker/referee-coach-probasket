import {Routes} from '@angular/router';
import {Login} from './login/login';
import {Overview} from './overview/overview';
import {authGuard, redirectIfAuthenticatedGuard} from './auth.guard';

export const PATH_LOGIN = 'login';
export const PATH_OVERVIEW = 'overview';

export const routes: Routes = [
    {path: PATH_LOGIN, component: Login, canActivate: [redirectIfAuthenticatedGuard]},
    {path: PATH_OVERVIEW, component: Overview, canActivate: [authGuard]},
    {path: '', pathMatch: 'full', redirectTo: PATH_OVERVIEW},
    {path: '**', redirectTo: PATH_OVERVIEW}
];
