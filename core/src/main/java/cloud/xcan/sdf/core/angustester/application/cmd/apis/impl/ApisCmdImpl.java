package cloud.xcan.sdf.core.angustester.application.cmd.apis.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.MOCK_APIS;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.MockApisConverter.toAssocOrCopeMockApis;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ADD_ASSOC_TARGET;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_AUTH_UPDATE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_DATASET_REF_ADD;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_DATASET_REF_DELETE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_PARAMETER_ADD;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_PARAMETER_DELETE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_PARAMETER_DISABLED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_PARAMETER_ENABLED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_PARAMETER_UPDATE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_SERVER_UPDATE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_VARIABLE_REF_ADD;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.APIS_VARIABLE_REF_DELETE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ARCHIVED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.EXPORT;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.MOVED_TO;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SCHEMA_SERVER_UPDATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.STATUS_UPDATE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.UPDATED;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getExtension;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isJobOrDoorApi;
import static cloud.xcan.sdf.core.utils.CoreUtils.batchCopyPropertiesIgnoreNull;
import static cloud.xcan.sdf.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.sdf.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisAuthCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisCaseCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisTrashCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.data.DatasetTargetCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.data.VariableTargetCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.indicator.IndicatorStabilityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockApisCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.script.ScriptCmd;
import cloud.xcan.sdf.core.angustester.application.converter.ApisAuthConverter;
import cloud.xcan.sdf.core.angustester.application.converter.ApisConverter;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasetQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuth;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuthRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.favourite.ApisFavouriteRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.follow.ApisFollowRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchivedRepo;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetTargetRepo;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableTargetRepo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisRepo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisSource;
import cloud.xcan.sdf.core.angustester.domain.services.ServiceApisScope;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.model.script.ScriptSource;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@Biz
@Slf4j
public class ApisCmdImpl extends CommCmd<Apis, Long> implements ApisCmd {

  @Resource
  private ApisRepo apisRepo;

  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ApisAuthQuery apisAuthQuery;

  @Resource
  private ApisAuthCmd apisAuthCmd;

  @Resource
  private ApisUnarchivedRepo apisUnarchivedRepo;

  @Resource
  private ApisTrashCmd trashApisCmd;

  @Resource
  private ApisAuthRepo apisAuthRepo;

  @Resource
  private ApisCaseCmd apisCaseCmd;

  @Resource
  private ApisFavouriteRepo apisFavoriteRepo;

  @Resource
  private ApisFollowRepo apisFollowRepo;

  @Resource
  private IndicatorPerfCmd indicatorPerfCmd;

  @Resource
  private IndicatorStabilityCmd indicatorStabilityCmd;

  @Resource
  private VariableQuery variableQuery;

  @Resource
  private VariableTargetRepo variableTargetRepo;

  @Resource
  private VariableTargetCmd variableTargetCmd;

  @Resource
  private DatasetQuery datasetQuery;

  @Resource
  private DatasetTargetRepo datasetTargetRepo;

  @Resource
  private DatasetTargetCmd datasetTargetCmd;

  @Resource
  private MockApisCmd mockApisCmd;

  @Resource
  private MockApisRepo mockApisRepo;

  @Resource
  private ScriptCmd scriptCmd;

  @Override
  public List<IdKey<Long, Object>> add(List<Apis> apis, Services servicesDb, boolean saveActivity) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        Set<Long> serviceIds = apis.stream().map(Apis::getServiceId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(serviceIds.size() == 1,
            "Only batch adding apis with one service is allowed");

        // Check the release apis permission
        servicesAuthQuery.checkAddAuth(getUserId(), serviceIds.iterator().next());

        // Check the apis existed by uri and method
        apisQuery.checkAddServiceApisExisted(apis);

        // Check the only a maximum of MAX_PROJECT_OR_SERVICE_APIS_NUM apis can be added to a service
        apisQuery.checkServiceApisQuota(apis);

        // Check the owner exists
        // Prevent user sync failure after user deletion
        apisQuery.checkOwnerExist(apis);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<ApisAuth> apisAuths = getApisAndServiceCreatorAuth();

        List<IdKey<Long, Object>> idKeys = batchInsert(apis, "summary");

        // Add the creator initialization permission of the api
        if (isNotEmpty(apisAuths)) {
          apisAuthRepo.batchInsert0(apisAuths);
        }

        // Add created api activity
        if (saveActivity) {
          activityCmd.batchAdd(toActivities(API, apis, ActivityType.CREATED, activityParams(apis)));
        }
        return idKeys;
      }

