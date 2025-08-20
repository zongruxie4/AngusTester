package cloud.xcan.angus.core.tester.application.query.common.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.NO_ADMIN_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.NO_ADMIN_PERMISSION_CODE;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstAndRemove;
import static cloud.xcan.angus.core.tester.infra.job.TaskWillOverdueEventJob.createInnerPrincipal;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasPolicy;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isOpSysAdmin;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isTenantSysAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.commonlink.dept.Dept;
import cloud.xcan.angus.api.commonlink.dept.DeptRepo;
import cloud.xcan.angus.api.commonlink.group.Group;
import cloud.xcan.angus.api.commonlink.group.GroupRepo;
import cloud.xcan.angus.api.commonlink.setting.SettingKey;
import cloud.xcan.angus.api.commonlink.setting.quota.Quota;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.angus.api.commonlink.setting.tenant.event.TesterEvent;
import cloud.xcan.angus.api.commonlink.setting.tenant.quota.SettingTenantQuota;
import cloud.xcan.angus.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.api.commonlink.tenant.Tenant;
import cloud.xcan.angus.api.commonlink.tenant.TenantRepo;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.commonlink.user.UserBaseRepo;
import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.SettingManager;
import cloud.xcan.angus.api.manager.SettingTenantManager;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintQuery;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.activity.SimpleActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of common query operations for AngusTester application.
 *
 * <p>This class provides centralized functionality for various common operations
 * including permission checking, target management, user information retrieval,
 * quota management, and organizational data handling.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Administrative permission validation and user role checking</li>
 *   <li>Combined target management for various entity types (API, Scenario, Task, etc.)</li>
 *   <li>User and organizational information retrieval and mapping</li>
 *   <li>Tenant quota management and validation</li>
 *   <li>Activity resource management and authorization</li>
 *   <li>Event notification type configuration</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Biz
@Slf4j
public class CommonQueryImpl implements CommonQuery {

  @Resource
  private ProjectQuery projectQuery;
  @Resource
  private TaskSprintQuery taskSprintQuery;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private FuncPlanQuery funcPlanQuery;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ScenarioQuery scenarioQuery;
  @Resource
  private ApisAuthQuery apisAuthQuery;
  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;
  @Resource
  private TenantRepo tenantRepo;
  @Resource
  private UserBaseRepo userBaseRepo;
  @Resource
  private GroupRepo groupRepo;
  @Resource
  private DeptRepo deptRepo;
  @Resource
  private UserManager userManager;
  @Resource
  private SettingManager settingManager;
  @Resource
  private SettingTenantManager settingTenantManager;
  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;
  @Resource
  private ApplicationInfo applicationInfo;
  @Resource
  private ExecQuery execQuery;

  /**
   * Checks if the current user has administrative permissions.
   *
   * <p>This method validates whether the current user possesses administrative
   * privileges, throwing a BizException if not authorized.</p>
   *
   * @throws BizException if the user lacks administrative permissions
   */
  @Override
  public void checkAdminPermission() {
    if (!isAdminUser()) {
      throw BizException.of(NO_ADMIN_PERMISSION_CODE, NO_ADMIN_PERMISSION);
    }
  }

  /**
   * Validates and retrieves the name of an authorization object.
   *
   * <p>This method checks the existence of various authorization object types
   * (User, Group, Department) and returns their names for display purposes.</p>
   *
   * @param authObjectType the type of authorization object
   * @param authObjectId the ID of the authorization object
   * @return the name of the authorization object
   * @throws ResourceNotFound if the authorization object is not found
   */
  @Override
  public String checkAndGetAuthName(AuthObjectType authObjectType, Long authObjectId) {
    return switch (authObjectType) {
      case USER -> {
        UserBase userBase = userBaseRepo.findValidById(authObjectId).orElseThrow(
            () -> ResourceNotFound.of(String.valueOf(authObjectId), "User"));
        yield userBase.getFullName();
      }
      case GROUP -> {
        Group group = groupRepo.findValidById(authObjectId).orElseThrow(
            () -> ResourceNotFound.of(String.valueOf(authObjectId), "Group"));
        yield group.getName();
      }
      case DEPT -> {
        Dept dept = deptRepo.findById(authObjectId).orElseThrow(
            () -> ResourceNotFound.of(String.valueOf(authObjectId), "Department"));
        yield dept.getName();
      }
    };
  }

