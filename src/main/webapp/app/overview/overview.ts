import {ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {AuthService} from '../auth.service';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {BasketplanGameDTO, CreateRefereeReportDTO, CreateRefereeReportResultDTO, OfficiatingMode, ReportOverviewDTO, ReportSearchResultDTO} from "../../rest";
import {MatFormField} from "@angular/material/form-field";
import {MatInput, MatLabel} from "@angular/material/input";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';
import {toObservable, takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';

interface RefereeSelection {
    id: number,
    name: string
}


@Component({
    selector: 'app-main',
    imports: [MatCardModule, MatButtonModule, Header, LoadingBar, FormsModule, MatFormField, MatLabel, MatInput, MatCheckbox, ReactiveFormsModule, MatSelect, MatOption, MatTableModule, MatPaginatorModule, MatDatepickerModule, MatNativeDateModule],
    templateUrl: './overview.html',
    styleUrl: './overview.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class Overview {
    private readonly fb = inject(FormBuilder);
    private readonly http = inject(HttpClient);
    protected readonly auth = inject(AuthService);
    private readonly snackBar = inject(MatSnackBar);

    protected readonly game = signal<BasketplanGameDTO | null>(null);
    protected readonly videoUrl = signal<string | undefined>(undefined);
    protected readonly videoUrlInputNeeded = signal(false);
    protected readonly textOnlyMode = signal(false);
    protected readonly referees = signal<RefereeSelection[]>([]);
    protected readonly referee = signal<RefereeSelection | null>(null);


    protected readonly fromDate = signal<Date>(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000));
    protected readonly toDate = signal<Date>(new Date());
    protected readonly textFilter = signal<string>('');
    protected readonly pageIndex = signal(0);
    protected readonly pageSize = signal(10);

    protected readonly reports = signal<ReportOverviewDTO[]>([]);
    protected readonly totalReports = signal(0);
    protected readonly tableLoading = signal(false);
    protected readonly tableError = signal<string | null>(null);

    protected readonly loading = computed(() => this.searching() || this.creating() || this.tableLoading());
    protected readonly creating = signal(false);
    protected readonly error = signal<string | null>(null);
    protected readonly searching = signal(false);
    protected readonly problemDescription = signal<string | null>(null);

    protected readonly form = this.fb.nonNullable.group({
        gameNumber: ['', [Validators.required]],
    });

    constructor() {
        // Auto load on date range changes
        effect(() => {
            // react to from/to changes
            const _from = this.fromDate();
            const _to = this.toDate();
            // reset to first page
            this.pageIndex.set(0);
            // Call outside of effect tracking to avoid tracking textFilter/pageIndex/pageSize
            untracked(() => this.loadReports());
        });

        // Debounced text filter changes
        toObservable(this.textFilter)
            .pipe(
                skip(1), // skip initial emission to avoid duplicate initial load
                debounceTime(1000),
                distinctUntilChanged(),
                takeUntilDestroyed()
            )
            .subscribe(() => {
                this.pageIndex.set(0);
                this.loadReports();
            });
    }

    loadReports(): void {
        this.tableError.set(null);
        this.tableLoading.set(true);

        const fromIso = this.toIsoDate(this.fromDate());
        const toIso = this.toIsoDate(this.toDate());
        const filter = this.textFilter();
        const page = this.pageIndex();
        const pagesize = this.pageSize();

        let params = new HttpParams()
            .set('from-date', fromIso)
            .set('to-date', toIso)
            .set('textfilter', filter)
            .set('page', page)
            .set('pagesize', pagesize)
            // backend compatibility
            .set('from', fromIso)
            .set('to', toIso)
            .set('filter', filter)
            .set('pageSize', pagesize);

        this.http.get<ReportSearchResultDTO>('/api/report', {params}).subscribe({
            next: result => {
                this.reports.set(result.items);
                this.totalReports.set(result.total);
                this.tableLoading.set(false);
            },
            error: () => {
                this.tableLoading.set(false);
                this.tableError.set('An error occurred.');
                this.reports.set([]);
                this.totalReports.set(0);
            }
        });
    }

    onPageChange(event: PageEvent): void {
        this.pageIndex.set(event.pageIndex);
        this.pageSize.set(event.pageSize);
        this.loadReports();
    }

    private toIsoDate(d: Date): string {
        const year = d.getFullYear();
        const month = (d.getMonth() + 1).toString().padStart(2, '0');
        const day = d.getDate().toString().padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    searchGame(): void {
        this.problemDescription.set(null);

        if (this.form.invalid) return;

        const {gameNumber} = this.form.getRawValue();

        this.searching.set(true);

        this.http.get<BasketplanGameDTO>(`/api/basketplan/${gameNumber}`).subscribe({
            next: game => {
                if (game.referee1Id && game.referee2Id
                    && (game.officiatingMode === OfficiatingMode.OFFICIATING_2PO || game.referee3Id)) {

                    this.game.set(game);
                    this.videoUrl.set(game.videoUrl);

                    this.referees.set([
                        {id: game.referee1Id, name: game.referee1Name!},
                        {id: game.referee2Id, name: game.referee2Name!}
                    ]);

                    if (game.officiatingMode === OfficiatingMode.OFFICIATING_3PO && game.referee3Id) {
                        this.referees.set([
                            {id: game.referee1Id, name: game.referee1Name!},
                            {id: game.referee2Id, name: game.referee2Name!},
                            {id: game.referee3Id, name: game.referee3Name!},
                        ]);
                    }
                    this.videoUrlInputNeeded.set(!game.videoUrl);
                } else {
                    this.problemDescription.set('At least one referee not available in database');
                }
                this.searching.set(false);
            },
            error: error => {
                if (error.status === 404) {
                    this.problemDescription.set('No game found for given game number');
                } else {
                    this.problemDescription.set('An unexpected error occurred');
                }
                this.searching.set(false);
            }
        });
    }

    createRefereeReport() {
        if (this.game() && (this.textOnlyMode() || this.videoUrl()) && this.referee()) {
            this.creating.set(true);
            const request: CreateRefereeReportDTO = {
                gameNumber: this.game()!.gameNumber,
                reporteeId: this.referee()!.id,
                videoUrl: this.videoUrl()
            };
            this.http.post<CreateRefereeReportResultDTO>('/api/report/referee', request).subscribe({
                next: response => {
                    this.creating.set(false);
                    this.snackBar.open(response.externalId);
                    // TODO
                    // this.edit(response.id, ReportType.COACHING);
                },
                error: () => {
                    this.creating.set(false);
                    this.snackBar.open("An unexpected error occurred, report could not be created.", undefined, {
                        duration: 3000,
                        horizontalPosition: "center",
                        verticalPosition: "top"
                    })
                }
            })
        } else {
            this.snackBar.open("Please search for a game or select a referee", undefined, {
                duration: 3000,
                horizontalPosition: "center",
                verticalPosition: "top"
            });
        }

    }
}
