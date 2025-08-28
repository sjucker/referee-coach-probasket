package ch.refereecoach.probasket.common;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

public enum Rank {
    RG1,
    RG2,
    RG3,
    RG4,
    RK;

    public static Optional<Rank> of(String rank) {
        if (isBlank(rank)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Rank.valueOf(rank));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
