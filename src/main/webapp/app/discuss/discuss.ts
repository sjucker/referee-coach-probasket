import {AfterViewInit, ChangeDetectionStrategy, Component, computed, effect, ElementRef, inject, OnDestroy, OnInit, signal, viewChild} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {ActivatedRoute, Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {RefereeReportDTO, ReportType, ReportVideoCommentDTO, ReportVideoCommentReplyDTO} from '../../rest';
import {HasUnsavedChanges} from "../can-deactivate.guard";
import {GameInfo} from "../components/game-info/game-info";
import {YouTubePlayer} from "@angular/youtube-player";
import {MatButton} from "@angular/material/button";
import {MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {PATH_OVERVIEW, PATH_VIEW} from "../app.routes";
import {MatSnackBar} from "@angular/material/snack-bar";
import {AuthService} from "../auth.service";
import {DatePipe, NgClass} from "@angular/common";
import {CdkTextareaAutosize} from "@angular/cdk/text-field";
import {MatInput} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-discuss',
    imports: [Header, LoadingBar, GameInfo, MatButton, MatCard, MatCardActions, MatCardContent, MatCardHeader, MatCardTitle, MatIconModule, MatTooltipModule, YouTubePlayer, NgClass, DatePipe, MatFormFieldModule, CdkTextareaAutosize, MatInput, FormsModule],
    templateUrl: './discuss.html',
    styleUrl: './discuss.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DiscussPage implements OnInit, OnDestroy, AfterViewInit, HasUnsavedChanges {
    private readonly http = inject(HttpClient);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly snackBar = inject(MatSnackBar);
    private readonly auth = inject(AuthService);

    readonly unsavedChanges = signal(false);

    protected readonly externalId = signal<string | null>(null);
    protected readonly report = signal<RefereeReportDTO | null>(null);
    protected readonly loading = signal<boolean>(true);
    protected readonly showLoadingBar = computed(() => this.loading());

    readonly youtube = viewChild<YouTubePlayer>('youtubePlayer');
    readonly widthMeasurement = viewChild<ElementRef<HTMLDivElement>>('widthMeasurement');
    protected readonly videoCommentsContainer = viewChild<ElementRef<HTMLDivElement>>('videoCommentsContainer');

    protected readonly videoWidth = signal<number | null>(null);
    protected readonly videoHeight = signal<number | null>(null);

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

    protected readonly ReportType = ReportType;

    view() {
        this.router.navigate([PATH_VIEW, this.report()!.externalId]).catch(err => console.error(err));
    }

    isReferee(): boolean {
        // TODO not correct, possible that is ref and coach...fix this
        return this.auth.isReferee();
    }

    addVideoComment() {
        const timestampInSeconds = Math.round(this.youtube()!.getCurrentTime());

        if (this.report()!.videoComments.some(comment => timestampInSeconds >= comment.timestampInSeconds - 3 && timestampInSeconds <= comment.timestampInSeconds + 3)) {
            this.displaySnackbar('There is already an existing comment around this timestamp');
        } else {
            this.report()!.videoComments.push({
                comment: '',
                requiresReply: false,
                timestampInSeconds: timestampInSeconds,
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

    }

    private displaySnackbar(message: string) {
        this.snackBar.open(message, undefined, {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "top"
        });
    }

    requiresReply(videoComment: ReportVideoCommentDTO): boolean {
        // TODO fix logic for coach
        return videoComment.requiresReply;
    }

    flaggedForReply(videoComment: ReportVideoCommentDTO) {
        // TODO fix logic for coach
        console.debug(videoComment);
        return false;
    }

    deleteReply(videoComment: ReportVideoCommentDTO, reply: ReportVideoCommentReplyDTO) {
        // TODO
        console.debug(videoComment, reply);
    }

    reply(videoComment: ReportVideoCommentDTO) {
        // TODO
        console.debug(videoComment);
    }
}
