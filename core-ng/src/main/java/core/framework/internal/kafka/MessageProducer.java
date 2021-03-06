package core.framework.internal.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author neo
 */
public interface MessageProducer {
    void send(ProducerRecord<byte[], byte[]> record);
}
