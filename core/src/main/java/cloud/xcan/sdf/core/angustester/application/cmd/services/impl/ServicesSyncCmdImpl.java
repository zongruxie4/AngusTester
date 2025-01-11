package cloud.xcan.sdf.core.angustester.application.cmd.services.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_SYNC_CONFIG_NOT_FOUND;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SYNC_CONFIG_ADD;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SYNC_CONFIG_DELETE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SYNC_CONFIG_UPDATE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.extension.angustester.api.utils.OpenApiParser.checkAndParseOpenApi;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.lengthSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.services.ServicesSchemaCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.services.ServicesSyncCmd;
import cloud.xcan.sdf.core.angustester.application.converter.ServicesSyncConverter;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesSyncQuery;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.sync.ServicesSync;
import cloud.xcan.sdf.core.angustester.domain.services.sync.ServicesSyncRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

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
  private ServicesSchemaQuery servicesSchemaQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(Long serviceId, ServicesSync sync) {
    new BizTemplate<Void>() {
      Services serviceDb;
      ServicesSync syncDb;

      @Override
      protected void checkParams() {
        // Check the permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Check the sync num quota
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceAll(Long serviceId, List<ServicesSync> syncs) {
    new BizTemplate<Void>() {
      List<String> names;
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the for duplicates in param
        names = syncs.stream().map(ServicesSync::getName).collect(Collectors.toList());
        servicesSyncQuery.checkRepeatedNameInParams(names);

        // Check the permissions
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Check the sync num quota
        // int existedNum = projectSyncRepo.countByProjectIdAndNameInNot(serviceId, names);
        servicesSyncQuery.checkSyncNumQuota(serviceId, syncs.size() /*+ existedNum*/);
      }

      @Override
      protected Void process() {
        List<ServicesSync> allSyncsDb = servicesSyncRepo.findByServiceId(serviceId);
        List<ServicesSync> existedSyncsDb = allSyncsDb.stream()
            .filter(x -> names.contains(x.getName())).collect(Collectors.toList());

        // Add sync
        List<ServicesSync> addSyncs = new ArrayList<>(syncs);
        CoreUtils.removeAll(addSyncs, existedSyncsDb);
        if (ObjectUtils.isNotEmpty(addSyncs)) {
          batchInsert0(addSyncs);

          // Save activity information when configuration is added
          activityCmd.batchAdd(addSyncs.stream()
              .map(x -> toActivity(SERVICE, serviceDb, SYNC_CONFIG_ADD, x.getName()))
              .collect(Collectors.toList()));
        }

        // Replace syncs
        if (ObjectUtils.isNotEmpty(existedSyncsDb)) {
          Map<String, ServicesSync> nameSyncMap = syncs.stream()
              .collect(Collectors.toMap(ServicesSync::getName, x -> x));
          for (ServicesSync syncDb : existedSyncsDb) {
            ServicesSyncConverter.setReplaceInfo(syncDb, nameSyncMap.get(syncDb.getName()));
          }
          batchUpdate0(existedSyncsDb);
          // Save activity information when configuration is updated
          activityCmd.batchAdd(addSyncs.stream()
              .map(x -> toActivity(SERVICE, serviceDb,
                  SYNC_CONFIG_UPDATE, x.getName())).collect(Collectors.toList()));
        }

        // Delete syncs
        if (ObjectUtils.isNotEmpty(allSyncsDb)) {
          List<ServicesSync> deletedSyncs = new ArrayList<>(allSyncsDb);
          CoreUtils.removeAll(deletedSyncs, syncs);

          if (ObjectUtils.isNotEmpty(deletedSyncs)) {
            servicesSyncRepo.deleteByServiceIdAndNameIn(serviceId,
                deletedSyncs.stream().map(ServicesSync::getName).collect(Collectors.toList()));
            // Save activity information when configuration is deleted
            activityCmd.batchAdd(addSyncs.stream()
                .map(x -> toActivity(SERVICE, serviceDb,
                    SYNC_CONFIG_DELETE, x.getName())).collect(Collectors.toList()));
          }
        }
        return null;
      }
    }.execute();
  }

  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void sync(Long serviceId, String name) {
    new BizTemplate<Void>() {
      List<ServicesSync> syncsDb;

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);

        syncsDb = servicesSyncQuery.find(serviceId,
            /*Note:: When the name of this method is null, the number of collection elements will be 1*/
            Collections.singleton(name));
        ProtocolAssert.assertResourceNotFound(syncsDb, SERVICE_SYNC_CONFIG_NOT_FOUND,
            new Object[]{});
      }

      @Override
      protected Void process() {
        for (ServicesSync sync : syncsDb) {
          sync0(sync, serviceId);
        }

        ServicesSync failure = syncsDb.stream().filter(x -> !x.getSyncSuccessFlag()).findFirst()
            .orElse(null);
        if (nonNull(failure)) {
          throw CommProtocolException
              .of(String.format("[%s]%s", failure.getName(), failure.getSyncFailureCause()));
        }
        return null;
      }
    }.execute();
  }

  @Override
  public OpenAPI test(String syncUrl, List<SimpleHttpAuth> auths) {
    return new BizTemplate<OpenAPI>() {
      String content;

      @Override
      protected void checkParams() {
        // Check the url and connectivity
        content = servicesSyncQuery.checkAndGetOpenApiContent(syncUrl, auths);
      }

      @Override
      protected OpenAPI process() {
        // Check the is OpenApi document
        List<AuthorizationValue> auths0 = ObjectUtils.isEmpty(auths) ? null
            : auths.stream().map(x -> new AuthorizationValue()
                    .type("apiKey").keyName(x.getKeyName()).value(x.getValue()))
                .collect(Collectors.toList());
        return checkAndParseOpenApi(content, auths0, null);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long serviceId, Set<String> names) {
    new BizTemplate<Void>() {
      Services projectDb;

      @Override
      protected void checkParams() {
        servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
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

  @Transactional(rollbackFor = Exception.class)
  public void sync0(ServicesSync projectSync, Long serviceId) {
    try {
      // Check the url and connectivity
      String content = servicesSyncQuery.checkAndGetOpenApiContent(projectSync.getApiDocsUrl(),
          projectSync.getAuths());
      // Save sync result
      servicesSchemaCmd.openapiReplace(serviceId, true, false, content,
          projectSync.getStrategyWhenDuplicated(), projectSync.getDeleteWhenNotExisted(),
          ApiSource.SYNC, null, true, projectSync.getName());
      projectSync.setSyncSuccessFlag(true);
      projectSync.setSyncFailureCause(null);
    } catch (Exception e) {
      projectSync.setSyncSuccessFlag(false);
      projectSync.setSyncFailureCause(lengthSafe(e.getMessage(), 200));
    }
    projectSync.setLastSyncDate(LocalDateTime.now());
    servicesSyncRepo.save(projectSync);
  }

  @Override
  public void deleteAllByServices(Collection<Long> servicesIds) {
    servicesSyncRepo.deleteByServiceIdIn(servicesIds);
  }

  @Override
  protected BaseRepository<ServicesSync, Long> getRepository() {
    return this.servicesSyncRepo;
  }
}
