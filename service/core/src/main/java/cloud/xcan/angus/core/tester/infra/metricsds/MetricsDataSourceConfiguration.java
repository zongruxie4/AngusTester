package cloud.xcan.angus.core.tester.infra.metricsds;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.repository.BaseRepositoryImpl;
import cloud.xcan.angus.core.tester.infra.metricsds.MetricsDataSourceConfiguration.JpaEnableMetricsMysqlConfiguration;
import cloud.xcan.angus.core.tester.infra.metricsds.config.MetricsDataSourceExtraProperties;
import cloud.xcan.angus.core.tester.infra.metricsds.config.MetricsDataSourceProperties;
import cloud.xcan.angus.core.tester.infra.metricsds.config.MetricsHikariProperties;
import cloud.xcan.angus.jpa.HibernateJpaConfiguration;
import cloud.xcan.angus.spec.experimental.Assert;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@ConditionalOnClass({LocalContainerEntityManagerFactoryBean.class, EntityManager.class,
    SessionImplementor.class})
@EnableConfigurationProperties({JpaProperties.class, MetricsDataSourceProperties.class,
    MetricsHikariProperties.class, MetricsDataSourceExtraProperties.class})
@Import({HibernateJpaConfiguration.class, JpaEnableMetricsMysqlConfiguration.class})
public class MetricsDataSourceConfiguration {

  public final static String MASTER_DATASOURCE_KEY = "dataSource";
  public final static String METRICS_DATASOURCE_PREFIX = "metrics";
  public final static String METRICS_DATASOURCE_SUFFIX = "DataSource";
  public final static String METRICS0_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "0" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS1_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "1" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS2_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "2" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS3_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "3" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS4_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "4" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS5_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "5" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS6_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "6" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS7_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "7" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS8_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "8" + METRICS_DATASOURCE_SUFFIX;
  public final static String METRICS9_DATASOURCE_KEY = METRICS_DATASOURCE_PREFIX + "9" + METRICS_DATASOURCE_SUFFIX;

  public final static String METRICS_ENTITY_MANAGER_FACTORY_NAMETRICS_ENTITY = "metricsEntityManagerFactory";

  @Resource
  private JpaProperties jpaProperties;

  @Resource
  private MetricsDataSourceProperties metricsDataSourceProperties;

