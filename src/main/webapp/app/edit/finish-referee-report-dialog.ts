import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {RefereeReportDTO} from "../../rest";

@Component({
    selector: 'app-finish-referee-report-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule],
    template: `
        <h2 mat-dialog-title>{{ data.internal ? 'Finish Internal Coaching' : 'Finish Coaching' }}</h2>
        <mat-dialog-content class="mat-typography">
            <p>Do you really want to finish this coaching?</p>
            @if (!data.internal) {
                <p>An email will be sent to the referee and the coaching cannot be edited anymore.</p>
            }
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="false">Cancel</button>
            <button mat-flat-button [mat-dialog-close]="true">Finish</button>
        </mat-dialog-actions>
    `,
    styles: []
})
export class FinishRefereeReportDialog {

    data = inject<RefereeReportDTO>(MAT_DIALOG_DATA);

}
