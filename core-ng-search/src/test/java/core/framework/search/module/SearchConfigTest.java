package core.framework.search.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class SearchConfigTest {
    private SearchConfig config;

    @BeforeEach
    void createSearchConfig() {
        config = new SearchConfig();
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("search host must be configured");
    }
}