  /**
   * Master dataSource {@see HibernateJpaAutoConfigurer}
   */
  @Resource(name = MASTER_DATASOURCE_KEY)
  private DataSource dataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS0_DATASOURCE_KEY)
  private DataSource metrics0DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS1_DATASOURCE_KEY)
  private DataSource metrics1DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS2_DATASOURCE_KEY)
  private DataSource metrics2DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS3_DATASOURCE_KEY)
  private DataSource metrics3DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS4_DATASOURCE_KEY)
  private DataSource metrics4DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS5_DATASOURCE_KEY)
  private DataSource metrics5DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS6_DATASOURCE_KEY)
  private DataSource metrics6DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS7_DATASOURCE_KEY)
  private DataSource metrics7DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS8_DATASOURCE_KEY)
  private DataSource metrics8DataSource;

  @Autowired(required = false)
  @Qualifier(value = METRICS9_DATASOURCE_KEY)
  private DataSource metrics9DataSource;

  @Bean(value = METRICS0_DATASOURCE_KEY)
  @Qualifier(METRICS0_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=1")
  public DataSource metrics0DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics0 datasource...");
    return createDataSource(0, mds, mdse, mk);
  }

  @Bean(value = METRICS1_DATASOURCE_KEY)
  @Qualifier(METRICS1_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=2")
  public DataSource metrics1DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics1 datasource...");
    return createDataSource(1, mds, mdse, mk);
  }

  @Bean(value = METRICS2_DATASOURCE_KEY)
  @Qualifier(METRICS2_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=3")
  public DataSource metrics2DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics2 datasource...");
    return createDataSource(2, mds, mdse, mk);
  }

  @Bean(value = METRICS3_DATASOURCE_KEY)
  @Qualifier(METRICS3_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=4")
  public DataSource metrics3DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics3 datasource...");
    return createDataSource(3, mds, mdse, mk);
  }

  @Bean(value = METRICS4_DATASOURCE_KEY)
  @Qualifier(METRICS4_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=5")
  public DataSource metrics4DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics4 datasource...");
    return createDataSource(4, mds, mdse, mk);
  }

  @Bean(value = METRICS5_DATASOURCE_KEY)
  @Qualifier(METRICS5_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=6")
  public DataSource metrics5DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics5 datasource...");
    return createDataSource(5, mds, mdse, mk);
  }

  @Bean(value = METRICS6_DATASOURCE_KEY)
  @Qualifier(METRICS6_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=7")
  public DataSource metrics6DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics6 datasource...");
    return createDataSource(6, mds, mdse, mk);
  }

  @Bean(value = METRICS7_DATASOURCE_KEY)
  @Qualifier(METRICS7_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=8")
  public DataSource metrics7DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics7 datasource...");
    return createDataSource(7, mds, mdse, mk);
  }

  @Bean(value = METRICS8_DATASOURCE_KEY)
  @Qualifier(METRICS8_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=9")
  public DataSource metrics8DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics8 datasource...");
    return createDataSource(8, mds, mdse, mk);
  }

  @Bean(value = METRICS9_DATASOURCE_KEY)
  @Qualifier(METRICS9_DATASOURCE_KEY)
  @ConditionalOnExpression("${xcan.datasource.metrics.shardDbNum}>=10")
  public DataSource metrics9DataSource(MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    log.info("Create metrics9 datasource...");
    return createDataSource(9, mds, mdse, mk);
  }

  private HikariDataSource createDataSource(int index, MetricsDataSourceProperties mds,
      MetricsDataSourceExtraProperties mdse, MetricsHikariProperties mk) {
    DataSourceBuilder<HikariDataSource> builder = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .username(mds.getUsername())
        .password(mds.getPassword());
    if ("postgres".equalsIgnoreCase(mdse.getDbType())) {
      builder.driverClassName(mds.getPostgresql().getDriverClassName())
          .url(mds.getPostgresql().getUrls()[index]);
    } else {// mysql
      builder.driverClassName(mds.getMysql().getDriverClassName())
          .url(mds.getMysql().getUrls()[index]);
    }
    HikariDataSource dataSource = builder.build();
    dataSource.setPoolName(mk.getPoolName() + "Metrics" + index);
    dataSource.setMaximumPoolSize(mk.getMaximumPoolSize());
    dataSource.setMinimumIdle(mk.getMinimumIdle());
    dataSource.setMaxLifetime(mk.getMaxLifetime());
    dataSource.setLeakDetectionThreshold(30000);
    dataSource.setConnectionTimeout(mk.getConnectionTimeout());
    dataSource.setValidationTimeout(mk.getValidationTimeout());
    dataSource.setIdleTimeout(mk.getIdleTimeout());
    dataSource.setInitializationFailTimeout(mk.getInitializationFailTimeout());
    dataSource.setAutoCommit(mk.isAutoCommit());
    dataSource.setReadOnly(mk.isReadOnly());
    dataSource.setConnectionTestQuery(mk.getConnectionTestQuery());
    return dataSource;
  }

  @Bean(name = "routingDataSource")
  public DataSource routingDataSource(MetricsDataSourceProperties mds) {
    MetricsDynamicDataSourceRouter proxy = new MetricsDynamicDataSourceRouter();
    Map<Object, Object> targetDataSources = new HashMap<>();
    if (nonNull(metrics0DataSource)) {
      targetDataSources.put(METRICS0_DATASOURCE_KEY, metrics0DataSource);
    }
    if (nonNull(metrics1DataSource)) {
      targetDataSources.put(METRICS1_DATASOURCE_KEY, metrics1DataSource);
    }
    if (nonNull(metrics2DataSource)) {
      targetDataSources.put(METRICS2_DATASOURCE_KEY, metrics2DataSource);
    }
    if (nonNull(metrics3DataSource)) {
      targetDataSources.put(METRICS3_DATASOURCE_KEY, metrics3DataSource);
    }
    if (nonNull(metrics4DataSource)) {
      targetDataSources.put(METRICS4_DATASOURCE_KEY, metrics4DataSource);
    }
    if (nonNull(metrics5DataSource)) {
      targetDataSources.put(METRICS5_DATASOURCE_KEY, metrics5DataSource);
    }
    if (nonNull(metrics6DataSource)) {
      targetDataSources.put(METRICS6_DATASOURCE_KEY, metrics6DataSource);
    }
    if (nonNull(metrics7DataSource)) {
      targetDataSources.put(METRICS7_DATASOURCE_KEY, metrics7DataSource);
    }
    if (nonNull(metrics8DataSource)) {
      targetDataSources.put(METRICS8_DATASOURCE_KEY, metrics8DataSource);
    }
    if (nonNull(metrics9DataSource)) {
      targetDataSources.put(METRICS9_DATASOURCE_KEY, metrics9DataSource);
    }
    Assert.assertTrue(targetDataSources.size() == mds.getShardDbNum(),
        "Metrics database num exception");
    Assert.assertTrue(mds.getShardDbNum() >= 1 && mds.getShardDbNum() <= 10,
        "shardDbNum value range: 1-10");
    Assert.assertTrue(mds.getShardTabledNum() >= 1 && mds.getShardTabledNum() <= 50,
        "shardTabledNum value range: 1-50");
    targetDataSources.put(MASTER_DATASOURCE_KEY, dataSource);
    proxy.setDefaultTargetDataSource(dataSource);
    proxy.setTargetDataSources(targetDataSources);
    return proxy;
  }

  @Bean(name = METRICS_ENTITY_MANAGER_FACTORY_NAMETRICS_ENTITY)
  public LocalContainerEntityManagerFactoryBean metricsEntityManagerFactory(
      EntityManagerFactoryBuilder builder,
      @Qualifier("routingDataSource") DataSource routingDataSource) {
    Map<String, String> properties = jpaProperties.getProperties();
    properties.put("hibernate.physical_naming_strategy",
        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
    return builder
        .dataSource(routingDataSource)
        .properties(properties)
          //.persistenceUnit("sample")
        .packages(metricsDataSourceProperties.getEntityPackages())
        .build();
  }

  @Bean(name = "metricsTransactionManager")
  public PlatformTransactionManager transactionManager(
      @Qualifier(METRICS_ENTITY_MANAGER_FACTORY_NAMETRICS_ENTITY) EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @EnableTransactionManagement
  @EnableJpaRepositories(
      repositoryBaseClass = BaseRepositoryImpl.class,
      entityManagerFactoryRef = METRICS_ENTITY_MANAGER_FACTORY_NAMETRICS_ENTITY,
      transactionManagerRef = "metricsTransactionManager",
      basePackages = {"cloud.xcan.angus.core.tester.infra.persistence.mysql.metrics"})
  @ConditionalOnProperty(name = "xcan.datasource.extra.dbType", havingValue = "mysql")
  protected static class JpaEnableMetricsMysqlConfiguration {

  }

  @EnableTransactionManagement
  @EnableJpaRepositories(
      repositoryBaseClass = BaseRepositoryImpl.class,
      entityManagerFactoryRef = METRICS_ENTITY_MANAGER_FACTORY_NAMETRICS_ENTITY,
      transactionManagerRef = "metricsTransactionManager",
      basePackages = {"cloud.xcan.angus.core.tester.infra.persistence.postgres.metrics"})
  @ConditionalOnProperty(name = "xcan.datasource.extra.dbType", havingValue = "postgres")
  protected static class MetricsJpaEnableMetricsPostgresConfiguration {

  }

}
