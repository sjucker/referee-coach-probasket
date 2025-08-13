package ch.refereecoach.probasket.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OfficiatingMode {
    OFFICIATING_2PO("2PO"),
    OFFICIATING_3PO("3PO");

    private final String description;
}
