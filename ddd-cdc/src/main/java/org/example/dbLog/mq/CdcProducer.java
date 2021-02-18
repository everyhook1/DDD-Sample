/**
 * @(#)KafkaProducer.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.dbLog.mq;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author liubin01
 */
public class CdcProducer {
    private Producer<String, byte[]> producer;
    private Properties producerProps;
    private StringSerializer stringSerializer = new StringSerializer();

    public CdcProducer(String bootstrapServers) {

        producerProps = new Properties();
        producerProps.put("bootstrap.servers", bootstrapServers);
        producerProps.put("acks", "all");
        producerProps.put("retries", 0);
        producerProps.put("batch.size", 16384);
        producerProps.put("linger.ms", 1);
        producerProps.put("buffer.memory", 33554432);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        producer = new KafkaProducer<>(producerProps);
    }

    public CompletableFuture<?> send(String topic, String key, String body) {
        return send(topic, key, body.getBytes(StandardCharsets.UTF_8));
    }

    public CompletableFuture<?> send(String topic, int partition, String key, String body) {
        return send(topic, partition, key, body.getBytes(StandardCharsets.UTF_8));
    }

    public CompletableFuture<?> send(String topic, String key, byte[] bytes) {
        return send(new ProducerRecord<>(topic, key, bytes));
    }

    public CompletableFuture<?> send(String topic, int partition, String key, byte[] bytes) {
        return send(new ProducerRecord<>(topic, partition, key, bytes));
    }

    private CompletableFuture<?> send(ProducerRecord<String, byte[]> producerRecord) {
        CompletableFuture<Object> result = new CompletableFuture<>();
        producer.send(producerRecord, (metadata, exception) -> {
            if (exception == null)
                result.complete(metadata);
            else
                result.completeExceptionally(exception);
        });

        return result;
    }

    public void close() {
        producer.close(1, TimeUnit.SECONDS);
    }
}
