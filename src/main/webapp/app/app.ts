import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {version} from '../../../../package.json'

@Component({
    selector: 'app-root',
    imports: [RouterOutlet],
    templateUrl: './app.html',
    styleUrl: './app.scss'
})
export class App {

    version = ""

    constructor() {
        this.version = version
    }
}
