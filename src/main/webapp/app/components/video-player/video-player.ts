import {AfterViewInit, Component, computed, ElementRef, inject, input, OnDestroy, OnInit, viewChild} from '@angular/core';
import {firstValueFrom, Subject} from "rxjs";
import {YouTubePlayer} from "@angular/youtube-player";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
    selector: 'app-video-player',
    imports: [
        YouTubePlayer
    ],
    templateUrl: './video-player.html',
    styleUrl: './video-player.scss'
})
export class VideoPlayer implements OnInit, AfterViewInit, OnDestroy {

    private sanitizer = inject(DomSanitizer);

    youtubeId = input<string | undefined>(undefined);
    asportId = input<number | undefined>(undefined);
    videoWidth = input<number | null>(null);
    videoHeight = input<number | null>(null);

    protected readonly youtube = viewChild<YouTubePlayer>('youtubePlayer');
    protected readonly asport = viewChild<ElementRef>('asportPlayer');
    protected readonly asportUrl = computed(() => {
        return this.sanitizer.bypassSecurityTrustResourceUrl(`https://arena.asport.tv/event/${this.asportId()}/embed?disableContentInfo&disableLogo&disableChromeCast`)
    });
    protected readonly asportPlayerPosition = new Subject<number>();

    ngOnInit(): void {
        if (this.youtubeId()) {
            // This code loads the IFrame Player API code asynchronously, according to the instructions at
            // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
            const tag = document.createElement('script');
            tag.src = 'https://www.youtube.com/iframe_api';
            document.body.appendChild(tag);
        }
    }

    ngAfterViewInit(): void {
        window.addEventListener('message', this.onAsportPlayerPosition());
    }

    private onAsportPlayerPosition() {
        return (event: MessageEvent) => {
            if (event.data.message === 'playerInfo') {
                this.asportPlayerPosition.next(event.data.position);
            }
        };
    }

    ngOnDestroy(): void {
        window.removeEventListener('message', this.onAsportPlayerPosition());
    }

    public jumpTo(time: number): void {
        if (this.youtubeId()) {
            this.youtube()!.seekTo(time, true);
            this.youtube()!.playVideo();
        } else {
            this.asport()!.nativeElement.contentWindow.postMessage({command: 'setPosition', seconds: time}, '*');
            this.asport()!.nativeElement.contentWindow.postMessage({command: 'play'}, '*');
        }
    }

    public async getCurrentVideoTime(): Promise<number> {
        if (this.youtubeId()) {
            return this.youtube()!.getCurrentTime();
        } else {
            this.asport()!.nativeElement.contentWindow.postMessage({command: 'getPlayerInfo'}, '*');
            return firstValueFrom(this.asportPlayerPosition);
        }
    }
}
