import {Component, input, output} from '@angular/core';
import {MatSlider, MatSliderThumb} from "@angular/material/slider";
import {FormsModule} from "@angular/forms";
import {HasScoreDTO, ScoreUtil} from "../../util/score-util";

@Component({
    selector: 'app-score',
    imports: [
        MatSlider,
        MatSliderThumb,
        FormsModule
    ],
    templateUrl: './score.component.html',
    styleUrl: './score.component.scss'
})
export class Score {
    readonly title = input.required<string>();
    readonly dto = input.required<HasScoreDTO>();
    readonly changed = output<void>();
    protected readonly ScoreUtil = ScoreUtil;
}
