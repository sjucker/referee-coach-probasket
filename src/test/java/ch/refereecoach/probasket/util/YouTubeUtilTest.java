package ch.refereecoach.probasket.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YouTubeUtilTest {

    @Test
    void parseYouTubeId() {
        assertThat(YouTubeUtil.parseYouTubeId("https://www.youtube.com/watch?v=jNQXAC9IVRw")).hasValue("jNQXAC9IVRw");
        assertThat(YouTubeUtil.parseYouTubeId("https://probasket.asport.tv/event/48044/frauenfeld-opfikon-basket")).isEmpty();
        assertThat(YouTubeUtil.parseYouTubeId(null)).isEmpty();
        assertThat(YouTubeUtil.parseYouTubeId("")).isEmpty();
    }
}
