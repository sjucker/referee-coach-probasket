import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';

const MIN_PASSWORD_LENGTH = 8;

export interface SetPasswordDialogData {
    fullName: string;
}

@Component({
    selector: 'app-set-password-dialog',
    imports: [MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, ReactiveFormsModule],
    template: `
        <h2 mat-dialog-title>Set Password</h2>
        <mat-dialog-content class="mat-typography">
            <p>New password for {{ data.fullName }}:</p>
            <form [formGroup]="form" autocomplete="off">
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
            <button mat-flat-button [disabled]="form.invalid" (click)="save()">Save</button>
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
export class SetPasswordDialog {
    private readonly fb = inject(FormBuilder);
    private readonly dialogRef = inject(MatDialogRef<SetPasswordDialog, string | null>);

    protected readonly data = inject<SetPasswordDialogData>(MAT_DIALOG_DATA);
    protected readonly minLength = MIN_PASSWORD_LENGTH;

    protected readonly form = this.fb.nonNullable.group({
        password: ['', [Validators.required, Validators.minLength(MIN_PASSWORD_LENGTH)]]
    });

    save() {
        if (this.form.invalid) return;
        this.dialogRef.close(this.form.getRawValue().password);
    }
}
