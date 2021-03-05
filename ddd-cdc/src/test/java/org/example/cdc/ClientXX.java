/**
 * @(#)ClientXX.java, 2月 25, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;
import com.github.shyiko.mysql.binlog.event.RotateEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.example.BinlogFileOffset;
import org.example.binlog.BinlogEntry;
import org.example.binlog.MySqlBinlogEntryExtractor;
import org.example.dbLog.OffsetKafkaStore;
import org.example.dbLog.mq.CdcProducer;
import org.example.dbLog.mq.KafkaConfigurationProperties;
import org.example.dbLog.mq.KafkaConsumerConfigurationProperties;
import org.example.util.JsonUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author liubin01
 */
@Slf4j
public class ClientXX {

    private static final String zkServers = "127.0.0.1";
    private static final String brokerServers = "127.0.0.1:9092";
    private static final CopyOnWriteArraySet<CuratorFramework> clientSets = new CopyOnWriteArraySet<>();

    private static final JsonMapper mapper = new JsonMapper();

    private static DataSource getDatasource() {
        Properties config = new Properties();

        config.setProperty("jdbcUrl", config.getProperty("jdbcUrl", "jdbc:mysql://localhost/eventuate"));
        config.setProperty("driverClassName", config.getProperty("driverClassName", "com.mysql.cj.jdbc.Driver"));
        config.setProperty("username", config.getProperty("username", "root"));
        config.setProperty("password", config.getProperty("password", "rootpassword"));
        config.setProperty("initializationFailTimeout", String.valueOf(Long.MAX_VALUE));
        config.setProperty("connectionTestQuery", "select 1");

        return new HikariDataSource(new HikariConfig(config));
    }

