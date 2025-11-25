import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {Header} from '../components/header/header';
import {MatCardModule} from '@angular/material/card';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatButtonModule} from '@angular/material/button';
import {MatSnackBar} from '@angular/material/snack-bar';

import {FormsModule} from '@angular/forms';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AuthService} from '../auth.service';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {takeUntilDestroyed, toObservable} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';
import {UpdateUserRolesDTO, UserDTO, UsersSearchResultDTO} from "../../rest";
import {LoadingBar} from "../components/loading-bar/loading-bar";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule, Sort} from "@angular/material/sort";
import {MatTooltipModule} from "@angular/material/tooltip";

@Component({
    selector: 'app-admin',
    imports: [FormsModule, Header, MatCardModule, MatTableModule, MatSortModule, MatPaginatorModule, MatCheckbox, MatButtonModule, MatFormFieldModule, MatInputModule, LoadingBar, MatIconModule, MatTooltipModule],
    templateUrl: './admin.html',
    styleUrl: './admin.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPage {
    private readonly http = inject(HttpClient);
    private readonly snackBar = inject(MatSnackBar);
    protected readonly auth = inject(AuthService);

    protected readonly users = signal<UserDTO[]>([]);
    protected readonly total = signal(0);
    protected readonly pageIndex = signal(0);
    protected readonly pageSize = signal(20);
    protected readonly loading = signal(false);
    protected readonly error = signal<string | null>(null);
    protected readonly textFilter = signal<string>('');
    protected readonly sortBy = signal<string>('lastName');
    protected readonly sortOrder = signal<'asc' | 'desc'>('asc');

    protected readonly canEdit = computed(() => this.auth.isAdmin());

    constructor() {
        // Debounce search text changes
        toObservable(this.textFilter)
            .pipe(skip(1), debounceTime(500), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe(() => {
                this.pageIndex.set(0);
                this.load();
            });

        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(null);

        const params = new HttpParams()
            .set('page', this.pageIndex())
            .set('pageSize', this.pageSize())
            .set('filter', this.textFilter())
            .set('sortBy', this.sortBy())
            .set('sortOrder', this.sortOrder());

        this.http.get<UsersSearchResultDTO>('/api/admin/users', {params}).subscribe({
            next: (res) => {
                this.users.set(res.items);
                this.total.set(res.total);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.error.set('Failed to load users');
            },
        });
    }

    onPageChange(event: PageEvent): void {
        this.pageIndex.set(event.pageIndex);
        this.pageSize.set(event.pageSize);
        this.load();
    }

    save(user: UserDTO): void {
        const body: UpdateUserRolesDTO = {
            refereeCoach: user.refereeCoach,
            refereeCoachPlus: user.refereeCoachPlus,
            referee: user.referee,
            trainerCoach: user.trainerCoach,
            trainer: user.trainer,
        };

        this.http.put<UserDTO>(`/api/admin/users/${user.id}/roles`, body).subscribe({
            next: (updated) => {
                // update local array with returned data to reflect any backend canonicalization
                const arr = this.users().map((u) => (u.id === updated.id ? updated : u));
                this.users.set(arr);
                this.snackBar.open('Saved', undefined, {duration: 1500, horizontalPosition: 'center', verticalPosition: 'top'});
            },
            error: () => {
                this.snackBar.open('Save failed', undefined, {duration: 2000, horizontalPosition: 'center', verticalPosition: 'top'});
            },
        });
    }

    get displayedColumns(): string[] {
        return ['name', 'email', 'rank', 'active', 'refereeCoach', 'refereeCoachPlus', 'referee', 'trainerCoach', 'trainer', 'actions'];
    }

    onMatSortChange($event: Sort) {
        if ($event.direction === '') {
            this.sortOrder.set('asc');
            this.sortBy.set('lastName');
        } else {
            this.sortOrder.set($event.direction);
            this.sortBy.set($event.active);
        }
        this.pageIndex.set(0);
        this.load();
    }
}
