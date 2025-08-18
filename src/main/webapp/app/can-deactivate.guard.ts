import {CanDeactivateFn} from '@angular/router';
import {inject, Signal} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmLeaveDialog} from './edit/confirm-leave-dialog';
import {Observable} from 'rxjs';

export interface HasUnsavedChanges {
    unsavedChanges: Signal<boolean>;
}

export const canDeactivateGuard: CanDeactivateFn<HasUnsavedChanges> = (component): boolean | Observable<boolean> => {
    // If there are no unsaved changes, allow navigation
    if (!component.unsavedChanges) {
        return true;
    }
    const dialog = inject(MatDialog);
    const ref = dialog.open(ConfirmLeaveDialog, {
        disableClose: false,
        hasBackdrop: true
    });
    return ref.afterClosed();
};
