import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {CreateUserDTO} from '../../rest';

const MIN_PASSWORD_LENGTH = 8;

@Component({
    selector: 'app-create-user-dialog',
    imports: [MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, ReactiveFormsModule],
    template: `
        <h2 mat-dialog-title>Add User</h2>
        <mat-dialog-content class="mat-typography">
            <form [formGroup]="form" autocomplete="off">
                <mat-form-field appearance="outline">
                    <mat-label>First name</mat-label>
                    <input matInput type="text" formControlName="firstName" required/>
                </mat-form-field>
                <mat-form-field appearance="outline">
                    <mat-label>Last name</mat-label>
                    <input matInput type="text" formControlName="lastName" required/>
                </mat-form-field>
                <mat-form-field appearance="outline">
                    <mat-label>E-Mail</mat-label>
                    <input matInput type="email" formControlName="email" required/>
                    @if (form.controls.email.touched && form.controls.email.hasError('email')) {
                        <mat-error>Not a valid e-mail address</mat-error>
                    }
                </mat-form-field>
                <mat-form-field appearance="outline">
                    <mat-label>Username</mat-label>
                    <input matInput type="text" formControlName="username" required/>
                </mat-form-field>
                <mat-form-field appearance="outline">
                    <mat-label>Password</mat-label>
                    <input matInput type="password" formControlName="password" required/>
                    @if (form.controls.password.touched && form.controls.password.hasError('minlength')) {
                        <mat-error>At least {{ minLength }} characters</mat-error>
                    }
                </mat-form-field>
            </form>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="null">Cancel</button>
            <button mat-flat-button [disabled]="form.invalid" (click)="create()">Add</button>
        </mat-dialog-actions>
    `,
    styles: [`
        form {
            display: flex;
            flex-direction: column;
            min-width: 20rem;
        }
    `],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateUserDialog {
    private readonly fb = inject(FormBuilder);
    private readonly dialogRef = inject(MatDialogRef<CreateUserDialog, CreateUserDTO | null>);

    protected readonly minLength = MIN_PASSWORD_LENGTH;

    protected readonly form = this.fb.nonNullable.group({
        firstName: ['', [Validators.required]],
        lastName: ['', [Validators.required]],
        email: ['', [Validators.required, Validators.email]],
        username: ['', [Validators.required]],
        password: ['', [Validators.required, Validators.minLength(MIN_PASSWORD_LENGTH)]]
    });

    create() {
        if (this.form.invalid) return;
        this.dialogRef.close(this.form.getRawValue());
    }
}
