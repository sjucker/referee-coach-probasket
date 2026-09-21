import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {AuthService} from '../auth.service';
import {PATH_OVERVIEW} from '../app.routes';

const MIN_PASSWORD_LENGTH = 8;

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmation = group.get('confirmation')?.value;
    return newPassword === confirmation ? null : {passwordsMismatch: true};
}

@Component({
    selector: 'app-change-password',
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
    templateUrl: './change-password.html',
    styleUrl: './change-password.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChangePassword {
    private readonly fb = inject(FormBuilder);
    private readonly auth = inject(AuthService);
    private readonly router = inject(Router);
    private readonly snackBar = inject(MatSnackBar);

    protected readonly loading = signal(false);
    protected readonly error = signal<string | null>(null);
    protected readonly minLength = MIN_PASSWORD_LENGTH;

    protected readonly form = this.fb.nonNullable.group({
        currentPassword: ['', [Validators.required]],
        newPassword: ['', [Validators.required, Validators.minLength(MIN_PASSWORD_LENGTH)]],
        confirmation: ['', [Validators.required]]
    }, {validators: passwordsMatch});

    submit() {
        this.error.set(null);
        if (this.form.invalid || this.loading()) return;

        const {currentPassword, newPassword} = this.form.getRawValue();
        this.loading.set(true);

        this.auth.changePassword(currentPassword, newPassword).subscribe({
            next: () => {
                this.loading.set(false);
                this.snackBar.open('Password changed', undefined, {duration: 2000, horizontalPosition: 'center', verticalPosition: 'top'});
                this.router.navigateByUrl(`/${PATH_OVERVIEW}`);
            },
            error: (err) => {
                this.loading.set(false);
                this.error.set(err?.status === 409
                    ? 'Passwords are managed in Basketplan and cannot be changed here.'
                    : 'Could not change the password. Is the current password correct?');
            }
        });
    }
}
