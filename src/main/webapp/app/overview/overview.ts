import {ChangeDetectionStrategy, Component, inject, OnInit, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {Header} from '../components/header/header';
import {LoadingBar} from '../components/loading-bar/loading-bar';
import {AuthService} from '../auth.service';

@Component({
    selector: 'app-main',
    imports: [MatCardModule, MatButtonModule, Header, LoadingBar],
    templateUrl: './overview.html',
    styleUrl: './overview.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class Overview implements OnInit {
    private readonly http = inject(HttpClient);
    protected readonly auth = inject(AuthService);

    protected readonly loading = signal(false);
    protected readonly pingResult = signal<string | null>(null);
    protected readonly error = signal<string | null>(null);

    ngOnInit(): void {
        this.ping();
    }

    ping() {
        if (this.loading()) return;
        this.error.set(null);
        this.loading.set(true);
        this.http.get('/api/ping', {responseType: 'text'}).subscribe({
            next: (res) => {
                this.pingResult.set(res);
                this.loading.set(false);
            },
            error: (err) => {
                const message = err?.error ?? 'Ping failed';
                this.error.set(typeof message === 'string' ? message : 'Ping failed');
                this.loading.set(false);
            }
        });
    }

    pingAdmin() {
        if (this.loading()) return;
        this.error.set(null);
        this.loading.set(true);
        this.http.get('/api/ping/admin', {responseType: 'text'}).subscribe({
            next: (res) => {
                this.pingResult.set(res);
                this.loading.set(false);
            },
            error: (err) => {
                const message = err?.error ?? 'Ping failed';
                this.error.set(typeof message === 'string' ? message : 'Ping failed');
                this.loading.set(false);
            }
        });
    }

    logout() {
        this.auth.logout();
        // On logout, the guard will prevent access to main, but navigate proactively
        location.hash = '#/login';
    }
}
