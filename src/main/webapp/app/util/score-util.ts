export interface HasScoreDTO {
    score?: number | null;
}

export class ScoreUtil {
    public static score(dto: HasScoreDTO, showScore: boolean): string {
        const description = this.scoreDescription(dto);
        if (showScore) {
            return `${dto.score} (${description})`;
        } else {
            return description;
        }
    }

    private static scoreDescription(dto: HasScoreDTO): string {
        if (dto.score) {
            if (dto.score >= 8.6) {
                return "excellent";
            } else if (dto.score >= 8.1) {
                return "very good";
            } else if (dto.score >= 7.6) {
                return "good";
            } else if (dto.score >= 7.1) {
                return "discreet";
            } else if (dto.score >= 6.6) {
                return "sufficient";
            } else {
                return "insufficient";
            }
        }
        return "";
    }
}
