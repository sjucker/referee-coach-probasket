import {Component, input, output} from '@angular/core';
import {ReportCommentDTO} from "../../../rest";
import {MatSlider, MatSliderThumb} from "@angular/material/slider";
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-rating',
    imports: [
        MatSlider,
        MatSliderThumb,
        FormsModule
    ],
    templateUrl: './rating.html',
    styleUrl: './rating.scss'
})
export class Rating {
    readonly title = input.required<string>();
    readonly dto = input.required<ReportCommentDTO>();
    readonly changed = output<void>();

    rating() {
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
