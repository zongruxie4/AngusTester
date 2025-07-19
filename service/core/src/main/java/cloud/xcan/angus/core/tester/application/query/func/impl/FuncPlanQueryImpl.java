package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstAndRemove;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCommonCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.FUNC_PLAN_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_CASE_NOT_COMPLETED;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_NOT_STARTED;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_NOT_STARTED_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_REVIEW_NOT_ENABLED;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_REVIEW_NOT_ENABLED_CODE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.count.PlanCaseNum;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanSearchRepo;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCaseRepo;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import com.google.common.base.Joiner;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class FuncPlanQueryImpl implements FuncPlanQuery {

  @Resource
  private FuncPlanRepo funcPlanRepo;

  @Resource
  private FuncPlanSearchRepo funcPlanSearchRepo;

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  @Resource
  private FuncReviewCaseRepo funcReviewCaseRepo;

  @Resource
  private FuncBaselineQuery funcBaselineQuery;

  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Override
  public FuncPlan detail(Long id) {
    return new BizTemplate<FuncPlan>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        planDb = checkAndFind(id);
      }

      @Override
      protected FuncPlan process() {
        List<FuncPlan> plans = List.of(planDb);
        Set<Long> ids = Set.of(id);
        setCaseNum(plans, ids);
        setProgress(plans, ids);
        setMembers(plans, ids);
        // Set user name and avatar
        userManager.setUserNameAndAvatar(List.of(planDb), "ownerId", "ownerName", "ownerAvatar");
        return planDb;
      }
    }.execute();
  }

  @Override
  public Page<FuncPlan> list(GenericSpecification<FuncPlan> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<FuncPlan>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<FuncPlan> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(SearchCriteria.equal("deleted", false));
        criteria.add(SearchCriteria.equal("planDeleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        // checkAndSetAuthObjectIdCriteria(criteria); -> All project members are visible

        Page<FuncPlan> page = fullTextSearch
            ? funcPlanSearchRepo.find(criteria, pageable, FuncPlan.class, match)
            : funcPlanRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          Set<Long> planIds = page.getContent().stream().map(FuncPlan::getId)
              .collect(Collectors.toSet());
          setCaseNum(page.getContent(), planIds);
          setProgress(page.getContent(), planIds);
          setMembers(page.getContent(), planIds);
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "ownerId", "ownerName",
              "ownerAvatar");
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<FuncCaseInfo> notReviewed(Long planId, @Nullable Long moduleId,
      @Nullable Long reviewId) {
    return new BizTemplate<List<FuncCaseInfo>>() {
      @Override
      protected void checkParams() {
        // Check the review permission
        funcPlanAuthQuery.checkReviewAuth(getUserId(), planId);
      }

      @Override
      protected List<FuncCaseInfo> process() {
        Set<Long> excludeCaseIds = new HashSet<>();
        if (nonNull(reviewId)) {
          Set<Long> otherReviewPendingCaseIds =
              funcReviewCaseRepo.findPendingCaseIdByPlanIdAndReviewIdNot(planId, reviewId);
          excludeCaseIds.addAll(otherReviewPendingCaseIds);
          Set<Long> currentReviewCaseIds =
              funcReviewCaseRepo.findCaseIdByPlanIdAndReviewId(planId, reviewId);
          excludeCaseIds.addAll(currentReviewCaseIds);
        } else {
          Set<Long> pendingCaseIdsInReview = funcReviewCaseRepo.findPendingCaseIdByPlanId(planId);
          excludeCaseIds.addAll(pendingCaseIdsInReview);
        }

        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("planId", planId));
        filters.add(SearchCriteria.notEqual("reviewStatus", ReviewStatus.PASSED.getValue()));
        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        if (isNotEmpty(excludeCaseIds)) {
          filters.add(SearchCriteria.notIn("id", excludeCaseIds));
        }

        List<FuncCaseInfo> caseInfos = funcCaseInfoRepo.findAllByFilters(filters);
        // Set tester name and avatar -> Not Used
        // userManager.setUserNameAndAvatar(caseInfos, "testerId", "testerName", "testerAvatar");
        return caseInfos;
      }
    }.execute();
  }

  @Override
  public List<FuncCaseInfo> notEstablishedBaseline(Long planId, @Nullable Long moduleId,
      @Nullable Long baselineId) {
    return new BizTemplate<List<FuncCaseInfo>>() {
      FuncBaseline funcBaselineDb;

      @Override
      protected void checkParams() {
        // Check and find. It is empty when creating a baseline.
        if (nonNull(baselineId)) {
          funcBaselineDb = funcBaselineQuery.checkAndFind(baselineId);
        }
        // Check the establish baseline permission
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), planId);
      }

      @Override
      protected List<FuncCaseInfo> process() {
        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("planId", planId));
        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        if (nonNull(funcBaselineDb) && isNotEmpty(funcBaselineDb.getCaseIds())) {
          filters.add(SearchCriteria.notIn("id", funcBaselineDb.getCaseIds()));
        }

        List<FuncCaseInfo> caseInfos = funcCaseInfoRepo.findAllByFilters(filters);
        // Set tester name and avatar -> Not Used
        // userManager.setUserNameAndAvatar(caseInfos, "testerId", "testerName", "testerAvatar");
        return caseInfos;
      }
    }.execute();
  }

  @Override
  public FuncPlan findLeastByProjectId(Long projectId) {
    return funcPlanRepo.findLeastByProjectId(projectId);
  }

  @Override
  public boolean isAuthCtrl(Long id) {
    FuncPlan plan = funcPlanRepo.findById(id).orElse(null);
    return nonNull(plan) && plan.getAuth();
  }

  @Override
  public FuncPlan checkAndFind(Long id) {
    return funcPlanRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncPlan"));
  }

  @Override
  public List<FuncPlan> checkAndFind(Collection<Long> ids) {
    List<FuncPlan> plans = funcPlanRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(plans), ids.iterator().next(), "FuncPlan");
    if (ids.size() != plans.size()) {
      for (FuncPlan plan : plans) {
        assertResourceNotFound(ids.contains(plan.getId()), plan.getId(), "FuncPlan");
      }
    }
    return plans;
  }

  @Override
  public void checkNameExists(Long projectId, String name) {
    long count = funcPlanRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(FUNC_PLAN_NAME_REPEATED_T, new Object[]{name});
    }
  }

  @Override
  public void checkHasStarted(FuncPlan plan) {
    if (plan.getStatus().isNotInProcess()) {
      throw BizException.of(PLAN_NOT_STARTED_CODE, PLAN_NOT_STARTED);
    }
  }

  @Override
  public void checkReviewEnabled(FuncPlan plan) {
    if (isNull(plan.getReview()) || !plan.getReview()) {
      throw BizException.of(PLAN_REVIEW_NOT_ENABLED_CODE, PLAN_REVIEW_NOT_ENABLED);
    }
  }

  @Override
  public void checkPlanCaseCompleted(Long id) {
    long num = funcCaseRepo.countNotPassedByPlanId(id);
    if (num > 0) {
      throw ProtocolException.of(PLAN_CASE_NOT_COMPLETED);
    }
  }

  @Override
  public void checkConsistent(Long planId, HashSet<Long> caseIds) {
    if (isNotEmpty(caseIds)) {
      Set<Long> planCaseIds = funcCaseRepo.findAll0IdByPlanIdAndIdIn(planId, caseIds);
      if (caseIds.size() != planCaseIds.size()) {
        Set<Long> noExistCaseIds = new HashSet<>(caseIds);
        noExistCaseIds.removeAll(planCaseIds);
        ProtocolAssert.assertTrue(noExistCaseIds.isEmpty(),
            String.format("The use case [%s] does not belong to the plan [%s]",
                Joiner.on(", ").join(noExistCaseIds), planId));
      }
    }
  }

  @Override
  public void checkQuota() {
    long count = funcPlanRepo.count();
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterFuncPlan,
        null, count + 1);
  }

  @Override
  public void setCaseNum(List<FuncPlan> plans, Set<Long> planIds) {
    if (isNotEmpty(plans)) {
      Map<Long, Long> caseNumsMap = funcCaseRepo.findPlanCaseNumsGroupByPlanId(planIds)
          .stream().collect(toMap(PlanCaseNum::getPlanId, PlanCaseNum::getCaseNum));
      for (FuncPlan plan : plans) {
        plan.setCaseNum(caseNumsMap.containsKey(plan.getId())
            ? caseNumsMap.get(plan.getId()) : 0);
      }

      Map<Long, Long> validCaseNumsMap = funcCaseRepo.findValidPlanCaseNumsGroupByPlanId(planIds)
          .stream().collect(toMap(PlanCaseNum::getPlanId, PlanCaseNum::getCaseNum));
      for (FuncPlan plan : plans) {
        plan.setValidCaseNum(validCaseNumsMap.containsKey(plan.getId())
            ? validCaseNumsMap.get(plan.getId()) : 0);
      }
    }
  }

  /**
   * Note: Must be executed after setCaseNum().
   */
  @Override
  public void setProgress(List<FuncPlan> plans, Set<Long> planIds) {
    if (isNotEmpty(plans)) {
      Map<Long, Long> planPassedNumsMap = funcCaseRepo.findPlanPassedCaseNumsGroupByPlanId(planIds)
          .stream().collect(toMap(PlanCaseNum::getPlanId, PlanCaseNum::getCaseNum));
      for (FuncPlan plan : plans) {
        if (planPassedNumsMap.containsKey(plan.getId())) {
          plan.setProgress(new Progress().setTotal(plan.getValidCaseNum())
              .setCompleted(planPassedNumsMap.get(plan.getId()))
              .setCompletedRate(plan.getValidCaseNum() > 0 ?
                  BigDecimal.valueOf(planPassedNumsMap.get(plan.getId()))
                      .divide(BigDecimal.valueOf(plan.getValidCaseNum()), 4,
                          RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) // X 100%
                      .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO));
        } else {
          plan.setProgress(new Progress().setTotal(plan.getValidCaseNum())
              .setCompleted(0).setCompletedRate(BigDecimal.ZERO));
        }
      }
    }
  }

  @Override
  public void setMembers(List<FuncPlan> plans, Set<Long> planIds) {
    if (isNotEmpty(plans)) {
      Set<Long> testerIds = new HashSet<>();
      for (FuncPlan plan : plans) {
        if (isNotEmpty(plan.getTesterResponsibilities())) {
          testerIds.addAll(plan.getTesterResponsibilities().keySet());
        }
      }
      if (isEmpty(testerIds)) {
        return;
      }
      Map<Long, UserInfo> userMap = userManager.getUserBaseMap(testerIds).entrySet().stream()
          .collect(toMap(Entry::getKey, x -> new UserInfo().setId(x.getValue().getId())
              .setFullName(x.getValue().getFullName()).setAvatar(x.getValue().getAvatar())));
      for (FuncPlan plan : plans) {
        if (isNotEmpty(plan.getTesterResponsibilities())) {
          List<UserInfo> members = new ArrayList<>();
          for (Long userId : plan.getTesterResponsibilities().keySet()) {
            members.add(userMap.getOrDefault(userId, new UserInfo().setId(userId)));
          }
          plan.setMembers(members);
        }
      }
    }
  }

  @Override
  public void setSafeCloneName(FuncPlan plan) {
    String saltName = randomAlphanumeric(3);
    String clonedName = funcPlanRepo.existsByProjectIdAndName(
        plan.getProjectId(), plan.getName() + "-Copy")
        ? plan.getName() + "-Copy." + saltName : plan.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_NAME_LENGTH ? clonedName.substring(0,
        MAX_NAME_LENGTH_X2 - 3) + saltName : clonedName;
    plan.setName(clonedName);
  }

  /**
   * Set authorization conditions when you are not an administrator or only query yourself
   */
  @Override
  public boolean checkAndSetAuthObjectIdCriteria(Set<SearchCriteria> criteria) {
    SearchCriteria adminCriteria = findFirstAndRemove(criteria, "admin");
    boolean admin = false;
    if (Objects.nonNull(adminCriteria)) {
      admin = Boolean.parseBoolean(adminCriteria.getValue().toString().replaceAll("\"", ""));
    }
    if (!admin || !funcPlanAuthQuery.isAdminUser()) {
      criteria.add(SearchCriteria.in("authObjectId", userManager.getValidOrgAndUserIds()));
    }
    return false;
  }

  @Override
  public List<FuncPlan> getPlanCreatedSummaries(Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getCommonCreatorResourcesFilter(projectId, planId,
        createdDateStart, createdDateEnd, creatorIds);
    return funcPlanRepo.findAllByFilters(allFilters);
  }
}
