import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {AuthService} from '../auth.service';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {BasketplanGameDTO, CreateRefereeReportDTO, CreateRefereeReportResultDTO, OfficiatingMode} from "../../rest";
import {MatFormField} from "@angular/material/form-field";
import {MatInput, MatLabel} from "@angular/material/input";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatSnackBar} from "@angular/material/snack-bar";

interface RefereeSelection {
    id: number,
    name: string
}

@Component({
    selector: 'app-main',
    imports: [MatCardModule, MatButtonModule, Header, LoadingBar, FormsModule, MatFormField, MatLabel, MatInput, MatCheckbox, ReactiveFormsModule, MatSelect, MatOption],
    templateUrl: './overview.html',
    styleUrl: './overview.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class Overview {
    private readonly fb = inject(FormBuilder);
    private readonly http = inject(HttpClient);
    protected readonly auth = inject(AuthService);
    private readonly snackBar = inject(MatSnackBar);

    protected readonly game = signal<BasketplanGameDTO | null>(null);
    protected readonly videoUrl = signal<string | undefined>(undefined);
    protected readonly videoUrlInputNeeded = signal(false);
    protected readonly textOnlyMode = signal(false);
    protected readonly referees = signal<RefereeSelection[]>([]);
    protected readonly referee = signal<RefereeSelection | null>(null);

    protected readonly loading = computed(() => this.searching() || this.creating());
    protected readonly creating = signal(false);
    protected readonly error = signal<string | null>(null);
    protected readonly searching = signal(false);
    protected readonly problemDescription = signal<string | null>(null);

    protected readonly form = this.fb.nonNullable.group({
        gameNumber: ['', [Validators.required]],
    });

    searchGame(): void {
        this.problemDescription.set(null);

        if (this.form.invalid) return;

        const {gameNumber} = this.form.getRawValue();

        this.searching.set(true);

        this.http.get<BasketplanGameDTO>(`/api/basketplan/${gameNumber}`).subscribe({
            next: game => {
                if (game.referee1Id && game.referee2Id
                    && (game.officiatingMode === OfficiatingMode.OFFICIATING_2PO || game.referee3Id)) {

                    this.game.set(game);
                    this.videoUrl.set(game.videoUrl);

                    this.referees.set([
                        {id: game.referee1Id, name: game.referee1Name!},
                        {id: game.referee2Id, name: game.referee2Name!}
                    ]);

                    if (game.officiatingMode === OfficiatingMode.OFFICIATING_3PO && game.referee3Id) {
                        this.referees.set([
                            {id: game.referee1Id, name: game.referee1Name!},
                            {id: game.referee2Id, name: game.referee2Name!},
                            {id: game.referee3Id, name: game.referee3Name!},
                        ]);
                    }
                    this.videoUrlInputNeeded.set(!game.videoUrl);
                } else {
                    this.problemDescription.set('At least one referee not available in database');
                }
                this.searching.set(false);
            },
            error: error => {
                if (error.status === 404) {
                    this.problemDescription.set('No game found for given game number');
                } else {
                    this.problemDescription.set('An unexpected error occurred');
                }
                this.searching.set(false);
            }
        });
    }

    createRefereeReport() {
        if (this.game() && (this.textOnlyMode() || this.videoUrl()) && this.referee()) {
            this.creating.set(true);
            const request: CreateRefereeReportDTO = {
                gameNumber: this.game()!.gameNumber,
                reporteeId: this.referee()!.id,
                videoUrl: this.videoUrl()
            };
            this.http.post<CreateRefereeReportResultDTO>('/api/report/referee', request).subscribe({
                next: response => {
                    this.creating.set(false);
                    this.snackBar.open(response.externalId);
                    // TODO
                    // this.edit(response.id, ReportType.COACHING);
                },
                error: () => {
                    this.creating.set(false);
                    this.snackBar.open("An unexpected error occurred, report could not be created.", undefined, {
                        duration: 3000,
                        horizontalPosition: "center",
                        verticalPosition: "top"
                    })
                }
            })
        } else {
            this.snackBar.open("Please search for a game or select a referee", undefined, {
                duration: 3000,
                horizontalPosition: "center",
                verticalPosition: "top"
            });
        }

    }
}
