import {Component} from '@angular/core';
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {CdkTextareaAutosize} from "@angular/cdk/text-field";
import {MatInputModule} from "@angular/material/input";

@Component({
    selector: 'app-reply-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule, MatFormFieldModule, FormsModule, CdkTextareaAutosize, MatInputModule],
    template: `
        <h2 mat-dialog-title>Reply to Video Comment</h2>
        <mat-dialog-content class="mat-typography">
            <div class="container">
                <mat-form-field appearance="outline">
                    <mat-label>Your Reply</mat-label>
                    <textarea [(ngModel)]="reply"
                              maxlength="1024"
                              cdkAutosizeMaxRows="5"
                              cdkAutosizeMinRows="2"
                              cdkTextareaAutosize
                              matInput></textarea>
                </mat-form-field>
            </div>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="">Cancel</button>
            <button mat-flat-button [mat-dialog-close]="reply">Reply</button>
        </mat-dialog-actions>
    `,
    styleUrl: './reply-dialog.scss',
})
export class ReplyDialog {

    reply = '';

}
