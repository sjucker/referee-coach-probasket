import {ChangeDetectionStrategy, Component, computed, effect, inject, signal} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {CriteriaState, RefereeReportDTO, ReportCommentDTO} from '../../rest';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {GameInfo} from "../components/game-info/game-info";
import {ScoreUtil} from "../util/score-util";

@Component({
    selector: 'app-view',
    imports: [Header, LoadingBar, MatCardModule, MatIconModule, MatButtonModule, GameInfo],
    templateUrl: './view.html',
    styleUrl: './view.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewPage {
    private readonly http = inject(HttpClient);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    protected readonly CriteriaState = CriteriaState;
    protected readonly ScoreUtil = ScoreUtil;

    protected readonly externalId = signal<string | null>(null);
    protected readonly report = signal<RefereeReportDTO | null>(null);
    protected readonly loading = signal<boolean>(true);
    protected readonly showLoadingBar = computed(() => this.loading());

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
                this.router.navigate(['overview']).catch(() => {
                });
            }
        });
    }

    hasCriteriaInState(comment: ReportCommentDTO, state: CriteriaState): boolean {
        return comment.criteria.some(c => c.state === state);
    }
}
