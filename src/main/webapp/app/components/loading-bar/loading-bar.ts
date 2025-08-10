import {Component, input} from '@angular/core';
import {MatProgressBar} from "@angular/material/progress-bar";

@Component({
    selector: 'app-loading-bar',
    imports: [
        MatProgressBar
    ],
    templateUrl: './loading-bar.html',
    styleUrl: './loading-bar.scss'
})
export class LoadingBar {
    readonly show = input.required<boolean>()
}
