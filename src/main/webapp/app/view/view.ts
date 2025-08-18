import {AfterViewInit, ChangeDetectionStrategy, Component, computed, effect, ElementRef, inject, OnDestroy, OnInit, signal, viewChild} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {CriteriaState, RefereeReportDTO, ReportCommentDTO, ReportType, ReportVideoCommentDTO} from '../../rest';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {GameInfo} from "../components/game-info/game-info";
import {ScoreUtil} from "../util/score-util";
import {YouTubePlayer} from "@angular/youtube-player";
import {NgClass} from "@angular/common";
import {MatTooltipModule} from "@angular/material/tooltip";

@Component({
    selector: 'app-view',
    imports: [Header, LoadingBar, MatCardModule, MatIconModule, MatButtonModule, GameInfo, YouTubePlayer, NgClass, MatTooltipModule],
    templateUrl: './view.html',
    styleUrl: './view.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewPage implements OnInit, AfterViewInit, OnDestroy {
    private readonly http = inject(HttpClient);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    protected readonly CriteriaState = CriteriaState;
    protected readonly ScoreUtil = ScoreUtil;
    protected readonly ReportType = ReportType;

    protected readonly externalId = signal<string | null>(null);
    protected readonly report = signal<RefereeReportDTO | null>(null);
    protected readonly loading = signal<boolean>(true);
    protected readonly showLoadingBar = computed(() => this.loading());

    readonly youtube = viewChild<YouTubePlayer>('youtubePlayer');
    readonly widthMeasurement = viewChild<ElementRef<HTMLDivElement>>('widthMeasurement');

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

    private fetchReport(externalId: string) {
        this.loading.set(true);
        this.http.get<RefereeReportDTO>(`/api/report/referee/${encodeURIComponent(externalId)}`).subscribe({
            next: (res) => {
                this.report.set(res);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.report.set(null);
                // If loading fails, navigate back to overview for a safe fallback
                this.router.navigate(['overview']).catch(err => console.error(err));
            }
        });
    }

    hasCriteriaInState(comment: ReportCommentDTO, state: CriteriaState): boolean {
        return comment.criteria.some(c => c.state === state);
    }


    requiresReply(videoComment: ReportVideoCommentDTO): boolean {
        // TODO
        console.log(videoComment);
        return false;
        // return (!this.isLoggedIn() || this.isReferee()) && videoComment.requiresReply && videoComment.replies.length < 1;
    }

    flaggedForReply(videoComment: ReportVideoCommentDTO): boolean {
        // TODO
        console.log(videoComment);
        return false;
        // return this.isCoach() && videoComment.requiresReply;
    }

}
