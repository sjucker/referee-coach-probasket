import {Component, ElementRef, input, OnInit, output, viewChild} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatChipGrid, MatChipInput, MatChipRemove, MatChipRow} from "@angular/material/chips";
import {MatAutocomplete, MatAutocompleteSelectedEvent, MatAutocompleteTrigger} from "@angular/material/autocomplete";
import {MatIcon} from "@angular/material/icon";
import {MatOption} from "@angular/material/select";
import {TagDTO} from "../../rest";
import {Observable} from "rxjs";
import {MatFormField, MatLabel} from "@angular/material/input";

@Component({
    selector: 'app-tag-selection',
    imports: [MatFormField, MatLabel, MatChipGrid, MatChipRow, MatChipRemove, MatIcon, FormsModule, MatAutocompleteTrigger, MatChipInput, ReactiveFormsModule, MatAutocomplete, MatOption],
    templateUrl: './tag-selection.html',
    styleUrl: './tag-selection.scss'
})
export class TagSelection implements OnInit {
    readonly tagInput = viewChild<ElementRef<HTMLInputElement>>('tagInput');

    readonly availableTags = input.required<Observable<TagDTO[]>>();
    allTags: TagDTO[] = [];

    readonly initialSelectedTags = input<TagDTO[]>([]);
    readonly disabled = input(false);

    selectedTags: TagDTO[] = [];

    readonly selected = output<TagDTO>();
    readonly removed = output<TagDTO>();

    filteredTags: TagDTO[] = [];
    tagController = new FormControl('');

    ngOnInit(): void {
        this.availableTags().subscribe({
            next: value => {
                this.filteredTags = [...value];
                this.allTags = [...value];
            }
        })

        this.selectedTags = [...this.initialSelectedTags()];

        this.tagController.valueChanges.subscribe(value => {
            if (value && value.length > 0) {
                this.filteredTags = this.allTags.filter(tag => tag.name.toLowerCase().includes(value.toLowerCase()));
            } else {
                this.filteredTags = this.allTags.slice();
            }
        });
    }

    removeTag(tag: TagDTO) {
        this.selectedTags.splice(this.selectedTags.indexOf(tag), 1);
        this.removed.emit(tag);
    }

    selectTag($event: MatAutocompleteSelectedEvent) {
        this.selectedTags.push($event.option.value);
        this.selected.emit($event.option.value);
        this.tagController.setValue(null);
        this.tagInput()!.nativeElement.value = '';
    }
}
