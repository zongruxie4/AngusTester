package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.MOCK_APIS;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ApisAuthConverter.toApisCreatorAuth;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.assembleApiAuthInfo;
import static cloud.xcan.angus.core.tester.application.converter.MockApisConverter.toAssocOrCopeMockApis;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ADD_ASSOC_TARGET;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_AUTH_UPDATE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_DATASET_REF_ADD;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_DATASET_REF_DELETE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_PARAMETER_ADD;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_PARAMETER_DELETE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_PARAMETER_DISABLED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_PARAMETER_ENABLED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_PARAMETER_UPDATE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_SERVER_UPDATE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_VARIABLE_REF_ADD;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.APIS_VARIABLE_REF_DELETE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ARCHIVED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.EXPORT;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.MOVED_TO;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SCHEMA_SERVER_UPDATED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.STATUS_UPDATE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.UPDATED;
import static cloud.xcan.angus.core.utils.CoreUtils.batchCopyPropertiesIgnoreNull;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isJobOrInnerApi;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.angus.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getExtension;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.data.DatasetTargetCmd;
import cloud.xcan.angus.core.tester.application.cmd.data.VariableTargetCmd;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorStabilityCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.data.DatasetQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuth;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuthRepo;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowRepo;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedRepo;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTargetRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTargetRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSource;
import cloud.xcan.angus.core.tester.domain.services.ServiceApisScope;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation of ApisCmd for API management and command operations.
 * </p>
 * <p>
 * Provides comprehensive API management services including adding, updating, replacing, renaming,
 * archiving, moving, status updating, and deleting APIs. Handles permission checks, quota validation,
 * activity logging, script synchronization, and related resource management. Supports parameter
 * management, authentication configuration, server management, and variable/dataset associations.
 * </p>
 */
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

  /**
   * <p>
   * Add a batch of APIs to a service.
   * </p>
   * <p>
   * Validates that all APIs belong to the same service, checks permission, duplication, quota, and
   * owner. Inserts APIs, initializes creator permissions, and logs creation activities if
   * required.
   * </p>
   * @param apis List of APIs to add
   * @param servicesDb Service entity
   * @param saveActivity Whether to save creation activities
   * @return List of ID keys for created APIs
   */
  @Override
  public List<IdKey<Long, Object>> add(List<Apis> apis, Services servicesDb, boolean saveActivity) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // Ensure all APIs belong to the same service (single service constraint)
        Set<Long> serviceIds = apis.stream().map(Apis::getServiceId).collect(Collectors.toSet());
        assertSingleService(serviceIds,"Only batch adding apis with one service is allowed");

        // Verify current user has permission to add APIs to the service
        servicesAuthQuery.checkAddAuth(getUserId(), serviceIds.iterator().next());

        // Check for duplicate APIs by URI and method within the service
        apisQuery.checkAddServiceApisExisted(apis);

        // Validate service API quota limits
        apisQuery.checkServiceApisQuota(apis);

        // Ensure API owners exist (prevent issues after user deletion)
        apisQuery.checkOwnerExist(apis);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Generate creator authorization records for APIs and service
        List<ApisAuth> apisAuths = getApisAndServiceCreatorAuth();

        // Insert all APIs and get their IDs
        List<IdKey<Long, Object>> idKeys = batchInsert(apis, "summary");

        // Initialize creator permissions for all APIs
        if (isNotEmpty(apisAuths)) {
          apisAuthRepo.batchInsert0(apisAuths);
        }

        // Log creation activities if requested
        if (saveActivity) {
          activityCmd.addAll(toActivities(API, apis, ActivityType.CREATED, activityParams(apis)));
        }
        return idKeys;
      }

      /**
       * <p>
       * Generate creator authorization records for APIs and service.
       * </p>
       * <p>
       * Creates authorization records for both the API creator and service creator,
       * ensuring proper permission hierarchy and access control.
       * </p>
       * @return List of authorization records to insert
       */
      private List<ApisAuth> getApisAndServiceCreatorAuth() {
        Set<ApisAuth> apisAuths = new HashSet<>();
        for (Apis api : apis) {
          // Assemble API authorization information
          assembleApiAuthInfo(api, servicesDb);

          Set<Long> creatorIds = new HashSet<>();
          // Add current user or service creator based on context
          creatorIds.add(isJobOrInnerApi() ? servicesDb.getCreatedBy() : getUserId());
          // Add service creator authorization
          creatorIds.add(servicesDb.getCreatedBy());

          // Generate creator authorization records
          apisAuths.addAll(toApisCreatorAuth(api, creatorIds));
        }
        return new ArrayList<>(apisAuths);
      }
    }.execute();
  }

  /**
   * <p>
   * Archive a batch of APIs (HTTP or WebSocket).
   * </p>
   * <p>
   * Validates service, archives APIs, logs activities, and deletes unarchived records.
   * </p>
   * @param apis List of APIs to archive
   * @return List of ID keys for archived APIs
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> archive(List<Apis> apis) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Services servicesDb;

      @Override
      protected void checkParams() {
        // Validate service exists (single service constraint already checked in add())
        servicesDb = servicesQuery.checkAndFind(apis.get(0).getServiceId());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Process unarchived API information and set project ID
        Set<Long> unarchivedIds = assembleUnarchivedInfo(apis, servicesDb);

        // Add APIs to the service (without activity logging)
        List<IdKey<Long, Object>> idKeys = add(apis, servicesDb, false);

        // Log archive activities with service and API names
        activityCmd.addAll(toActivities(API, apis, ARCHIVED,
            archiveActivityParams(apis, servicesDb)));

        // Clean up unarchived API records
        apisUnarchivedRepo.deleteAllByCreatedByAndIdIn(getUserId(), unarchivedIds);
        return idKeys;
      }
    }.execute();
  }

  /**
   * <p>
   * Update a batch of APIs.
   * </p>
   * <p>
   * Validates APIs, service, owner, duplication, permission, and status. Updates APIs and logs
   * activities if required.
   * </p>
   * @param apis List of APIs to update
   * @param saveActivity Whether to save update activities
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<Apis> apis, boolean saveActivity) {
    new BizTemplate<Void>() {
      List<Apis> apisDbs;

      @Override
      protected void checkParams() {
        // Retrieve existing APIs for validation
        List<Long> apisIds = apis.stream().map(Apis::getId).toList();
        apisDbs = apisQuery.checkAndFind(apisIds);

        // Ensure all APIs belong to the same service (single service constraint)
        Set<Long> serviceIds = apisDbs.stream().map(Apis::getServiceId).collect(Collectors.toSet());
        assertSingleService(serviceIds,"Only batch updating apis with one service is allowed");

        // Ensure API owners exist (prevent issues after user deletion)
        apisQuery.checkOwnerExist(apis);

        // Check for duplicate APIs within the service
        apisQuery.checkServiceApisOperationNotExisted(apis, apisDbs, apisDbs.get(0).getServiceId(),
            false);

        // Verify current user has permission to modify the APIs
        Set<Long> apiIds = apis.stream().map(Apis::getId).collect(Collectors.toSet());
        apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.MODIFY);

        // Ensure released APIs are not being modified
        apisQuery.checkReleasedStatus(apisDbs);

        // Verify current user has permission to release APIs (if any are being released)
        apiIds = apis.stream().filter(x -> nonNull(x.getStatus()) && x.getStatus().isReleased())
            .map(Apis::getId).collect(Collectors.toSet());
        apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.RELEASE);
      }

      @Override
      protected Void process() {
        // Update APIs in database (copy non-null properties)
        batchUpdate0(batchCopyPropertiesIgnoreNull(apis, apisDbs));

        if (saveActivity) {
          // Log update activities
          List<Activity> activities = toActivities(API, apisDbs, UPDATED, activityParams(apis));
          activityCmd.addAll(activities);

          // Send modification notification events
          apisQuery.assembleAndSendModifyNoticeEvent(apisDbs.stream().map(x -> {
            ApisBasicInfo apisBasicInfos = new ApisBasicInfo()
                .setId(x.getId()).setSummary(x.getSummary());
            apisBasicInfos.setCreatedBy(x.getCreatedBy());
            return apisBasicInfos;
          }).toList(), activities);
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Replace (add or update) a batch of APIs.
   * </p>
   * <p>
   * Handles both new and existing APIs, validates all constraints, archives new APIs, updates
   * existing ones, and logs activities.
   * </p>
   * @param apis List of APIs to replace
   * @return List of ID keys for newly added APIs
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> replace(List<Apis> apis) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<Apis> addApis;
      List<Apis> updateApisDbs;
      List<Apis> updateApis;

      @Override
      protected void checkParams() {
        // Separate new APIs (without ID) for addition
        addApis = apis.stream().filter(x -> isNull(x.getId())).toList();
        if (isNotEmpty(addApis)) {
          // Ensure all new APIs belong to the same service
          Set<Long> serviceIds = addApis.stream().map(Apis::getServiceId)
              .collect(Collectors.toSet());
          assertSingleService(serviceIds,"Only batch adding apis with one service is allowed");
        }

        // Separate existing APIs (with ID) for update
        updateApis = apis.stream().filter(x -> nonNull(x.getId())).toList();
        if (isNotEmpty(updateApis)) {
          // Retrieve existing APIs for validation
          List<Long> updateApisIds = updateApis.stream().map(Apis::getId).toList();
          updateApisDbs = apisQuery.checkAndFind(updateApisIds);

          // Ensure all existing APIs belong to the same service
          Set<Long> serviceIds = updateApisDbs.stream().map(Apis::getServiceId)
              .collect(Collectors.toSet());
          assertSingleService(serviceIds, "Only batch updating apis with one service is allowed");

          // Check for duplicate APIs within the service
          apisQuery.checkServiceApisOperationNotExisted(updateApis, updateApisDbs,
              updateApisDbs.get(0).getServiceId(), true);

          // Ensure released APIs are not being modified
          apisQuery.checkReleasedStatus(updateApisDbs);

          // Verify current user has permission to modify the APIs
          apisAuthQuery.batchCheckPermission(updateApisIds, ApiPermission.MODIFY);

          // Verify current user has permission to release APIs (if any are being released)
          Set<Long> apiIds = updateApis.stream().filter(x -> nonNull(x.getStatus())
              && x.getStatus().isReleased()).map(Apis::getId).collect(Collectors.toSet());
          apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.RELEASE);
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = null;

        // Archive new APIs if any
        if (isNotEmpty(addApis)) {
          idKeys = archive(addApis);
        }

        // Update existing APIs if any
        if (isNotEmpty(updateApis)) {
          // Retain replace-specific fields and update APIs
          ApisConverter.retainReplaceField(updateApis, updateApisDbs);
          batchUpdate0(updateApisDbs);

          // Log update activities
          List<Activity> activities = toActivities(API, updateApisDbs, UPDATED, activityParams(apis));
          activityCmd.addAll(activities);

          // Send modification notification events
          apisQuery.assembleAndSendModifyNoticeEvent(updateApis.stream().map(x -> {
            ApisBasicInfo apisBasicInfos = new ApisBasicInfo()
                .setId(x.getId()).setSummary(x.getSummary());
            apisBasicInfos.setCreatedBy(x.getCreatedBy());
            return apisBasicInfos;
          }).toList(), activities);
        }
        return idKeys;
      }
    }.execute();
  }

  /**
   * Rename an API.
   * <p>
   * Validates permission and status, updates the name, and logs the activity. Note: Published APIs
   * cannot be renamed.
   */
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
          // Check if you have the permission to modify the apis
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

  /**
   * Clone an API.
   * <p>
   * Validates permission, clones the API with a unique name, and logs the activity.
   */
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
        servicesAuthQuery.checkAddAuth(getUserId(), apisDb.getServiceId());
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

  /**
   * <p>
   * Move a batch of APIs to another service.
   * </p>
   * <p>
   * Validates service, APIs, permission, and quota. Updates service ID, manages creator
   * permissions, and logs activities. After moving, permissions and references are re-initialized.
   * </p>
   * @param apiIds List of API IDs to move
   * @param targetServiceId Target service ID
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(List<Long> apiIds, Long targetServiceId) {
    new BizTemplate<Void>() {
      Services targetServiceDb;
      List<Apis> apisDb;

      @Override
      protected void checkParams() {
        // Validate target service exists
        targetServiceDb = servicesQuery.checkAndFind(targetServiceId);

        // Validate all APIs exist
        apisDb = apisQuery.checkAndFind(apiIds);

        // Ensure all APIs belong to the same service (single service constraint)
        Set<Long> serviceIds = apisDb.stream().map(Apis::getServiceId).collect(Collectors.toSet());
        assertSingleService(serviceIds, "Only batch move apis with one service is allowed");

        // Verify the move actually changes the service (not moving to same service)
        assertTrue(!apisDb.get(0).getServiceId().equals(targetServiceId),
            "The moving position has not changed");

        // Verify current user has permission to add APIs to target service
        servicesAuthQuery.checkAddAuth(getUserId(), targetServiceId);

        // Verify current user has permission to modify the APIs
        apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.MODIFY);

        // Validate target service API quota limits
        apisQuery.checkServiceApisQuota(apiIds.stream().map(
                id -> new Apis().setId(id).setServiceId(targetServiceId))
            .toList());

        // Note: Allow repeated names after move (names can be duplicated across services)
      }

      @Override
      protected Void process() {
        // Transfer creator permissions to target service creator
        apisAuthCmd.moveCreatorAuth(targetServiceDb, apiIds, apisDb);

        // Update service ID for all APIs and fix OpenAPI path references
        for (Apis api : apisDb) {
          api.setServiceId(targetServiceId);
          // Fix: Cannot find service component after moving.
          apisQuery.setOpenApiPathRefModels(api);
        }
        batchUpdate0(apisDb);

        // Note: API creator permissions to view parent service are initialized by WEB-UI

        // Log move activities
        List<Activity> activities = toActivities(API, apisDb, MOVED_TO, targetServiceDb.getName());
        activityCmd.addAll(activities);

        // Send modification notification events
        apisQuery.assembleAndSendModifyNoticeEvent(
            apisDb.stream().map(x -> {
              ApisBasicInfo apisBasicInfos = new ApisBasicInfo()
                  .setId(x.getId()).setSummary(x.getSummary());
              apisBasicInfos.setCreatedBy(x.getCreatedBy());
              return apisBasicInfos;
            }).toList(), activities);
        return null;
      }
    }.execute();
  }

  /**
   * Update the status of an API.
   * <p>
   * Validates permission, updates status, and logs the activity. Note: Published APIs require
   * release permission, others require modify permission.
   */
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

  /**
   * Replace the server configuration of an API.
   * <p>
   * Validates permission and status, updates server, and logs the activity. Only allowed for
   * unpublished APIs.
   */
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

  /**
   * Replace all server configurations of an API.
   * <p>
   * Validates permission and status, updates servers, and logs the activity. Only allowed for
   * unpublished APIs.
   */
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

  /**
   * Delete specified server configurations from an API.
   * <p>
   * Validates permission and status, deletes servers, and logs the activity. Only allowed for
   * unpublished APIs.
   */
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
            .filter(x -> !serverUrls.contains(x.getUrl())).toList());
        apisRepo.save(apisDb);

        activityCmd.add(toActivity(API, apisDb, SCHEMA_SERVER_UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Batch add parameters to APIs.
   * </p>
   * <p>
   * Validates service and permission, adds parameters, and logs activities.
   * </p>
   * @param serviceId Service ID
   * @param modifyScope Scope of APIs to modify
   * @param matchEndpointRegex Endpoint regex pattern to match
   * @param matchMethod HTTP method to match
   * @param selectedApisIds Selected API IDs
   * @param filterTags Filter tags
   * @param parameters Parameters to add
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void addParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<Parameter> parameters) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;

      @Override
      protected void checkParams() {
        // Find APIs that match the specified criteria
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
      }

      @Override
      protected Void process() {
        // Extract parameter names for duplicate checking
        Set<String> addParameterNames = parameters.stream().map(Parameter::getName)
            .collect(Collectors.toSet());

        // Add parameters to each matching API
        for (Apis apis : serviceApisDb) {
          if (isEmpty(apis.getParameters())) {
            // If no existing parameters, set the new parameters directly
            apis.setParameters(parameters);
            continue;
          }

          // Filter out existing parameters with the same names and add new ones
          List<Parameter> finalParameters = apis.getParameters().stream()
              .filter(x -> !addParameterNames.contains(x.getName())).toList();
          finalParameters.addAll(parameters);
          apis.setParameters(finalParameters);
        }

        // Update all modified APIs
        batchUpdate0(serviceApisDb);

        // Log parameter addition activities
        activityCmd.addAll(toActivities(API, serviceApisDb, APIS_PARAMETER_ADD,
            String.join(",", addParameterNames)));
        return null;
      }
    }.execute();
  }

  /**
   * Batch update parameters of APIs.
   * <p>
   * Validates service and permission, updates parameters, and logs activities.
   */
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
                .toList());
            apisRepo.save(apis);
            activityCmd.add(toActivity(API, apis, APIS_PARAMETER_UPDATE,
                String.join(",", updateParameterNames)));
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Batch delete parameters from APIs.
   * <p>
   * Validates service and permission, deletes parameters, and logs activities.
   */
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

  /**
   * <p>
   * Batch enable or disable parameters of APIs.
   * </p>
   * <p>
   * Validates service and permission, updates parameter status, and logs activities.
   * </p>
   * @param serviceId Service ID
   * @param modifyScope Scope of APIs to modify
   * @param matchEndpointRegex Endpoint regex pattern to match
   * @param matchMethod HTTP method to match
   * @param selectedApisIds Selected API IDs
   * @param filterTags Filter tags
   * @param names Parameter names to enable/disable
   * @param enabled Whether to enable or disable the parameters
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enableParameters(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags, List<String> names, Boolean enabled) {
    new BizTemplate<Void>() {
      List<Apis> serviceApisDb;

      @Override
      protected void checkParams() {
        // Find APIs that match the specified criteria
        serviceApisDb = apisQuery.checkAndFindServiceApis(serviceId, modifyScope,
            matchEndpointRegex, matchMethod, selectedApisIds, filterTags);
      }

      @Override
      protected Void process() {
        // Process each matching API
        for (Apis apis : serviceApisDb) {
          if (isEmpty(apis.getParameters())) {
            continue;
          }

          Set<String> updateParameterNames = new HashSet<>();

          // Update parameter enabled status for each matching parameter
          for (Parameter parameter : apis.getParameters()) {
            if (names.contains(parameter.getName())) {
              if (isEmpty(parameter.getExtensions())) {
                // Add enabled extension if no extensions exist
                parameter.addExtension(ExtensionKey.ENABLED_KEY, enabled);
                updateParameterNames.add(parameter.getName());
              } else {
                if (!parameter.getExtensions().containsKey(ExtensionKey.ENABLED_KEY)) {
                  // Add enabled extension if it doesn't exist
                  parameter.getExtensions().put(ExtensionKey.ENABLED_KEY, enabled);
                  updateParameterNames.add(parameter.getName());
                } else {
                  // Update enabled extension if value has changed
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

          // Save API and log activity if parameters were updated
          if (!updateParameterNames.isEmpty()) {
            apisRepo.save(apis);
            activityCmd.add(toActivity(API, apis, enabled ? APIS_PARAMETER_ENABLED
                : APIS_PARAMETER_DISABLED, String.join(",", updateParameterNames)));
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Batch update authentication for APIs.
   * <p>
   * Validates service and permission, updates authentication, and logs activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateAuth(Long serviceId, ServiceApisScope modifyScope, String matchEndpointRegex,
      HttpMethod matchMethod, Set<Long> selectedApisIds, Set<String> filterTags,
      SecurityScheme authentication) {
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

        activityCmd.addAll(toActivities(API, serviceApisDb, APIS_AUTH_UPDATE));
        return null;
      }
    }.execute();
  }

  /**
   * Batch update server for APIs.
   * <p>
   * Validates service and permission, updates server, and logs activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateServer(Long serviceId, ServiceApisScope modifyScope, String matchEndpointRegex,
      HttpMethod matchMethod, Set<Long> selectedApisIds, Set<String> filterTags, Server server) {
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

        activityCmd.addAll(toActivities(API, serviceApisDb, APIS_SERVER_UPDATE));
        return null;
      }
    }.execute();
  }

  /**
   * Batch add variable references to APIs.
   * <p>
   * Validates service and permission, adds variable references, and logs activities.
   */
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

  /**
   * Batch delete variable references from APIs.
   * <p>
   * Validates service and permission, deletes variable references, and logs activities.
   */
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

  /**
   * Batch add dataset references to APIs.
   * <p>
   * Validates service and permission, adds dataset references, and logs activities.
   */
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

  /**
   * Batch delete dataset references from APIs.
   * <p>
   * Validates service and permission, deletes dataset references, and logs activities.
   */
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

  /**
   * <p>
   * Associate a mock API with an API.
   * </p>
   * <p>
   * Validates permission, generates mock API and responses, and logs the association activity.
   * </p>
   * @param id API ID to associate with mock
   * @param mockServiceId Mock service ID
   * @param summary Mock API summary
   * @return ID key of the created mock API
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> assocMockApisAdd(Long id, Long mockServiceId, String summary) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Verify current user has permission to view the API
        apisAuthQuery.checkViewAuth(getUserId(), id);

        // Validate API exists and retrieve full details
        apisDb = apisQuery.checkAndFind(id);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Resolve all reference models for the API
        Map<String, String> allRefModels = apisQuery.findApisAllRef(apisDb);
        apisDb.setResolvedRefModels(allRefModels);

        // Convert API to mock API with association source
        MockApis mockApis = toAssocOrCopeMockApis(apisDb, mockServiceId, MockApisSource.ASSOC_APIS);
        if (isNotEmpty(summary)) {
          mockApis.setSummary(summary);
        }

        // Create mock API
        List<IdKey<Long, Object>> idKeys = mockApisCmd.add(List.of(mockApis), false);

        // Generate mock API responses based on the original API
        mockApisCmd.addMockApisResponses(mockApis, apisDb, mockServiceId);

        // Log association activity
        activityCmd.add(toActivity(MOCK_APIS, apisDb, ADD_ASSOC_TARGET));
        return idKeys.get(0);
      }
    }.execute();
  }

  /**
   * <p>
   * Export an API as an OpenAPI document.
   * </p>
   * <p>
   * Supports multiple formats and logs the export activity.
   * </p>
   * @param id API ID to export
   * @param format Export format (JSON/YAML)
   * @param response HTTP response object
   * @return ResponseEntity containing the exported document
   */
  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(Long id, SchemaFormat format,
      HttpServletResponse response) {
    return new BizTemplate<ResponseEntity<org.springframework.core.io.Resource>>() {

      @Override
      protected ResponseEntity<org.springframework.core.io.Resource> process() {
        // Generate OpenAPI document in the specified format
        String openAPI = apisQuery.openapiDetail(id, format, false, true);

        // Retrieve API information for filename and activity logging
        Apis apisDb = (Apis) getExtension(id.toString());

        // Convert to byte array for download
        byte[] bytes = openAPI.getBytes(UTF_8);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));

        // Build download response with proper headers
        ResponseEntity<org.springframework.core.io.Resource> responseEntity =
            buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
                apisDb.getSummary() + "." + format.name(), bytes.length, resource);

        // Log export activity
        activityCmd.add(toActivity(API, apisDb, EXPORT, apisDb.getName()));
        return responseEntity;
      }
    }.execute();
  }

  /**
   * <p>
   * Batch logical delete APIs.
   * </p>
   * <p>
   * Validates permission and status, updates delete status, logs activities, and adds to trash.
   * This is a soft delete operation that marks APIs as deleted without removing them from the database.
   * </p>
   * @param apiIds Collection of API IDs to delete
   * @param checkPermission Whether to check permissions (legacy parameter)
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> apiIds, boolean checkPermission) {
    new BizTemplate<Void>() {
      List<ApisBaseInfo> apisBasesDb;

      @Override
      protected void checkParams() {
        // Retrieve API base information for validation
        apisBasesDb = apisBaseInfoRepo.findAllByIdIn(apiIds);

        if (isNotEmpty(apisBasesDb)) {
          // Verify current user has permission to delete the APIs
          apisAuthQuery.batchCheckPermission(apiIds, ApiPermission.DELETE);

          // Ensure released APIs are not being deleted (business rule)
          apisQuery.checkApisBaseReleasedStatus(apisBasesDb);
        }
      }

      @Override
      protected Void process() {
        if (isEmpty(apisBasesDb)) {
          return null;
        }

        // Mark APIs as deleted (soft delete)
        apisRepo.updateDeleteStatus(apiIds, true, getUserId(), LocalDateTime.now());

        // Log deletion activities
        List<Activity> activities = toActivities(API, apisBasesDb, ActivityType.DELETED,
            activityParams(apisBasesDb));
        activityCmd.addAll(activities);

        // Add deleted APIs to trash for potential recovery
        trashApisCmd.add0(apisBasesDb.stream().map(ApisConverter::toApisTrash)
            .toList());

        // Send modification notification events
        apisQuery.assembleAndSendModifyNoticeEvent(
            apisBasesDb.stream().map(x -> {
              ApisBasicInfo apisBasicInfos = new ApisBasicInfo()
                  .setId(x.getId()).setSummary(x.getSummary());
              apisBasicInfos.setCreatedBy(x.getCreatedBy());
              return apisBasicInfos;
            }).toList(), activities);
        return null;
      }
    }.execute();
  }

  @Override
  public void update0(List<Apis> apisDb) {
    batchUpdate0(apisDb);
  }

  /**
   * <p>
   * Physically delete APIs and all related resources.
   * </p>
   * <p>
   * Completely removes APIs and all associated data from the database. This is a destructive
   * operation that cannot be undone. External calling business logic must ensure data is
   * properly authorized and secured before calling this method.
   * </p>
   * @param apiIds List of API IDs to physically delete
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0(List<Long> apiIds) {
    // Clear mock API associations to prevent orphaned references
    mockApisRepo.updateAssocToNullByApisIdIn(apiIds);

    // Remove APIs from database
    apisRepo.deleteAllByIdIn(apiIds);

    // Delete associated API test cases
    apisCaseCmd.deleteByApisIdIn(apiIds);

    // Delete associated API scripts
    scriptCmd.deleteBySource(ScriptSource.API, apiIds);

    // Delete API authorization records
    apisAuthRepo.deleteByApisIdIn(apiIds);

    // Delete API favorite records
    apisFavoriteRepo.deleteByApisIdIn(apiIds);

    // Delete API follow records
    apisFollowRepo.deleteByApisIdIn(apiIds);

    // Delete API performance indicators
    indicatorPerfCmd.deleteAllByTarget(apiIds, API);

    // Delete API stability indicators
    indicatorStabilityCmd.deleteAllByTarget(apiIds, API);

    // Delete API variable associations
    variableTargetRepo.deleteByTarget(apiIds, API.getValue());

    // Delete API dataset associations
    datasetTargetRepo.deleteByTarget(apiIds, API.getValue());

    // Note: Associated test tasks are not deleted to preserve test history
    // apisTestCmd.delete0(apiIds);
  }

  /**
   * Batch update APIs from schema sync.
   * <p>
   * Updates APIs from schema and logs activities.
   */
  @Override
  public void updateSyncApis(Map<String, Apis> updatedApisDbMap, Map<String, Apis> openApisMap) {
    for (String uniqueKey : updatedApisDbMap.keySet()) {
      ApisConverter.assembleSchemaToUpdateApis(updatedApisDbMap.get(uniqueKey),
          openApisMap.get(uniqueKey));
    }
    batchUpdate0(updatedApisDbMap.values());

    // Add update api activity
    activityCmd.addAll(toActivities(API, new ArrayList<>(updatedApisDbMap.values()), UPDATED,
        activityParams(updatedApisDbMap.values())));
  }

  /**
   * <p>
   * Assemble unarchived API information and set project ID.
   * </p>
   * <p>
   * Processes unarchived API data, merges missing parameters, and sets project ID for all APIs.
   * </p>
   * @param apis List of APIs to process
   * @param servicesDb Service entity
   * @return Set of unarchived API IDs
   */
  @NotNull
  private Set<Long> assembleUnarchivedInfo(List<Apis> apis, Services servicesDb) {
    // Extract unarchived API IDs
    Set<Long> unarchivedIds = apis.stream().map(Apis::getUnarchivedId)
        .filter(Objects::nonNull).collect(Collectors.toSet());

    if (isNotEmpty(unarchivedIds)) {
      // Retrieve unarchived API details
      List<ApisUnarchived> unarchivedApis = checkAndGetUnarchived(unarchivedIds);

      // Create mapping for efficient lookup
      Map<Long, ApisUnarchived> unarchivedMap = unarchivedApis.stream()
          .collect(Collectors.toMap(ApisUnarchived::getId, o -> o));

      // Merge missing parameters from unarchived APIs to current APIs
      apis.forEach(api -> {
        ApisUnarchived apisUnarchived = unarchivedMap.get(api.getUnarchivedId());
        if (nonNull(apisUnarchived)) {
          ApisConverter.assembleUnarchivedInfo(api, apisUnarchived);
        }
      });
    }

    // Set project ID for all APIs
    apis.forEach(api -> {
          api.setProjectId(servicesDb.getProjectId());
        }
    );
    return unarchivedIds;
  }

  /**
   * <p>
   * Check and retrieve unarchived APIs by IDs.
   * </p>
   * <p>
   * Validates that all requested unarchived APIs exist and belong to the current user.
   * </p>
   * @param unarchivedIds Set of unarchived API IDs to retrieve
   * @return List of unarchived APIs
   */
  @NotNull
  private List<ApisUnarchived> checkAndGetUnarchived(Set<Long> unarchivedIds) {
    // Query unarchived APIs for current user
    List<ApisUnarchived> unarchivedApisDb = apisUnarchivedRepo.findAllByCreatedByAndIdIn(
        getUserId(), unarchivedIds);

    // Validate that unarchived APIs exist
    assertResourceNotFound(unarchivedApisDb, unarchivedIds.iterator().next(), "UnarchiveApi");

    // Check for missing unarchived APIs
    Set<Long> unarchivedExistIds = unarchivedApisDb.stream()
        .map(ApisUnarchived::getId).filter(Objects::nonNull).collect(Collectors.toSet());
    Set<Long> unarchiveRequestIds = new HashSet<>(unarchivedIds);
    unarchiveRequestIds.removeAll(unarchivedExistIds);

    // Validate that all requested APIs were found
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

  /**
   * <p>
   * Assert that all APIs belong to a single service.
   * </p>
   * <p>
   * Throws an exception if the set size is not 1, ensuring batch operations
   * are performed on APIs from the same service.
   * </p>
   * @param serviceIds Set of service IDs to validate
   * @param message Error message to display if validation fails
   */
  private void assertSingleService(Set<Long> serviceIds, String message) {
      assertTrue(serviceIds.size() == 1, message);
  }

  /**
   * Get the repository for Apis entity.
   * <p>
   *
   * @return the ApisRepo instance
   */
  @Override
  protected BaseRepository<Apis, Long> getRepository() {
    return this.apisRepo;
  }
}