  /**
   * Retrieves indicator target information including deleted records.
   *
   * <p>This method fetches target information for indicators, including records
   * marked as deleted, supporting API and Scenario target types.</p>
   *
   * @param targetType the type of target to retrieve
   * @param targetId the ID of the target
   * @return the target object or null if not supported
   */
  @Override
  public Object checkAndGetIndicatorTarget(CombinedTargetType targetType, Long targetId) {
    if (targetType.equals(API)) {
      return apisQuery.checkAndFindBaseInfo(targetId);
    }
    if (targetType.equals(SCENARIO)) {
      return scenarioQuery.checkAndFind0(targetId);
    }
    return null;
  }

  /**
   * Validates modification permissions for indicator targets.
   *
   * <p>This method checks if the current user has permission to modify
   * the specified indicator target based on its type.</p>
   *
   * @param targetType the type of target to check
   * @param targetId the ID of the target
   */
  @Override
  public void checkIndicatorTargetModifyAuth(CombinedTargetType targetType, Long targetId) {
    if (targetType.equals(API)) {
      apisAuthQuery.checkModifyAuth(getUserId(), targetId);
    }

    if (targetType.equals(SCENARIO)) {
      scenarioAuthQuery.checkModifyAuth(getUserId(), targetId);
    }
  }

