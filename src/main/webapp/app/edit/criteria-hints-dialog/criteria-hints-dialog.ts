import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

export type CriteriaHintsDialogData = {
    hints: readonly string[];
    title: string;
};

@Component({
    selector: 'app-criteria-hints-dialog',
    standalone: true,
    imports: [MatDialogModule, MatButtonModule, MatIconModule],
    templateUrl: './criteria-hints-dialog.html',
    styleUrl: './criteria-hints-dialog.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CriteriaHintsDialog {
    protected readonly data = inject<CriteriaHintsDialogData>(MAT_DIALOG_DATA);
}
