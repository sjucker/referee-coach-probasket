import {Component, inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatOptionModule} from "@angular/material/core";
import {RefereeDTO} from "../../rest";

export interface CopyReportDialogData {
    title: string,
    description: string,
    reporteeId: number,
    referee1?: RefereeDTO,
    referee2?: RefereeDTO,
    referee3?: RefereeDTO,
}

@Component({
    selector: 'app-copy-report-dialog',
    standalone: true,
    imports: [MatDialogModule, MatFormFieldModule, MatSelectModule, MatOptionModule, MatButtonModule],
    template: `
        <h2 mat-dialog-title>{{ title }}</h2>
        <mat-dialog-content class="mat-typography">
            <mat-form-field appearance="fill">
                <mat-label>Select Referee</mat-label>
                <mat-select [(value)]="selectedReferee">
                    @for (r of referees; track r.id) {
                        <mat-option [value]="r">{{ r.name }}</mat-option>
                    }
                </mat-select>
            </mat-form-field>
            <p>{{ description }}</p>
        </mat-dialog-content>
        <mat-dialog-actions align="end">
            <button mat-stroked-button [mat-dialog-close]="null">Cancel</button>
            <button mat-flat-button [mat-dialog-close]="selectedReferee">Copy</button>
        </mat-dialog-actions>
    `,
    styles: []
})
export class CopyReportDialog implements OnInit {
    data = inject<CopyReportDialogData>(MAT_DIALOG_DATA);

    title = '';
    description = '';

    selectedReferee?: RefereeDTO;
    referees: RefereeDTO[] = [];

    ngOnInit(): void {
        this.title = this.data.title;
        this.description = this.data.description;

        if (this.data.referee1 && this.data.referee1.id !== this.data.reporteeId) {
            this.referees.push(this.data.referee1);
        }
        if (this.data.referee2 && this.data.referee2.id !== this.data.reporteeId) {
            this.referees.push(this.data.referee2);
        }
        if (this.data.referee3 && this.data.referee3.id !== this.data.reporteeId) {
            this.referees.push(this.data.referee3);
        }
        this.selectedReferee = this.referees[0];
    }

}
