package core.framework.api.search;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Optional;

/**
 * @author neo
 */
public interface ElasticSearchType<T> {
    SearchResponse search(SearchSourceBuilder source);

    Optional<T> get(String id);

    void index(String id, T source);

    void update(String id, UpdateRequest request);

    void delete(String id);
}
