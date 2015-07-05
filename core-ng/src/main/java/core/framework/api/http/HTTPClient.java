package core.framework.api.http;

import core.framework.api.log.ActionLogContext;
import core.framework.api.util.InputStreams;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * @author neo
 */
public final class HTTPClient {
    private final Logger logger = LoggerFactory.getLogger(HTTPClient.class);

    private final CloseableHttpClient client;
    private final long slowTransactionThresholdInMs;

    public HTTPClient(CloseableHttpClient client, long slowTransactionThresholdInMs) {
        this.client = client;
        this.slowTransactionThresholdInMs = slowTransactionThresholdInMs;
    }

    public void shutdown() {
        logger.info("shutdown http client");
        try {
            client.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public HTTPResponse execute(HTTPRequest request) {
        StopWatch watch = new StopWatch();
        logger.debug("send http request, uri={}", request.uri());
        HttpUriRequest httpRequest = request.builder.build();
        try (CloseableHttpResponse response = client.execute(httpRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            logger.debug("responseStatus={}", statusCode);

            Map<String, String> headers = Maps.newHashMap();
            for (Header header : response.getAllHeaders()) {
                logger.debug("[responseHeader] {}={}", header.getName(), header.getValue());
                headers.putIfAbsent(header.getName(), header.getValue());
            }

            byte[] bytes = InputStreams.bytes(response.getEntity().getContent());
            HTTPResponse httpResponse = new HTTPResponse(HTTPStatus.parse(statusCode), headers, bytes);

            logResponseText(httpResponse);

            return httpResponse;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("http", elapsedTime);
            logger.debug("execute, elapsedTime={}", elapsedTime);
            if (elapsedTime > slowTransactionThresholdInMs) logger.warn("slow http transaction detected");
        }
    }

    private void logResponseText(HTTPResponse httpResponse) {
        String contentType = httpResponse.contentType();
        if (contentType != null && (contentType.contains("text") || contentType.contains("json")))
            logger.debug("responseText={}", httpResponse.text());
    }
}
