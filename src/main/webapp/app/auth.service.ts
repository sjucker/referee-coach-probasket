import {computed, inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {LoginRequestDTO, LoginResponseDTO} from "../rest";
import {environment} from "../environments/environment";


@Injectable({providedIn: 'root'})
export class AuthService {
    private readonly http = inject(HttpClient);

    private baseUrl = environment.baseUrl

    private readonly tokenKey = 'auth_token';
    private readonly _token = signal<string | null>(this.readToken());
    readonly token = computed(() => this._token());
    readonly isAuthenticated = computed(() => !!this._token());

    login(username: string, password: string) {
        const body: LoginRequestDTO = {username, password};
        return this.http.post<LoginResponseDTO>(`${this.baseUrl}/api/auth/login`, body).pipe(
            tap((res) => {
                this.setToken(res.token);
            })
        );
    }

    logout() {
        this.setToken(null);
    }

    private setToken(token: string | null) {
        this._token.set(token);
        if (token) {
            sessionStorage.setItem(this.tokenKey, token);
        } else {
            sessionStorage.removeItem(this.tokenKey);
        }
    }

    private readToken(): string | null {
        return sessionStorage.getItem(this.tokenKey);
    }
}
