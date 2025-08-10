import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../auth.service';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {Header} from "../components/header/header";
import {LoadingBar} from "../components/loading-bar/loading-bar";
import {PATH_LOGIN, PATH_OVERVIEW} from "../app.routes";

@Component({
    selector: 'app-login',
    imports: [
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatCardModule,
        MatIconModule,
        Header,
        LoadingBar
    ],
    templateUrl: './login.html',
    styleUrl: './login.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class Login {
    private readonly fb = inject(FormBuilder);
    private readonly auth = inject(AuthService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);

    protected readonly loading = signal(false);
    protected readonly error = signal<string | null>(null);

    protected readonly form = this.fb.nonNullable.group({
        username: ['', [Validators.required]],
        password: ['', [Validators.required]]
    });

    submit() {
        this.error.set(null);
        if (this.form.invalid || this.loading()) return;

        const {username, password} = this.form.getRawValue();
        this.loading.set(true);

        this.auth.login(username, password).subscribe({
            next: () => {
                this.loading.set(false);
                const requestedUrl = this.route.snapshot.queryParamMap.get('requestedUrl');
                const defaultUrl = `/${PATH_OVERVIEW}`;
                this.router.navigateByUrl(requestedUrl && !requestedUrl.includes(`/${PATH_LOGIN}`) ? requestedUrl : defaultUrl);
            },
            error: (err) => {
                this.loading.set(false);
                this.error.set(err?.error?.message ?? 'Invalid username or password');
            }
        });
    }
}
