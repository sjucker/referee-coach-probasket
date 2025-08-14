import {Component, input, output} from '@angular/core';
import {MatSlider, MatSliderThumb} from "@angular/material/slider";
import {FormsModule} from "@angular/forms";

export interface HasScoreDTO {
    score?: number | null;
}

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

    score() {
        const dto = this.dto();
        if (dto.score) {
            if (dto.score >= 8.6) {
                return "excellent";
            } else if (dto.score >= 8.1) {
                return "very good";
            } else if (dto.score >= 7.6) {
                return "good";
            } else if (dto.score >= 7.1) {
                return "discreet";
            } else if (dto.score >= 6.6) {
                return "sufficient";
            } else {
                return "insufficient";
            }
        }
        return "";
    }
}
