package core.framework.search.module;

import core.framework.test.inject.TestBeanFactory;
import core.framework.test.module.TestModuleContext;
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
        config = new SearchConfig(new TestModuleContext(new TestBeanFactory()));
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("search().host() must be configured");
    }
}
