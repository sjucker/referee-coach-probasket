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

export interface BasketplanGameDTO {
    gameNumber: string;
    competition: string;
    date: DateAsString;
    result: string;
    homeTeam: string;
    homeTeamId: number;
    guestTeam: string;
    guestTeamId: number;
    officiatingMode?: OfficiatingMode;
    referee1Id?: number;
    referee1Name?: string;
    referee2Id?: number;
    referee2Name?: string;
    referee3Id?: number;
    referee3Name?: string;
    videoUrl?: string;
}

export interface CreateRefereeReportDTO {
    gameNumber: string;
    reporteeId: number;
    videoUrl?: string;
}

export interface CreateRefereeReportResultDTO {
    externalId: string;
}

export type DateAsString = string;

export enum UserRole {
    REFEREE_COACH = "REFEREE_COACH",
    REFEREE = "REFEREE",
    TRAINER_COACH = "TRAINER_COACH",
    TRAINER = "TRAINER",
    ADMIN = "ADMIN",
}

export enum OfficiatingMode {
    OFFICIATING_2PO = "OFFICIATING_2PO",
    OFFICIATING_3PO = "OFFICIATING_3PO",
}
