import {AfterViewInit, ChangeDetectionStrategy, Component, computed, effect, ElementRef, HostListener, inject, OnDestroy, OnInit, signal, viewChild} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {CriteriaState, OfficiatingMode, RefereeReportDTO, ReportCommentDTO, ReportType, ReportVideoCommentDTO} from '../../rest';
import {PATH_VIEW} from '../app.routes';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {DecimalPipe} from "@angular/common";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatInput} from "@angular/material/input";
import {CdkTextareaAutosize} from "@angular/cdk/text-field";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {MatTooltipModule} from "@angular/material/tooltip";
import {Score} from "../components/score/score.component";
import {MatIconModule} from "@angular/material/icon";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {CriteriaHintsDialog} from "./criteria-hints-dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {HasUnsavedChanges} from "../can-deactivate.guard";
import {FinishRefereeReportDialog} from "./finish-referee-report-dialog";
import {GameInfo} from "../components/game-info/game-info";
import {YouTubePlayer} from "@angular/youtube-player";
import {MatCheckbox} from "@angular/material/checkbox";

@Component({
    selector: 'app-edit',
    imports: [Header, LoadingBar, MatCardModule, MatButtonModule, MatFormFieldModule, FormsModule, MatInput, CdkTextareaAutosize, MatRadioGroup, MatRadioButton, MatTooltipModule, Score, DecimalPipe, MatIconModule, MatDialogModule, GameInfo, YouTubePlayer, MatCheckbox],
    templateUrl: './edit.html',
    styleUrl: './edit.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditPage implements HasUnsavedChanges, AfterViewInit, OnInit, OnDestroy {
    private readonly dialog = inject(MatDialog);
    private readonly http = inject(HttpClient);
    private readonly route = inject(ActivatedRoute);
    private readonly snackBar = inject(MatSnackBar);
    private readonly router = inject(Router);

    protected readonly OfficiatingMode = OfficiatingMode;
    protected readonly CriteriaState = CriteriaState;
    protected readonly ReportType = ReportType;

    readonly unsavedChanges = signal(false);

    protected readonly externalId = signal<string | null>(null);
    protected readonly report = signal<RefereeReportDTO | null>(null);
    protected readonly loading = signal<boolean>(true);
    protected readonly saving = signal(false);
    protected readonly showLoadingBar = computed(() => this.saving() || this.loading());

    protected readonly videoWidth = signal<number | null>(null);
    protected readonly videoHeight = signal<number | null>(null);

    protected readonly youtube = viewChild<YouTubePlayer>('youtubePlayer');
    protected readonly widthMeasurement = viewChild<ElementRef<HTMLDivElement>>('widthMeasurement');
    protected readonly videoCommentsContainer = viewChild<ElementRef<HTMLDivElement>>('videoCommentsContainer');

    @HostListener('window:beforeunload', ['$event'])
    handleClose($event: BeforeUnloadEvent) {
        if (this.unsavedChanges()) {
            $event.preventDefault();
        }
    }

    constructor() {
        effect(() => {
            const eid = this.route.snapshot.paramMap.get('externalId');
            this.externalId.set(eid);
            if (!eid) {
                this.loading.set(false);
                this.report.set(null);
                return;
            }
            this.fetchReport(eid);
        });
    }

    ngOnInit(): void {
        // This code loads the IFrame Player API code asynchronously, according to the instructions at
        // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        document.body.appendChild(tag);
    }

    ngAfterViewInit(): void {
        window.addEventListener('resize', this.onResize);
        setTimeout(() => {
            // wait some time until everything is ready (e.g., scrollbar)
            this.onResize();
        }, 200)
    }

    onResize = (): void => {
        // margin and padding each 16px on both sides
        const contentWidth = this.widthMeasurement()!.nativeElement.scrollWidth - (4 * 16);

        this.videoWidth.set(Math.min(contentWidth, 720));
        this.videoHeight.set(this.videoWidth()! * 0.6);
    }

    ngOnDestroy(): void {
        window.removeEventListener('resize', this.onResize);
    }

    private fetchReport(externalId: string) {
        this.loading.set(true);
        this.http.get<RefereeReportDTO>(`/api/report/referee/${encodeURIComponent(externalId)}`).subscribe({
            next: (res) => {
                this.report.set(res);
                this.loading.set(false);
                if (res.finished) {
                    // Forward to read-only view if report is already finished
                    this.router.navigate([PATH_VIEW, res.externalId]).catch(err => console.error(err));
                }
            },
            error: (err) => {
                this.loading.set(false);
                if (err?.status === 404) {
                    this.displaySnackbar('Report not found');
                } else if (err?.status === 403) {
                    this.displaySnackbar('You are not allowed to access this report');
                } else {
                    this.displaySnackbar('An unexpected error occurred');
                }
                this.report.set(null);
            }
        });
    }

    onChange() {
        this.unsavedChanges.set(true);
    }

    average(): number {
        const length = this.report()!.comments.filter(comment => comment.scoreRequired).length;
        const totalScore = this.report()!.comments
            .filter(comment => comment.scoreRequired)
            .map(comment => comment.score ?? 0)
            .reduce((a, b) => a + b, 0);
        return totalScore / length;
    }

    save() {
        const report = this.report()!;
        this.saving.set(true);
        this.http.put<void>(`/api/report/referee/${report.externalId}`, report).subscribe({
            next: () => {
                this.unsavedChanges.set(false);
                this.saving.set(false);
                this.displaySnackbar('Saved successfully!');
            },
            error: () => {
                this.saving.set(false);
                this.displaySnackbar('An unexpected error occurred');
            }
        });
    }

    finish() {
        if (!this.isCriteriaValid()) {
            this.displaySnackbar("Report is not yet completed, please add a comment for each criteria.")
            return;
        }

        // TODO flag at least for comments which require a reply from referee?

        this.dialog.open(FinishRefereeReportDialog).afterClosed().subscribe({
            next: decision => {
                if (decision) {
                    this.saving.set(true);
                    this.http.post<void>(`/api/report/referee/${this.report()!.externalId}/finish`, {}).subscribe({
                        next: () => {
                            this.saving.set(false);
                            this.unsavedChanges.set(false);
                            this.view();
                        },
                        error: () => {
                            this.saving.set(false);
                            this.displaySnackbar("An unexpected error occurred, report could not be finished.");
                        }
                    });
                }
            },
        });
    }

    view() {
        this.router.navigate([PATH_VIEW, this.report()!.externalId]).catch(err => console.error(err));
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

    private displaySnackbar(message: string) {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "top"
        });
    }

    private isCriteriaValid() {
        return this.report()!.comments.every(comment => !!comment.comment && comment.comment.length > 0);
    }


    jumpTo(time: number): void {
        this.youtube()!.seekTo(time, true);
        this.youtube()!.playVideo();
    }

    addVideoComment(): void {
        this.report()!.videoComments.push({
            comment: '',
            requiresReply: false,
            timestampInSeconds: Math.round(this.youtube()!.getCurrentTime()),
            createdAt: '',
            createdBy: '',
            createdById: 0,
            reference: false,
            replies: [],
            tags: []
        });

        setTimeout(() => {
            const videoCommentsContainer = this.videoCommentsContainer();
            if (videoCommentsContainer) {
                videoCommentsContainer.nativeElement.scrollTop = videoCommentsContainer.nativeElement.scrollHeight;
            }
        }, 200);
    }

    deleteComment(videoComment: ReportVideoCommentDTO) {
        this.onChange();
        const report = this.report()!;
        report.videoComments.splice(report.videoComments.indexOf(videoComment), 1);
    }

    copyComment(videoComment: ReportVideoCommentDTO) {
        console.debug(videoComment);
        // TODO
        // this.dialog.open(VideoReportCopyDialogComponent, {
        //     data: {
        //         reportee: this.report!.reportee,
        //         referee1: this.report!.otherReportees.indexOf(Reportee.FIRST_REFEREE) >= 0 ? this.report!.basketplanGame.referee1 : null,
        //         referee2: this.report!.otherReportees.indexOf(Reportee.SECOND_REFEREE) >= 0 ? this.report!.basketplanGame.referee2 : null,
        //         referee3: this.report!.otherReportees.indexOf(Reportee.THIRD_REFEREE) >= 0 ? this.report!.basketplanGame.referee3 : null,
        //
        //         title: 'Copy Comment to other Report',
        //         description: 'This will create the same comment in the report for selected referee.'
        //     } as VideoReportCopyDialogData
        // }).afterClosed().subscribe((reportee?: Reportee) => {
        //     if (reportee) {
        //         this.videoReportService.copyVideoComment(videoComment, reportee).subscribe({
        //             next: () => {
        //                 this.showMessage("Successfully copied!");
        //             },
        //             error: () => {
        //                 this.showMessage("An unexpected error occurred, comment could not be copied.");
        //             }
        //         })
        //     }
        // });
    }

    isCopyCommentVisible(videoComment: ReportVideoCommentDTO): boolean {
        console.debug(videoComment);
        return false;
        // TODO
        // return this.report!.otherReportees.length > 0 && !!videoComment.id
    }
}
