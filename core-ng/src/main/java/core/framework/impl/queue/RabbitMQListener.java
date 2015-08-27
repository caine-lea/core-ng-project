package core.framework.impl.queue;

import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import core.framework.api.module.MessageHandlerConfig;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessageHandler;
import core.framework.api.util.Charsets;
import core.framework.api.util.Exceptions;
import core.framework.api.util.JSON;
import core.framework.api.util.Maps;
import core.framework.api.util.Strings;
import core.framework.api.util.Threads;
import core.framework.impl.concurrent.Executor;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public class RabbitMQListener implements MessageHandlerConfig {
    static final String HEADER_TRACE = "trace";
    static final String HEADER_CLIENT_IP = "clientIP";

    private final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Thread listenerThread;
    private int maxConcurrentHandlers = 10;
    private Semaphore semaphore;

    private final String queue;
    private final Executor executor;
    private final LogManager logManager;
    private final MessageValidator validator;
    private final Map<String, MessageHandler> handlers = Maps.newHashMap();
    private final Map<String, Class> messageClasses = Maps.newHashMap();

    public RabbitMQListener(RabbitMQ rabbitMQ, String queue, Executor executor, MessageValidator validator, LogManager logManager) {
        this.executor = executor;
        this.queue = queue;
        this.validator = validator;
        this.logManager = logManager;

        listenerThread = new Thread(() -> {
            logger.info("rabbitMQ message listener started, queue={}", queue);
            while (!stop.get()) {
                try (RabbitMQConsumer consumer = rabbitMQ.consumer(queue, maxConcurrentHandlers)) {
                    pullMessages(consumer);
                } catch (ShutdownSignalException | InterruptedException e) {
                    // pass thru for stopping
                } catch (Throwable e) {
                    logger.error("failed to pull message, retry in 30 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(30));
                }
            }
        });
        listenerThread.setName("rabbitMQ-listener-" + queue);
    }

    @Override
    public <T> MessageHandlerConfig handle(Class<T> messageClass, MessageHandler<T> handler) {
        if (handler.getClass().isSynthetic())
            throw Exceptions.error("handler class must not be anonymous or lambda, please create static class, handlerClass={}", handler.getClass().getCanonicalName());

        validator.register(messageClass);
        String messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        messageClasses.put(messageType, messageClass);
        handlers.put(messageType, handler);
        return this;
    }

    @Override
    public MessageHandlerConfig maxConcurrentHandlers(int maxConcurrentHandlers) {
        this.maxConcurrentHandlers = maxConcurrentHandlers;
        return this;
    }

    private void pullMessages(RabbitMQConsumer consumer) throws InterruptedException {
        while (!stop.get()) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            semaphore.acquire(); // acquire permit right before submit, to avoid permit failing to release back due to exception in between
            executor.submit(() -> {
                try {
                    process(delivery);
                    return null;
                } finally {
                    semaphore.release(); // release permit first, not let exception from basic ack bypass it.
                    consumer.acknowledge(delivery.getEnvelope().getDeliveryTag());
                }
            });
        }
    }

    public void start() {
        semaphore = new Semaphore(maxConcurrentHandlers, false);
        listenerThread.start();
    }

    public void stop() {
        logger.info("stop rabbitMQ message listener, queue={}", queue);
        stop.set(true);
        listenerThread.interrupt();
    }

    private <T> void process(QueueingConsumer.Delivery delivery) throws Exception {
        ActionLog actionLog = logManager.currentActionLog();
        actionLog.action("queue/" + queue);

        String messageBody = new String(delivery.getBody(), Charsets.UTF_8);
        String messageType = delivery.getProperties().getType();
        actionLog.context("messageType", messageType);

        logger.debug("message={}", messageBody);

        if (Strings.empty(messageType)) throw new Error("messageType must not be empty");

        actionLog.refId(delivery.getProperties().getCorrelationId());

        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if (headers != null) {
            if ("true".equals(String.valueOf(headers.get(HEADER_TRACE)))) {
                actionLog.triggerTraceLog();
            }

            Object clientIP = headers.get(HEADER_CLIENT_IP);
            if (clientIP != null) {
                actionLog.context("clientIP", clientIP);
            }
        }

        String appId = delivery.getProperties().getAppId();
        if (appId != null) {
            actionLog.context("client", appId);
        }

        @SuppressWarnings("unchecked")
        Class<T> messageClass = messageClasses.get(messageType);
        if (messageClass == null) {
            throw Exceptions.error("can not find message class, messageType={}", messageType);
        }
        T message = JSON.fromJSON(messageClass, messageBody);
        validator.validate(message);

        @SuppressWarnings("unchecked")
        MessageHandler<T> handler = handlers.get(messageType);
        actionLog.context("handler", handler.getClass().getCanonicalName());
        handler.handle(message);
    }
}
