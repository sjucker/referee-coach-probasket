import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MatToolbar, MatToolbarRow} from "@angular/material/toolbar";
import {MatButtonModule} from "@angular/material/button";
import {AuthService} from "../../auth.service";
import {Router} from "@angular/router";
import {PATH_LOGIN} from "../../app.routes";
import {MatTooltip} from "@angular/material/tooltip";
import {MatIcon} from "@angular/material/icon";

@Component({
    selector: 'app-header',
    imports: [
        MatToolbar,
        MatToolbarRow,
        MatButtonModule,
        MatTooltip,
        MatIcon,
    ],
    templateUrl: './header.html',
    styleUrl: './header.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class Header {
    protected readonly auth = inject(AuthService);
    private readonly router = inject(Router);

    logout() {
        this.auth.logout();
        this.router.navigate([PATH_LOGIN]).catch(err => console.error(err));
    }
}
