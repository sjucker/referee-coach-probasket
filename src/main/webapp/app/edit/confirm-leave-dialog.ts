import {Component} from '@angular/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

@Component({
    selector: 'app-confirm-leave-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule],
    template: `
        <h2 mat-dialog-title>Discard Changes?</h2>
        <mat-dialog-content>
            <p>You have unsaved changes. Do you really want to discard them?</p>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="true">Discard changes and leave this page</button>
            <button mat-flat-button [mat-dialog-close]="false">Stay on this page</button>
        </mat-dialog-actions>
    `,
    styles: []
})
export class ConfirmLeaveDialog {
}
