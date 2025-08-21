import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";

export interface FinishReplyDialogData {
    totalReplies: number;
    requiredRepliesRemaining: number;
}

@Component({
    selector: 'app-finish-reply-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule, MatFormFieldModule, FormsModule, MatInputModule],
    template: `
        <h2 mat-dialog-title>Finish Replying</h2>
        <mat-dialog-content class="mat-typography">
            <p>Do you really want to finish replying?<br/>An email will be sent to your colleagues that you added some replies.</p>

            <p>You have replied to {{ data.totalReplies }} video comments.</p>

            @if (data.requiredRepliesRemaining > 0) {
                <p><b>There are {{ data.requiredRepliesRemaining }} more video comments that require a reply from you!</b></p>
            }
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="false">Cancel</button>
            <button mat-flat-button [mat-dialog-close]="true">Finish</button>
        </mat-dialog-actions>
    `,
    styleUrl: './reply-dialog.scss',
})
export class FinishReplyDialog {
    protected readonly data = inject<FinishReplyDialogData>(MAT_DIALOG_DATA);
}
