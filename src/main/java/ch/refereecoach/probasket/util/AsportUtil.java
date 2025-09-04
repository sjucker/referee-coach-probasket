package ch.refereecoach.probasket.util;

import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

@NoArgsConstructor(access = PRIVATE)
public class AsportUtil {

    private static final Pattern ASPORT_EVENT_ID_PATTERN = Pattern.compile("event/(\\d+)");

    public static Optional<Long> parseAsportEventId(String url) {
        if (isBlank(url)) {
            return Optional.empty();
        }

        var matcher = ASPORT_EVENT_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return Optional.of(toLong(matcher.group(1)));
        }
        return Optional.empty();
    }
}
