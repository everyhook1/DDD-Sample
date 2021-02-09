/**
 * @(#)MySqlBinaryLogClient.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.dbLog;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.RotateEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.BinlogFileOffset;
import org.example.OffsetStore;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

/**
 * @author liubin01
 */
@Slf4j
@Data
public class MySqlBinaryLogClient {

    private String dbUserName;
    private String dbPassword;
    private String host;
    private int port;
    private long serverId;

    private OffsetStore offsetStore;
    private static final Set<EventType> SUPPORTED_EVENTS = ImmutableSet.of(EventType.TABLE_MAP,
            EventType.ROTATE,
            EventType.WRITE_ROWS,
            EventType.EXT_WRITE_ROWS,
            EventType.UPDATE_ROWS,
            EventType.EXT_UPDATE_ROWS);

    private BinaryLogClient.EventListener eventListener;
    private BinaryLogClient client;

    private Optional<Exception> publishingException = Optional.empty();
    private Optional<Runnable> restartCallback = Optional.empty();
    private int rowsToSkip;
    private CountDownLatch stopCountDownLatch;
    private int connectionTimeoutInMilliseconds;
    private int maxAttemptsForBinlogConnection;
    private volatile boolean connected;
    protected volatile Optional<String> processingError = Optional.empty();
    private String binlogFilename;

    public MySqlBinaryLogClient() {

    }

    public void start() {
        log.info("Starting MySqlBinaryLogClient");
        client = new BinaryLogClient(host, port, dbUserName, dbPassword);
        client.setServerId(serverId);
        client.setKeepAliveInterval(5 * 1000);

        Optional<BinlogFileOffset> binlogFileOffset;
        try {
            binlogFileOffset = getStartingBinlogFileOffset();
        } catch (Exception e) {
            handleRestart(e);
            return;
        }
        BinlogFileOffset bfo = binlogFileOffset.orElse(new BinlogFileOffset("", 4L));
        rowsToSkip = bfo.getRowsToSkip();
        log.info("mysql binlog starting offset {}", bfo);
        client.setBinlogFilename(bfo.getBinlogFilename());
        client.setBinlogPosition(bfo.getOffset());

        client.setEventDeserializer(getEventDeserializer());

        eventListener = event -> handleBinlogEventWithErrorHandling(event, binlogFileOffset);

        client.registerEventListener(eventListener);

        connectWithRetriesOnFail();

        try {
            stopCountDownLatch.await();
        } catch (InterruptedException e) {
            handleProcessingFailException(e);
        }
        log.info("MySqlBinaryLogClient finished processing");
    }

    private void handleProcessingFailException(Exception e) {
        log.error("Stopping due to exception", e);
        processingError = Optional.of(e.getMessage());
        stopCountDownLatch.countDown();
        throw new RuntimeException(e);
    }

    private void handleBinlogEventWithErrorHandling(Event event, Optional<BinlogFileOffset> binlogFileOffset) {
        if (publishingException.isPresent()) {
            return;
        }
        try {
//            handleBinlogEvent(event, binlogFileOffset);
        } catch (Exception e) {
            handleRestart(e);
        }
    }

//    private void handleBinlogEvent(Event event, Optional<BinlogFileOffset> binlogFileOffset) {
//
//        switch (event.getHeader().getEventType()) {
//            case TABLE_MAP: {
//                TableMapEventData tableMapEvent = event.getData();
//
//                if (cdcMonitoringDao.isMonitoringTableChange(tableMapEvent.getDatabase(), tableMapEvent.getTable())) {
//                    cdcMonitoringTableId = Optional.of(tableMapEvent.getTableId());
//                    tableMapEventByTableId.put(tableMapEvent.getTableId(), tableMapEvent);
//                    break;
//                }
//
//                cdcMonitoringTableId = cdcMonitoringTableId.filter(id -> !id.equals(tableMapEvent.getTableId()));
//
//                SchemaAndTable schemaAndTable = new SchemaAndTable(tableMapEvent.getDatabase(), tableMapEvent.getTable());
//
//                boolean shouldHandleTable = binlogEntryHandlers
//                        .stream()
//                        .map(BinlogEntryHandler::getSchemaAndTable)
//                        .anyMatch(schemaAndTable::equals);
//
//                if (shouldHandleTable) {
//                    tableMapEventByTableId.put(tableMapEvent.getTableId(), tableMapEvent);
//                } else {
//                    tableMapEventByTableId.remove(tableMapEvent.getTableId());
//                }
//
//                dbLogMetrics.onBinlogEntryProcessed();
//
//                break;
//            }
//            case EXT_WRITE_ROWS: {
//                initProcessingInfo();
//                handleWriteRowsEvent(event, binlogFileOffset);
//                break;
//            }
//            case WRITE_ROWS: {
//                initProcessingInfo();
//                handleWriteRowsEvent(event, binlogFileOffset);
//                break;
//            }
//            case EXT_UPDATE_ROWS: {
//                handleUpdateRowsEvent(event);
//                break;
//            }
//            case UPDATE_ROWS: {
//                handleUpdateRowsEvent(event);
//                break;
//            }
//            case ROTATE: {
//                RotateEventData eventData = event.getData();
//                if (eventData != null) {
//                    binlogFilename = eventData.getBinlogFilename();
//                }
//                break;
//            }
//        }
//
//        saveEndingOffsetOfLastProcessedEvent(event);
//    }

    private void connectWithRetriesOnFail() {
        for (int i = 1; ; i++) {
            try {
                log.info("trying to connect to mysql binlog");
                client.connect(connectionTimeoutInMilliseconds);
                onConnected();
                log.info("connection to mysql binlog succeed");
                break;
            } catch (TimeoutException | IOException e) {
                onDisconnected();
                log.error("connection to mysql binlog failed");
                if (i == maxAttemptsForBinlogConnection) {
                    handleProcessingFailException(e);
                }
                try {
                    Thread.sleep(connectionTimeoutInMilliseconds);
                } catch (InterruptedException ex) {
                    handleProcessingFailException(ex);
                }
            } catch (Exception e) {
                handleProcessingFailException(e);
            }
        }
    }

    private void onDisconnected() {
        connected = false;
    }

    private void onConnected() {
        connected = true;
    }

    private EventDeserializer getEventDeserializer() {
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        return eventDeserializer;
    }

    private void handleRestart(Exception e) {
        log.error("Restarting due to exception", e);
        publishingException = Optional.of(e);
        restartCallback
                .orElseThrow(() -> new IllegalArgumentException("Restart callback is not specified, but restart is requsted"))
                .run();
    }

    private Optional<BinlogFileOffset> getStartingBinlogFileOffset() {
        Optional<BinlogFileOffset> binlogFileOffset = offsetStore.getLastBinlogFileOffset();
        log.info("mysql binlog client received offset from the offset store: {}", binlogFileOffset);
        return binlogFileOffset;
    }

    public void stop() {
        client.unregisterEventListener(eventListener);
        try {
            client.disconnect();
        } catch (IOException e) {
            log.error("Cannot stop the MySqlBinaryLogClient", e);
        }
    }
}
