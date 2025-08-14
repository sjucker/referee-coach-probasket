import {ChangeDetectionStrategy, Component, computed, effect, inject, signal} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {CriteriaState, OfficiatingMode, RefereeReportDTO, ReportCommentDTO} from '../../rest';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {DatePipe, DecimalPipe, NgClass} from "@angular/common";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatInput} from "@angular/material/input";
import {CdkTextareaAutosize} from "@angular/cdk/text-field";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {MatTooltipModule} from "@angular/material/tooltip";
import {Score} from "../components/score/score.component";
import {MatIconModule} from "@angular/material/icon";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {CriteriaHintsDialog} from "./criteria-hints-dialog/criteria-hints-dialog";

@Component({
    selector: 'app-edit',
    imports: [Header, LoadingBar, MatCardModule, MatButtonModule, NgClass, DatePipe, MatFormFieldModule, FormsModule, MatInput, CdkTextareaAutosize, MatRadioGroup, MatRadioButton, MatTooltipModule, Score, DecimalPipe, MatIconModule, MatDialogModule],
    templateUrl: './edit.html',
    styleUrl: './edit.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditPage {
    private readonly dialog = inject(MatDialog);
    private readonly http = inject(HttpClient);
    private readonly route = inject(ActivatedRoute);

    protected readonly OfficiatingMode = OfficiatingMode;

    protected readonly externalId = signal<string | null>(null);
    protected readonly report = signal<RefereeReportDTO | null>(null);
    protected readonly loading = signal<boolean>(true);
    protected readonly error = signal<string | null>(null);
    protected readonly unsavedChanges = signal(false);
    protected readonly saving = signal(false);
    protected readonly saveEnabled = computed(() => !this.saving() && this.unsavedChanges());
    protected readonly showLoadingBar = computed(() => this.saving() || this.loading());

    constructor() {
        effect(() => {
            const eid = this.route.snapshot.paramMap.get('externalId');
            this.externalId.set(eid);
            if (!eid) {
                this.loading.set(false);
                this.error.set('Missing externalId route parameter.');
                this.report.set(null);
                return;
            }
            this.fetchReport(eid);
        });
    }

    private fetchReport(externalId: string) {
        this.loading.set(true);
        this.error.set(null);
        this.http.get<RefereeReportDTO>(`/api/report/referee/${encodeURIComponent(externalId)}`).subscribe({
            next: (res) => {
                this.report.set(res);
                this.loading.set(false);
            },
            error: (err) => {
                this.loading.set(false);
                if (err?.status === 404) {
                    this.error.set('Report not found');
                } else if (err?.status === 403) {
                    this.error.set('You are not allowed to access this report');
                } else {
                    this.error.set('An unexpected error occurred');
                }
                this.report.set(null);
            }
        });
    }

    onChange() {
        this.unsavedChanges.set(true);
    }

    protected readonly CriteriaState = CriteriaState;

    average(): number {
        const length = this.report()!.comments.filter(comment => comment.scoreRequired).length;
        const totalScore = this.report()!.comments
            .filter(comment => comment.scoreRequired)
            .map(comment => comment.score ?? 0)
            .reduce((a, b) => a + b, 0);
        return totalScore / length;
    }

    // TODO handle unsaved changes

    save() {
        const report = this.report()!;
        this.saving.set(true);
        this.http.put<void>(`/api/report/referee/${encodeURIComponent(report.externalId)}`, report).subscribe({
            next: () => {
                this.unsavedChanges.set(false);
                this.saving.set(false);
            },
            error: () => {
                this.saving.set(false);
                this.error.set('An unexpected error occurred');
            }
        });
    }

    openCriteriaHints(comment: ReportCommentDTO) {
        this.dialog.open(CriteriaHintsDialog, {
            data: {
                hints: comment.criteriaHints,
                title: comment.typeDescription
            }
        });
    }

    displayRatings(): boolean {
        return this.report()!.comments.some(comment => comment.scoreRequired);
    }
}
