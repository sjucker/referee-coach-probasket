import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatButtonModule} from '@angular/material/button';
import {HttpClient, HttpParams} from '@angular/common/http';
import {DateTime} from 'luxon';
import {saveAs} from 'file-saver';
import {FormsModule} from "@angular/forms";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
    selector: 'app-export',
    imports: [Header, LoadingBar, MatCardModule, MatFormFieldModule, MatInputModule, MatDatepickerModule, MatButtonModule, FormsModule],
    templateUrl: './export.html',
    styleUrl: './export.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExportPage {
    private readonly http = inject(HttpClient);
    private readonly snackBar = inject(MatSnackBar);

    protected readonly exporting = signal(false);
    protected readonly cutoffDate = signal<DateTime>(DateTime.now().minus({year: 1}));

    export() {
        this.exporting.set(true);
        const params = new HttpParams().set('from', this.cutoffDate().toISODate()!);
        this.http.get(`/api/admin/export`, {responseType: 'blob', params}).subscribe({
            next: (response) => {
                this.exporting.set(false);
                saveAs(response, 'export.xlsx');
            },
            error: () => {
                this.exporting.set(false);
                this.snackBar.open("An unexpected error occurred, export could not be created!", undefined, {
                    duration: 3000,
                    horizontalPosition: "center",
                    verticalPosition: "top"
                })
            }
        });
    }
}
