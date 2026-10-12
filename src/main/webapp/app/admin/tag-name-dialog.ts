import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';

const MAX_NAME_LENGTH = 255;

export interface TagNameDialogData {
    title: string;
    name: string;
    confirmText: string;
}

@Component({
    selector: 'app-tag-name-dialog',
    imports: [MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, ReactiveFormsModule],
    template: `
        <h2 mat-dialog-title>{{ data.title }}</h2>
        <mat-dialog-content class="mat-typography">
            <form [formGroup]="form" autocomplete="off" (ngSubmit)="save()">
                <mat-form-field appearance="outline">
                    <mat-label>Name</mat-label>
                    <input matInput type="text" formControlName="name" required/>
                    @if (form.controls.name.hasError('maxlength')) {
                        <mat-error>At most {{ maxLength }} characters</mat-error>
                    }
                </mat-form-field>
            </form>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="null">Cancel</button>
            <button mat-flat-button [disabled]="form.invalid" (click)="save()">{{ data.confirmText }}</button>
        </mat-dialog-actions>
    `,
    styles: [`
        form {
            display: flex;
            flex-direction: column;
            min-width: 24rem;
        }
    `],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TagNameDialog {
    private readonly fb = inject(FormBuilder);
    private readonly dialogRef = inject(MatDialogRef<TagNameDialog, string | null>);
    protected readonly data = inject<TagNameDialogData>(MAT_DIALOG_DATA);

    protected readonly maxLength = MAX_NAME_LENGTH;

    protected readonly form = this.fb.nonNullable.group({
        name: [this.data.name, [Validators.required, Validators.pattern(/\S/), Validators.maxLength(MAX_NAME_LENGTH)]]
    });

    save() {
        if (this.form.invalid) return;
        this.dialogRef.close(this.form.getRawValue().name.trim());
    }
}
