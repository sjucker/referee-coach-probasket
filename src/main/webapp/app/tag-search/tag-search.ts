import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';

@Component({
    selector: 'app-tag-search',
    imports: [Header, LoadingBar],
    templateUrl: './tag-search.html',
    styleUrl: './tag-search.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TagSearch {
}
