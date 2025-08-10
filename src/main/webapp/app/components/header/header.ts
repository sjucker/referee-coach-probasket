import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {MatToolbar, MatToolbarRow} from "@angular/material/toolbar";
import {MatButtonModule} from "@angular/material/button";
import {AuthService} from "../../auth.service";
import {NavigationEnd, Router, RouterLink} from "@angular/router";
import {PATH_LOGIN, PATH_OVERVIEW} from "../../app.routes";
import {MatTooltip} from "@angular/material/tooltip";
import {MatIcon} from "@angular/material/icon";
import {toSignal} from '@angular/core/rxjs-interop';
import {filter, map, startWith} from 'rxjs';

@Component({
    selector: 'app-header',
    imports: [
        MatToolbar,
        MatToolbarRow,
        MatButtonModule,
        MatTooltip,
        MatIcon,
        RouterLink,
    ],
    templateUrl: './header.html',
    styleUrl: './header.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class Header {
    protected readonly PATH_OVERVIEW = PATH_OVERVIEW;
    protected readonly auth = inject(AuthService);
    protected readonly isOverview = computed(() => this.currentUrl().startsWith('/' + PATH_OVERVIEW));

    private readonly router = inject(Router);

    private readonly currentUrl = toSignal(
        this.router.events.pipe(
            filter(e => e instanceof NavigationEnd),
            map(() => this.router.url),
            startWith(this.router.url)
        ),
        {initialValue: this.router.url}
    );

    logout() {
        this.auth.logout();
        this.router.navigate([PATH_LOGIN]).catch(err => console.error(err));
    }
}
