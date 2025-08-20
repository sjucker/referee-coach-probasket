import {computed, inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {LoginRequestDTO, LoginResponseDTO, UserRole} from "../rest";
import {environment} from "../environments/environment";


@Injectable({providedIn: 'root'})
export class AuthService {
    private readonly http = inject(HttpClient);

    private baseUrl = environment.baseUrl

    private readonly tokenKey = 'probasket-token';
    private readonly usernameKey = 'probasket-username';
    private readonly rolesKey = 'probasket-roles';

    private readonly _token = signal<string | null>(this.readValue(this.tokenKey));
    private readonly _username = signal<string | null>(this.readValue(this.usernameKey));
    private readonly _roles = signal<string[]>(this.readRoles());

    readonly token = computed(() => this._token());
    readonly username = computed(() => this._username());
    readonly roles = computed(() => this._roles());
    readonly isAuthenticated = computed(() => !!this._token());

    hasRole(role: string) {
        return this._roles().includes(role);
    }

    login(username: string, password: string) {
        const body: LoginRequestDTO = {username, password};
        return this.http.post<LoginResponseDTO>(`${this.baseUrl}/api/auth/login`, body).pipe(
            tap((res) => {
                this._token.set(res.token);
                this.storeValue(this.tokenKey, res.token);

                this._username.set(res.username);
                this.storeValue(this.usernameKey, res.username);

                this._roles.set(res.roles);
                this.storeValue(this.rolesKey, res.roles.join(','))
            })
        );
    }

    logout() {
        this._token.set(null);
        this.storeValue(this.tokenKey, null);

        this._username.set(null);
        this.storeValue(this.usernameKey, null);

        this._roles.set([]);
        this.storeValue(this.rolesKey, null);
    }

    private storeValue(key: string, token: string | null) {
        if (token) {
            localStorage.setItem(key, token);
        } else {
            localStorage.removeItem(key);
        }
    }

    private readValue(key: string): string | null {
        return localStorage.getItem(key);
    }

    private readRoles(): string[] {
        return localStorage.getItem(this.rolesKey)?.split(',') ?? [];
    }

    public isRefereeCoach(): boolean {
        return this.hasRole(UserRole.REFEREE_COACH);
    }

    public isTrainerCoach(): boolean {
        return this.hasRole(UserRole.TRAINER_COACH);
    }
}
