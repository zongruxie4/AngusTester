package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_DAILY_WORKLOAD;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.assembleCaseTesterCount;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.assembleCaseTesterProgressCount;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.assembleTimeSeriesByFormat;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.countCreationBaseline;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.countCreationCase;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.countCreationPlan;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.countCreationReview;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCaseCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCaseTesterResourcesFilter;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCommonCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.toCaseDetailSummary;
import static cloud.xcan.angus.core.tester.application.converter.FuncPlanConverter.toFuncPlanSummary;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.assembleBackloggedCaseCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.assembleCaseProgressCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.assembleLeadTimeCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.assembleOverdueAssessmentCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.assembleRecentDeliveryCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.assembleUnplannedWorkCount0;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.calcDailyProcessedWorkload;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleCaseCoreKpiOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleGrowthTrendCount;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleGrowthTrendDetail;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleResourceCreationCount;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleResourceCreationDetail;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleReviewEfficiencyOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleTestingEfficiencyOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleWorkload0;
import static cloud.xcan.angus.core.tester.application.converter.ProjectConverter.toProjectSummary;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleBurnDownChartDetail;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MESSAGE_TOTAL;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.FunctionCaseAssignment;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.FunctionCaseAssignmentCode;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.FunctionCaseModification;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.FunctionCaseModificationCode;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.CASE_CAN_NOT_REVIEWED_T;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.CASE_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.CASE_NOT_REVIEWED_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.CASE_NOT_REVIEWED_T;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_BACKLOG_CASES;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_BURNDOWN;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_CORE_KPI;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_GROWTH_TREND;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_LEAD_TIME;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_OVERDUE_ASSESSMENT;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_PROGRESS;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_RECENT_DELIVERY;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_RESOURCE_CREATION;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_REVIEW_EFFICIENCY;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_TESTING_EFFICIENCY;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_UNPLANNED_CASE;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.EXPORT_ANALYSIS_CASE_WORKLOAD;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.merge;
import static cloud.xcan.angus.remote.search.SearchCriteria.notEqual;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserFullName;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.DateUtils.asDate;
import static cloud.xcan.angus.spec.utils.DateUtils.formatByDatePattern;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter;
import cloud.xcan.angus.core.tester.application.query.activity.ActivityQuery;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.comment.CommentTargetType;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoListRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.count.BackloggedOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.BurnDownChartOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.CoreKpiCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.CoreKpiDetail;
import cloud.xcan.angus.core.tester.domain.func.cases.count.CoreKpiOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncCaseCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncTesterCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncTesterProgressCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.GrowthTrendCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.GrowthTrendDetail;
import cloud.xcan.angus.core.tester.domain.func.cases.count.GrowthTrendOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.LeadTimeOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.OverdueAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ProgressOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.RecentDeliveryOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ResourceCreationDetail;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ResourceCreationOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ReviewEfficiencyCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ReviewEfficiencyDetail;
import cloud.xcan.angus.core.tester.domain.func.cases.count.ReviewEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.TestingEfficiencyCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.TestingEfficiencyDetail;
import cloud.xcan.angus.core.tester.domain.func.cases.count.TestingEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.UnplannedWorkOverview;
import cloud.xcan.angus.core.tester.domain.func.cases.count.WorkloadOverview;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavourite;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollow;
import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollowRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReviewRepo;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseDetailSummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseEfficiencySummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseSummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncPlanSummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncPlanWorkSummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncProjectWorkSummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncTesterWorkSummary;
import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.summary.ProjectSummary;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.cases.TaskFuncCase;
import cloud.xcan.angus.core.tester.domain.task.count.BackloggedCount;
import cloud.xcan.angus.core.tester.domain.task.count.BackloggedDetail;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartCount;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartDetail;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeCount;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeDetail;
import cloud.xcan.angus.core.tester.domain.task.count.OverdueAssessmentCount;
import cloud.xcan.angus.core.tester.domain.task.count.OverdueAssessmentDetail;
import cloud.xcan.angus.core.tester.domain.task.count.ProgressCount;
import cloud.xcan.angus.core.tester.domain.task.count.ProgressDetail;
import cloud.xcan.angus.core.tester.domain.task.count.RecentDeliveryCount;
import cloud.xcan.angus.core.tester.domain.task.count.RecentDeliveryDetail;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkCount;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkDetail;
import cloud.xcan.angus.core.tester.domain.task.count.WorkloadCount;
import cloud.xcan.angus.core.tester.domain.task.count.WorkloadDetail;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.annotations.NonNullable;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@Biz
public class FuncCaseQueryImpl implements FuncCaseQuery {

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  @Resource
  private FuncCaseInfoListRepo funcCaseInfoListRepo;

  @Resource
  private FuncPlanRepo funcPlanRepo;

  @Resource
  private TagQuery tagQuery;

  @Resource
  private FuncReviewRepo funcReviewRepo;

  @Resource
  private FuncBaselineRepo funcBaselineRepo;

  @Resource
  private FuncCaseFollowRepo funcCaseFollowRepo;

  @Resource
  private FuncCaseFavouriteRepo funcCaseFavoriteRepo;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private FuncReviewQuery funcReviewQuery;

  @Resource
  private FuncBaselineQuery funcBaselineQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommentQuery commentQuery;

  @Resource
  private ActivityQuery activityQuery;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Resource
  private UserManager userManager;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public FuncCase detail(Long id) {
    return new BizTemplate<FuncCase>() {
      FuncCase caseDb;

      @Override
      protected void checkParams() {
        caseDb = checkAndFind(id);
      }

      @Override
      protected FuncCase process() {
        List<FuncCase> cases = List.of(caseDb);
        if (isUserAction()) {
          // Set favourite state
          setFavourite(cases);
          // Set follow state
          setFollow(cases);
        }
        // Set tags
        tagQuery.setTags(cases);
        // Set reference tasks and cases
        taskFuncCaseQuery.setAssocForCase(cases);
        // Set user name and avatar
        userManager.setUserNameAndAvatar(cases, "createdBy");
        // Set comment num
        int commentNum = commentQuery.getCommentNum(id, CommentTargetType.FUNC_CASE.getValue());
        caseDb.setCommentNum(commentNum);
        // Set activity num
        int activityNum = activityQuery.getActivityNumByMainTarget(id);
        caseDb.setActivityNum(activityNum);
        // Set progress
        setCaseProgress(caseDb);
        return caseDb;
      }
    }.execute();
  }

