import {Component} from '@angular/core';
import {MatToolbar, MatToolbarRow} from "@angular/material/toolbar";

@Component({
  selector: 'app-header',
    imports: [
        MatToolbar,
        MatToolbarRow
    ],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {

}
