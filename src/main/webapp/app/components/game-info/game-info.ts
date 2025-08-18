import {Component, input} from '@angular/core';
import {DatePipe, NgClass} from "@angular/common";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {OfficiatingMode, RefereeReportDTO} from "../../../rest";
import {ScoreUtil} from "../../util/score-util";

@Component({
    selector: 'app-game-info',
    imports: [
        DatePipe,
        MatCard,
        MatCardContent,
        MatCardHeader,
        MatCardTitle,
        NgClass
    ],
    templateUrl: './game-info.html',
    styleUrl: './game-info.scss'
})
export class GameInfo {

    protected readonly OfficiatingMode = OfficiatingMode;
    protected readonly ScoreUtil = ScoreUtil;

    report = input.required<RefereeReportDTO>();
    showOverallScore = input(false);
}
