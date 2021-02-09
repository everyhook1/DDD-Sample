/**
 * @(#)binlog.java, 1月 13, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.test;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.RotateEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.example.BinlogFileOffset;
import org.example.dbLog.OffsetKafkaStore;
import org.example.dbLog.mq.CdcProducer;
import org.example.dbLog.mq.KafkaConfigurationProperties;
import org.example.dbLog.mq.KafkaConsumerConfigurationProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;


/**
 * @author liubin01
 */
@Slf4j
public class binlog {
    static OffsetKafkaStore offsetKafkaStore;

    @BeforeAll
    public static void init() {
        String bookServers = "127.0.0.1:9092";
        CdcProducer cdcProducer = new CdcProducer(bookServers);
        KafkaConfigurationProperties properties = new KafkaConfigurationProperties(bookServers, 3000);
        offsetKafkaStore = new OffsetKafkaStore(
                "dbHistoryTopicName", "offsetStoreKey", cdcProducer, properties,
                KafkaConsumerConfigurationProperties.empty());
    }

    static long serverId = 10002L;

    @Test
    public void resetKafka() {
        offsetKafkaStore.save(new BinlogFileOffset());
    }

    @Test
    public void readWithOffset() throws IOException {
        BinaryLogClient client = new BinaryLogClient("tutor-pub-book-editor-test", 3306, "tutor", "tutor123");
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        client.setEventDeserializer(eventDeserializer);
        Optional<BinlogFileOffset> binlogFileOffset = offsetKafkaStore.getLastBinlogFileOffset();
        BinlogFileOffset bfo = binlogFileOffset.orElse(new BinlogFileOffset("", 4L));
        log.info("mysql binlog starting offset {}", bfo);
        client.setBinlogFilename(bfo.getBinlogFilename());
        client.setBinlogPosition(bfo.getOffset());
        client.setServerId(serverId);
        BinlogFileOffset offset = new BinlogFileOffset();
        client.registerEventListener(event -> {
            offset.setOffset(extractOffset(event));
            if (event.getHeader().getEventType() == EventType.ROTATE) {
                RotateEventData d = event.getData();
                offset.setBinlogFilename(d.getBinlogFilename());
            }
            log.info("{}->\n {}", event, offset);
            offsetKafkaStore.save(offset);
        });
        client.connect();
    }


    private long extractOffset(Event event) {
        return ((EventHeaderV4) event.getHeader()).getPosition();
    }
}
