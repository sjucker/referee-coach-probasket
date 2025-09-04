import {Routes} from '@angular/router';
import {Login} from './login/login';
import {Overview} from './overview/overview';
import {adminGuard, authGuard, redirectIfAuthenticatedGuard} from './auth.guard';
import {TagSearch} from './tag-search/tag-search';
import {EditPage} from './edit/edit';
import {canDeactivateGuard} from './can-deactivate.guard';
import {ViewPage} from './view/view';
import {DiscussPage} from './discuss/discuss';
import {AdminPage} from './admin/admin';
import {ExportPage} from './export/export';

export const PATH_LOGIN = 'login';
export const PATH_OVERVIEW = 'overview';
export const PATH_TAG_SEARCH = 'tag-search';
export const PATH_EDIT = 'edit';
export const PATH_VIEW = 'view';
export const PATH_DISCUSS = 'discuss';
export const PATH_ADMIN = 'admin';
export const PATH_EXPORT = 'export';

export const routes: Routes = [
    {path: PATH_LOGIN, component: Login, canActivate: [redirectIfAuthenticatedGuard]},
    {path: PATH_OVERVIEW, component: Overview, canActivate: [authGuard]},
    {path: PATH_TAG_SEARCH, component: TagSearch, canActivate: [authGuard]},
    {path: PATH_ADMIN, component: AdminPage, canActivate: [authGuard, adminGuard]},
    {path: PATH_EXPORT, component: ExportPage, canActivate: [authGuard, adminGuard]},
    {path: `${PATH_EDIT}/:externalId`, component: EditPage, canActivate: [authGuard], canDeactivate: [canDeactivateGuard]},
    {path: `${PATH_VIEW}/:externalId`, component: ViewPage, canActivate: [authGuard]},
    {path: `${PATH_DISCUSS}/:externalId`, component: DiscussPage, canActivate: [authGuard], canDeactivate: [canDeactivateGuard]},
    {path: '', pathMatch: 'full', redirectTo: PATH_OVERVIEW},
    {path: '**', redirectTo: PATH_OVERVIEW}
];
