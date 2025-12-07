package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SYNC_CONFIG_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SYNC_CONFIG_ADD;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SYNC_CONFIG_DELETE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SYNC_CONFIG_UPDATE;
import static cloud.xcan.angus.extension.angustester.api.utils.OpenApiParser.checkAndParseOpenApi;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.lengthSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesSchemaCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesSyncCmd;
import cloud.xcan.angus.core.tester.application.converter.ServicesSyncConverter;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSyncQuery;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.sync.ServicesSync;
import cloud.xcan.angus.core.tester.domain.services.sync.ServicesSyncRepo;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.remote.message.ProtocolException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of service synchronization command operations.
 *
 * <p>This class provides comprehensive functionality for managing service
 * synchronization with external API documentation sources, including configuration management,
 * synchronization execution, and testing.</p>
 *
 * <p>It handles the complete lifecycle of synchronization from configuration
 * to execution, including quota management, activity logging, and error handling.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>External API documentation synchronization</li>
 *   <li>Synchronization configuration management</li>
 *   <li>OpenAPI content validation and testing</li>
 *   <li>Quota and permission management</li>
 *   <li>Activity logging for audit trails</li>
 * </ul></p>
 */
@Biz
@Slf4j
public class ServicesSyncCmdImpl extends CommCmd<ServicesSync, Long> implements ServicesSyncCmd {

