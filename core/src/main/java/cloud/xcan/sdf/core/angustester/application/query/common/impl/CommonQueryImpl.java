package cloud.xcan.sdf.core.angustester.application.query.common.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.NO_ADMIN_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.NO_ADMIN_PERMISSION_CODE;
import static cloud.xcan.sdf.core.angustester.infra.job.TaskWillOverdueEventJob.createInnerPrincipal;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstAndRemove;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.hasPolicy;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isOpSysAdmin;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isTenantSysAdmin;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.angusctrl.exec.ExecDoorRemote;
import cloud.xcan.sdf.api.angusctrl.exec.vo.ExecInfoVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.commonlink.dept.Dept;
import cloud.xcan.sdf.api.commonlink.dept.DeptRepo;
import cloud.xcan.sdf.api.commonlink.group.Group;
import cloud.xcan.sdf.api.commonlink.group.GroupRepo;
import cloud.xcan.sdf.api.commonlink.setting.SettingKey;
import cloud.xcan.sdf.api.commonlink.setting.quota.Quota;
import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.sdf.api.commonlink.setting.tenant.event.TesterEvent;
import cloud.xcan.sdf.api.commonlink.setting.tenant.quota.SettingTenantQuota;
import cloud.xcan.sdf.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import cloud.xcan.sdf.api.commonlink.tenant.Tenant;
import cloud.xcan.sdf.api.commonlink.tenant.TenantRepo;
import cloud.xcan.sdf.api.commonlink.user.UserBase;
import cloud.xcan.sdf.api.commonlink.user.UserBaseRepo;
import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.manager.SettingManager;
import cloud.xcan.sdf.api.manager.SettingTenantManager;
import cloud.xcan.sdf.api.manager.SettingTenantQuotaManager;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintQuery;
import cloud.xcan.sdf.core.angustester.domain.CombinedTarget;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.activity.SimpleActivityResource;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

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
  private ExecDoorRemote execDoorRemote;

  @Override
  public void checkAdminPermission() {
    if (!isAdminUser()) {
      throw BizException.of(NO_ADMIN_PERMISSION_CODE, NO_ADMIN_PERMISSION);
    }
  }

  @Override
  public String checkAndGetAuthName(AuthObjectType authObjectType, Long authObjectId) {
    switch (authObjectType) {
      case USER:
        UserBase userBase = userBaseRepo.findValidById(authObjectId).orElseThrow(
            () -> ResourceNotFound.of(String.valueOf(authObjectId), "User"));
        return userBase.getFullname();
      case GROUP:
        Group group = groupRepo.findValidById(authObjectId).orElseThrow(
            () -> ResourceNotFound.of(String.valueOf(authObjectId), "Group"));
        return group.getName();
      case DEPT:
        Dept dept = deptRepo.findById(authObjectId).orElseThrow(
            () -> ResourceNotFound.of(String.valueOf(authObjectId), "Department"));
        return dept.getName();
      default:
        // NOOP
    }
    return null;
  }

  /**
   * Contains data with deletedFlag=true
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

  @Override
  public void checkIndicatorTargetModifyAuth(CombinedTargetType targetType, Long targetId) {
    if (targetType.equals(API)) {
      apisAuthQuery.checkModifyAuth(getUserId(), targetId);
    }

    if (targetType.equals(SCENARIO)) {
      scenarioAuthQuery.checkModifyAuth(getUserId(), targetId);
    }
  }

  @Override
  public boolean isAdminUser() {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN) || isTenantSysAdmin() || isOpSysAdmin();
  }

  public static boolean isAdmin() {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN) || isTenantSysAdmin() || isOpSysAdmin();
  }

  @Override
  public @NotNull Map<Long, UserInfo> getUserInfoMap(Collection<Long> userIds) {
    return userManager.findUserBases(userIds).stream().collect(
        Collectors.toMap(UserBase::getId, x -> x.toUserInfo().setEmail(null).setMobile(null)));
  }

  @Override
  public CombinedTarget checkAndGetCombinedTarget(CombinedTargetType targetType, Long targetId,
      boolean findParent) {
    String targetName = null;
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
          .setTargetName(targetName).setApisBaseInfo(apiDb)
          .setApisParent(apisParent);
    } else if (CombinedTargetType.SERVICE.equals(targetType)) {
      Services serviceDb = servicesQuery.checkAndFind(targetId);
      targetName = serviceDb.getName();
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setServices(serviceDb)
          .setApisParent(null);
    } else if (CombinedTargetType.TASK.equals(targetType)) {
      TaskInfo taskDb = taskQuery.checkAndFindInfo(targetId);
      targetName = taskDb.getName();
      TaskSprint taskParent = null;
      if (findParent) {
        taskParent = taskSprintQuery.checkAndFind(taskDb.getSprintId());
      }
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setTaskInfo(taskDb)
          .setTaskParent(taskParent);
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
          .setTargetName(targetName).setCaseInfo(caseDb)
          .setCaseParent(caseParent);
    } else if (CombinedTargetType.FUNC_PLAN.equals(targetType)) {
      FuncPlan planDb = funcPlanQuery.checkAndFind(targetId);
      targetName = planDb.getName();
      return new CombinedTarget().setTargetType(targetType).setTargetId(targetId)
          .setTargetName(targetName).setPlan(planDb)
          .setCaseParent(null);
    }
    return null;
  }

  /**
   * No exception is thrown when the resource is not found to prevent query exceptions after data is
   * deleted.
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
          .setTargetName(targetName).setServices(serviceDb)
          .setApisParent(null);
    }
  }

  @Override
  public ActivityResource checkAndFindActivityResource(CombinedTargetType targetType,
      Long targetId) {
    switch (targetType) {
      case PROJECT:
        return projectQuery.checkAndFind(targetId);
      case TASK_SPRINT:
        return taskSprintQuery.checkAndFind(targetId);
      case TASK:
        return taskQuery.checkAndFindInfo(targetId);
      case FUNC_PLAN:
        return funcPlanQuery.checkAndFind(targetId);
      case FUNC_CASE:
        return funcCaseQuery.checkAndFindInfo(targetId);
      case SERVICE:
        return servicesQuery.checkAndFind(targetId);
      case API:
        return apisQuery.checkAndFindBaseInfo(targetId);
      case SCENARIO:
        return scenarioQuery.checkAndFind(targetId);
      case EXECUTION:
        ExecInfoVo execInfoVo = execDoorRemote.info(targetId, true).orElseContentThrow();
        return new SimpleActivityResource().setId(execInfoVo.getId()).setName(execInfoVo.getName())
            .setProjectId(execInfoVo.getProjectId());
    }
    throw CommProtocolException.of("Unsupported target type: " + targetType);
  }

  /**
   * Set authorization conditions when you are not an administrator or only query yourself
   */
  @Override
  public boolean checkAndSetAuthObjectIdCriteria(Set<SearchCriteria> criterias) {
    SearchCriteria adminCriteria = findFirstAndRemove(criterias, "adminFlag");
    boolean adminFlag = false;
    if (Objects.nonNull(adminCriteria)) {
      adminFlag = Boolean.parseBoolean(
          adminCriteria.getValue().toString().replaceAll("\"", ""));
    }
    if (!adminFlag || !isAdminUser()) {
      criterias.add(SearchCriteria.in("authObjectId", userManager.getValidOrgAndUserIds()));
    }
    return false;
  }

  @Override
  public void checkTenantQuota(QuotaResource quotaObject, Set<Long> objectIds, Long incr) {
    settingTenantQuotaManager.checkTenantQuota(quotaObject, objectIds, incr);
  }

  @Override
  public void checkLcsQuota(QuotaResource quotaObject, Set<Long> objectIds, Long incr) {
    // TODO
  }

  @Override
  public SettingTenantQuota findTenantQuota(QuotaResource name) {
    return settingTenantQuotaManager.findTenantQuota(PrincipalContext.getOptTenantId(), name);
  }

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

  @Override
  public void checkOrgExists(LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> typeIds) {
    if (isNotEmpty(typeIds)) {
      for (Entry<OrgTargetType, LinkedHashSet<Long>> entry : typeIds.entrySet()) {
        userManager.checkOrgExists(entry.getKey(), entry.getValue());
      }
    }
  }

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
                      x -> new OrgTargetInfo().setId(x.getId()).setName(x.getFullname())
                          .setAvatar(x.getAvatar()).setType(OrgTargetType.USER))
                  .collect(Collectors.toList())));
              break;
            case DEPT:
              List<Dept> depts = (List<Dept>) userManager.findOrgs(entry.getKey(),
                  entry.getValue());
              infos.put(entry.getKey(), new LinkedHashSet<>(depts.stream().map(
                      x -> new OrgTargetInfo().setId(x.getId()).setName(x.getName())
                          .setAvatar(null).setType(OrgTargetType.DEPT))
                  .collect(Collectors.toList())));
              break;
            case GROUP:
              List<Group> groups = (List<Group>) userManager.findOrgs(entry.getKey(),
                  entry.getValue());
              infos.put(entry.getKey(), new LinkedHashSet<>(groups.stream().map(
                      x -> new OrgTargetInfo().setId(x.getId()).setName(x.getName())
                          .setAvatar(null).setType(OrgTargetType.GROUP))
                  .collect(Collectors.toList())));
              break;
          }
        }
      }
    }
    return infos;
  }

  @Override
  public void setInnerPrincipal(Long tenantId, Long userId) {
    // Transfer principal downwards
    // Note: Passed by FeignRequestInterceptor#headerRelay() for remoted AngusCtrl sharding sampling
    Tenant tenant = tenantRepo.findById(tenantId)
        .orElseThrow(() -> ResourceNotFound.of(tenantId, "Tenant"));
    UserBase user = userManager.checkValidAndFindUserBase(userId);
    PrincipalContext.set(createInnerPrincipal(tenant, user, applicationInfo));
  }

  // TODO 云服务版本和私有化版本都往租户许可表里写
  @Override
  public Quota findLcsQuota() {
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
    //          throw CommSysException.of(PRODUCT_LIC_OBTAIN_ERROR_CODE, PRODUCT_LIC_OBTAIN_ERROR);
    //        }
    //      }
    //    } else {
    //      try {
    //        PageResult<TenantLcsDetailVo> page = tenantLcsPrivRemote.list(
    //            new TenantLcsFindDto().setProductCode(TesterConstant.ANGUSTESTER_PRODUCT_CODE))
    //            .orElseContentThrow();
    //        if (!page.getList().isEmpty()) {
    //          String content = page.getList().get(0).getContent();
    //          String lcsNo = page.getList().get(0).getNo();
    //          String lcsPath = new SpringAppDirUtils().getWorkDir() + TesterConstant.LCS_DIR + lcsNo
    //              + File.separator;
    //          File lcsFile = new File(lcsPath);
    //          FileUtils.writeByteArrayToFile(lcsFile, content.getBytes(StandardCharsets.UTF_8));
    //          String lcsKeypass = lcsNo + new Str0(
    //              new long[]{0x473558DE36922716L, 0x43CCDE755449D9F4L, 0x93C95B98018A1E61L,
    //                  0x3BDB4EA026F4DDC4L});
    //          Guard guard = new Guard(lcsKeypass, lcsPath);
    //          // quotaData.setUser(Integer.parseInt(guard.var101())); // Not required for current service
    //          quotaData.setThreads(Integer.parseInt(guard.var120()));
    //          quotaData.setNode(Integer.parseInt(guard.var121()));
    //          quotaData.setExpiredDate(
    //              new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.US)
    //                  .parse(guard.var108()));
    //          return quotaData;
    //        }
    //      } catch (Exception e) {
    //        log.error("", e);
    //        throw CommSysException.of(PRODUCT_LIC_OBTAIN_ERROR_CODE, PRODUCT_LIC_OBTAIN_ERROR);
    //      }
    //    }
    //    throw CommSysException.of(PRODUCT_LIC_OBTAIN_ERROR_CODE, PRODUCT_LIC_OBTAIN_ERROR);
    return null;
  }


}