  /**
   * Checks if the current user has administrative privileges.
   *
   * <p>This method validates whether the current user is an AngusTester admin,
   * tenant system admin, or operation system admin.</p>
   *
   * @return true if the user has administrative privileges, false otherwise
   */
  @Override
  public boolean isAdminUser() {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN) || isTenantSysAdmin() || isOpSysAdmin();
  }

  /**
   * Static method to check if the current user has administrative privileges.
   *
   * <p>This method provides the same functionality as isAdminUser() but as a
   * static method for use in utility contexts.</p>
   *
   * @return true if the user has administrative privileges, false otherwise
   */
  public static boolean isAdmin() {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN) || isTenantSysAdmin() || isOpSysAdmin();
  }

  /**
   * Retrieves user information map for a collection of user IDs.
   *
   * <p>This method fetches user information for multiple users and returns
   * a map keyed by user ID, with email and mobile information removed for privacy.</p>
   *
   * @param userIds collection of user IDs to retrieve information for
   * @return map of user ID to user information
   */
  @Override
  public @NotNull Map<Long, UserInfo> getUserInfoMap(Collection<Long> userIds) {
    return userManager.findUserBases(userIds).stream().collect(
        Collectors.toMap(UserBase::getId, x -> x.toUserInfo().setEmail(null).setMobile(null)));
  }

  /**
   * Validates and retrieves combined target information with optional parent lookup.
   *
   * <p>This method fetches comprehensive target information for various entity types,
   * including their parent relationships when requested. Throws exceptions for
   * non-existent targets.</p>
   *
   * @param targetType the type of target to retrieve
   * @param targetId the ID of the target
   * @param findParent whether to include parent information
   * @return combined target information
   */
  @Override
  public CombinedTarget checkAndGetCombinedTarget(CombinedTargetType targetType, Long targetId,
      boolean findParent) {
    String targetName;
    if (CombinedTargetType.SCENARIO.equals(targetType)) {
      Scenario scenarioDb = scenarioQuery.checkAndFind0(targetId);
      targetName = scenarioDb.getName();
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setScenarioInfo(scenarioDb);
    } else if (CombinedTargetType.API.equals(targetType)) {
      ApisBaseInfo apiDb = apisQuery.checkAndFindBaseInfo(targetId);
      targetName = apiDb.getName();
      Services apisParent = null;
      if (findParent) {
        apisParent = servicesQuery.checkAndFind(apiDb.getServiceId());
      }
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setApisBaseInfo(apiDb).setApisParent(apisParent);
    } else if (CombinedTargetType.SERVICE.equals(targetType)) {
      Services serviceDb = servicesQuery.checkAndFind(targetId);
      targetName = serviceDb.getName();
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setServices(serviceDb).setApisParent(null);
    } else if (CombinedTargetType.TASK.equals(targetType)) {
      TaskInfo taskDb = taskQuery.checkAndFindInfo(targetId);
      targetName = taskDb.getName();
      TaskSprint taskParent = null;
      if (findParent) {
        taskParent = taskSprintQuery.checkAndFind(taskDb.getSprintId());
      }
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setTaskInfo(taskDb).setTaskParent(taskParent);
    } else if (CombinedTargetType.TASK_SPRINT.equals(targetType)) {
      TaskSprint sprintDb = taskSprintQuery.checkAndFind(targetId);
      targetName = sprintDb.getName();
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setSprint(sprintDb)
          .setTaskParent(null);
    } else if (CombinedTargetType.FUNC_CASE.equals(targetType)) {
      FuncCaseInfo caseDb = funcCaseQuery.checkAndFindInfo(targetId);
      targetName = caseDb.getName();
      FuncPlan caseParent = null;
      if (findParent) {
        caseParent = funcPlanQuery.checkAndFind(caseDb.getPlanId());
      }
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setCaseInfo(caseDb).setCaseParent(caseParent);
    } else if (CombinedTargetType.FUNC_PLAN.equals(targetType)) {
      FuncPlan planDb = funcPlanQuery.checkAndFind(targetId);
      targetName = planDb.getName();
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setPlan(planDb).setCaseParent(null);
    }
    return null;
  }

  /**
   * Retrieves combined target information without throwing exceptions for missing resources.
   *
   * <p>This method fetches target information but returns null values for missing
   * resources instead of throwing exceptions, preventing query failures after
   * data deletion.</p>
   *
   * @param targetType the type of target to retrieve
   * @param targetId the ID of the target
   * @param findParent whether to include parent information
   * @return combined target information with null values for missing resources
   */
  @Override
  public CombinedTarget getCombinedTarget(CombinedTargetType targetType, Long targetId,
      boolean findParent) {
    String targetName;
    if (CombinedTargetType.SCENARIO.equals(targetType)) {
      Scenario scenarioDb = scenarioQuery.find0(targetId);
      if (isNull(scenarioDb)) {
        return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
            .setTargetName(null).setScenarioInfo(null);
      }
      targetName = scenarioDb.getName();
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setScenarioInfo(scenarioDb);
    } else if (CombinedTargetType.API.equals(targetType)) {
      ApisBaseInfo apiDb = apisQuery.checkAndFindBaseInfo(targetId);
      if (isNull(apiDb)) {
        return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
            .setTargetName(null).setScenarioInfo(null);
      }
      targetName = apiDb.getName();
      Services apisParent = null;
      if (findParent) {
        apisParent = servicesQuery.find0(apiDb.getServiceId());
      }
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setApisBaseInfo(apiDb).setServices(apisParent)
          .setApisParent(apisParent);
    } else {
      Services serviceDb = servicesQuery.find0(targetId);
      if (isNull(serviceDb)) {
        return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
            .setTargetName(null).setScenarioInfo(null);
      }
      targetName = serviceDb.getName();
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setServices(serviceDb).setApisParent(null);
    }
  }

  /**
   * Validates and retrieves activity resource information for a target.
   *
   * <p>This method fetches activity resource information for various target types,
   * including special handling for execution targets.</p>
   *
   * @param targetType the type of target
   * @param targetId the ID of the target
   * @return activity resource information
   * @throws ProtocolException if the target type is not supported
   */
  @Override
  public ActivityResource checkAndFindActivityResource(CombinedTargetType targetType,
      Long targetId) {
    return switch (targetType) {
      case PROJECT -> projectQuery.checkAndFind(targetId);
      case TASK_SPRINT -> taskSprintQuery.checkAndFind(targetId);
      case TASK -> taskQuery.checkAndFindInfo(targetId);
      case FUNC_PLAN -> funcPlanQuery.checkAndFind(targetId);
      case FUNC_CASE -> funcCaseQuery.checkAndFindInfo(targetId);
      case SERVICE -> servicesQuery.checkAndFind(targetId);
      case API -> apisQuery.checkAndFindBaseInfo(targetId);
      case SCENARIO -> scenarioQuery.checkAndFind(targetId);
      case EXECUTION -> {
        ExecInfo execInfo = execQuery.listInfo(Set.of(targetId), false).get(0);
        yield new SimpleActivityResource().setId(execInfo.getId()).setName(execInfo.getName())
            .setProjectId(execInfo.getProjectId());
      }
      default -> throw ProtocolException.of("Unsupported target type: " + targetType);
    };
  }

  /**
   * Sets authorization conditions for non-administrator users or self-query scenarios.
   *
   * <p>This method modifies search criteria to include authorization object ID
   * restrictions when the user is not an administrator or when querying only
   * their own data.</p>
   *
   * @param criteria the search criteria to modify
   * @return false (return value not used)
   */
  @Override
  public boolean checkAndSetAuthObjectIdCriteria(Set<SearchCriteria> criteria) {
    SearchCriteria adminCriteria = findFirstAndRemove(criteria, "admin");
    boolean admin = false;
    if (Objects.nonNull(adminCriteria)) {
      admin = Boolean.parseBoolean(
          adminCriteria.getValue().toString().replaceAll("\"", ""));
    }
    if (!admin || !isAdminUser()) {
      criteria.add(SearchCriteria.in("authObjectId", userManager.getValidOrgAndUserIds()));
    }
    return false;
  }

  /**
   * Validates tenant quota for specified resources.
   *
   * <p>This method checks if the tenant has sufficient quota for the specified
   * resource objects and increment.</p>
   *
   * @param quotaObject the quota resource type
   * @param objectIds set of object IDs to check
   * @param incr the increment amount
   */
  @Override
  public void checkTenantQuota(QuotaResource quotaObject, Set<Long> objectIds, Long incr) {
    settingTenantQuotaManager.checkTenantQuota(quotaObject, objectIds, incr);
  }

  /**
   * Validates LCS (License Control System) quota for specified resources.
   *
   * <p>This method is currently a placeholder for LCS quota validation
   * functionality.</p>
   *
   * @param quotaObject the quota resource type
   * @param objectIds set of object IDs to check
   * @param incr the increment amount
   */
  @Override
  public void checkLcsQuota(QuotaResource quotaObject, Set<Long> objectIds, Long incr) {
    // TODO: Implement LCS quota validation logic
  }

  /**
   * Retrieves tenant quota information for a specific resource.
   *
   * <p>This method fetches the current quota settings for the specified
   * resource type in the current tenant context.</p>
   *
   * @param name the quota resource type
   * @return tenant quota information
   */
  @Override
  public SettingTenantQuota findTenantQuota(QuotaResource name) {
    return settingTenantQuotaManager.findTenantQuota(getOptTenantId(), name);
  }

  /**
   * Retrieves tenant event notification type configurations.
   *
   * <p>This method fetches the notification type mappings for various events
   * in the specified tenant, falling back to global settings if tenant-specific
   * settings are not available.</p>
   *
   * @param tenantId the tenant ID (uses current tenant if null)
   * @return map of event codes to notification types
   */
  @Override
  public Map<String, List<NoticeType>> findTenantEventNoticeTypes(Long tenantId) {
    Long finalTenantId = nullSafe(tenantId, getOptTenantId());
    String cachedSettingTenant = settingTenantManager.getCachedSetting(finalTenantId);
    SettingTenant settingTenant = settingTenantManager.parseCachedSetting(cachedSettingTenant);
    List<TesterEvent> eventData =
        isNull(settingTenant) || isEmpty(settingTenant.getTesterEventData())
            ? settingManager.setting(SettingKey.TESTER_EVENT).getTesterEvent()
            : settingTenant.getTesterEventData();
    return eventData.stream()
        .filter(x -> isNotEmpty(x.getEventCode()) && isNotEmpty(x.getNoticeTypes())).
        collect(Collectors.toMap(TesterEvent::getEventCode, TesterEvent::getNoticeTypes));
  }

  /**
   * Validates the existence of organizational entities.
   *
   * <p>This method checks if all specified organizational entities (users,
   * departments, groups) exist in the system.</p>
   *
   * @param typeIds map of organization types to sets of IDs to validate
   */
  @Override
  public void checkOrgExists(LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> typeIds) {
    if (isNotEmpty(typeIds)) {
      for (Entry<OrgTargetType, LinkedHashSet<Long>> entry : typeIds.entrySet()) {
        userManager.checkOrgExists(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Retrieves organizational information for specified entities.
   *
   * <p>This method fetches detailed information for users, departments, and groups,
   * converting them to standardized OrgTargetInfo objects.</p>
   *
   * @param typeIds map of organization types to sets of IDs to retrieve
   * @return map of organization types to sets of organization information
   */
  @Override
  public LinkedHashMap<OrgTargetType, LinkedHashSet<OrgTargetInfo>> findOrgs(
      LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> typeIds) {
    LinkedHashMap<OrgTargetType, LinkedHashSet<OrgTargetInfo>> infos = new LinkedHashMap<>();
    if (isNotEmpty(typeIds)) {
      for (Entry<OrgTargetType, LinkedHashSet<Long>> entry : typeIds.entrySet()) {
        if (isNotEmpty(entry.getValue())) {
          switch (entry.getKey()) {
            case USER:
              List<UserBase> users = (List<UserBase>) userManager.findOrgs(entry.getKey(),
                  entry.getValue());
              infos.put(entry.getKey(), new LinkedHashSet<>(users.stream().map(
                      x -> new OrgTargetInfo().setId(x.getId()).setName(x.getFullName())
                          .setAvatar(x.getAvatar()).setType(OrgTargetType.USER))
                  .toList()));
              break;
            case DEPT:
              List<Dept> depts = (List<Dept>) userManager.findOrgs(entry.getKey(),
                  entry.getValue());
              infos.put(entry.getKey(), new LinkedHashSet<>(depts.stream().map(
                      x -> new OrgTargetInfo().setId(x.getId()).setName(x.getName())
                          .setAvatar(null).setType(OrgTargetType.DEPT))
                  .toList()));
              break;
            case GROUP:
              List<Group> groups = (List<Group>) userManager.findOrgs(entry.getKey(),
                  entry.getValue());
              infos.put(entry.getKey(), new LinkedHashSet<>(groups.stream().map(
                      x -> new OrgTargetInfo().setId(x.getId()).setName(x.getName())
                          .setAvatar(null).setType(OrgTargetType.GROUP))
                  .toList()));
              break;
          }
        }
      }
    }
    return infos;
  }

  /**
   * Sets the inner principal context for remote operations.
   *
   * <p>This method establishes the principal context for internal operations,
   * typically used for Feign request interceptor header relay in remote
   * AngusCtrl sharding sampling scenarios.</p>
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   */
  @Override
  public void setInnerPrincipal(Long tenantId, Long userId) {
    // Transfer principal downwards for remote operations
    // Note: Used by FeignRequestInterceptor#headerRelay() for remote AngusCtrl sharding sampling
    Tenant tenant = tenantRepo.findById(tenantId)
        .orElseThrow(() -> ResourceNotFound.of(tenantId, "Tenant"));
    UserBase user = userManager.checkValidAndFindUserBase(userId);
    PrincipalContext.set(createInnerPrincipal(tenant, user, applicationInfo));
  }

  /**
   * Retrieves LCS (License Control System) quota information.
   *
   * <p>This method is currently a placeholder for retrieving LCS quota data.
   * Both cloud service and private deployment versions will write to tenant
   * license tables in the future.</p>
   *
   * @return LCS quota information (currently returns null)
   */
  @Override
  public Quota findLcsQuota() {
    // TODO: Implement LCS quota retrieval for both cloud service and private deployment versions
    // Cloud service version: Retrieve from order details and product specifications
    // Private deployment version: Read from license files and validate with guard

    //    QuotaData quotaData = new QuotaData();
    //    if (applicationInfo.isCloudServiceEdition()) {
    //      OrderDetailVo order = orderRemote.findOrderByProduct(
    //          new FindOrderByProductDto().setProductCode(TesterConstant.ANGUSTESTER_PRODUCT_CODE)
    //              .setEditionType(EditionType.CLOUD_SERVICE)).orElseContentThrow();
    //      if (Objects.nonNull(order.getSpec()) && Objects
    //          .nonNull(order.getSpec().get("byBusiness"))) {
    //        try {
    //          ByBusinessSpecTo byBusinessSpec = objectMapper.readValue(
    //              objectMapper.writeValueAsString(order.getSpec().get("byBusiness")),
    //              ByBusinessSpecTo.class);
    //          if (Objects.nonNull(byBusinessSpec) && Objects.nonNull(
    //              byBusinessSpec.getAngusTesterSpec())) {
    //            quotaData.setThreads(byBusinessSpec.getAngusTesterSpec().getThreadsNum());
    //            // quotaData.setNode(byBusinessSpec.getANGUSTESTERSpec().getNodeNum()); TODO 云服务版缺失字段
    //            quotaData.setExpiredDate(
    //                Date.from(order.getExpiredDate().atZone(ZoneId.systemDefault()).toInstant()));
    //            return quotaData;
    //          }
    //        } catch (Exception e) {
    //          log.error("", e);
    //          throw SysException.of(PRODUCT_LIC_OBTAIN_ERROR_CODE, PRODUCT_LIC_OBTAIN_ERROR);
    //        }
    //      }
    //    } else {
    //      try {
    //        PageResult<TenantLcsDetailVo> page = tenantLcsPrivRemote.list(
    //            new TenantLcsFindDto().setProductCode(TesterConstant.ANGUSTESTER_PRODUCT_CODE))
    //            .orElseContentThrow();
    //        if (!page.getList().isEmpty()) {
    //          String content = page.getList().get(0).getContent();
    //          String licenseNo = page.getList().get(0).getNo();
    //          String lcsPath = new SpringAppDirUtils().getWorkDir() + TesterConstant.LCS_DIR + licenseNo
    //              + File.separator;
    //          File lcsFile = new File(lcsPath);
    //          FileUtils.writeByteArrayToFile(lcsFile, content.getBytes(StandardCharsets.UTF_8));
    //          String lcsKeypass = licenseNo + new Str0(
    //              new long[]{0x473558DE36922716L, 0x43CCDE755449D9F4L, 0x93C95B98018A1E61L,
    //                  0x3BDB4EA026F4DDC4L});
    return null;
  }


}
