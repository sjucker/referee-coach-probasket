import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

export interface CriteriaHintsDialogData {
    hints: string[];
    title: string;
}

@Component({
    selector: 'app-criteria-hints-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule, MatIconModule],
    template: `
        <h3 mat-dialog-title>{{ data.title }}</h3>
        <mat-dialog-content class="mat-typography">
            <ul>
                @for (hint of data.hints; track hint) {
                    <li>{{ hint }}</li>
                }
            </ul>
        </mat-dialog-content>
    `,
    styles: [],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CriteriaHintsDialog {
    protected readonly data = inject<CriteriaHintsDialogData>(MAT_DIALOG_DATA);
}
