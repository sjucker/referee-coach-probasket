import {AfterViewInit, Component, computed, effect, ElementRef, HostListener, inject, OnDestroy, OnInit, signal, viewChild} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {
    CreateRefereeReportDiscussionReplyDTO,
    NewReportVideoCommentDTO,
    NewReportVideoCommentReplyDTO,
    RefereeReportDTO,
    ReportType,
    ReportVideoCommentDTO,
    ReportVideoCommentReplyDTO
} from '../../rest';
import {HasUnsavedChanges} from "../can-deactivate.guard";
import {GameInfo} from "../components/game-info/game-info";
import {YouTubePlayer} from "@angular/youtube-player";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {PATH_OVERVIEW, PATH_VIEW} from "../app.routes";
import {MatSnackBar} from "@angular/material/snack-bar";
import {DatePipe, NgClass} from "@angular/common";
import {CdkTextareaAutosize} from "@angular/cdk/text-field";
import {MatInput} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {ReplyDialog} from "./reply-dialog";
import {DateTime} from "luxon";
import {FinishReplyDialog, FinishReplyDialogData} from "./finish-reply-dialog";

@Component({
    selector: 'app-discuss',
    imports: [Header, LoadingBar, GameInfo, MatButton, MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle, MatIconModule, MatTooltipModule, YouTubePlayer, NgClass, DatePipe, MatFormFieldModule, CdkTextareaAutosize, MatInput, FormsModule, MatIconButton],
    templateUrl: './discuss.html',
    styleUrl: './discuss.scss',
})
export class DiscussPage implements OnInit, OnDestroy, AfterViewInit, HasUnsavedChanges {
    private readonly http = inject(HttpClient);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly snackBar = inject(MatSnackBar);
    private readonly dialog = inject(MatDialog);

    protected readonly ReportType = ReportType;

    readonly unsavedChanges = signal(false);

    protected readonly externalId = signal<string | null>(null);
    protected readonly report = signal<RefereeReportDTO | null>(null);
    protected readonly loading = signal<boolean>(false);
    protected readonly saving = signal<boolean>(false);
    protected readonly showLoadingBar = computed(() => this.loading() || this.saving());

    readonly youtube = viewChild<YouTubePlayer>('youtubePlayer');
    readonly widthMeasurement = viewChild<ElementRef<HTMLDivElement>>('widthMeasurement');
    protected readonly videoCommentsContainer = viewChild<ElementRef<HTMLDivElement>>('videoCommentsContainer');

    protected readonly videoWidth = signal<number | null>(null);
    protected readonly videoHeight = signal<number | null>(null);

    newReplies = signal<NewReportVideoCommentReplyDTO[]>([]);
    newComments = signal<NewReportVideoCommentDTO[]>([]);

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

    play(time: number): void {
        this.youtube()!.seekTo(time, true);
        this.youtube()!.playVideo();
    }

    private fetchReport(externalId: string): void {
        this.loading.set(true);
        this.http.get<RefereeReportDTO>(`/api/report/referee/${encodeURIComponent(externalId)}`).subscribe({
            next: (res) => {
                if (!res.finished) {
                    this.router.navigate([PATH_OVERVIEW]).catch(err => console.error(err));
                }
                this.report.set(res);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.report.set(null);
                this.router.navigate([PATH_OVERVIEW]).catch(err => console.error(err));
            }
        });
    }

    view() {
        this.router.navigate([PATH_VIEW, this.report()!.externalId]).catch(err => console.error(err));
    }

    addVideoComment() {
        const timestampInSeconds = Math.round(this.youtube()!.getCurrentTime());
        const dto = this.report()!;

        if (dto.videoComments.some(comment => timestampInSeconds >= comment.timestampInSeconds - 3 && timestampInSeconds <= comment.timestampInSeconds + 3)) {
            this.displaySnackbar('There is already an existing comment around this timestamp');
        } else {
            this.newComments.update(values => {
                return [...values, {
                    comment: '',
                    timestampInSeconds: timestampInSeconds,
                }];
            });

            setTimeout(() => {
                const videoCommentsContainer = this.videoCommentsContainer();
                if (videoCommentsContainer) {
                    videoCommentsContainer.nativeElement.scrollTop = videoCommentsContainer.nativeElement.scrollHeight;
                }
            }, 200);
        }
    }

    private displaySnackbar(message: string) {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "top"
        });
    }

    requiresReply(videoComment: ReportVideoCommentDTO): boolean {
        const dto = this.report()!;
        return dto.userIsReportee && videoComment.requiresReply && !videoComment.replies.some(c => c.createdById == dto.reporteeId);
    }

    flaggedForReply(videoComment: ReportVideoCommentDTO) {
        const dto = this.report()!;
        return !dto.userIsReportee && videoComment.requiresReply;
    }

    deleteReply(videoComment: ReportVideoCommentDTO, reply: ReportVideoCommentReplyDTO) {
        videoComment.replies = videoComment.replies.filter(r => r !== reply);
        this.newReplies.update(values => {
            return values.filter(r => !(r.commentId === videoComment.id && r.reply === reply.reply));
        });
    }

    reply(videoComment: ReportVideoCommentDTO) {
        this.dialog.open<ReplyDialog, unknown, string>(ReplyDialog, {
            disableClose: true,
            hasBackdrop: true,
            width: '500px',
        }).afterClosed().subscribe(reply => {
            const dto = this.report()!;
            if (reply) {
                this.newReplies.update(values => {
                    return [...values, {
                        commentId: videoComment.id!,
                        reply: reply,
                    }];
                });

                videoComment.replies.push({
                    id: 0,
                    reply: reply,
                    createdBy: 'New Reply',
                    createdAt: DateTime.now().toISODate(),
                    createdById: dto.reporteeId,
                });

                this.unsavedChanges.set(true);
            }
        });
    }

    onChange() {
        this.unsavedChanges.set(true);
    }

    finishReply() {
        const dto = this.report()!;
        this.dialog.open<FinishReplyDialog, FinishReplyDialogData, boolean>(FinishReplyDialog, {
            data: {
                totalReplies: this.newReplies().length,
                requiredRepliesRemaining: dto.videoComments.filter(comment => this.requiresReply(comment)).length,
            },
            disableClose: true,
            hasBackdrop: true,
        }).afterClosed().subscribe(decision => {
            if (decision) {
                this.saving.set(true);
                const body: CreateRefereeReportDiscussionReplyDTO = {
                    replies: this.newReplies(),
                    comments: this.newComments()
                };
                this.http.post<void>(`/api/report/referee/${dto.externalId}/discussion`, body).subscribe({
                    next: () => {
                        this.saving.set(false);
                        this.newReplies.set([]);
                        this.newComments.set([]);
                        this.unsavedChanges.set(false);
                        this.displaySnackbar('Replies saved!');
                    },
                    error: () => {
                        this.saving.set(false);
                        this.displaySnackbar('An unexpected error occurred');
                    }
                });
            }
        })
    }
}