  @Override
  public Page<FuncCaseInfo> list(GenericSpecification<FuncCaseInfo> spec, Pageable pageable) {
    return new BizTemplate<Page<FuncCaseInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<FuncCaseInfo> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(equal("deleted", false));
        criteria.add(equal("planDeleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        funcPlanQuery.checkAndSetAuthObjectIdCriteria(criteria);

        // Assemble mainClass table
        Page<FuncCaseInfo> page = funcCaseInfoListRepo.find(criteria, pageable,
            FuncCaseInfo.class, null);

        if (page.hasContent()) {
          if (isUserAction()) {
            // Set favourite state
            setFavourite(page.getContent());
            // Set follow state
            setFollow(page.getContent());
          }

          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
          // Set tester name and avatar
          userManager.setUserNameAndAvatar(page.getContent(),
              "testerId", "testerName", "testerAvatar");

          // Set tags
          tagQuery.setTags(page.getContent());
          // Set progress
          setCaseInfoProgress(page.getContent());
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<FuncCaseInfo> notAssociatedCaseInTask(Long taskId, @Nullable Long moduleId) {
    return new BizTemplate<List<FuncCaseInfo>>() {
      TaskInfo taskDb;

      @Override
      protected void checkParams() {
        taskDb = taskQuery.checkAndFindInfo(taskId);
      }

      @Override
      protected List<FuncCaseInfo> process() {
        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("projectId", taskDb.getProjectId()));
        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        //Auto added by @Where
        //filters.add(SearchCriteria.equal("deleted", false));
        //filters.add(SearchCriteria.equal("planDeleted", false));

        Set<Long> associatedCaseIds = taskFuncCaseQuery.findWideByTargetId(List.of(taskId))
            .stream().filter(TaskFuncCase::isTaskAssocCase).map(TaskFuncCase::getWideTargetIds)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        associatedCaseIds.remove(taskId); // Delete unrelated IDs
        if (isNotEmpty(associatedCaseIds)) {
          filters.add(SearchCriteria.notIn("id", associatedCaseIds));
        }
        return funcCaseInfoRepo.findAllByFilters(filters, Sort.by(Direction.DESC, "createdDate"));
      }
    }.execute();
  }

  @Override
  public List<FuncCaseInfo> notAssociatedCaseInCase(Long caseId, @Nullable Long moduleId) {
    return new BizTemplate<List<FuncCaseInfo>>() {
      FuncCaseInfo caseDb;

      @Override
      protected void checkParams() {
        caseDb = checkAndFindInfo(caseId);
      }

      @Override
      protected List<FuncCaseInfo> process() {
        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("projectId", caseDb.getProjectId()));
        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        //Auto added by @Where
        //filters.add(SearchCriteria.equal("deleted", false));
        //filters.add(SearchCriteria.equal("planDeleted", false));

        Set<Long> associatedCaseIds = taskFuncCaseQuery.findWideByTargetId(List.of(caseId))
            .stream().filter(TaskFuncCase::isCaseAssocCase).map(TaskFuncCase::getWideTargetIds)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        associatedCaseIds.add(caseId); // Exclude assoc oneself
        if (isNotEmpty(associatedCaseIds)) {
          filters.add(SearchCriteria.notIn("id", associatedCaseIds));
        }
        return funcCaseInfoRepo.findAllByFilters(filters, Sort.by(Direction.DESC, "createdDate"));
      }
    }.execute();
  }

  @Override
  public FuncLastResourceCreationCount creationResourcesStatistics(Long projectId, Long planId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinPlan, boolean joinReview, boolean joinBaseline) {
    return new BizTemplate<FuncLastResourceCreationCount>() {

      @Override
      protected FuncLastResourceCreationCount process() {
        final FuncLastResourceCreationCount result = new FuncLastResourceCreationCount();

        // Find all when condition is null, else find by condition
        Set<Long> createdBys = isNull(creatorObjectType) ? null
            : userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);

        // Number of statistical case
        Set<SearchCriteria> allCaseFilters = getCaseCreatorResourcesFilter(
            projectId, planId, createdDateStart, createdDateEnd, createdBys);
        List<FuncCaseEfficiencySummary> allCases = funcCaseInfoRepo.findProjectionByFilters(
            FuncCaseInfo.class, FuncCaseEfficiencySummary.class, allCaseFilters);
        countCreationCase(result, allCases);

        // Number of statistical plan
        Set<SearchCriteria> commonFilters = getCommonCreatorResourcesFilter(projectId,
            planId, createdDateStart, createdDateEnd, createdBys);
        if (joinPlan) {
          Set<SearchCriteria> planFilters = merge(commonFilters, equal("deleted", false));
          List<FuncPlan> plans = funcPlanRepo.findAllByFilters(planFilters);
          countCreationPlan(result, plans);
        }

        // Number of statistical case
        if (joinReview) {
          List<FuncReview> reviews = funcReviewRepo.findAllByFilters(commonFilters);
          countCreationReview(result, reviews);
        }

        // Number of statistical case
        if (joinBaseline) {
          List<FuncBaseline> baselines = funcBaselineRepo.findAllByFilters(commonFilters);
          countCreationBaseline(result, baselines);
        }
        return result;
      }
    }.execute();
  }

  @Override
  public FuncCaseCount countStatistics(Set<SearchCriteria> criteria) {
    return new BizTemplate<FuncCaseCount>() {
      String projectId;

      @Override
      protected void checkParams() {
        projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
        assertTrue(nonNull(projectId), "Parameter projectId is required");
      }

      @Override
      protected FuncCaseCount process() {
        criteria.add(equal("deleted", false));
        criteria.add(equal("planDeleted", false));
        return funcCaseInfoListRepo.count(criteria);
      }
    }.execute();
  }

  @Override
  public List<FuncTesterCount> testerSummaryStatistics(Long projectId, Long planId) {
    return new BizTemplate<List<FuncTesterCount>>() {

      @Override
      protected List<FuncTesterCount> process() {
        final List<FuncTesterCount> testerCounts = new ArrayList<>();

        Set<SearchCriteria> allTaskFilters = getCaseTesterResourcesFilter(projectId, planId,
            null, null, null);
        Map<Long, List<FuncCaseEfficiencySummary>> testerTaskMap = funcCaseInfoRepo.findProjectionByFilters(
                FuncCaseInfo.class, FuncCaseEfficiencySummary.class, allTaskFilters).stream()
            .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
        if (isEmpty(testerTaskMap)) {
          return testerCounts;
        }

        Set<Long> testerIds = testerTaskMap.keySet();  // Is Full
        Map<Long, UserBase> userMaps = userManager.getUserBaseMap(testerIds);

        for (Long testerId : testerIds) {
          testerCounts.add(assembleCaseTesterCount(testerId, userMaps, testerTaskMap));
        }

        return testerCounts;
      }
    }.execute();
  }

  @Override
  public List<FuncTesterProgressCount> testerProgressStatistics(Long projectId, Long planId) {
    return new BizTemplate<List<FuncTesterProgressCount>>() {

      @Override
      protected List<FuncTesterProgressCount> process() {
        final List<FuncTesterProgressCount> testerProgressCounts = new ArrayList<>();

        Set<SearchCriteria> allTaskFilters = getCaseTesterResourcesFilter(projectId, planId,
            null, null, null);
        Map<Long, List<FuncCaseEfficiencySummary>> testerTaskMap = funcCaseInfoRepo.findProjectionByFilters(
                FuncCaseInfo.class, FuncCaseEfficiencySummary.class, allTaskFilters).stream()
            .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
        if (isEmpty(testerTaskMap)) {
          return testerProgressCounts;
        }

        Set<Long> testerIds = testerTaskMap.keySet();  // Is Full
        Map<Long, UserBase> userMaps = userManager.getUserBaseMap(testerIds);

        for (Long testerId : testerIds) {
          testerProgressCounts.add(
              assembleCaseTesterProgressCount(testerId, userMaps, testerTaskMap));
        }

        return testerProgressCounts;
      }
    }.execute();
  }

  @Override
  public FuncProjectWorkSummary projectWorkStatistics(Long projectId) {
    return new BizTemplate<FuncProjectWorkSummary>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        projectDb = projectQuery.detail(projectId);
      }

      @Override
      protected FuncProjectWorkSummary process() {
        FuncProjectWorkSummary summary = new FuncProjectWorkSummary();
        summary.setSummary(getProjectSummary(projectDb));
        summary.setCaseCount(countStatistics(new HashSet<>(
            Set.of(equal("projectId", projectId)))));

        // When there are no use plans, the default status value is set to 0
        FuncLastResourceCreationCount resourcesCount = creationResourcesStatistics(projectId, null,
            null, null, null, null, true, false, false);
        summary.setPlanByStatus(resourcesCount.getPlanByStatus());
        summary.setCaseByTestResult(resourcesCount.getCaseByTestResult());
        summary.setCaseByReviewStatus(resourcesCount.getCaseByReviewStatus());
        summary.setCaseByPriority(resourcesCount.getCaseByPriority());

        List<FuncPlan> plans = funcPlanRepo.findByProjectId(projectId);
        for (FuncPlan planDb : plans) {
          summary.getPlanSummaries().add(planWorkStatistics(planDb.getId()));
        }
        return summary;
      }
    }.execute();
  }

  @Override
  public FuncPlanWorkSummary planWorkStatistics(Long planId) {
    return new BizTemplate<FuncPlanWorkSummary>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        planDb = funcPlanQuery.detail(planId);
      }

      @Override
      protected FuncPlanWorkSummary process() {
        FuncPlanWorkSummary summary = new FuncPlanWorkSummary();
        summary.setSummary(joinSupplier.execute(() -> getPlanSummary(planDb)));
        summary.setProgress(planDb.getProgress());
        summary.setMembers(planDb.getMembers());

        summary.setCaseCount(countStatistics(new HashSet<>(
            Set.of(equal("projectId", planDb.getProjectId()), equal("planId", planId)))));

        // When there are no use cases, the default status value is set to 0
        FuncLastResourceCreationCount resourcesCount = creationResourcesStatistics(null, planId,
            null, null, null, null, true, false, false);
        summary.setCaseByTestResult(resourcesCount.getCaseByTestResult());
        summary.setCaseByReviewStatus(resourcesCount.getCaseByReviewStatus());
        summary.setCaseByPriority(resourcesCount.getCaseByPriority());

        Map<Long, FuncTesterCount> testerCountMap = testerSummaryStatistics(null,
            planId).stream().collect(Collectors.toMap(FuncTesterCount::getTesterId, x -> x));

        if (isNotEmpty(testerCountMap)) {
          List<FuncCaseInfo> testCases0 = funcCaseInfoRepo.findAllByPlanId(planId);
          if (isNotEmpty(testCases0)) {
            Map<Long, List<FuncCaseSummary>> testCaseMap = joinSupplier.execute(
                    () -> getCaseSummary(testCases0)).stream().
                collect(groupingBy(FuncCaseSummary::getTesterId));
            for (Entry<Long, List<FuncCaseSummary>> entry : testCaseMap.entrySet()) {
              FuncTesterWorkSummary testerSummary = new FuncTesterWorkSummary();
              FuncTesterCount count = testerCountMap.getOrDefault(entry.getKey(),
                  new FuncTesterCount());
              testerSummary.setTesterId(count.getTesterId());
              testerSummary.setTesterName(count.getTesterName());
              testerSummary.setTesterAvatar(count.getTesterAvatar());
              count.setTesterId(null).setTesterName(null).setTesterAvatar(null);
              testerSummary.setCount(count);

              Map<LocalDateTime, List<FuncCaseSummary>> testCasesGroup = entry.getValue().stream()
                  .collect(groupingBy(x -> x.getCreatedDate().truncatedTo(ChronoUnit.DAYS),
                      Collectors.mapping(Function.identity(), Collectors.toList())));
              Map<String, List<FuncCaseSummary>> sumCaseByDay = testCasesGroup.entrySet().stream()
                  .collect(Collectors.toMap(x -> formatByDatePattern(asDate(x.getKey())),
                      Entry::getValue));
              for (Entry<String, List<FuncCaseSummary>> entry0 : sumCaseByDay.entrySet()) {
                testerSummary.getGroupByDay().put(entry0.getKey(), entry0.getValue());
              }
              summary.getTesterSummaries().add(testerSummary);
            }
          }
        }
        return summary;
      }
    }.execute();
  }

  @Override
  public FuncTesterWorkSummary testerWorkStatistics(
      Long projectId, @Nullable Long planId, Long userId) {
    return new BizTemplate<FuncTesterWorkSummary>() {

      @Override
      protected FuncTesterWorkSummary process() {
        FuncTesterWorkSummary summary = new FuncTesterWorkSummary();

        Map<Long, FuncTesterCount> testerCountMap = testerSummaryStatistics(projectId, planId)
            .stream().collect(Collectors.toMap(FuncTesterCount::getTesterId, x -> x));
        FuncTesterCount count = testerCountMap.getOrDefault(userId, new FuncTesterCount());
        summary.setTesterId(count.getTesterId());
        summary.setTesterName(count.getTesterName());
        summary.setTesterAvatar(count.getTesterAvatar());
        count.setTesterId(null).setTesterName(null).setTesterAvatar(null);
        summary.setCount(count);

        List<FuncCaseInfo> testCases0 = nonNull(planId)
            ? funcCaseInfoRepo.findAllByPlanIdAndTesterId(planId, userId)
            : funcCaseInfoRepo.findAllByProjectIdAndTesterId(projectId, userId);

        if (isNotEmpty(testCases0)) {
          List<FuncCaseSummary> testCases = joinSupplier.execute(() -> getCaseSummary(testCases0));

          Map<LocalDateTime, List<FuncCaseSummary>> testCasesGroup = testCases.stream()
              .collect(groupingBy(x -> x.getCreatedDate().truncatedTo(ChronoUnit.DAYS),
                  Collectors.mapping(Function.identity(), Collectors.toList())));
          Map<String, List<FuncCaseSummary>> sumCaseByDay = testCasesGroup.entrySet().stream()
              .collect(Collectors.toMap(x -> formatByDatePattern(asDate(x.getKey())),
                  Entry::getValue));
          for (Entry<String, List<FuncCaseSummary>> entry0 : sumCaseByDay.entrySet()) {
            summary.getGroupByDay().put(entry0.getKey(), entry0.getValue());
          }
        }
        return summary;
      }
    }.execute();
  }

  @Override
  public ProgressOverview progress(Long projectId, Long planId, AuthObjectType testerOrgType,
      Long testerOrgId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinTesterDetail, boolean joinDataDetail) {
    return new BizTemplate<ProgressOverview>() {

      @Override
      protected ProgressOverview process() {
        ProgressOverview overview = new ProgressOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        ProgressCount total = assembleCaseProgressCount0(cases);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_PROGRESS).split(","));
          ProgressDetail totalDetail = new ProgressDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> caseMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            ProgressCount tester = assembleCaseProgressCount0(
                caseMap.getOrDefault(testerId, emptyList()));
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              ProgressDetail testerDetail = new ProgressDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public BurnDownChartOverview burndownChart(
      Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail) {
    return new BizTemplate<BurnDownChartOverview>() {


      @Override
      protected BurnDownChartOverview process() {
        BurnDownChartOverview overview = new BurnDownChartOverview();
        LocalDateTime safeCreatedDateStart = null, safeCreatedDateEnd = null;
        if (nonNull(planId)) {
          FuncCaseInfo earliestCase = funcCaseInfoRepo.findEarliestByPlanId(planId);
          if (nonNull(earliestCase)) {
            FuncPlan planDb = funcPlanQuery.checkAndFind(planId);
            safeCreatedDateStart = nullSafe(createdDateStart, earliestCase.getCreatedDate());
            FuncCaseInfo leastCase = funcCaseInfoRepo.findLeastByPlanId(planId);
            safeCreatedDateEnd = nullSafe(createdDateEnd,
                planDb.getDeadlineDate().isAfter(leastCase.getCreatedDate())
                    ? planDb.getDeadlineDate() : leastCase.getCreatedDate());
          }
        } else if (nonNull(projectId)) {
          FuncCaseInfo caseInfo = funcCaseInfoRepo.findEarliestByProjectId(projectId);
          if (nonNull(caseInfo)) {
            Project projectDb = projectQuery.checkAndFind(projectId);
            safeCreatedDateStart = nullSafe(createdDateStart, projectDb.getStartDate());
            safeCreatedDateEnd = nullSafe(createdDateEnd, projectDb.getDeadlineDate());
          }
        }

        if (isNull(safeCreatedDateStart)) {
          // Set the default data for web
          overview.getTotalBurnDownCharts()
              .put(BurnDownResourceType.NUM, new BurnDownChartCount());
          overview.getTotalBurnDownCharts()
              .put(BurnDownResourceType.WORKLOAD, new BurnDownChartCount());
          return overview;
        }

        List<FuncCaseEfficiencySummary> validCases = getCaseValidEfficiencySummaries(
            projectId, planId, safeCreatedDateStart, safeCreatedDateEnd, testerOrgType,
            testerOrgId);
        if (isEmpty(validCases)) {
          // Set the default data for web
          overview.getTotalBurnDownCharts()
              .put(BurnDownResourceType.NUM, new BurnDownChartCount());
          overview.getTotalBurnDownCharts()
              .put(BurnDownResourceType.WORKLOAD, new BurnDownChartCount());
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            validCases.stream().map(FuncCaseEfficiencySummary::getTesterId)
                .collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        Map<BurnDownResourceType, BurnDownChartCount> total = new HashMap<>();
        assembleTimeSeriesByFormat(total, validCases, safeCreatedDateStart, safeCreatedDateEnd);
        overview.setTotalBurnDownCharts(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_BURNDOWN).split(","));
          BurnDownChartDetail totalDetail = assembleBurnDownChartDetail(
              message(MESSAGE_TOTAL), total);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> testerTaskMap = validCases.stream()
              .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testerTaskMap.keySet()) {
            Map<BurnDownResourceType, BurnDownChartCount> tester = new HashMap<>();
            assembleTimeSeriesByFormat(tester, testerTaskMap.get(testerId),
                safeCreatedDateStart, safeCreatedDateEnd);
            overview.getTestersBurnDownCharts().put(testerId, tester);
            if (joinDataDetail) {
              BurnDownChartDetail testerDetail = assembleBurnDownChartDetail(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName(), tester);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public WorkloadOverview workload(Long projectId, Long planId, AuthObjectType testerOrgType,
      Long testerOrgId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinTesterDetail, boolean joinDataDetail) {
    return new BizTemplate<WorkloadOverview>() {

      @Override
      protected WorkloadOverview process() {
        WorkloadOverview overview = new WorkloadOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        WorkloadCount total = new WorkloadCount();
        assembleWorkload0(cases, total);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_WORKLOAD).split(","));
          WorkloadDetail totalDetail = new WorkloadDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> caseMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            WorkloadCount tester = new WorkloadCount();
            assembleWorkload0(caseMap.getOrDefault(testerId, emptyList()), tester);
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              WorkloadDetail testerDetail = new WorkloadDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public OverdueAssessmentOverview overdueAssessment(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail) {
    return new BizTemplate<OverdueAssessmentOverview>() {

      @Override
      protected OverdueAssessmentOverview process() {
        OverdueAssessmentOverview overview = new OverdueAssessmentOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        double dailyProcessedWorkload = calcDailyProcessedWorkload(cases,
            DEFAULT_DAILY_WORKLOAD);
        OverdueAssessmentCount total = assembleOverdueAssessmentCount0(cases,
            dailyProcessedWorkload);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_OVERDUE_ASSESSMENT).split(","));
          OverdueAssessmentDetail totalDetail = new OverdueAssessmentDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> caseMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            List<FuncCaseEfficiencySummary> testerCases = caseMap.getOrDefault(testerId,
                emptyList());
            dailyProcessedWorkload = calcDailyProcessedWorkload(testerCases,
                DEFAULT_DAILY_WORKLOAD);
            OverdueAssessmentCount tester = assembleOverdueAssessmentCount0(
                testerCases, dailyProcessedWorkload);
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              OverdueAssessmentDetail testerDetail = new OverdueAssessmentDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public TestingEfficiencyOverview testingEfficiency(Long projectId, Long planId,
      AuthObjectType testerOrgType, Long testerOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinTesterDetail, boolean joinDataDetail) {
    return new BizTemplate<TestingEfficiencyOverview>() {

      @Override
      protected TestingEfficiencyOverview process() {
        TestingEfficiencyOverview overview = new TestingEfficiencyOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        TestingEfficiencyCount total = assembleTestingEfficiencyOverview(cases);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_TESTING_EFFICIENCY).split(","));
          TestingEfficiencyDetail totalDetail = new TestingEfficiencyDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> taskMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            TestingEfficiencyCount tester = assembleTestingEfficiencyOverview(
                taskMap.getOrDefault(testerId, emptyList()));
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              TestingEfficiencyDetail testerDetail = new TestingEfficiencyDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public CoreKpiOverview coreKpi(Long projectId, Long planId, AuthObjectType testerOrgType,
      Long testerOrgId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinTesterDetail, boolean joinDataDetail) {
    return new BizTemplate<CoreKpiOverview>() {

      @Override
      protected CoreKpiOverview process() {
        CoreKpiOverview overview = new CoreKpiOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        CoreKpiCount total = assembleCaseCoreKpiOverview(cases);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_CORE_KPI).split(","));
          CoreKpiDetail totalDetail = new CoreKpiDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> caseMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            CoreKpiCount tester = assembleCaseCoreKpiOverview(
                caseMap.getOrDefault(testerId, emptyList()));
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              CoreKpiDetail testerDetail = new CoreKpiDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public ReviewEfficiencyOverview reviewEfficiency(Long projectId, Long planId,
      AuthObjectType testerOrgType, Long testerOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinTesterDetail, boolean joinDataDetail) {
    return new BizTemplate<ReviewEfficiencyOverview>() {

      @Override
      protected ReviewEfficiencyOverview process() {
        ReviewEfficiencyOverview overview = new ReviewEfficiencyOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        ReviewEfficiencyCount total = assembleReviewEfficiencyOverview(cases);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_REVIEW_EFFICIENCY).split(","));
          ReviewEfficiencyDetail totalDetail = new ReviewEfficiencyDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> taskMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            ReviewEfficiencyCount tester = assembleReviewEfficiencyOverview(
                taskMap.getOrDefault(testerId, emptyList()));
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              ReviewEfficiencyDetail testerDetail = new ReviewEfficiencyDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public BackloggedOverview backloggedWork(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail) {
    return new BizTemplate<BackloggedOverview>() {

      @Override
      protected BackloggedOverview process() {
        BackloggedOverview overview = new BackloggedOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        BackloggedCount total = assembleBackloggedCaseCount0(cases);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_BACKLOG_CASES).split(","));
          BackloggedDetail totalDetail = new BackloggedDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> taskMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            BackloggedCount tester = assembleBackloggedCaseCount0(
                taskMap.getOrDefault(testerId, emptyList()));
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              BackloggedDetail testerDetail = new BackloggedDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public RecentDeliveryOverview recentDelivery(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail) {
    return new BizTemplate<RecentDeliveryOverview>() {

      @Override
      protected RecentDeliveryOverview process() {
        RecentDeliveryOverview overview = new RecentDeliveryOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        Map<String, RecentDeliveryCount> total = assembleRecentDeliveryCount0(cases);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_RECENT_DELIVERY).split(","));
          for (Entry<String, RecentDeliveryCount> entry : total.entrySet()) {
            RecentDeliveryDetail totalDetail = new RecentDeliveryDetail();
            totalDetail.setName(message(MESSAGE_TOTAL));
            CoreUtils.copyProperties(total.get(entry.getKey()), totalDetail);
            List<RecentDeliveryDetail> details = new ArrayList<>();
            details.add(totalDetail);
            overview.getDataDetails().put(entry.getKey(), details);
          }
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> caseMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            Map<String, RecentDeliveryCount> tester = assembleRecentDeliveryCount0(
                caseMap.getOrDefault(testerId, emptyList()));
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              for (Entry<String, RecentDeliveryCount> entry : tester.entrySet()) {
                RecentDeliveryDetail testerDetail = new RecentDeliveryDetail();
                testerDetail.setName(
                    testers.getOrDefault(testerId, new UserInfo()).getFullName());
                CoreUtils.copyProperties(tester.get(entry.getKey()), testerDetail);
                overview.getDataDetails().get(entry.getKey()).add(testerDetail);
              }
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public LeadTimeOverview leadTime(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail) {
    return new BizTemplate<LeadTimeOverview>() {

      @Override
      protected LeadTimeOverview process() {
        LeadTimeOverview overview = new LeadTimeOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        LeadTimeCount total = assembleLeadTimeCount0(cases);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_LEAD_TIME).split(","));
          LeadTimeDetail totalDetail = new LeadTimeDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> caseMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            LeadTimeCount tester = assembleLeadTimeCount0(
                caseMap.getOrDefault(testerId, emptyList()));
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              LeadTimeDetail testerDetail = new LeadTimeDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public UnplannedWorkOverview unplannedWork(
      @NonNullable Long projectId, Long planId, AuthObjectType testerOrgType, Long testerOrgId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTesterDetail,
      boolean joinDataDetail) {
    return new BizTemplate<UnplannedWorkOverview>() {

      @Override
      protected UnplannedWorkOverview process() {
        UnplannedWorkOverview overview = new UnplannedWorkOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        double dailyProcessedWorkload = calcDailyProcessedWorkload(cases, DEFAULT_DAILY_WORKLOAD);
        UnplannedWorkCount total = assembleUnplannedWorkCount0(cases, dailyProcessedWorkload);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          overview.setDataDetailTitles(message(EXPORT_ANALYSIS_CASE_UNPLANNED_CASE).split(","));
          UnplannedWorkDetail totalDetail = new UnplannedWorkDetail();
          totalDetail.setName(message(MESSAGE_TOTAL));
          CoreUtils.copyProperties(total, totalDetail);
          overview.getDataDetails().add(totalDetail);
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> caseMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            List<FuncCaseEfficiencySummary> testerCases = caseMap.getOrDefault(testerId,
                emptyList());
            dailyProcessedWorkload = calcDailyProcessedWorkload(testerCases,
                DEFAULT_DAILY_WORKLOAD);
            UnplannedWorkCount tester = assembleUnplannedWorkCount0(
                testerCases, dailyProcessedWorkload);
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              UnplannedWorkDetail testerDetail = new UnplannedWorkDetail();
              testerDetail.setName(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName());
              CoreUtils.copyProperties(tester, testerDetail);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public GrowthTrendOverview growthTrend(Long projectId, Long planId, AuthObjectType testerOrgType,
      Long testerOrgId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      boolean joinTesterDetail, boolean joinDataDetail) {
    return new BizTemplate<GrowthTrendOverview>() {

      @Override
      protected GrowthTrendOverview process() {
        GrowthTrendOverview overview = new GrowthTrendOverview();

        List<FuncCaseEfficiencySummary> cases = getCaseEfficiencySummaries(projectId, planId,
            createdDateStart, createdDateEnd, testerOrgType, testerOrgId);
        if (isEmpty(cases)) {
          return overview;
        }

        // Testers
        Map<Long, UserInfo> testers = commonQuery.getUserInfoMap(
            cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(Collectors.toSet()));
        overview.setTesters(testers);

        // Total overview
        GrowthTrendCount total = assembleGrowthTrendCount(cases);
        overview.setTotalOverview(total);
        if (joinDataDetail) {
          GrowthTrendDetail totalDetail = assembleGrowthTrendDetail(message(MESSAGE_TOTAL), total);
          overview.getDataDetails().add(totalDetail);
          String[] times = total.getTimeSeries().stream()
              .map(DataAssetsTimeSeries::getTimeSeries).toArray(String[]::new);
          overview.setDataDetailTitles(Stream.concat(
                  Arrays.stream(message(EXPORT_ANALYSIS_CASE_GROWTH_TREND).split(",")),
                  Arrays.stream(times))
              .toArray(String[]::new));
        }

        // Testers overview
        if (joinTesterDetail && (isNull(testerOrgType)
            || !AuthObjectType.USER.equals(testerOrgType)) // Total is equal tester
        ) {
          Map<Long, List<FuncCaseEfficiencySummary>> caseMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getTesterId));
          for (Long testerId : testers.keySet()) {
            GrowthTrendCount tester = assembleGrowthTrendCount(
                caseMap.getOrDefault(testerId, emptyList()));
            overview.getTestersOverview().put(testerId, tester);
            if (joinDataDetail) {
              GrowthTrendDetail testerDetail = assembleGrowthTrendDetail(
                  testers.getOrDefault(testerId, new UserInfo()).getFullName(), tester);
              overview.getDataDetails().add(testerDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public ResourceCreationOverview resourceCreation(Long projectId, Long planId,
      AuthObjectType creatorOrgType, Long creatorOrgId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinCreatorDetail, boolean joinDataDetail) {
    return new BizTemplate<ResourceCreationOverview>() {

      @Override
      protected ResourceCreationOverview process() {
        ResourceCreationOverview overview = new ResourceCreationOverview();

        List<FuncPlan> plans = funcPlanQuery.getPlanCreatedSummaries(projectId, planId,
            createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<FuncCaseEfficiencySummary> cases = getCaseCreatedSummaries(projectId, planId,
            createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<FuncReview> reviews = funcReviewQuery.getReviewCreatedSummaries(projectId, planId,
            createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);
        List<FuncBaseline> baselines = funcBaselineQuery.getBaselineCreatedSummaries(projectId,
            planId, createdDateStart, createdDateEnd, creatorOrgType, creatorOrgId);

        // Testers
        Set<Long> creatorIds = new HashSet<>();
        creatorIds.addAll(plans.stream().map(FuncPlan::getCreatedBy).toList());
        creatorIds.addAll(cases.stream().map(FuncCaseEfficiencySummary::getCreatedBy).toList());
        creatorIds.addAll(reviews.stream().map(FuncReview::getCreatedBy).toList());
        creatorIds.addAll(baselines.stream().map(FuncBaseline::getCreatedBy).toList());
        if (isEmpty(creatorIds)) {
          return overview;
        }

        Map<Long, UserInfo> creators = commonQuery.getUserInfoMap(creatorIds);
        overview.setCreators(creators);

        // Total overview
        ResourceCreationCount total = assembleResourceCreationCount(
            plans, cases, reviews, baselines);

        overview.setTotalOverview(total);
        List<DataAssetsTimeSeries> allTimeSeries = new ArrayList<>();
        if (joinDataDetail) {
          allTimeSeries = total.getTimeSeries().getOrDefault("TOTAL", emptyList());
          ResourceCreationDetail totalDetail = assembleResourceCreationDetail(
              message(MESSAGE_TOTAL), total, allTimeSeries);
          overview.getDataDetails().add(totalDetail);
          String[] times = total.getTimeSeries().get("TOTAL").stream()
              .map(DataAssetsTimeSeries::getTimeSeries).toArray(String[]::new);
          overview.setDataDetailTitles(Stream.concat(
                  Arrays.stream(message(EXPORT_ANALYSIS_CASE_RESOURCE_CREATION).split(",")),
                  Arrays.stream(times))
              .toArray(String[]::new));
        }

        // Assignees overview
        if (joinCreatorDetail && (isNull(creatorOrgType)
            || !AuthObjectType.USER.equals(creatorOrgType)) // Total is equal assignee
        ) {
          Map<Long, List<FuncPlan>> planMap = plans.stream()
              .collect(groupingBy(FuncPlan::getCreatedBy));
          Map<Long, List<FuncCaseEfficiencySummary>> casetMap = cases.stream()
              .collect(groupingBy(FuncCaseEfficiencySummary::getCreatedBy));
          Map<Long, List<FuncReview>> reviewMap = reviews.stream()
              .collect(groupingBy(FuncReview::getCreatedBy));
          Map<Long, List<FuncBaseline>> baselineMap = baselines.stream()
              .collect(groupingBy(FuncBaseline::getCreatedBy));
          for (Long creatorId : creators.keySet()) {
            ResourceCreationCount creator = assembleResourceCreationCount(
                planMap.getOrDefault(creatorId, emptyList()),
                casetMap.getOrDefault(creatorId, emptyList()),
                reviewMap.getOrDefault(creatorId, emptyList()),
                baselineMap.getOrDefault(creatorId, emptyList()));
            overview.getCreatorOverview().put(creatorId, creator);
            if (joinDataDetail) {
              ResourceCreationDetail creatorDetail = assembleResourceCreationDetail(
                  creators.getOrDefault(creatorId, new UserInfo()).getFullName(), creator,
                  allTimeSeries);
              overview.getDataDetails().add(creatorDetail);
            }
          }
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public void checkCaseQuota(int inc, Long planId) {
    // Check the total cases quota
    long caseNum = funcCaseRepo.count();
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterFuncCase,
        null, caseNum + inc);
    // Check the plan cases quota
    long planCaseNum = funcCaseRepo.countByPlanId(planId);
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterFuncPlanCase,
        null, planCaseNum + inc);
  }

  @Override
  public FuncCaseInfo findLeastByProjectId(Long projectId) {
    return funcCaseInfoRepo.findLeastByProjectId(projectId);
  }

  @Override
  public Map<String, List<FuncCaseInfo>> checkAndFindByPlanAndName(Long planId, Set<String> names) {
    if (ObjectUtils.isEmpty(names)) {
      return emptyMap();
    }
    List<FuncCaseInfo> caseDb = funcCaseInfoRepo.findByPlanIdAndNameIn(planId, names);
    if (ObjectUtils.isEmpty(caseDb)) {
      throw ResourceNotFound.of(names.iterator().next(), "FuncCase");
    }
    if (names.size() != caseDb.size()) {
      Collection<String> namesDb = caseDb.stream()
          .map(FuncCaseInfo::getName).collect(Collectors.toSet());
      names.removeAll(namesDb);
      throw ResourceNotFound.of(names.iterator().next(), "FuncCase");
    }
    return caseDb.stream().collect(Collectors.groupingBy(FuncCaseInfo::getName));
  }

  @Override
  public void checkAddCaseNameExists(FuncPlan planDb, List<FuncCase> cases) {
    List<String> existedNames = funcCaseRepo.findNamesByNameInAndPlanId(
        cases.stream().map(x -> nullSafe(planDb.getCasePrefix(), "") + x.getName())
            .collect(Collectors.toSet()), planDb.getId());
    if (!existedNames.isEmpty()) {
      throw ResourceExisted.of(CASE_NAME_REPEATED_T, new Object[]{existedNames.get(0)});
    }
  }

  @Override
  public void checkAndSafeUpdateNameExists(FuncPlan planDb, List<FuncCase> cases) {
    Set<String> updateNames = cases.stream()
        .map(x -> nullSafe(planDb.getCasePrefix(), "") + x.getName())
        .collect(Collectors.toSet());
    if (isNotEmpty(updateNames)) {
      Map<String, FuncCaseInfo> caseInfosMap = funcCaseInfoRepo.findAllByNameInAndPlanId(
              updateNames, planDb.getId()).stream()
          .collect(Collectors.toMap(FuncCaseInfo::getName, x -> x));
      if (isEmpty(caseInfosMap)) {
        return;
      }
      for (FuncCase case0 : cases) {
        if (isNotEmpty(planDb.getCasePrefix())
            && !case0.getName().startsWith(planDb.getCasePrefix())) {
          case0.setName(planDb.getCasePrefix() + case0.getName());
        }
        if (caseInfosMap.containsKey(case0.getName())
            && !caseInfosMap.get(case0.getName()).getId().equals(case0.getId())) {
          throw ResourceExisted.of(CASE_NAME_REPEATED_T, new Object[]{case0.getName()});
        }
      }
    }
  }

  @Override
  public FuncCase checkAndFind(Long id) {
    return funcCaseRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncCase"));
  }

  @Override
  public FuncCaseInfo checkAndFindInfo(Long id) {
    return funcCaseInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncCase"));
  }

  @Override
  public List<FuncCase> checkAndFind(Collection<Long> ids) {
    List<FuncCase> cases = funcCaseRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(cases), ids.iterator().next(), "FuncCase");
    if (ids.size() != cases.size()) {
      for (FuncCase case0 : cases) {
        assertResourceNotFound(ids.contains(case0.getId()), case0.getId(), "FuncCase");
      }
    }
    return cases;
  }

  @Override
  public List<FuncCaseInfo> checkAndFindInfo(Collection<Long> ids) {
    List<FuncCaseInfo> cases = funcCaseInfoRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(cases), ids.iterator().next(), "FuncCase");
    if (ids.size() != cases.size()) {
      for (FuncCaseInfo case0 : cases) {
        assertResourceNotFound(ids.contains(case0.getId()), case0.getId(), "FuncCase");
      }
    }
    return cases;
  }

  /**
   * Note: The test plan enable the review.
   */
  @Override
  public void checkReviewPassed(List<FuncCase> cases) {
    if (isNotEmpty(cases)) {
      for (FuncCase case0 : cases) {
        BizAssert.assertTrue(case0.isReviewed(), CASE_NOT_REVIEWED_CODE,
            CASE_NOT_REVIEWED_T, new Object[]{case0.getName()});
      }
    }
  }

  /**
   * Note: The test plan enable the review.
   */
  @Override
  public void checkCanReview(List<FuncCase> cases) {
    if (isNotEmpty(cases)) {
      for (FuncCase case0 : cases) {
        assertTrue(case0.canReview(), CASE_CAN_NOT_REVIEWED_T,
            new Object[]{case0.getName(), case0.getReviewStatus()});
      }
    }
  }

  /**
   * Note: The test plan enable the review.
   */
  @Override
  public void checkInfoCanReview(List<FuncCaseInfo> cases) {
    if (isNotEmpty(cases)) {
      for (FuncCaseInfo case0 : cases) {
        assertTrue(case0.canReview(), CASE_CAN_NOT_REVIEWED_T,
            new Object[]{case0.getName(), case0.getReviewStatus()});
      }
    }
  }

  @Override
  public boolean hasModifyAttachments(List<Attachment> attachments, FuncCase caseDb) {
    if (isEmpty(attachments) && isEmpty(caseDb.getAttachments())) {
      return false;
    }
    if ((isEmpty(attachments) && isNotEmpty(caseDb.getAttachments()))
        || (isNotEmpty(attachments) && isEmpty(caseDb.getAttachments()))
        || caseDb.getAttachments().size() != attachments.size()) {
      return true;
    }
    for (Attachment attachment : attachments) {
      if (!caseDb.getAttachments().contains(attachment)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void checkUpdateNameExists(Long planId, String name, Long id) {
    assertResourceExisted(isEmpty(name)
            || funcCaseRepo.countByPlanIdAndNameAndIdNot(planId, name, id) < 1,
        CASE_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * Set favourite state
   */
  @Override
  public void setFavourite(List<? extends ResourceFavouriteAndFollow<?, ?>> cases) {
    Set<Long> caseIds = cases.stream().map(ResourceFavouriteAndFollow::getId)
        .collect(Collectors.toSet());
    List<FuncCaseFavourite> favourites = funcCaseFavoriteRepo
        .findAllByCaseIdInAndCreatedBy(caseIds, getUserId());
    Set<Long> favouritesCaseIds = favourites.stream().map(FuncCaseFavourite::getCaseId)
        .collect(Collectors.toSet());
    cases.forEach(c -> {
      if (favouritesCaseIds.contains(c.getId())) {
        c.setFavourite(true);
      }
    });
  }

  /**
   * Set follow state
   */
  @Override
  public void setFollow(List<? extends ResourceFavouriteAndFollow<?, ?>> cases) {
    Set<Long> caseIds = cases.stream().map(ResourceFavouriteAndFollow::getId)
        .collect(Collectors.toSet());
    List<FuncCaseFollow> follows = funcCaseFollowRepo
        .findAllByCaseIdInAndCreatedBy(caseIds, getUserId());
    Set<Long> followCaseIds = follows.stream().map(FuncCaseFollow::getCaseId)
        .collect(Collectors.toSet());
    cases.forEach(c -> {
      if (followCaseIds.contains(c.getId())) {
        c.setFollow(true);
      }
    });
  }

  @Override
  public void setSafeCloneName(FuncCase funcCase) {
    String saltName = randomAlphanumeric(3);
    String clonedName = funcCaseRepo.existsByPlanIdAndName(
        funcCase.getPlanId(), funcCase.getName() + "-Copy")
        ? funcCase.getName() + "-Copy." + saltName : funcCase.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_NAME_LENGTH_X4 ? clonedName.substring(0,
        MAX_NAME_LENGTH_X4 - 3) + saltName : clonedName;
    funcCase.setName(clonedName);
  }

  @Override
  public void setCaseProgress(FuncCase caseDb) {
    caseDb.setProgress(new Progress()
        .setCompleted(caseDb.getTestResult().isPassed() ? 1 : 0)
        .setTotal(!caseDb.getTestResult().isCanceled() ? 1 : 0));
  }

  @Override
  public void setCaseInfoProgress(List<FuncCaseInfo> caseDbs) {
    for (FuncCaseInfo caseDb : caseDbs) {
      caseDb.setProgress(new Progress()
          .setCompleted(caseDb.getTestResult().isPassed() ? 1 : 0)
          .setTotal(!caseDb.getTestResult().isCanceled() ? 1 : 0));
    }
  }

  @Override
  public void assembleAndSendModifyNoticeEvent(List<FuncCaseInfo> casesDb,
      List<Activity> activities) {
    Map<Long, Activity> taskActivityMap = activities.stream()
        .collect(Collectors.toMap(Activity::getTargetId, x -> x));
    for (FuncCaseInfo caseDb : casesDb) {
      assembleAndSendModifyNoticeEvent(caseDb, taskActivityMap.get(caseDb.getId()));
    }
  }

  @Override
  public void assembleAndSendModifyNoticeEvent(FuncCaseInfo caseDb, Activity activity) {
    List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
        nullSafe(caseDb.getTenantId(), getOptTenantId())).get(FunctionCaseModificationCode);
    if (isEmpty(noticeTypes)) {
      return;
    }
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(caseDb.getTesterId());
    List<Long> followUserIds = funcCaseFollowRepo.findUserIdsByCaseId(caseDb.getId());
    receiveObjectIds.addAll(followUserIds);
    receiveObjectIds.remove(getUserId());
    if (isNotEmpty(receiveObjectIds)) {
      String message = message(FunctionCaseModification, new Object[]{getUserFullName(),
              caseDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(FunctionCaseModificationCode,
          message, FUNC_CASE.getValue(), caseDb.getId().toString(), caseDb.getName(),
          noticeTypes, receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  @Override
  public void assembleAndSendModifyNoticeEvent(FuncCase caseDb, Activity activity) {
    List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
        nullSafe(caseDb.getTenantId(), getOptTenantId())).get(FunctionCaseModificationCode);
    if (isEmpty(noticeTypes)) {
      return;
    }
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(caseDb.getTesterId());
    List<Long> followUserIds = funcCaseFollowRepo.findUserIdsByCaseId(caseDb.getId());
    receiveObjectIds.addAll(followUserIds);
    receiveObjectIds.remove(getUserId());
    if (isNotEmpty(receiveObjectIds)) {
      String message = message(FunctionCaseModification, new Object[]{getUserFullName(),
              caseDb.getName(), activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(FunctionCaseModificationCode,
          message, FUNC_CASE.getValue(), caseDb.getId().toString(), caseDb.getName(),
          noticeTypes, receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  @Override
  public void assembleAndSendModifyTesterNoticeEvent(FuncCase caseDb) {
    if (nonNull(caseDb.getTesterId())) {
      List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
          nullSafe(caseDb.getTenantId(), getOptTenantId())).get(FunctionCaseAssignmentCode);
      if (isEmpty(noticeTypes)) {
        return;
      }
      List<Long> receiveObjectIds = new ArrayList<>();
      receiveObjectIds.add(caseDb.getTesterId());
      String message = message(FunctionCaseAssignment,
          new Object[]{getUserFullName(), caseDb.getName()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(FunctionCaseAssignmentCode, message,
          FUNC_CASE.getValue(), caseDb.getId().toString(), caseDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  @Override
  public List<FuncCaseEfficiencySummary> getCaseEfficiencySummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType testerOrgType,
      Long testerOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> testerIds = isNull(testerOrgType) ? null
        : userManager.getUserIdByOrgType0(testerOrgType, testerOrgId);
    Set<SearchCriteria> allFilters = getCaseTesterResourcesFilter(projectId,
        sprintId, createdDateStart, createdDateEnd, testerIds);
    return funcCaseInfoRepo.findProjectionByFilters(FuncCaseInfo.class,
        FuncCaseEfficiencySummary.class, allFilters);
  }

  private List<FuncCaseEfficiencySummary> getCaseCreatedSummaries(
      @NonNullable Long projectId, Long planId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getCaseCreatorResourcesFilter(projectId, planId,
        createdDateStart, createdDateEnd, creatorIds);
    return funcCaseInfoRepo.findProjectionByFilters(FuncCaseInfo.class,
        FuncCaseEfficiencySummary.class, allFilters);
  }

  private List<FuncCaseEfficiencySummary> getCaseValidEfficiencySummaries(
      @NonNullable Long projectId, Long planId, LocalDateTime safeCreatedDateStart,
      LocalDateTime safeCreatedDateEnd, AuthObjectType testerOrgType, Long testerOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> testerIds = isNull(testerOrgType) ? null
        : userManager.getUserIdByOrgType0(testerOrgType, testerOrgId);
    Set<SearchCriteria> allFilters = getCaseTesterResourcesFilter(projectId, planId,
        safeCreatedDateStart, safeCreatedDateEnd, testerIds);
    allFilters.add(notEqual("testResult", "CANCELED"));
    return funcCaseInfoRepo.findProjectionByFilters(FuncCaseInfo.class,
        FuncCaseEfficiencySummary.class, allFilters);
  }

  //@NameJoin
  public static ProjectSummary getProjectSummary(Project projectDb) {
    return toProjectSummary(projectDb);
  }

  @NameJoin
  public static FuncPlanSummary getPlanSummary(FuncPlan planDb) {
    return toFuncPlanSummary(planDb);
  }

  @NameJoin
  public static List<FuncCaseSummary> getCaseSummary(List<FuncCaseInfo> cases) {
    return isEmpty(cases) ? null : cases.stream().map(FuncCaseConverter::toCaseSummary)
        .collect(Collectors.toList());
  }

  @NameJoin
  public static FuncCaseDetailSummary getCaseDetailSummary(FuncCase funcCase) {
    return toCaseDetailSummary(funcCase);
  }


}
