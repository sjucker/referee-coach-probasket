import {ChangeDetectionStrategy, Component, computed, inject, OnInit, signal} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {AuthService} from '../auth.service';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {
    BasketplanGameDTO,
    CopyRefereeReportDTO,
    CreateRefereeReportDTO,
    CreateRefereeReportResultDTO,
    OfficiatingMode,
    RefereeDTO,
    ReportOverviewDTO,
    ReportSearchResultDTO,
    ReportType
} from "../../rest";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule, MatLabel} from "@angular/material/input";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSortModule, Sort} from '@angular/material/sort';
import {takeUntilDestroyed, toObservable} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {PATH_EDIT, PATH_VIEW} from "../app.routes";
import {Router} from "@angular/router";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {DateTime} from "luxon";
import {MatIcon} from "@angular/material/icon";
import {DatePipe} from "@angular/common";
import {MatDialog} from "@angular/material/dialog";
import {CopyReportDialog, CopyReportDialogData} from "./copy-report-dialog";
import {ConfirmDeleteDialog} from "./confirm-delete-dialog";
import {MatTooltipModule} from "@angular/material/tooltip";

interface RefereeSelection {
    id: number,
    name: string
}

@Component({
    selector: 'app-main',
    imports: [MatCardModule, MatButtonModule, Header, LoadingBar, FormsModule, MatLabel, MatCheckbox, ReactiveFormsModule, MatSelect, MatOption, MatTableModule, MatSortModule, MatPaginatorModule, MatDatepickerModule, MatFormFieldModule, MatInputModule, MatIcon, DatePipe, MatTooltipModule],
    templateUrl: './overview.html',
    styleUrl: './overview.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class Overview implements OnInit {
    private readonly dialog = inject(MatDialog);
    private readonly fb = inject(FormBuilder);
    private readonly http = inject(HttpClient);
    private readonly router = inject(Router);
    protected readonly auth = inject(AuthService);
    private readonly snackBar = inject(MatSnackBar);

    private static readonly STORAGE_KEY = 'overviewSearchParams';

    protected readonly ReportType = ReportType;

    protected readonly game = signal<BasketplanGameDTO | null>(null);
    protected readonly videoUrl = signal<string | undefined>(undefined);
    protected readonly videoUrlInputNeeded = signal(false);
    protected readonly textOnlyMode = signal(false);
    protected readonly referees = signal<RefereeSelection[]>([]);
    protected readonly referee = signal<RefereeSelection | null>(null);
    protected readonly internal = signal(false);

    protected readonly fromDate = signal<DateTime>(this.getDefaultFromDate());
    protected readonly toDate = signal<DateTime>(this.getDefaultToDate());
    protected readonly textFilter = signal<string>('');
    protected readonly pageIndex = signal(0);
    protected readonly pageSize = signal(10);

    protected readonly sortBy = signal<string>('date');
    protected readonly sortOrder = signal<'asc' | 'desc'>('desc');

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

    ngOnInit(): void {
        this.restoreSearchFromSession();
        this.loadReports();
    }

    private saveSearchToSession(): void {
        const state = {
            from: this.fromDate().toISODate(),
            to: this.toDate().toISODate(),
            filter: this.textFilter(),
            page: this.pageIndex(),
            pageSize: this.pageSize(),
            sortBy: this.sortBy(),
            sortOrder: this.sortOrder()
        };
        sessionStorage.setItem(Overview.STORAGE_KEY, JSON.stringify(state));
    }

    private restoreSearchFromSession(): void {
        const raw = sessionStorage.getItem(Overview.STORAGE_KEY);
        if (!raw) {
            return;
        }
        const parsed = JSON.parse(raw) as { from?: string; to?: string; filter?: string; page?: number; pageSize?: number; sortBy?: string; sortOrder?: 'asc' | 'desc' };

        if (parsed.from) {
            const f = DateTime.fromISO(parsed.from);
            if (f.isValid) {
                this.fromDate.set(f);
            }
        }
        if (parsed.to) {
            const t = DateTime.fromISO(parsed.to);
            if (t.isValid) {
                this.toDate.set(t);
            }
        }
        if (typeof parsed.filter === 'string') {
            this.textFilter.set(parsed.filter);
        }
        if (typeof parsed.page === 'number' && parsed.page >= 0) {
            this.pageIndex.set(parsed.page);
        }
        if (typeof parsed.pageSize === 'number' && parsed.pageSize > 0) {
            this.pageSize.set(parsed.pageSize);
        }
        if (typeof parsed.sortBy === 'string') {
            this.sortBy.set(parsed.sortBy);
        }
        if (parsed.sortOrder === 'asc' || parsed.sortOrder === 'desc') {
            this.sortOrder.set(parsed.sortOrder);
        }
    }

    loadReports(): void {
        this.tableError.set(null);
        this.tableLoading.set(true);

        // persist current search params on every load
        this.saveSearchToSession();

        const fromIso = this.fromDate().toISODate()
        const toIso = this.toDate().toISODate()
        const filter = this.textFilter();
        const page = this.pageIndex();
        const pagesize = this.pageSize();

        const params = new HttpParams()
            .set('from', fromIso!)
            .set('to', toIso!)
            .set('filter', filter)
            .set('page', page)
            .set('pageSize', pagesize)
            .set('sortBy', this.sortBy())
            .set('sortOrder', this.sortOrder());

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

    onMatSortChange($event: Sort) {
        if ($event.direction === '') {
            this.sortOrder.set('desc');
            this.sortBy.set('date');
        } else {
            this.sortOrder.set($event.direction);
            this.sortBy.set($event.active);
        }
        this.pageIndex.set(0);
        this.loadReports();
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
                videoUrl: this.videoUrl(),
                internal: this.internal()
            };
            this.http.post<CreateRefereeReportResultDTO>('/api/report/referee', request).subscribe({
                next: response => {
                    this.creating.set(false);
                    this.edit(response.externalId);
                },
                error: err => {
                    this.creating.set(false);
                    let errorMessage;
                    if (err.status === 400) {
                        errorMessage = "Invalid video URL provided.";
                    } else {
                        errorMessage = "An unexpected error occurred, report could not be created.";
                    }
                    this.snackBar.open(errorMessage, undefined, {
                        duration: 3000,
                        horizontalPosition: "center",
                        verticalPosition: "top"
                    });
                }
            });
        } else {
            this.snackBar.open("Please search for a game or select a referee", undefined, {
                duration: 3000,
                horizontalPosition: "center",
                verticalPosition: "top"
            });
        }

    }

    public edit(externalId: string) {
        this.router.navigate([PATH_EDIT, externalId]).catch(err => console.error(err))
    }


    view(dto: ReportOverviewDTO) {
        this.router.navigate([PATH_VIEW, dto.externalId]).catch(err => console.error(err))
    }

    copy(dto: ReportOverviewDTO) {
        this.dialog.open<CopyReportDialog, CopyReportDialogData, RefereeDTO>(CopyReportDialog, {
            data: {
                reporteeId: dto.reporteeId,
                referee1: dto.referee1,
                referee2: dto.referee2,
                referee3: dto.referee3,
                title: 'Copy Report',
                description: 'A new report will be created containing all comments from the existing source report.'
            }
        }).afterClosed().subscribe((referee) => {
            if (referee) {
                const request: CopyRefereeReportDTO = {
                    reporteeId: referee.id,
                };
                this.http.post<CreateRefereeReportResultDTO>(`/api/report/referee/${dto.externalId}/copy`, request).subscribe({
                    next: response => {
                        this.creating.set(false);
                        this.edit(response.externalId);
                    },
                    error: () => {
                        this.creating.set(false);
                        this.snackBar.open("An unexpected error occurred, report could not be copied.", undefined, {
                            duration: 3000,
                            horizontalPosition: "center",
                            verticalPosition: "top"
                        });
                    }
                });
            }
        });
    }

    delete(dto: ReportOverviewDTO) {
        this.dialog.open(ConfirmDeleteDialog, {
            data: {
                title: 'Confirm Deletion',
                message: `Delete report for ${dto.reportee} (game ${dto.gameNumber})?`,
            }
        }).afterClosed().subscribe((confirmed: boolean) => {
            if (confirmed) {
                this.tableLoading.set(true);
                this.http.delete<void>(`/api/report/referee/${dto.externalId}`).subscribe({
                    next: () => {
                        this.snackBar.open('Report deleted', undefined, {
                            duration: 3000,
                            horizontalPosition: 'center',
                            verticalPosition: 'top'
                        });
                        this.loadReports();
                    },
                    error: (err) => {
                        this.tableLoading.set(false);
                        const msg = err?.status === 403 ? 'Not allowed to delete this report' : 'Deletion failed';
                        this.snackBar.open(msg, undefined, {
                            duration: 3000,
                            horizontalPosition: 'center',
                            verticalPosition: 'top'
                        });
                    }
                });
            }
        });
    }

    get displayedColumns(): string[] {
        if (this.auth.isRefereeCoach() || this.auth.isTrainerCoach()) {
            return ['finished', 'date', 'gameNumber', 'competition', 'teams', 'coach', 'type', 'reportee', 'edit', 'view', 'copy', 'delete'];
        } else {
            return ['date', 'gameNumber', 'competition', 'teams', 'coach', 'type', 'view'];
        }
    }

    isEditable(dto: ReportOverviewDTO) {
        return this.isCoaching(dto) && dto.userIsCoach && !dto.finished;
    }

    isCopyable(dto: ReportOverviewDTO) {
        return this.isCoaching(dto) && dto.userIsCoach;
    }

    isDeletable(dto: ReportOverviewDTO): boolean {
        return this.isCoaching(dto) && (this.isEditable(dto) || this.auth.isAdmin());
    }

    isCoaching(dto: ReportOverviewDTO): boolean {
        return dto.type !== ReportType.GAME_DISCUSSION;
    }

    isCurrentUserCoachOf(dto: ReportOverviewDTO): boolean {
        return this.auth.userId() === dto.coachId;
    }

    private getDefaultFromDate(): DateTime {
        const now = DateTime.now();
        if (now.month > 6) {
            return DateTime.local(now.year, 9, 1);
        } else {
            return DateTime.local(now.year - 1, 9, 1);
        }
    }

    private getDefaultToDate(): DateTime {
        const now = DateTime.now();
        if (now.month > 6) {
            return DateTime.local(now.year + 1, 6, 30);
        } else {
            return DateTime.local(now.year, 6, 30);
        }
    }

}
