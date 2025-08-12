import {Routes} from '@angular/router';
import {Login} from './login/login';
import {Overview} from './overview/overview';
import {authGuard, redirectIfAuthenticatedGuard} from './auth.guard';
import {TagSearch} from './tag-search/tag-search';
import {EditPage} from './edit/edit';

export const PATH_LOGIN = 'login';
export const PATH_OVERVIEW = 'overview';
export const PATH_TAG_SEARCH = 'tag-search';
export const PATH_EDIT = 'edit';

export const routes: Routes = [
    {path: PATH_LOGIN, component: Login, canActivate: [redirectIfAuthenticatedGuard]},
    {path: PATH_OVERVIEW, component: Overview, canActivate: [authGuard]},
    {path: PATH_TAG_SEARCH, component: TagSearch, canActivate: [authGuard]},
    {path: `${PATH_EDIT}/:externalId`, component: EditPage, canActivate: [authGuard]},
    {path: '', pathMatch: 'full', redirectTo: PATH_OVERVIEW},
    {path: '**', redirectTo: PATH_OVERVIEW}
];
