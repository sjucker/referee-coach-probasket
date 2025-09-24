import {AfterViewInit, ChangeDetectionStrategy, Component, computed, effect, ElementRef, inject, OnDestroy, signal, viewChild} from '@angular/core';
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
import {NgClass} from "@angular/common";
import {MatTooltipModule} from "@angular/material/tooltip";
import {PATH_DISCUSS, PATH_OVERVIEW} from "../app.routes";
import {VideoPlayer} from "../components/video-player/video-player";
import {AuthService} from "../auth.service";

@Component({
    selector: 'app-view',
    imports: [Header, LoadingBar, MatCardModule, MatIconModule, MatButtonModule, GameInfo, NgClass, MatTooltipModule, VideoPlayer],
    templateUrl: './view.html',
    styleUrl: './view.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewPage implements AfterViewInit, OnDestroy {
    private readonly http = inject(HttpClient);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    public readonly authService = inject(AuthService);

    protected readonly CriteriaState = CriteriaState;
    protected readonly ScoreUtil = ScoreUtil;
    protected readonly ReportType = ReportType;

    protected readonly externalId = signal<string | null>(null);
    protected readonly report = signal<RefereeReportDTO | null>(null);
    protected readonly loading = signal<boolean>(true);
    protected readonly showLoadingBar = computed(() => this.loading());

    protected readonly videoPlayer = viewChild<VideoPlayer>('videoPlayer');
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
        this.videoPlayer()!.jumpTo(time);
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
                this.router.navigate([PATH_OVERVIEW]).catch(err => console.error(err));
            }
        });
    }

    hasCriteriaInState(comment: ReportCommentDTO, state: CriteriaState): boolean {
        return comment.criteria.some(c => c.state === state);
    }


    requiresReply(videoComment: ReportVideoCommentDTO): boolean {
        const dto = this.report()!;
        return dto.userIsReportee && videoComment.requiresReply && !videoComment.replies.some(c => c.createdById == dto.reporteeId);
    }

    flaggedForReply(videoComment: ReportVideoCommentDTO): boolean {
        const dto = this.report()!;
        return !dto.userIsReportee && videoComment.requiresReply;
    }

    discuss() {
        this.router.navigate([PATH_DISCUSS, this.report()!.externalId]).catch(err => console.error(err));
    }

    tagNames(videoComment: ReportVideoCommentDTO) {
        return videoComment.tags.map(t => t.name).join(', ');
    }
}
