import {AfterViewInit, ChangeDetectionStrategy, Component, computed, ElementRef, inject, OnDestroy, OnInit, signal, viewChild} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {MatPaginatorModule, PageEvent} from "@angular/material/paginator";
import {TagSelection} from "../tag-selection/tag-selection";
import {Observable, of, share} from "rxjs";
import {TagDTO, TagOverviewDTO, TagSearchResultDTO} from "../../rest";
import {HttpClient, HttpParams} from "@angular/common/http";
import {MatCard, MatCardActions, MatCardContent} from "@angular/material/card";
import {MatButton, MatIconButton} from "@angular/material/button";
import {DatePipe} from "@angular/common";
import {MatCell, MatCellDef, MatColumnDef, MatHeaderCell, MatHeaderCellDef, MatHeaderRow, MatHeaderRowDef, MatNoDataRow, MatRow, MatRowDef, MatTable} from "@angular/material/table";
import {MatIcon} from "@angular/material/icon";
import {YouTubePlayer} from "@angular/youtube-player";
import {AuthService} from "../auth.service";

@Component({
    selector: 'app-tag-search',
    imports: [Header, LoadingBar, MatPaginatorModule, TagSelection, MatCard, MatCardContent, MatCardActions, MatButton, DatePipe, MatCell, MatCellDef, MatColumnDef, MatHeaderCell, MatHeaderRow, MatHeaderRowDef, MatIcon, MatIconButton, MatRow, MatRowDef, MatTable, MatHeaderCellDef, YouTubePlayer, MatNoDataRow],
    templateUrl: './tag-search.html',
    styleUrl: './tag-search.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TagSearch implements AfterViewInit, OnInit, OnDestroy {
    private readonly http = inject(HttpClient);
    protected readonly auth = inject(AuthService);

    protected readonly pageIndex = signal(0);
    protected readonly pageSize = signal(10);
    protected readonly searching = signal(false);
    protected readonly showLoadingBar = computed(() => this.searching());
    protected readonly totalResults = signal(0);

    protected readonly currentVideoId = signal<string | undefined>(undefined);
    protected readonly videoWidth = signal<number | null>(null);
    protected readonly videoHeight = signal<number | null>(null);

    readonly youtube = viewChild<YouTubePlayer>('youtubePlayer');
    readonly widthMeasurement = viewChild<ElementRef<HTMLDivElement>>('widthMeasurement');

    protected readonly results = signal<TagOverviewDTO[]>([]);
    protected readonly availableTags: Observable<TagDTO[]> = of([]);
    protected selectedTags = signal<TagDTO[]>([]);

    constructor() {
        this.availableTags = this.http.get<TagDTO[]>(`/api/tag`).pipe(share())
    }

    ngOnInit(): void {
        // This code loads the IFrame Player API code asynchronously, according to the instructions at
        // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        document.body.appendChild(tag);
    }

    ngAfterViewInit(): void {
        this.onResize();
        window.addEventListener('resize', this.onResize);
    }

    ngOnDestroy(): void {
        window.removeEventListener('resize', this.onResize);
    }

    onResize = (): void => {
        setTimeout(() => {
            // margin and padding each 16px on both sides
            const contentWidth = this.widthMeasurement()!.nativeElement.scrollWidth - (4 * 16);

            this.videoWidth.set(Math.min(contentWidth, 720));
            this.videoHeight.set(this.videoWidth()! * 0.6);
        });
    };

    onPageChange(event: PageEvent): void {
        this.pageIndex.set(event.pageIndex);
        this.pageSize.set(event.pageSize);
        this.search();
    }

    selectTag(tag: TagDTO) {
        this.selectedTags.update(values => {
            return [...values, tag];
        })
    }

    removeTag(tag: TagDTO) {
        this.selectedTags.update(values => {
            return values.filter(t => t.id !== tag.id);
        })
    }

    search() {
        const params = new HttpParams()
            .set('page', this.pageIndex())
            .set('pageSize', this.pageSize());

        const body: TagDTO[] = this.selectedTags();

        this.searching.set(true);

        this.http.post<TagSearchResultDTO>('/api/tag/search', body, {params}).subscribe({
            next: (res) => {
                this.searching.set(false);
                this.results.set(res.items);
                this.totalResults.set(res.total);
            },
            error: () => {
                this.searching.set(false);
            }
        });
    }

    play(dto: TagOverviewDTO) {
        this.currentVideoId.set(dto.youtubeId);

        const interval = setInterval(() => {
            const youtube = this.youtube()!;
            if (youtube.getPlayerState() === YT.PlayerState.PLAYING) {
                clearInterval(interval);
            }
            youtube.seekTo(dto.timestampInSeconds, true);
            youtube.playVideo();
        }, 500);
    }

    get displayedColumns(): string[] {
        if (this.auth.isRefereeCoach()) {
            return ['date', 'gameNumber', 'competition', 'comment', 'tags', 'play'];
        } else {
            return ['date', 'gameNumber', 'competition', 'tags', 'play'];
        }
    }
}
