import {Component} from '@angular/core';
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";

@Component({
    selector: 'app-finish-referee-report-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule],
    template: `
        <h2 mat-dialog-title>Finish Report</h2>
        <mat-dialog-content class="mat-typography">
            <p>Do you really want to finish this report?</p>
            <p>An email will be sent to the referee and the report cannot be edited anymore.</p>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="false">Cancel</button>
            <button mat-flat-button [mat-dialog-close]="true">Finish</button>
        </mat-dialog-actions>
    `,
    styles: []
})
export class FinishRefereeReportDialog {
}
