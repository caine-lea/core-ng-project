package core.log;

import core.framework.internal.log.message.ActionLogMessage;
import core.framework.internal.log.message.EventMessage;
import core.framework.internal.log.message.LogTopics;
import core.framework.internal.log.message.StatMessage;
import core.framework.module.App;
import core.framework.search.module.SearchConfig;
import core.log.domain.ActionDocument;
import core.log.domain.EventDocument;
import core.log.domain.StatDocument;
import core.log.domain.TraceDocument;
import core.log.job.CleanupOldIndexJob;
import core.log.kafka.ActionLogMessageHandler;
import core.log.kafka.EventMessageHandler;
import core.log.kafka.StatMessageHandler;
import core.log.service.ActionService;
import core.log.service.ElasticSearchAppender;
import core.log.service.EventService;
import core.log.service.IndexService;
import core.log.service.StatService;

import java.time.Duration;
import java.time.LocalTime;

/**
 * @author neo
 */
public class LogProcessorApp extends App {
    @Override
    protected void initialize() {
        loadProperties("sys.properties");

        SearchConfig search = config(SearchConfig.class);
        search.host(requiredProperty("sys.elasticsearch.host"));
        search.timeout(Duration.ofSeconds(20)); // use longer timeout/slowES threshold as log indexing can be slower with large batches
        search.slowOperationThreshold(Duration.ofSeconds(10));
        search.type(ActionDocument.class);
        search.type(TraceDocument.class);
        search.type(StatDocument.class);
        search.type(EventDocument.class);

        IndexService indexService = bind(IndexService.class);
        bind(ActionService.class);
        bind(StatService.class);
        bind(EventService.class);

        property("sys.log.appender").ifPresent(appender -> {
            if ("console".equals(appender)) {
                log().appendToConsole();
            } else if ("elasticsearch".equals(appender)) {
                log().appender(bind(ElasticSearchAppender.class));
            }
        });

        onStartup(indexService::createIndexTemplatesUntilSuccess);

        kafka().uri(requiredProperty("sys.kafka.uri"));
        kafka().poolSize(Runtime.getRuntime().availableProcessors() == 1 ? 1 : 2);
        kafka().minPoll(1024 * 1024, Duration.ofMillis(500));           // try to get at least 1M message
        kafka().maxPoll(2000, 3 * 1024 * 1024);     // get 3M message at max
        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(ActionLogMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_STAT, StatMessage.class, bind(StatMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_EVENT, EventMessage.class, bind(EventMessageHandler.class));

        schedule().dailyAt("cleanup-old-index-job", bind(CleanupOldIndexJob.class), LocalTime.of(1, 0));
    }
}
