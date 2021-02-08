/**
 * @(#)OffsetKafkaStore.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.dbLog;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.example.BinlogFileOffset;
import org.example.OffsetStore;
import org.example.dbLog.mq.CdcProducer;
import org.example.dbLog.mq.KafkaConfigurationProperties;
import org.example.dbLog.mq.KafkaConsumerConfigurationProperties;
import org.example.util.CompletableFutureUtil;
import org.example.util.JsonUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author liubin01
 */
@Slf4j
public class OffsetKafkaStore implements OffsetStore {

    protected final String dbHistoryTopicName;
    private final String offsetStoreKey;
    private final CdcProducer cdcProducer;
    protected KafkaConfigurationProperties kafkaConfigurationProperties;
    private final KafkaConsumerConfigurationProperties kafkaConsumerConfigurationProperties;

    private final static int N = 20;

    public OffsetKafkaStore(String dbHistoryTopicName,
                            String offsetStoreKey,
                            CdcProducer cdcProducer,
                            KafkaConfigurationProperties kafkaConfigurationProperties,
                            KafkaConsumerConfigurationProperties kafkaConsumerConfigurationProperties) {
        this.kafkaConfigurationProperties = kafkaConfigurationProperties;
        this.dbHistoryTopicName = dbHistoryTopicName;
        this.kafkaConsumerConfigurationProperties = kafkaConsumerConfigurationProperties;
        this.cdcProducer = cdcProducer;
        this.offsetStoreKey = offsetStoreKey;
    }

    @Override
    public Optional<BinlogFileOffset> getLastBinlogFileOffset() {
        try (KafkaConsumer<String, String> consumer = createConsumer()) {
            getPartitionsForTopicRetryOnFail(consumer, 10);
            consumer.subscribe(Collections.singletonList(dbHistoryTopicName));

            int count = N;
            BinlogFileOffset result = null;
            boolean lastRecordFound = false;
            while (!lastRecordFound) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                if (records.isEmpty()) {
                    count--;
                    if (count == 0)
                        lastRecordFound = true;
                } else {
                    count = N;
                    for (ConsumerRecord<String, String> record : records) {
                        BinlogFileOffset current = handleRecord(record);
                        if (current != null) {
                            result = current;
                        }
                    }
                }
            }
            return Optional.ofNullable(result);
        }
    }

    @Override
    public void save(BinlogFileOffset binlogFileOffset) {
        CompletableFuture<?> future = cdcProducer.send(
                dbHistoryTopicName,
                offsetStoreKey,
                JsonUtils.writeValue(binlogFileOffset)
        );
        CompletableFutureUtil.get(future);
    }

    public List<PartitionInfo> getPartitionsForTopicRetryOnFail(KafkaConsumer<String, String> consumer, int attempts) {
        try {
            return consumer.partitionsFor(dbHistoryTopicName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (attempts > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(e);
                }
                return getPartitionsForTopicRetryOnFail(consumer, attempts - 1);
            } else throw new RuntimeException(e);
        }
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaConfigurationProperties.getBootstrapServers());
        props.put("auto.offset.reset", "earliest");
        props.put("group.id", UUID.randomUUID().toString());
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.putAll(kafkaConsumerConfigurationProperties.getProperties());
        return new KafkaConsumer<>(props);
    }

    public BinlogFileOffset handleRecord(ConsumerRecord<String, String> record) {
        if (record.key().equals(offsetStoreKey)) {
            return JsonUtils.readValue(record.value(), BinlogFileOffset.class);
        }
        return null;
    }
}