  @Resource
  private ServicesSyncRepo servicesSyncRepo;
  @Resource
  private ServicesSyncQuery servicesSyncQuery;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesSchemaCmd servicesSchemaCmd;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Replaces or creates a synchronization configuration.
   *
   * <p>This method handles both creation and updates of synchronization
   * configurations. It performs quota validation and logs appropriate activities for audit
   * tracking.</p>
   *
   * @param serviceId the ID of the service
   * @param sync      the synchronization configuration
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(Long serviceId, ServicesSync sync) {
    new BizTemplate<Void>() {
      Services serviceDb;
      ServicesSync syncDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        // Verify service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Verify synchronization quota availability
        syncDb = servicesSyncQuery.find(serviceId, sync.getName());
        if (isNull(syncDb)) {
          servicesSyncQuery.checkSyncAddNumQuota(serviceId, 1);
        }
      }

      @Override
      protected Void process() {
        if (isNull(syncDb)) {
          insert0(sync);
          // Save activity information when configuration is added
          activityCmd.add(toActivity(SERVICE, serviceDb, SYNC_CONFIG_ADD, sync.getName()));
        } else {
          ServicesSyncConverter.setReplaceInfo(syncDb, sync);
          servicesSyncRepo.save(syncDb);
          // Save activity information when configuration is updated
          activityCmd.add(toActivity(SERVICE, serviceDb, SYNC_CONFIG_UPDATE, sync.getName()));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces all synchronization configurations for a service.
   *
   * <p>This method handles bulk replacement of synchronization configurations,
   * including creation, updates, and deletions. It performs comprehensive validation and quota
   * management.</p>
   *
   * @param serviceId the ID of the service
   * @param syncs     the list of synchronization configurations
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceAll(Long serviceId, List<ServicesSync> syncs) {
    new BizTemplate<Void>() {
      List<String> names;
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify no duplicate names in parameters
        names = syncs.stream().map(ServicesSync::getName).toList();
        servicesSyncQuery.checkRepeatedNameInParams(names);

        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        // Verify service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Verify synchronization quota availability
        // TODO: Consider existing sync count in quota calculation
        // int existedNum = projectSyncRepo.countByProjectIdAndNameInNot(serviceId, names);
        servicesSyncQuery.checkSyncNumQuota(serviceId, syncs.size() /*+ existedNum*/);
      }

      @Override
      protected Void process() {
        List<ServicesSync> allSyncsDb = servicesSyncRepo.findByServiceId(serviceId);
        List<ServicesSync> existedSyncsDb = allSyncsDb.stream()
            .filter(x -> names.contains(x.getName())).toList();

        // Add sync
        List<ServicesSync> addSyncs = new ArrayList<>(syncs);
        CoreUtils.removeAll(addSyncs, existedSyncsDb);
        if (isNotEmpty(addSyncs)) {
          batchInsert0(addSyncs);

          // Save activity information when configuration is added
          activityCmd.addAll(addSyncs.stream()
              .map(x -> toActivity(SERVICE, serviceDb, SYNC_CONFIG_ADD, x.getName()))
              .toList());
        }

        // Replace syncs
        if (isNotEmpty(existedSyncsDb)) {
          Map<String, ServicesSync> nameSyncMap = syncs.stream()
              .collect(Collectors.toMap(ServicesSync::getName, x -> x));
          for (ServicesSync syncDb : existedSyncsDb) {
            ServicesSyncConverter.setReplaceInfo(syncDb, nameSyncMap.get(syncDb.getName()));
          }
          batchUpdate0(existedSyncsDb);
          // Save activity information when configuration is updated
          activityCmd.addAll(addSyncs.stream()
              .map(x -> toActivity(SERVICE, serviceDb,
                  SYNC_CONFIG_UPDATE, x.getName())).toList());
        }

        // Delete syncs
        if (isNotEmpty(allSyncsDb)) {
          List<ServicesSync> deletedSyncs = new ArrayList<>(allSyncsDb);
          CoreUtils.removeAll(deletedSyncs, syncs);

          if (isNotEmpty(deletedSyncs)) {
            servicesSyncRepo.deleteByServiceIdAndNameIn(serviceId,
                deletedSyncs.stream().map(ServicesSync::getName).toList());
            // Save activity information when configuration is deleted
            activityCmd.addAll(addSyncs.stream()
                .map(x -> toActivity(SERVICE, serviceDb,
                    SYNC_CONFIG_DELETE, x.getName())).toList());
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Executes synchronization for a specific configuration.
   *
   * <p>This method performs the actual synchronization operation using
   * the specified configuration. It handles error reporting and validates synchronization
   * results.</p>
   *
   * @param serviceId the ID of the service
   * @param name      the name of the synchronization configuration
   * @throws ProtocolException if synchronization fails
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void sync(Long serviceId, String name) {
    new BizTemplate<Void>() {
      List<ServicesSync> syncsDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);

        // Verify synchronization configuration exists
        // Note: When the name parameter is null, the collection will have 1 element
        syncsDb = servicesSyncQuery.find(serviceId,
            Collections.singleton(name));
        assertResourceNotFound(syncsDb, SERVICE_SYNC_CONFIG_NOT_FOUND, new Object[]{});
      }

      @Override
      protected Void process() {
        for (ServicesSync sync : syncsDb) {
          sync0(sync, serviceId);
        }

        ServicesSync failure = syncsDb.stream().filter(x -> !x.getSyncSuccess()).findFirst()
            .orElse(null);
        if (nonNull(failure)) {
          throw ProtocolException.of(String.format("[%s]%s", failure.getName(),
              failure.getSyncFailureCause()));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Tests synchronization configuration by validating OpenAPI content.
   *
   * <p>This method validates the synchronization URL and connectivity,
   * then parses and validates the OpenAPI content to ensure it can be successfully
   * synchronized.</p>
   *
   * @param syncUrl the synchronization URL to test
   * @param auths   the authentication configuration for the URL
   * @return the parsed OpenAPI object
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public OpenAPI test(String syncUrl, List<SimpleHttpAuth> auths) {
    return new BizTemplate<OpenAPI>() {
      String content;

      @Override
      protected void checkParams() {
        // Verify URL and connectivity, retrieve OpenAPI content
        content = servicesSyncQuery.checkAndGetOpenApiContent(syncUrl, auths);
      }

      @Override
      protected OpenAPI process() {
        // Validate and parse OpenAPI document
        List<AuthorizationValue> auths0 = isEmpty(auths) ? null
            : auths.stream().map(x -> new AuthorizationValue()
                    .type("apiKey").keyName(x.getKeyName()).value(x.getValue()))
                .toList();
        return checkAndParseOpenApi(content, auths0, null);
      }
    }.execute();
  }

  /**
   * Deletes synchronization configurations by names.
   *
   * <p>This method removes specific synchronization configurations
   * and logs the deletion activity for audit purposes.</p>
   *
   * @param serviceId the ID of the service
   * @param names     the set of configuration names to delete
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long serviceId, Set<String> names) {
    new BizTemplate<Void>() {
      Services projectDb;

      @Override
      protected void checkParams() {
        // Verify user has modification permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        // Verify service exists
        projectDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        List<String> namesDb = servicesSyncRepo.findNameByServiceIdAndNameIn(serviceId, names);
        if (names.isEmpty()) {
          return null;
        }

        // Delete by names
        servicesSyncRepo.deleteByServiceIdAndNameIn(serviceId, namesDb);

        // Save activity
        activityCmd.add(toActivity(SERVICE, projectDb, SYNC_CONFIG_DELETE,
            String.join(",", namesDb)));
        return null;
      }
    }.execute();
  }

  /**
   * Performs the actual synchronization operation.
   *
   * <p>This method executes the synchronization process, including URL validation,
   * content retrieval, and schema updates. It handles success and failure states and updates the
   * synchronization record accordingly.</p>
   *
   * @param projectSync the synchronization configuration to execute
   * @param serviceId   the ID of the service to synchronize
   */
  public void sync0(ServicesSync projectSync, Long serviceId) {
    try {
      // Verify URL and connectivity, retrieve OpenAPI content
      String content = servicesSyncQuery.checkAndGetOpenApiContent(
          projectSync.getApiDocsUrl(), projectSync.getAuths());
      // Perform schema replacement with synchronization
      servicesSchemaCmd.openapiReplace(serviceId, true, false, content,
          projectSync.getStrategyWhenDuplicated(), projectSync.getDeleteWhenNotExisted(),
          ApiSource.SYNC, ApiImportSource.OPENAPI, true, projectSync.getName());
      projectSync.setSyncSuccess(true);
      projectSync.setSyncFailureCause(null);
    } catch (Exception e) {
      projectSync.setSyncSuccess(false);
      projectSync.setSyncFailureCause(lengthSafe(e.getMessage(), 200));
    }
    projectSync.setLastSyncDate(LocalDateTime.now());
    servicesSyncRepo.save(projectSync);
  }

  /**
   * Deletes all synchronization configurations for multiple services.
   *
   * @param servicesIds the collection of service IDs to delete sync configs for
   */
  @Override
  public void deleteAllByServices(Collection<Long> servicesIds) {
    servicesSyncRepo.deleteByServiceIdIn(servicesIds);
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the services sync repository
   */
  @Override
  protected BaseRepository<ServicesSync, Long> getRepository() {
    return this.servicesSyncRepo;
  }
}
