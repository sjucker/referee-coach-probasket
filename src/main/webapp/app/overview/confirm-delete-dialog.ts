import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

export interface ConfirmDeleteData {
    title: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
}

@Component({
    selector: 'app-confirm-delete-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule],
    template: `
        <h2 mat-dialog-title>{{ data.title }}</h2>
        <mat-dialog-content>
            <p>{{ data.message }}</p>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="false">{{ data.cancelText || 'Cancel' }}</button>
            <button color="warn" mat-flat-button [mat-dialog-close]="true">{{ data.confirmText || 'Delete' }}</button>
        </mat-dialog-actions>
    `,
    styles: []
})
export class ConfirmDeleteDialog {
    protected readonly data = inject<ConfirmDeleteData>(MAT_DIALOG_DATA);
}
