package ch.refereecoach.probasket.dto.basketplan;

import ch.refereecoach.probasket.common.OfficiatingMode;
import ch.refereecoach.probasket.common.Rank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;

public record BasketplanGameDTO(@NotNull String gameNumber,
                                @NotNull String competition,
                                @NotNull LocalDate date,
                                @NotNull String result,
                                @NotNull String homeTeam,
                                @NotNull Integer homeTeamId,
                                @NotNull String guestTeam,
                                @NotNull Integer guestTeamId,
                                OfficiatingMode officiatingMode,
                                Long referee1Id,
                                String referee1Name,
                                Rank referee1Rank,
                                Long referee2Id,
                                String referee2Name,
                                Rank referee2Rank,
                                Long referee3Id,
                                String referee3Name,
                                Rank referee3Rank,
                                String videoUrl) {

    public boolean containsReferee(Long refereeId) {
        return Objects.equals(refereeId, referee1Id) ||
                Objects.equals(refereeId, referee2Id) ||
                Objects.equals(refereeId, referee3Id);
    }
}