      private List<ApisAuth> getApisAndServiceCreatorAuth() {
        Set<ApisAuth> apisAuths = new HashSet<>();
        for (Apis api : apis) {
          ApisConverter.assembleApiAuthInfo(api, servicesDb);
          Set<Long> creatorIds = new HashSet<>();
          creatorIds.add(isJobOrDoorApi() ? servicesDb.getCreatedBy() : getUserId());
          // Current services creator authorization
          creatorIds.add(servicesDb.getCreatedBy());
          apisAuths.addAll(ApisAuthConverter.toApisCreatorAuth(api, creatorIds));
        }
        return new ArrayList<>(apisAuths);
      }
    }.execute();
  }

  /**
   * Archive the http or websocket apis.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> archive(List<Apis> apis) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Services servicesDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        // Only batch adding apis with one service is allowed <- Check in add()
        servicesDb = servicesQuery.checkAndFind(apis.get(0).getServiceId());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        Set<Long> unarchiveIds = assembleUnarchivedInfo(apis, servicesDb);

        // Add unarchived apis
        List<IdKey<Long, Object>> idKeys = add(apis, servicesDb, false);

        // Add archived apis activity
        activityCmd.batchAdd(toActivities(API, apis, ARCHIVED,
            archiveActivityParams(apis, servicesDb)));

        // Delete unarchived apis
        apisUnarchivedRepo.deleteAllByCreatedByAndIdIn(getUserId(), unarchiveIds);
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<Apis> apis, boolean saveActivity) {
    new BizTemplate<Void>() {
      List<Apis> apisDbs;

      @Override
      protected void checkParams() {
        // Check and find apis
        List<Long> apisIds = apis.stream().map(Apis::getId).collect(Collectors.toList());
        apisDbs = apisQuery.checkAndFind(apisIds);

        // Note: service id is not allowed modify
        // Check the allowing operations on a single service
        Set<Long> serviceIds = apisDbs.stream().map(Apis::getServiceId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(serviceIds.size() == 1,
            "Only batch updating apis with one service is allowed");

        // Check the owner exists
        // Prevent user sync failure after user deletion
        apisQuery.checkOwnerExist(apis);

        // Check the repeated apis
        apisQuery.checkServiceApisOperationNotExisted(apis, apisDbs, apisDbs.get(0).getServiceId(),
            false);

        // Check the modify apis permission
        Set<Long> apiIds = apis.stream().map(Apis::getId).collect(Collectors.toSet());
        apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.MODIFY);

        // Check the released api are not allowed to be modified
        apisQuery.checkReleasedStatus(apisDbs);

        // Check the release apis permission
        apiIds = apis.stream().filter(x -> nonNull(x.getStatus()) && x.getStatus().isReleased())
            .map(Apis::getId).collect(Collectors.toSet());
        apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.RELEASE);
      }

      @Override
      protected Void process() {
        // Save apis to database
        batchUpdate0(batchCopyPropertiesIgnoreNull(apis, apisDbs));

        if (saveActivity) {
          // Add update api activity
          List<Activity> activities = toActivities(API, apisDbs, UPDATED, activityParams(apis));
          activityCmd.batchAdd(activities);

          // Add modification events
          apisQuery.assembleAndSendModifyNoticeEvent(apisDbs.stream().map(x -> {
            ApisBasicInfo apisBasicInfos = new ApisBasicInfo()
                .setId(x.getId()).setSummary(x.getSummary());
            apisBasicInfos.setCreatedBy(x.getCreatedBy());
            return apisBasicInfos;
          }).collect(Collectors.toList()), activities);
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> replace(List<Apis> apis) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<Apis> addApis;
      List<Apis> updateApisDbs;
      List<Apis> updateApis;

      @Override
      protected void checkParams() {
        addApis = apis.stream().filter(x -> isNull(x.getId())).collect(Collectors.toList());
        if (isNotEmpty(addApis)) {
          // Check the allowing operations on a single service
          Set<Long> serviceIds = addApis.stream().map(Apis::getServiceId)
              .collect(Collectors.toSet());
          ProtocolAssert.assertTrue(serviceIds.size() == 1,
              "Only batch adding apis with one service is allowed");
        }

        // Note: service id is not allowed modify
        updateApis = apis.stream().filter(x -> nonNull(x.getId())).collect(Collectors.toList());
        if (isNotEmpty(updateApis)) {
          // Check the apis exists
          List<Long> updateApisIds = updateApis.stream().map(Apis::getId)
              .collect(Collectors.toList());
          updateApisDbs = apisQuery.checkAndFind(updateApisIds);

          // Check the allowing operations on a single service
          Set<Long> serviceIds = updateApisDbs.stream().map(Apis::getServiceId)
              .collect(Collectors.toSet());
          ProtocolAssert.assertTrue(serviceIds.size() == 1,
              "Only batch updating apis with one service is allowed");

          // Check the operation is not repeated
          apisQuery.checkServiceApisOperationNotExisted(updateApis, updateApisDbs,
              updateApisDbs.get(0).getServiceId(), true);

          // Check the released api are not allowed to be modified
          apisQuery.checkReleasedStatus(updateApisDbs);

          // Check the modify apis permission
          apisAuthQuery.batchCheckPermission(updateApisIds, ApiPermission.MODIFY);

          // Check the release apis permission
          Set<Long> apiIds = updateApis.stream().filter(x -> nonNull(x.getStatus())
              && x.getStatus().isReleased()).map(Apis::getId).collect(Collectors.toSet());
          apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.RELEASE);
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = null;

        // Add unarchived apis
        if (isNotEmpty(addApis)) {
          idKeys = archive(addApis);
        }

        if (isNotEmpty(updateApis)) {
          // Save apis
          ApisConverter.retainReplaceField(updateApis, updateApisDbs);
          batchUpdate0(updateApisDbs);

          List<Activity> activities = toActivities(API, updateApisDbs, UPDATED,
              activityParams(apis));
          activityCmd.batchAdd(activities);

          // Add modification events
          apisQuery.assembleAndSendModifyNoticeEvent(updateApis.stream().map(x -> {
            ApisBasicInfo apisBasicInfos = new ApisBasicInfo()
                .setId(x.getId()).setSummary(x.getSummary());
            apisBasicInfos.setCreatedBy(x.getCreatedBy());
            return apisBasicInfos;
          }).collect(Collectors.toList()), activities);
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      ApisBasicInfo apisInfoDb;

      @Override
      protected void checkParams() {
        // Published api are not allowed to be modified
        apisInfoDb = apisQuery.checkAndFindBasicInfo(id);

        if (!name.equals(apisInfoDb.getName())) {
          // Check the whether you have the permission to modify the apis
          apisAuthQuery.checkModifyAuth(getUserId(), id);

          // Check the released api are not allowed to be modified
          apisQuery.checkReleasedStatus(apisInfoDb);
        }
      }

      @Override
      protected Void process() {
        if (!name.equals(apisInfoDb.getName())) {
          apisRepo.updateSummaryById(id, name);

          // Add api name updated activity
          Activity activity = toActivity(API, apisInfoDb, ActivityType.NAME_UPDATED, name);
          activityCmd.add(activity);

          // Add modification event
          apisQuery.assembleAndSendModifyNoticeEvent(apisInfoDb, activity);
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Apis apisDb = null;

      @Override
      protected void checkParams() {
        // Check the apis exists
        apisDb = apisQuery.checkAndFind(id);
        // Check the view api permission
        apisAuthQuery.checkViewAuth(getUserId(), id);
        // Check the add api to service permission
        servicesAuthQuery.checkAddAuth(PrincipalContext.getUserId(), apisDb.getServiceId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Clone and save api
        Services serviceDb = servicesQuery.checkAndFind(apisDb.getServiceId());
        Apis apis = ApisConverter.toCloneApis(apisDb, serviceDb);
        apisQuery.setSafeCloneName(apis);
        IdKey<Long, Object> idKey = add(List.of(apis), serviceDb, false).get(0);

        // Add clone api activity
        Activity activity = toActivity(API, apisDb, ActivityType.CLONE, apisDb.getName());
        activityCmd.add(activity);
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(List<Long> apiIds, Long targetServiceId) {
    new BizTemplate<Void>() {
      Services targetServiceDb;
      List<Apis> apisDb;

      @Override
      protected void checkParams() {
        // Check and get service exists
        targetServiceDb = servicesQuery.checkAndFind(targetServiceId);

        // Check and get apis exists
        List<Apis> apisDb = apisQuery.checkAndFind(apiIds);

        // Check the move one service is allowed
        Set<Long> serviceIds = apisDb.stream().map(Apis::getServiceId).collect(Collectors.toSet());
        ProtocolAssert.assertTrue(serviceIds.size() == 1,
            "Only batch move apis with one service is allowed");

        // Check the if the movement position has changed
        ProtocolAssert.assertTrue(!apisDb.get(0).getServiceId().equals(targetServiceId),
            "The moving position has not changed");

        // Check the services add auth
        servicesAuthQuery.checkAddAuth(getUserId(), targetServiceId);

        // Check the to have permission to modify apis
        apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.MODIFY);

        // Check the only a maximum of MAX_PROJECT_OR_SERVICE_APIS_NUM apis can be added to a service
        apisQuery.checkServiceApisQuota(apiIds.stream().map(
                id -> new Apis().setId(id).setServiceId(targetServiceId))
            .collect(Collectors.toList()));

        // NOOP: Allow repeated names after move
      }

      @Override
      protected Void process() {
        // Grant the apis creator permission to the target service creator
        apisAuthCmd.moveCreatorAuth(targetServiceDb, apiIds, apisDb);

        // Modify the serviceId of apis
        // apisRepo.updateServiceById(apiIds, targetServiceId);

        for (Apis api : apisDb) {
          api.setServiceId(targetServiceId);
          // Fix: Cannot find service component after moving.
          apisQuery.setOpenApiPathRefModels(api);
        }
        batchUpdate0(apisDb);

        // NOOP: Init apis creator to view parent service and service permissions by WEB-UI

        // Add move api activity
        List<Activity> activities = toActivities(API, apisDb, MOVED_TO, targetServiceDb.getName());
        activityCmd.batchAdd(activities);

        // Add modification events
        apisQuery.assembleAndSendModifyNoticeEvent(
            apisDb.stream().map(x -> {
              ApisBasicInfo apisBasicInfos = new ApisBasicInfo()
                  .setId(x.getId()).setSummary(x.getSummary());
              apisBasicInfos.setCreatedBy(x.getCreatedBy());
              return apisBasicInfos;
            }).collect(Collectors.toList()), activities);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void statusUpdate(Long apiId, ApiStatus status) {
    new BizTemplate<Void>() {
      ApisBasicInfo apiInfoDb;

      @Override
      protected void checkParams() {
        // Check the apis exists
        apiInfoDb = apisQuery.checkAndFindBasicInfo(apiId);

        // Check the to have permission to release
        if (status.isReleased() || apiInfoDb.isReleased()) {
          apisAuthQuery.checkReleaseAuth(getUserId(), apiId);
        } else {
          apisAuthQuery.checkModifyAuth(getUserId(), apiId);
        }
      }

      @Override
      protected Void process() {
        apisRepo.updateStatusById(apiId, status.getValue());

        // Add public api activity
        Activity activity = toActivity(API, apiInfoDb, STATUS_UPDATE, status);
        activityCmd.add(activity);

        // Add modification event
        apisQuery.assembleAndSendModifyNoticeEvent(apiInfoDb, activity);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void serverReplace(Long id, Server server) {
    new BizTemplate<Void>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        apisAuthQuery.checkModifyAuth(getUserId(), id);
        apisDb = apisQuery.checkAndFind(id);
        // Check the released api are not allowed to be modified
        apisQuery.checkReleasedStatus(apisDb);
      }

      @Override
      protected Void process() {
        if (isNotEmpty(apisDb.getServers())) {
          boolean update = false;
          for (Server serverDb : apisDb.getServers()) {
            if (serverDb.getUrl().equals(server.getUrl())) {
              serverDb.description(server.getDescription())
                  .variables(server.getVariables()).extensions(server.getExtensions());
              update = true;
              break;
            }
          }
          // Add new server
          if (!update) {
            apisDb.getServers().add(server);
          }
        } else {
          apisDb.setServers(List.of(server));
        }
        apisRepo.save(apisDb);

        activityCmd.add(toActivity(API, apisDb, SCHEMA_SERVER_UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void serverReplaceAll(Long id, List<Server> servers) {
    new BizTemplate<Void>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        apisAuthQuery.checkModifyAuth(getUserId(), id);
        apisDb = apisQuery.checkAndFind(id);
        // Check the released api are not allowed to be modified
        apisQuery.checkReleasedStatus(apisDb);
      }

      @Override
      protected Void process() {
        apisDb.setServers(servers);
        apisRepo.save(apisDb);

        activityCmd.add(toActivity(API, apisDb, SCHEMA_SERVER_UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void serverDelete(Long id, Set<String> serverUrls) {
    new BizTemplate<Void>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        apisAuthQuery.checkModifyAuth(getUserId(), id);
        apisDb = apisQuery.checkAndFind(id);
        // Check the released api are not allowed to be modified
        apisQuery.checkReleasedStatus(apisDb);
      }

      @Override
      protected Void process() {
        if (isEmpty(apisDb.getServers())) {
          return null;
        }
        apisDb.setServers(apisDb.getServers().stream()
            .filter(x -> !serverUrls.contains(x.getUrl())).collect(Collectors.toList()));
        apisRepo.save(apisDb);

        activityCmd.add(toActivity(API, apisDb, SCHEMA_SERVER_UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void addParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<Parameter> parameters) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
      }

      @Override
      protected Void process() {
        Set<String> addParameterNames = parameters.stream().map(Parameter::getName)
            .collect(Collectors.toSet());
        for (Apis apis : serviceApisDb) {
          if (isEmpty(apis.getParameters())) {
            apis.setParameters(parameters);
            continue;
          }
          List<Parameter> finalParameters = apis.getParameters().stream()
              .filter(x -> !addParameterNames.contains(x.getName())).collect(Collectors.toList());
          finalParameters.addAll(parameters);
          apis.setParameters(finalParameters);
        }

        batchUpdate0(serviceApisDb);

        activityCmd.batchAdd(toActivities(API, serviceApisDb, APIS_PARAMETER_ADD,
            String.join(",", addParameterNames)));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<Parameter> parameters) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
      }

      @Override
      protected Void process() {
        Map<String, List<Parameter>> parameterMap = parameters.stream()
            .collect(Collectors.groupingBy(Parameter::getName));
        for (Apis apis : serviceApisDb) {
          if (isEmpty(apis.getParameters())) {
            continue;
          }
          Map<String, List<Parameter>> finalParameters = new HashMap<>();
          Set<String> updateParameterNames = new HashSet<>();
          for (Parameter parameter : apis.getParameters()) {
            if (parameterMap.containsKey(parameter.getName())) {
              if (!finalParameters.containsKey(parameter.getName())) {
                finalParameters.put(parameter.getName(), parameterMap.get(parameter.getName()));
                updateParameterNames.add(parameter.getName());
              }
            } else {
              finalParameters.put(parameter.getName(), List.of(parameter));
            }
          }
          if (!updateParameterNames.isEmpty()) {
            apis.setParameters(finalParameters.values().stream().flatMap(List::stream)
                .collect(Collectors.toList()));
            apisRepo.save(apis);
            activityCmd.add(toActivity(API, apis, APIS_PARAMETER_UPDATE,
                String.join(",", updateParameterNames)));
          }
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> names) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
      }

      @Override
      protected Void process() {
        for (Apis apis : serviceApisDb) {
          if (isEmpty(apis.getParameters())) {
            continue;
          }
          List<Parameter> finalParameters = new ArrayList<>();
          Set<String> deleteParameterNames = new HashSet<>();
          for (Parameter parameter : apis.getParameters()) {
            if (names.contains(parameter.getName())) {
              deleteParameterNames.add(parameter.getName());
              continue;
            }
            finalParameters.add(parameter);
          }
          if (!deleteParameterNames.isEmpty()) {
            apis.setParameters(finalParameters);
            apisRepo.save(apis);
            activityCmd.add(toActivity(API, apis, APIS_PARAMETER_DELETE,
                String.join(",", deleteParameterNames)));
          }
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enableParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> names, Boolean enabled) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
      }

      @Override
      protected Void process() {
        for (Apis apis : serviceApisDb) {
          if (isEmpty(apis.getParameters())) {
            continue;
          }
          Set<String> updateParameterNames = new HashSet<>();
          for (Parameter parameter : apis.getParameters()) {
            if (names.contains(parameter.getName())) {
              if (isEmpty(parameter.getExtensions())) {
                parameter.addExtension(ExtensionKey.ENABLED_KEY, enabled);
                updateParameterNames.add(parameter.getName());
              } else {
                if (!parameter.getExtensions().containsKey(ExtensionKey.ENABLED_KEY)) {
                  parameter.getExtensions().put(ExtensionKey.ENABLED_KEY, enabled);
                  updateParameterNames.add(parameter.getName());
                } else {
                  Boolean current = Boolean.parseBoolean(
                      parameter.getExtensions().get(ExtensionKey.ENABLED_KEY).toString());
                  if (!Objects.equals(current, enabled)) {
                    parameter.getExtensions().put(ExtensionKey.ENABLED_KEY, enabled);
                    updateParameterNames.add(parameter.getName());
                  }
                }
              }
            }
          }
          if (!updateParameterNames.isEmpty()) {
            // apis.setParameters(updateParameterNames);
            apisRepo.save(apis);
            activityCmd.add(toActivity(API, apis, enabled ? APIS_PARAMETER_ENABLED
                : APIS_PARAMETER_DISABLED, String.join(",", updateParameterNames)));
          }
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateAuth(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, SecurityScheme authentication) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
      }

      @Override
      protected Void process() {
        for (Apis apis : serviceApisDb) {
          apis.setAuthentication(authentication);
        }
        batchUpdate0(serviceApisDb);

        activityCmd.batchAdd(toActivities(API, serviceApisDb, APIS_AUTH_UPDATE));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateServer(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, Server server) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
      }

      @Override
      protected Void process() {
        for (Apis apis : serviceApisDb) {
          apis.setCurrentServer(server);
        }
        batchUpdate0(serviceApisDb);

        activityCmd.batchAdd(toActivities(API, serviceApisDb, APIS_SERVER_UPDATE));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void addVariableReference(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> variableNames) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;
      List<Variable> variablesDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
        variablesDb = variableQuery.checkAndFindByName(serviceApisDb.get(0).getProjectId(),
            variableNames);
      }

      @Override
      protected Void process() {
        Set<Long> variableIds = variablesDb.stream().map(Variable::getId)
            .collect(Collectors.toSet());
        for (Apis apis : serviceApisDb) {
          variableTargetCmd.add(apis.getId(), API.getValue(), variableIds, false);
          activityCmd.add(toActivity(API, apis, APIS_VARIABLE_REF_ADD,
              String.join(",", variableNames)));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteVariableReference(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> variableNames) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;
      List<Variable> variablesDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
        variablesDb = variableQuery.checkAndFindByName(serviceApisDb.get(0).getProjectId(),
            variableNames);
      }

      @Override
      protected Void process() {
        Set<Long> variableIds = variablesDb.stream().map(Variable::getId)
            .collect(Collectors.toSet());
        for (Apis apis : serviceApisDb) {
          variableTargetCmd.delete(apis.getId(), API.getValue(), variableIds, false);
          activityCmd.add(toActivity(API, apis, APIS_VARIABLE_REF_DELETE,
              String.join(",", variableNames)));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void addDatasetReference(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> datasetNames) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;
      List<Dataset> datasetsDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
        datasetsDb = datasetQuery.checkAndFindByName(serviceApisDb.get(0).getProjectId(),
            datasetNames);
      }

      @Override
      protected Void process() {
        Set<Long> datasetIds = datasetsDb.stream().map(Dataset::getId).collect(Collectors.toSet());
        for (Apis apis : serviceApisDb) {
          datasetTargetCmd.add(apis.getId(), API.getValue(), datasetIds, false);
          activityCmd.add(toActivity(API, apis, APIS_DATASET_REF_ADD,
              String.join(",", datasetNames)));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteDatasetReference(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> datasetNames) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;
      List<Dataset> datasetsDb;

      @Override
      protected void checkParams() {
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
        datasetsDb = datasetQuery.checkAndFindByName(serviceApisDb.get(0).getProjectId(),
            datasetNames);
      }

      @Override
      protected Void process() {
        Set<Long> datasetIds = datasetsDb.stream().map(Dataset::getId)
            .collect(Collectors.toSet());
        for (Apis apis : serviceApisDb) {
          datasetTargetCmd.delete(apis.getId(), API.getValue(), datasetIds, false);
          activityCmd.add(toActivity(API, apis, APIS_DATASET_REF_DELETE,
              String.join(",", datasetNames)));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> assocMockApisAdd(Long id, Long mockServiceId, String summary) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Check the apis view permission
        apisAuthQuery.checkViewAuth(getUserId(), id);

        // Check the apis exists and get apis
        apisDb = apisQuery.checkAndFind(id);
      }

      @Override
      protected IdKey<Long, Object> process() {
        Map<String, String> allRefModels = apisQuery.findApisAllRef(apisDb);
        apisDb.setResolvedRefModels(allRefModels);

        // Convert apis to mock apis
        MockApis mockApis = toAssocOrCopeMockApis(apisDb, mockServiceId, MockApisSource.ASSOC_APIS);
        if (isNotEmpty(summary)) {
          mockApis.setSummary(summary);
        }

        // Add mock apis
        List<IdKey<Long, Object>> idKeys = mockApisCmd.add(List.of(mockApis), false);

        // Add mocks apis responses
        mockApisCmd.addMockApisResponses(mockApis, apisDb, mockServiceId);

        // Save the associated apis activity
        activityCmd.add(toActivity(MOCK_APIS, apisDb, ADD_ASSOC_TARGET));
        return idKeys.get(0);
      }
    }.execute();
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(Long id, SchemaFormat format,
      HttpServletResponse response) {
    return new BizTemplate<ResponseEntity<org.springframework.core.io.Resource>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected ResponseEntity<org.springframework.core.io.Resource> process() {
        String openAPI = apisQuery.openapiDetail(id, format, false, true);
        Apis apisDb = (Apis) getExtension(id.toString());
        byte[] bytes = openAPI.getBytes(UTF_8);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        ResponseEntity<org.springframework.core.io.Resource> responseEntity =
            buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
                apisDb.getSummary() + "." + format.name(), bytes.length, resource);

        // Add export api activity
        activityCmd.add(toActivity(API, apisDb, EXPORT, apisDb.getName()));
        return responseEntity;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> apiIds, boolean checkPermission) {
    new BizTemplate<Void>() {
      List<ApisBaseInfo> apisBasesDb;

      @Override
      protected void checkParams() {
        // Published api are not allowed to be modified
        apisBasesDb = apisBaseInfoRepo.findAllByIdIn(apiIds);

        if (isNotEmpty(apisBasesDb)) {
          // Check the whether you have the permission to modify the apis
          apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.DELETE);

          // Check the released api are not allowed to be modified
          apisQuery.checkApisBaseReleasedStatus(apisBasesDb);
        }
      }

      @Override
      protected Void process() {
        if (isEmpty(apisBasesDb)) {
          return null;
        }

        // Update deletion status
        apisRepo.updateDeleteStatus(apiIds, true, getUserId(), LocalDateTime.now());

        // Add delete activity
        List<Activity> activities = toActivities(API, apisBasesDb, ActivityType.DELETED,
            activityParams(apisBasesDb));
        activityCmd.batchAdd(activities);

        // Add deleted api to trash
        trashApisCmd.add0(apisBasesDb.stream().map(ApisConverter::toApisTrash)
            .collect(Collectors.toList()));

        // Add modification events
        apisQuery.assembleAndSendModifyNoticeEvent(
            apisBasesDb.stream().map(x -> {
              ApisBasicInfo apisBasicInfos = new ApisBasicInfo()
                  .setId(x.getId()).setSummary(x.getSummary());
              apisBasicInfos.setCreatedBy(x.getCreatedBy());
              return apisBasicInfos;
            }).collect(Collectors.toList()), activities);
        return null;
      }
    }.execute();
  }

  @Override
  public void update0(List<Apis> apisDb) {
    batchUpdate0(apisDb);
  }

  /**
   * Physically delete, External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0(List<Long> apiIds) {
    // Set to empty if the apis is associated
    mockApisRepo.updateAssocToNullByApisIdIn(apiIds);
    // Delete apis
    apisRepo.deleteAllByIdIn(apiIds);
    // Delete apis case
    apisCaseCmd.deleteByApisIdIn(apiIds);
    // Delete apis script
    scriptCmd.deleteBySource(ScriptSource.API, apiIds);
    // Delete apis auth
    apisAuthRepo.deleteByApisIdIn(apiIds);
    // Delete apis favorite
    apisFavoriteRepo.deleteByApisIdIn(apiIds);
    // Delete apis follow
    apisFollowRepo.deleteByApisIdIn(apiIds);
    // Delete apis indicator
    indicatorPerfCmd.deleteAllByTarget(apiIds, API);
    indicatorStabilityCmd.deleteAllByTarget(apiIds, API);
    // Delete apis variable association
    variableTargetRepo.deleteByTarget(apiIds, API.getValue());
    // Delete apis dataset association
    datasetTargetRepo.deleteByTarget(apiIds, API.getValue());
    // Do not delete associated test tasks
    // apisTestCmd.delete0(apiIds);
  }

  @Override
  public void updateSyncApis(Map<String, Apis> updatedApisDbMap, Map<String, Apis> openApisMap) {
    for (String uniqueKey : updatedApisDbMap.keySet()) {
      ApisConverter.assembleSchemaToUpdateApis(updatedApisDbMap.get(uniqueKey),
          openApisMap.get(uniqueKey));
    }
    batchUpdate0(updatedApisDbMap.values());

    // Add update api activity
    activityCmd.batchAdd(toActivities(API, new ArrayList<>(updatedApisDbMap.values()), UPDATED,
        activityParams(updatedApisDbMap.values())));
  }

  @NotNull
  private Set<Long> assembleUnarchivedInfo(List<Apis> apis, Services servicesDb) {
    Set<Long> unarchiveIds = apis.stream().map(Apis::getUnarchiveId)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    if (isNotEmpty(unarchiveIds)) {
      List<ApisUnarchived> unarchivedApis = checkAndGetUnarchived(unarchiveIds);
      // If the apis parameters queryParams, requestBodyData... are empty when archiving,
      // set the corresponding parameter value in the unarchived api to the current apis
      Map<Long, ApisUnarchived> unarchivedMap = unarchivedApis.stream()
          .collect(Collectors.toMap(ApisUnarchived::getId, o -> o));
      apis.forEach(api -> {
        ApisUnarchived apisUnarchived = unarchivedMap.get(api.getUnarchiveId());
        if (nonNull(apisUnarchived)) {
          ApisConverter.assembleUnarchivedInfo(api, apisUnarchived);
        }
      });
    }

    apis.forEach(api -> {
          api.setProjectId(servicesDb.getProjectId());
        }
    );
    return unarchiveIds;
  }

  @NotNull
  private List<ApisUnarchived> checkAndGetUnarchived(Set<Long> unarchiveIds) {
    // Query unarchive apis
    List<ApisUnarchived> unarchivedApisDb = apisUnarchivedRepo.findAllByCreatedByAndIdIn(
        getUserId(), unarchiveIds);
    assertResourceNotFound(unarchivedApisDb, unarchiveIds.iterator().next(), "UnarchiveApi");

    Set<Long> unarchivedExistIds = unarchivedApisDb.stream()
        .map(ApisUnarchived::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    Set<Long> unarchiveRequestIds = new HashSet<>(unarchiveIds);
    unarchiveRequestIds.removeAll(unarchivedExistIds);
    assertResourceNotFound(unarchiveRequestIds.isEmpty(), unarchiveRequestIds, "UnarchiveApi");
    return unarchivedApisDb;
  }

  private List<Object[]> archiveActivityParams(List<Apis> apis, Services servicesDb) {
    List<Object[]> params = new ArrayList<>(apis.size());
    for (Apis api : apis) {
      params.add(new Object[]{servicesDb.getName(), api.getName()});
    }
    return params;
  }

  @Override
  protected BaseRepository<Apis, Long> getRepository() {
    return this.apisRepo;
  }
}
