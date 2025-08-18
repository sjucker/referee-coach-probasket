package ch.refereecoach.probasket.util;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class YouTubeUtil {

    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile("v=([^&]+)");

    public static Optional<String> parseYouTubeId(String youtubeLink) {
        if (StringUtils.isBlank(youtubeLink)) {
            return Optional.empty();
        }

        var matcher = YOUTUBE_ID_PATTERN.matcher(youtubeLink);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1));
        }
        return Optional.empty();
    }
}
