package core.log.job;

import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.log.service.IndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class CleanupOldIndexJobTest {
    private CleanupOldIndexJob job;
    private ElasticSearch elasticSearch;

    @BeforeEach
    void createCleanupOldIndexJob() {
        elasticSearch = mock(ElasticSearch.class);

        job = new CleanupOldIndexJob();
        job.elasticSearch = elasticSearch;
        job.indexService = new IndexService();
    }

    @Test
    void cleanup() {
        var state = new ClusterStateResponse();
        state.metadata = new ClusterStateResponse.Metadata();
        state.metadata.indices = Map.of("action-2017.10.01", index(ClusterStateResponse.IndexState.OPEN),
                "action-2017.10.30", index(ClusterStateResponse.IndexState.CLOSE),
                "action-2017.11.01", index(ClusterStateResponse.IndexState.OPEN));
        when(elasticSearch.state()).thenReturn(state);

        job.cleanup(LocalDate.of(2017, 11, 8));

        verify(elasticSearch).deleteIndex("action-2017.10.01");
        verify(elasticSearch).closeIndex("action-2017.11.01");
    }

    private ClusterStateResponse.Index index(ClusterStateResponse.IndexState state) {
        ClusterStateResponse.Index index = new ClusterStateResponse.Index();
        index.state = state;
        return index;
    }
}
