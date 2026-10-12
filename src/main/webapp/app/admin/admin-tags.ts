import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatDialog} from '@angular/material/dialog';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {SaveTagDTO, TagAdminDTO} from '../../rest';
import {TagNameDialog, TagNameDialogData} from './tag-name-dialog';
import {ConfirmDeleteData, ConfirmDeleteDialog} from '../overview/confirm-delete-dialog';

@Component({
    selector: 'app-admin-tags',
    imports: [FormsModule, Header, LoadingBar, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatTooltipModule, MatFormFieldModule, MatInputModule],
    templateUrl: './admin-tags.html',
    styleUrl: './admin.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminTagsPage {
    private readonly http = inject(HttpClient);
    private readonly snackBar = inject(MatSnackBar);
    private readonly dialog = inject(MatDialog);

    protected readonly tags = signal<TagAdminDTO[]>([]);
    protected readonly loading = signal(false);
    protected readonly error = signal<string | null>(null);
    protected readonly textFilter = signal<string>('');

    protected readonly filteredTags = computed(() => {
        const filter = this.textFilter().trim().toLowerCase();
        return filter ? this.tags().filter(t => t.name.toLowerCase().includes(filter)) : this.tags();
    });

    protected readonly displayedColumns = ['name', 'usageCount', 'actions'];

    constructor() {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set(null);

        this.http.get<TagAdminDTO[]>('/api/admin/tags').subscribe({
            next: (res) => {
                this.tags.set(res);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.error.set('Failed to load tags');
            },
        });
    }

    addTag(): void {
        const data: TagNameDialogData = {title: 'Add Tag', name: '', confirmText: 'Add'};

        this.dialog.open(TagNameDialog, {data}).afterClosed().subscribe((name: string | null | undefined) => {
            if (!name) return;

            const body: SaveTagDTO = {name};
            this.http.post<TagAdminDTO>('/api/admin/tags', body).subscribe({
                next: () => {
                    this.notify('Tag added');
                    this.load();
                },
                error: (err) => this.notify(err?.status === 409 ? 'A tag with this name already exists' : 'Could not add tag'),
            });
        });
    }

    renameTag(tag: TagAdminDTO): void {
        const data: TagNameDialogData = {title: 'Rename Tag', name: tag.name, confirmText: 'Save'};

        this.dialog.open(TagNameDialog, {data}).afterClosed().subscribe((name: string | null | undefined) => {
            if (!name || name === tag.name) return;

            const body: SaveTagDTO = {name};
            this.http.put<TagAdminDTO>(`/api/admin/tags/${tag.id}`, body).subscribe({
                next: (updated) => {
                    this.tags.set(this.tags().map(t => (t.id === updated.id ? updated : t)));
                    this.notify('Tag renamed');
                },
                error: (err) => this.notify(err?.status === 409 ? 'A tag with this name already exists' : 'Could not rename tag'),
            });
        });
    }

    deleteTag(tag: TagAdminDTO): void {
        const data: ConfirmDeleteData = {title: 'Delete Tag', message: `Do you really want to delete the tag "${tag.name}"?`};

        this.dialog.open(ConfirmDeleteDialog, {data}).afterClosed().subscribe((confirmed: boolean) => {
            if (!confirmed) return;

            this.http.delete<void>(`/api/admin/tags/${tag.id}`).subscribe({
                next: () => {
                    this.tags.set(this.tags().filter(t => t.id !== tag.id));
                    this.notify('Tag deleted');
                },
                error: (err) => {
                    this.notify(err?.status === 409 ? 'Tag is in use and cannot be deleted' : 'Could not delete tag');
                    // the usage count may be outdated
                    this.load();
                },
            });
        });
    }

    private notify(message: string): void {
        this.snackBar.open(message, undefined, {duration: 2000, horizontalPosition: 'center', verticalPosition: 'top'});
    }
}
