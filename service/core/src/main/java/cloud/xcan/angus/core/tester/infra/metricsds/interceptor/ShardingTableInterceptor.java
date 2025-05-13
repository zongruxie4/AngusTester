package cloud.xcan.angus.core.tester.infra.metricsds.interceptor;

import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceContextHolder.checkDataSourceShard;
import static cloud.xcan.angus.core.tester.infra.metricsds.TableSchemaManager.SHARD_TABLE_NAME_SPLIT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.jpa.interceptor.TenantInterceptor;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceContextHolder;
import cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceContextHolder.Shard;
import cloud.xcan.angus.core.tester.infra.metricsds.ShardingTable;
import cloud.xcan.angus.core.tester.infra.metricsds.TableSchemaManager;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ShardingTableInterceptor extends TenantInterceptor {

  public static Set<String> shardingTables = new CopyOnWriteArraySet<>();

  //public static Pattern selectPattern = Pattern
  //    .compile("select\\s.+from\\s(.+)where\\s(.*)", Pattern.CASE_INSENSITIVE);
  public static Pattern selectPattern = Pattern
      .compile("select\\s.+from\\s(.+)", Pattern.CASE_INSENSITIVE);
  public static Pattern insertPattern = Pattern
      .compile("insert\\sinto\\s(.+)\\(.*\\)\\s.*", Pattern.CASE_INSENSITIVE);
  public static Pattern updatePattern = Pattern
      .compile("update\\s(.+)set\\s.*", Pattern.CASE_INSENSITIVE);
  //public static Pattern deletePattern = Pattern
  //    .compile("delete\\sfrom\\s(.+)where\\s(.*)", Pattern.CASE_INSENSITIVE);
  public static Pattern deletePattern = Pattern
      .compile("delete\\sfrom\\s(.+)", Pattern.CASE_INSENSITIVE);
  private TableSchemaManager schemaManager;

  public ShardingTableInterceptor() {
    if (isEmpty(shardingTables)) {
      shardingTables = loadAnnotationTable(
          "cloud.xcan.angus.core.tester.infra.metricsds.domain", ShardingTable.class);
    }
  }

  @Override
  public String inspect(String sql) {
    String tableName = matchTableName(sql);
    if (tableName == null) {
      return super.inspect(sql);
    }

    if (!checkDataSourceShard()) {
      return super.inspect(sql);
    }

    tableName = tableName.substring(0, tableName.indexOf(" "));
    if (!shardingTables.contains(tableName)) {
      // Exclude isCreatedShardTable for "Select * from shard_tables where table_name like ? tenantId ? "
      return super.inspect(sql);
    }

    String realTabledName = getRealTableName(tableName);
    if (!getSchemaManager().isCreatedShardTable(realTabledName)) {
      getSchemaManager().checkAndCreate();
    }

    return super.inspect(sql.replaceAll(tableName, "`" + realTabledName + "`"));
  }

  @NotNull
  private String getRealTableName(String tableName) {
    Shard shard = MetricsDataSourceContextHolder.getShard();
    return shard.tableSecondIndex() > 0 ? tableName + SHARD_TABLE_NAME_SPLIT + shard.tenantId()
        + SHARD_TABLE_NAME_SPLIT + shard.tableSecondIndex()
        : tableName + SHARD_TABLE_NAME_SPLIT + shard.tenantId();
  }

  public static String matchTableName(String sql) {
    Matcher matcher;
    if (sql.startsWith("select") || sql.startsWith("SELECT")) {
      matcher = selectPattern.matcher(sql);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    if (sql.startsWith("insert") || sql.startsWith("INSERT")) {
      matcher = insertPattern.matcher(sql);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    if (sql.startsWith("update") || sql.startsWith("UPDATE")) {
      matcher = updatePattern.matcher(sql);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    if (sql.startsWith("delete") || sql.startsWith("DELETE")) {
      matcher = deletePattern.matcher(sql);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return null;
  }

  public synchronized TableSchemaManager getSchemaManager() {
    if (schemaManager == null) {
      schemaManager = SpringContextHolder.getBean(TableSchemaManager.class);
    }
    return schemaManager;
  }

}
