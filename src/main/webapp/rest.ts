/* eslint-disable */

export interface LoginRequestDTO {
    username: string;
    password: string;
}

export interface LoginResponseDTO {
    token: string;
    username: string;
    userId: number;
    roles: UserRole[];
}

export interface UpdateUserRolesDTO {
    refereeCoach: boolean;
    referee: boolean;
    trainerCoach: boolean;
    trainer: boolean;
}

export interface UserDTO {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    rank?: Rank;
    refereeCoach: boolean;
    referee: boolean;
    trainerCoach: boolean;
    trainer: boolean;
    admin: boolean;
    active: boolean;
}

export interface UsersSearchResultDTO {
    items: UserDTO[];
    total: number;
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
    referee1Rank?: Rank;
    referee2Id?: number;
    referee2Name?: string;
    referee2Rank?: Rank;
    referee3Id?: number;
    referee3Name?: string;
    referee3Rank?: Rank;
    videoUrl?: string;
}

export interface CopyRefereeReportDTO {
    reporteeId: number;
}

export interface CreateRefereeReportDTO {
    gameNumber: string;
    reporteeId: number;
    videoUrl?: string;
}

export interface CreateRefereeReportDiscussionReplyDTO {
    replies: NewReportVideoCommentReplyDTO[];
    comments: NewReportVideoCommentDTO[];
}

export interface CreateRefereeReportResultDTO {
    id: number;
    externalId: string;
}

export interface NewReportVideoCommentDTO {
    timestampInSeconds: number;
    comment: string;
}

export interface NewReportVideoCommentReplyDTO {
    commentId: number;
    reply: string;
}

export interface RefereeDTO {
    id: number;
    name: string;
}

export interface RefereeReportDTO {
    id: number;
    externalId: string;
    reportType: ReportType;
    coachId: number;
    coachName: string;
    reporteeId: number;
    reporteeName: string;
    reporteeRank: Rank;
    game: BasketplanGameDTO;
    youtubeId?: string;
    asportId?: number;
    score?: number;
    finished: boolean;
    userIsReportee: boolean;
    comments: ReportCommentDTO[];
    videoComments: ReportVideoCommentDTO[];
}

export interface ReportCommentDTO {
    id: number;
    type: CategoryType;
    typeDescription: string;
    criteriaHints: string[];
    comment?: string;
    scoreRequired: boolean;
    score?: number;
    criteria: ReportCriteriaDTO[];
}

export interface ReportCriteriaDTO {
    id: number;
    type: CriteriaType;
    description: string;
    state?: CriteriaState;
}

export interface ReportOverviewDTO {
    externalId: string;
    type: ReportType;
    date: DateAsString;
    gameNumber: string;
    competition: string;
    teams: string;
    coach: string;
    coachId: number;
    reportee: string;
    reporteeId: number;
    referee1?: RefereeDTO;
    referee2?: RefereeDTO;
    referee3?: RefereeDTO;
    finished: boolean;
    userIsCoach: boolean;
}

export interface ReportSearchResultDTO {
    items: ReportOverviewDTO[];
    total: number;
}

export interface ReportVideoCommentDTO {
    id?: number;
    timestampInSeconds: number;
    comment: string;
    createdAt: DateAsString;
    createdById: number;
    createdBy: string;
    requiresReply: boolean;
    reference: boolean;
    replies: ReportVideoCommentReplyDTO[];
    tags: TagDTO[];
}

export interface ReportVideoCommentReplyDTO {
    id: number;
    reply: string;
    createdAt: DateAsString;
    createdById: number;
    createdBy: string;
}

export interface TagDTO {
    id: number;
    name: string;
}

export interface TagOverviewDTO {
    gameNumber: string;
    competition: string;
    date: DateAsString;
    timestampInSeconds: number;
    comment: string;
    youtubeId?: string;
    asportId?: number;
    tags: string;
}

export interface TagSearchResultDTO {
    items: TagOverviewDTO[];
    total: number;
}

export type DateAsString = string;

export enum UserRole {
    REFEREE_COACH = "REFEREE_COACH",
    REFEREE = "REFEREE",
    TRAINER_COACH = "TRAINER_COACH",
    TRAINER = "TRAINER",
    ADMIN = "ADMIN",
}

export enum Rank {
    RG1 = "RG1",
    RG2 = "RG2",
    RG3 = "RG3",
    RG4 = "RG4",
    RK = "RK",
}

export enum OfficiatingMode {
    OFFICIATING_2PO = "OFFICIATING_2PO",
    OFFICIATING_3PO = "OFFICIATING_3PO",
}

export enum ReportType {
    REFEREE_VIDEO_REPORT = "REFEREE_VIDEO_REPORT",
    REFEREE_COMMENT_REPORT = "REFEREE_COMMENT_REPORT",
    TRAINER_REPORT = "TRAINER_REPORT",
    GAME_DISCUSSION = "GAME_DISCUSSION",
}

export enum CategoryType {
    GENERAL = "GENERAL",
    IMAGE = "IMAGE",
    FOULS = "FOULS",
    VIOLATIONS = "VIOLATIONS",
    MECHANICS = "MECHANICS",
    FITNESS = "FITNESS",
    GAME_CONTROL = "GAME_CONTROL",
    POINTS_TO_KEEP = "POINTS_TO_KEEP",
    POINTS_TO_IMPROVE = "POINTS_TO_IMPROVE",
}

export enum CriteriaType {
    IMAGE_ON_TIME = "IMAGE_ON_TIME",
    IMAGE_PRE_GAME_CHECKS = "IMAGE_PRE_GAME_CHECKS",
    IMAGE_PRE_GAME = "IMAGE_PRE_GAME",
    FITNESS_SPEED = "FITNESS_SPEED",
    FITNESS_ENDURANCE = "FITNESS_ENDURANCE",
    FITNESS_EFFECT = "FITNESS_EFFECT",
    LEAD_OA = "LEAD_OA",
    LEAD_DS = "LEAD_DS",
    LEAD_CD = "LEAD_CD",
    LEAD_ETP = "LEAD_ETP",
    LEAD_AOR = "LEAD_AOR",
    TRAIL_AOR = "TRAIL_AOR",
    TRAIL_DS = "TRAIL_DS",
    TRAIL_PENETRATION = "TRAIL_PENETRATION",
    TRAIL_RB = "TRAIL_RB",
    TRAIL_AOR3 = "TRAIL_AOR3",
    FOULS_HC = "FOULS_HC",
    FOULS_BLOCK = "FOULS_BLOCK",
    FOULS_AOS = "FOULS_AOS",
    FOULS_RB = "FOULS_RB",
    FOULS_PNR = "FOULS_PNR",
    FOULS_OFF_BALL = "FOULS_OFF_BALL",
    VIOLATION_TV = "VIOLATION_TV",
    VIOLATION_DD = "VIOLATION_DD",
    VIOLATION_OOB = "VIOLATION_OOB",
    VIOLATION_BCV = "VIOLATION_BCV",
    VIOLATION_SECS = "VIOLATION_SECS",
    GAME_CONTROL_PLAYER = "GAME_CONTROL_PLAYER",
    GAME_CONTROL_COACH = "GAME_CONTROL_COACH",
    GAME_CONTROL_PREVENTION = "GAME_CONTROL_PREVENTION",
    GAME_CONTROL_EOP = "GAME_CONTROL_EOP",
}

export enum CriteriaState {
    MINUS = "MINUS",
    NEUTRAL = "NEUTRAL",
    PLUS = "PLUS",
}
