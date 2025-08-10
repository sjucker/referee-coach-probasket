/* eslint-disable */

export interface LoginRequestDTO {
    username: string;
    password: string;
}

export interface LoginResponseDTO {
    token: string;
    username: string;
    roles: UserRole[];
}

export enum UserRole {
    REFEREE_COACH = "REFEREE_COACH",
    REFEREE = "REFEREE",
    TRAINER_COACH = "TRAINER_COACH",
    TRAINER = "TRAINER",
    ADMIN = "ADMIN",
}
