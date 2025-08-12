import {ChangeDetectionStrategy, Component, computed, effect, inject, signal} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {ReportDTO} from '../../rest';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';

@Component({
    selector: 'app-edit',
    imports: [Header, LoadingBar, MatCardModule, MatButtonModule],
    templateUrl: './edit.html',
    styleUrl: './edit.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditPage {
    private readonly http = inject(HttpClient);
    private readonly route = inject(ActivatedRoute);

    protected readonly externalId = signal<string | null>(null);
    protected readonly report = signal<ReportDTO | null>(null);
    protected readonly loading = signal<boolean>(true);
    protected readonly error = signal<string | null>(null);

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

    private fetchReport(eid: string) {
        this.loading.set(true);
        this.error.set(null);
        this.http.get<ReportDTO>(`/api/report/${encodeURIComponent(eid)}`).subscribe({
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

    protected readonly hasData = computed(() => !!this.report());
}