    private CuratorFramework getClient() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkServers,
                new RetryNTimes(3, 100));
        client.start();
        clientSets.add(client);
        return client;
    }

    @AfterAll
    public static void closeAll() {
        clientSets.forEach(CuratorFramework::close);
    }

    @Test
    public void addSee() throws Exception {
        String path = "/cdc";
        CuratorFramework client = getClient();
        Stat stat = client.checkExists().forPath(path);
        if (stat == null) {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .inBackground((client1, event) -> {
                        log.info("Code：" + event.getResultCode());
                        log.info("Type：" + event.getType());
                        log.info("Path：" + event.getPath());
                    }).forPath(path, "你好世界".getBytes());

        }

        TreeCache treeCache = new TreeCache(client, path);
        treeCache.start();
        treeCache.getListenable().addListener((curatorFramework, event) -> {
            switch (event.getType()) {
                case NODE_ADDED:
                    log.info("NODE_ADDED {}", event.getData());
                    new Thread(new RLeader(event.getData(), client)).start();
                    break;
                case NODE_UPDATED:
                    log.info("NODE_UPDATED {}", event.getData());
                    break;
                case NODE_REMOVED:
                    log.info("NODE_REMOVED {}", event.getData());
                    break;
                case CONNECTION_SUSPENDED:
                    log.info("CONNECTION_SUSPENDED {}", event.getData());
                    break;
                case CONNECTION_RECONNECTED:
                    log.info("CONNECTION_RECONNECTED {}", event.getData());
                    break;
                case CONNECTION_LOST:
                    log.info("CONNECTION_LOST {}", event.getData());
                    break;
                case INITIALIZED:
                    log.info("INITIALIZED {}", event.getData());
                    break;
                default:
                    break;
            }
        });

        while (true) {
            Thread.sleep(2000);
        }
    }


    @Test
    public void reset() throws Exception {
        CuratorFramework client = getClient();
        String path = "/cdc";
        Stat stat = client.checkExists().forPath(path);
        if (stat != null) {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }
        BinlogConnect connect = new BinlogConnect("localhost", 3306, "root", "rootpassword");
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/cdc/local-cdc", mapper.writeValueAsBytes(connect));
    }

    static class RLeader implements Runnable {

        ChildData data;
        CuratorFramework client;
        String path;
        BinlogConnect connect;


        RLeader(ChildData data, CuratorFramework client) throws IOException {
            this.data = data;
            this.path = "/cdc-leader" + data.getPath();
            this.client = client;
            this.connect = mapper.readValue(data.getData(), BinlogConnect.class);
        }

        @Override
        public void run() {
            LeaderSelector leaderSelector = new LeaderSelector(client, path, new LeaderSelectorListener() {
                @Override
                public void takeLeadership(CuratorFramework client) throws Exception {
                    log.info("抢占 {} 成功", path);
                    BinlogReader reader = new BinlogReader(connect);
                    reader.addBinlogHandlers(new BinlogHandler());
                    reader.start();
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    log.info("抢占 {},状态变更{}", path, newState);
                }
            });

            leaderSelector.autoRequeue();
            leaderSelector.start();
        }
    }

    static class BinlogReader {
        BinlogConnect connect;
        OffsetKafkaStore offsetKafkaStore;
        List<BinlogHandler> binlogHandlers;
        BinaryLogClient client;
        private MySqlBinlogEntryExtractor extractor;
        private final Map<Long, TableMapEventData> tableMapEventByTableId = new HashMap<>();

        public BinlogReader(BinlogConnect connect) {
            this.connect = connect;
        }

        public void addBinlogHandlers(BinlogHandler handler) {
            if (CollectionUtils.isEmpty(binlogHandlers)) {
                binlogHandlers = new ArrayList<>();
            }
            binlogHandlers.add(handler);
        }

        public void start() throws IOException {
            extractor = new MySqlBinlogEntryExtractor(getDatasource());
            CdcProducer cdcProducer = new CdcProducer(brokerServers);
            KafkaConfigurationProperties properties = new KafkaConfigurationProperties(brokerServers, 3000);
            offsetKafkaStore = new OffsetKafkaStore(
                    connect.getHost() + "-dbHistoryTopicName",
                    "offsetStoreKey", cdcProducer, properties,
                    KafkaConsumerConfigurationProperties.empty());

            client = new BinaryLogClient(connect.getHost(),
                    connect.getPort(),
                    connect.getUser(),
                    connect.getPassword());
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
            client.setServerId(1);
            BinlogFileOffset offset = new BinlogFileOffset();
            client.registerEventListener(event -> {
                offset.setOffset(extractOffset(event));
                switch (event.getHeader().getEventType()) {
                    case TABLE_MAP:
                        TableMapEventData tableMapEvent = event.getData();
                        tableMapEventByTableId.put(tableMapEvent.getTableId(), tableMapEvent);
                        break;
                    case ROTATE:
                        RotateEventData d = event.getData();
                        offset.setBinlogFilename(d.getBinlogFilename());
                        break;
                    case EXT_WRITE_ROWS:
                    case WRITE_ROWS:
                        WriteRowsEventData data = event.getData();
                        TableMapEventData tableMapEventData = tableMapEventByTableId.get(data.getTableId());
                        SchemaAndTable schemaAndTable = new SchemaAndTable(tableMapEventData.getDatabase(), tableMapEventData.getTable());
                        BinlogEntry entry = extractor.extract(schemaAndTable, data, offset.getBinlogFilename(), offset.getOffset());
                        log.info("{}", entry);
                        break;

                }
                log.info("{}->\n {}", event, offset);
                offsetKafkaStore.save(offset);
            });
            client.connect();
        }
    }

    static class BinlogHandler {
        void process(Event event) {
            WriteRowsEventData data = event.getData();
            System.out.println(JsonUtils.writeValue(data));
        }
    }


    private static long extractOffset(Event event) {
        return ((EventHeaderV4) event.getHeader()).getPosition();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class BinlogConnect implements Serializable {
        private String host;
        private int port;
        private String user;
        private String password;
    }
}
