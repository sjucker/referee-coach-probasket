package ch.refereecoach.probasket.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsportUtilTest {

    @Test
    void parseAsportEventId() {
        assertThat(AsportUtil.parseAsportEventId(null)).isEmpty();
        assertThat(AsportUtil.parseAsportEventId("null")).isEmpty();
        assertThat(AsportUtil.parseAsportEventId("")).isEmpty();
        assertThat(AsportUtil.parseAsportEventId("https://probasket.asport.tv/event/55513/zug-basket-ksc-wiedikon?autostartCouchMode")).hasValue(55513L);
        assertThat(AsportUtil.parseAsportEventId("https://www.youtube.com/watch?v=jNQXAC9IVRw")).isEmpty();
    }
}
