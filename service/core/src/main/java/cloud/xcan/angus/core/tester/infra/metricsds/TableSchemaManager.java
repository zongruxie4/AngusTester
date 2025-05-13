package cloud.xcan.angus.core.tester.infra.metricsds;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.domain.shard.ShardTables;
import cloud.xcan.angus.core.tester.domain.shard.ShardTablesRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.config.MetricsDataSourceProperties;
import cloud.xcan.angus.idgen.UidGenerator;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;

@Slf4j
public class TableSchemaManager {

  protected final AtomicReference<Boolean> initState = new AtomicReference<>(false);

  private final Set<String> CREATED_SHARD_TABLES = new CopyOnWriteArraySet<>();

  public static final List<String> TEMPLATE_TABLE_NAMES = Arrays
      .asList("node_usage", "node_disk_usage", "node_net_usage",
          "exec_sample", "exec_sample_error_cause", "exec_sample_content", "jvm_service_usage");

  public static final String SHARD_TABLE_NAME_SPLIT = "-";

  private final Map<String, String> TEMPLATE_TABLE_SCHEMA = new HashMap<>();

  @Resource
  private ShardTablesRepo shardTablesRepo;

  @Resource
  private UidGenerator uidGenerator;

  @Resource
  private MetricsDataSourceProperties metricsDataSourceProperties;

  /**
   * Initialize at startup
   */
  public void init() {
    if (initState.compareAndSet(false, true)) {
      try {
        List<ShardTables> shardTables = shardTablesRepo.findAll();
        if (isNotEmpty(shardTables)) {
          for (ShardTables shardTable : shardTables) {
            CREATED_SHARD_TABLES.add(shardTable.getTableName());
          }
        }
        log.info("Number of shard tables created: {}", CREATED_SHARD_TABLES.size());
        for (String templateTableName : TEMPLATE_TABLE_NAMES) {
          TEMPLATE_TABLE_SCHEMA.put(templateTableName, IOUtils.toString(
              Objects.requireNonNull(
                  this.getClass().getResourceAsStream("/schema/" + templateTableName + ".sql")),
              StandardCharsets.UTF_8));
        }
        log.info("Number of shard tables template: {}", TEMPLATE_TABLE_SCHEMA.size());
      } catch (Exception e) {
        log.error("Load shard table schema exception: {}", e.getMessage());
        initState.set(false);
      }
    }
  }

  public boolean isCreatedShardTable(String tableName) {
    return CREATED_SHARD_TABLES.contains(tableName);
  }

  public void checkAndCreate() {
    checkAndCreate(getOptTenantId(), metricsDataSourceProperties.getShardTabledNum(),
        metricsDataSourceProperties.getEnableTabledSecondIndex());
  }

  public void checkAndCreate(long tenantId, long shardTableNum, Boolean enableTabledSecondIndex) {
    if (log.isDebugEnabled()) {
      log.debug("Check tenant {} initialization number of shard tables:{}", tenantId,
          shardTableNum);
    }
    List<String> tenantTables = new ArrayList<>();
    if (Objects.nonNull(enableTabledSecondIndex) && enableTabledSecondIndex) {
      for (long i = 0; i < shardTableNum; i++) {
        for (String templateTableName : TEMPLATE_TABLE_NAMES) {
          tenantTables.add(templateTableName + SHARD_TABLE_NAME_SPLIT
              + tenantId + SHARD_TABLE_NAME_SPLIT + i);
        }
      }
    } else {
      for (String templateTableName : TEMPLATE_TABLE_NAMES) {
        tenantTables.add(templateTableName + SHARD_TABLE_NAME_SPLIT + tenantId);
      }
    }

    if (!CREATED_SHARD_TABLES.containsAll(tenantTables)) {
      // uninitialized or partially initialized
      List<String> leastTables = reloadByTenantId(tenantId);
      if (isNotEmpty(leastTables)) {
        CREATED_SHARD_TABLES.addAll(leastTables);
      }
      // Continue to find uninitialized tables after refreshing
      List<String> uninitialized = new ArrayList<>();
      for (String tenantTable : tenantTables) {
        if (!CREATED_SHARD_TABLES.contains(tenantTable)) {
          uninitialized.add(tenantTable);
        }
      }
      // Create uninitialized tables
      if (isNotEmpty(uninitialized)) {
        List<ShardTables> createdTables = createTenantTable(uninitialized);
        if (isNotEmpty(createdTables)) {
          CREATED_SHARD_TABLES.addAll(
              createdTables.stream().map(ShardTables::getTableName).toList());
        }
      }
    }
  }

  /**
   * Prevent being initialized by other instances(Node)
   */
  private List<String> reloadByTenantId(long tenantId) {
    List<ShardTables> shardTables = shardTablesRepo.findByTableNameLike(String.valueOf(tenantId));
    return ObjectUtils.isEmpty(shardTables) ? null
        : shardTables.stream().map(ShardTables::getTableName).collect(
            Collectors.toList());
  }

  private List<ShardTables> createTenantTable(List<String> createTenantTables) {
    List<ShardTables> shardTables = new ArrayList<>(createTenantTables.size());
    EntityManager em = SpringContextHolder.getBean(
        MetricsDataSourceConfiguration.METRICS_ENTITY_MANAGER_FACTORY_NAMETRICS_ENTITY,
        EntityManagerFactoryInfo.class).getNativeEntityManagerFactory().createEntityManager();
    EntityTransaction transaction = null;
    try {
      transaction = em.getTransaction();
      transaction.begin();
      for (String createTenantTable : createTenantTables) {
        String[] shards = createTenantTable.split(SHARD_TABLE_NAME_SPLIT);
        String schemaSql = TEMPLATE_TABLE_SCHEMA.get(shards[0]);
        long tenantId = Long.parseLong(shards[1]);
        ShardTables table = new ShardTables().setId(uidGenerator.getUID())
            .setDbIndex(Math.toIntExact(tenantId % metricsDataSourceProperties.getShardDbNum()))
            .setTableName(createTenantTable)
            .setTenantId(tenantId);
        Query query = em.createNativeQuery(schemaSql.replaceFirst(shards[0], createTenantTable));
        query.executeUpdate();
        shardTables.add(table);
      }
      shardTablesRepo.saveAll(shardTables);
      transaction.commit();
    } catch (Exception e) {
      log.error("Create tenant tables exception", e);
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
    } finally {
      em.close(); // Fix:: One em meaning one connection
    }
    return shardTables;
  }

}
