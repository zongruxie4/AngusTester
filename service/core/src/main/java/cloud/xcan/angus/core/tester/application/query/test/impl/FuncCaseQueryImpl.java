package cloud.xcan.angus.core.tester.application.query.test.impl;

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
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter;
import cloud.xcan.angus.core.tester.application.query.activity.ActivityQuery;
import cloud.xcan.angus.core.tester.application.query.comment.CommentQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.TagQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncReviewQuery;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.comment.CommentTargetType;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCase;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedCount;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartCount;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeCount;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.OverdueAssessmentCount;
import cloud.xcan.angus.core.tester.domain.issue.count.OverdueAssessmentDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.ProgressCount;
import cloud.xcan.angus.core.tester.domain.issue.count.ProgressDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.RecentDeliveryCount;
import cloud.xcan.angus.core.tester.domain.issue.count.RecentDeliveryDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkCount;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkDetail;
import cloud.xcan.angus.core.tester.domain.issue.count.WorkloadCount;
import cloud.xcan.angus.core.tester.domain.issue.count.WorkloadDetail;
import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.summary.ProjectSummary;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoListRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.count.BackloggedOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.BurnDownChartOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.CoreKpiCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.CoreKpiDetail;
import cloud.xcan.angus.core.tester.domain.test.cases.count.CoreKpiOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncCaseCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncTesterCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncTesterProgressCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.GrowthTrendCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.GrowthTrendDetail;
import cloud.xcan.angus.core.tester.domain.test.cases.count.GrowthTrendOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.LeadTimeOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.OverdueAssessmentOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ProgressOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.RecentDeliveryOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ResourceCreationDetail;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ResourceCreationOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ReviewEfficiencyCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ReviewEfficiencyDetail;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ReviewEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.TestingEfficiencyCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.TestingEfficiencyDetail;
import cloud.xcan.angus.core.tester.domain.test.cases.count.TestingEfficiencyOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.UnplannedWorkOverview;
import cloud.xcan.angus.core.tester.domain.test.cases.count.WorkloadOverview;
import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavourite;
import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavouriteRepo;
import cloud.xcan.angus.core.tester.domain.test.follow.FuncCaseFollow;
import cloud.xcan.angus.core.tester.domain.test.follow.FuncCaseFollowRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReviewRepo;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseDetailSummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseEfficiencySummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseSummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncPlanSummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncPlanWorkSummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncProjectWorkSummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncTesterWorkSummary;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.annotations.NonNullable;
import cloud.xcan.angus.spec.principal.PrincipalContext;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Implementation of FuncCaseQuery for managing functional test case queries and statistics.
 * <p>
 * This class provides comprehensive functionality for querying, analyzing, and managing functional test cases.
 * It handles case retrieval, validation, progress tracking, and various statistical analyses including
 * workload, efficiency, and trend analysis.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Case detail retrieval with enrichment (tags, favourites, follows, user info)</li>
 *   <li>Comprehensive statistical analysis (progress, workload, efficiency, trends)</li>
 *   <li>Quota management and validation</li>
 *   <li>Permission checking and authorization</li>
 *   <li>Notification event assembly and dispatch</li>
 *   <li>Case lifecycle management (creation, updates, reviews)</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and validation checks.
 * <p>
 * Supports both individual case operations and bulk operations with proper error handling
 * and resource validation.
 */
@Biz
public class FuncCaseQueryImpl implements FuncCaseQuery {

  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;
  @Resource
  private FuncCaseInfoListRepo funcCaseInfoListRepo;
  @Resource
  private FuncCaseInfoSearchRepo funcCaseInfoSearchRepo;
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

  /**
   * Retrieves detailed information for a specific functional test case.
   * <p>
   * Fetches the case by ID and enriches it with additional information including
   * favourite/follow status, tags, associated tasks/cases, user information,
   * comment count, activity count, and progress information.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution with
   * parameter validation and error handling.
   *
   * @param id the case ID to retrieve details for
   * @return FuncCase object with complete details and enriched information
   * @throws ResourceNotFound if the case is not found
   */
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

  /**
   * Retrieves a paginated list of functional test case information.
   * <p>
   * Supports both regular search and full-text search with comprehensive filtering.
   * Enriches results with user information, tags, progress, and export data when needed.
   * <p>
   * Includes permission checking and authorization filtering for security.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param export whether to include export-specific data (precondition, steps, associations)
   * @param spec the search specification with criteria and filters
   * @param pageable pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match full-text search match parameters
   * @return Page of FuncCaseInfo objects with enriched information
   * @throws BizException if permission validation fails
   */
  @Override
  public Page<FuncCaseInfo> list(boolean export, GenericSpecification<FuncCaseInfo> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
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
        Page<FuncCaseInfo> page = fullTextSearch
            ? funcCaseInfoSearchRepo.find(criteria, pageable, FuncCaseInfo.class, match)
            : funcCaseInfoListRepo.find(criteria, pageable, FuncCaseInfo.class, null);

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

          if (export) {
            List<Long> caseIds = page.getContent().stream().map(FuncCaseInfo::getId)
                .toList();
            Map<Long, FuncCase> caseMap = funcCaseRepo.findAllById(caseIds).stream()
                .collect(Collectors.toMap(FuncCase::getId, x -> x));
            for (FuncCaseInfo caseInfo : page.getContent()) {
              caseInfo.setPrecondition(caseMap.get(caseInfo.getId()).getPrecondition());
              caseInfo.setSteps(caseMap.get(caseInfo.getId()).getSteps());
            }
            // Set reference tasks and cases
            taskFuncCaseQuery.setAssocForCase(page.getContent());
          }
        }
        return page;
      }
    }.execute();
  }

  /**
   * Retrieves cases that are not associated with a specific task.
   * <p>
   * Finds cases within the same project as the task that are not currently
   * associated with the specified task. Optionally filters by module ID.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * Results are sorted by creation date in descending order.
   *
   * @param taskId the task ID to find unassociated cases for
   * @param moduleId optional module ID to filter cases by module
   * @return List of FuncCaseInfo objects not associated with the task
   * @throws ResourceNotFound if the task is not found
   */
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

  /**
   * Retrieves cases that are not associated with a specific case.
   * <p>
   * Finds cases within the same project as the reference case that are not
   * currently associated with the specified case. Optionally filters by module ID.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * Results are sorted by creation date in descending order.
   *
   * @param caseId the case ID to find unassociated cases for
   * @param moduleId optional module ID to filter cases by module
   * @return List of FuncCaseInfo objects not associated with the case
   * @throws ResourceNotFound if the case is not found
   */
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

  /**
   * Generates comprehensive resource creation statistics for a project or plan.
   * <p>
   * Provides detailed statistics on resource creation including cases, plans, reviews, and baselines.
   * Supports filtering by creator organization type and date range with optional inclusion of
   * different resource types based on boolean flags.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method aggregates creation counts and categorizes them by various criteria such as
   * test results, review status, and priority levels for comprehensive analysis.
   *
   * @param projectId the project ID for filtering resources
   * @param planId optional plan ID for plan-specific filtering
   * @param creatorObjectType the creator organization type for filtering
   * @param creatorObjectId the creator organization ID for filtering
   * @param createdDateStart start date for filtering resource creation
   * @param createdDateEnd end date for filtering resource creation
   * @param joinPlan whether to include plan creation statistics
   * @param joinReview whether to include review creation statistics
   * @param joinBaseline whether to include baseline creation statistics
   * @return FuncLastResourceCreationCount object with comprehensive creation statistics
   */
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

  /**
   * Counts functional test cases based on specified search criteria.
   * <p>
   * Provides case count statistics with automatic filtering for deleted cases
   * and deleted plans. Requires projectId parameter for validation.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param criteria search criteria for filtering cases
   * @return FuncCaseCount object containing the count statistics
   * @throws BizException if projectId parameter is missing
   */
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

  /**
   * Generates summary statistics for testers in a project or plan.
   * <p>
   * Provides comprehensive tester statistics including case counts, progress metrics,
   * and user information. Groups cases by tester and enriches with user details.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific statistics
   * @return List of FuncTesterCount objects with tester statistics
   */
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

  /**
   * Generates progress statistics for testers in a project or plan.
   * <p>
   * Provides detailed progress metrics for each tester including completion rates,
   * workload distribution, and progress trends. Groups cases by tester and
   * calculates progress-based statistics.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific statistics
   * @return List of FuncTesterProgressCount objects with tester progress statistics
   */
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

  /**
   * Generates comprehensive work statistics for a project.
   * <p>
   * Provides detailed project-level statistics including case counts, resource creation
   * statistics, plan summaries, and various categorization metrics. Aggregates data
   * from all plans within the project.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param projectId the project ID to generate statistics for
   * @return FuncProjectWorkSummary object with comprehensive project statistics
   * @throws ResourceNotFound if the project is not found
   */
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

  /**
   * Generates comprehensive work statistics for a plan.
   * <p>
   * Provides detailed plan-level statistics including case counts, tester summaries,
   * progress metrics, and various categorization statistics. Groups cases by tester
   * and provides daily workload distribution.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param planId the plan ID to generate statistics for
   * @return FuncPlanWorkSummary object with comprehensive plan statistics
   * @throws ResourceNotFound if the plan is not found
   */
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

  /**
   * Generates work statistics for a specific tester.
   * <p>
   * Provides detailed statistics for a single tester including case counts,
   * daily workload distribution, and progress metrics. Groups cases by creation date
   * to show daily work patterns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific filtering
   * @param userId the user ID to generate statistics for
   * @return FuncTesterWorkSummary object with tester-specific statistics
   */
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

  /**
   * Generates progress overview for functional test cases.
   * <p>
   * Provides comprehensive progress analysis including total overview and
   * individual tester progress details. Supports filtering by tester organization
   * and date range with optional detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return ProgressOverview object with comprehensive progress statistics
   */
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

  /**
   * Generates burndown chart overview for functional test cases.
   * <p>
   * Provides comprehensive burndown analysis including total overview and individual tester
   * burndown details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method automatically determines safe date ranges based on plan or project boundaries
   * and excludes canceled cases from the analysis. Provides both numeric and workload-based
   * burndown charts.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return BurnDownChartOverview object with comprehensive burndown statistics
   */
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

  /**
   * Generates workload overview for functional test cases.
   * <p>
   * Provides comprehensive workload analysis including total overview and individual tester
   * workload details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method analyzes case workload distribution across testers and provides metrics
   * for workload planning and resource allocation.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return WorkloadOverview object with comprehensive workload statistics
   */
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

  /**
   * Generates overdue assessment overview for functional test cases.
   * <p>
   * Provides comprehensive overdue analysis including total overview and individual tester
   * overdue details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method calculates overdue assessments based on daily processed workload and provides
   * metrics for identifying potential delays and resource constraints.
   *
   * @param projectId the project ID for filtering cases (required)
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return OverdueAssessmentOverview object with comprehensive overdue statistics
   */
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

  /**
   * Generates testing efficiency overview for functional test cases.
   * <p>
   * Provides comprehensive efficiency analysis including total overview and individual tester
   * efficiency details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method analyzes testing efficiency metrics including completion rates, time utilization,
   * and productivity indicators for performance optimization.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return TestingEfficiencyOverview object with comprehensive efficiency statistics
   */
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

  /**
   * Generates core KPI overview for functional test cases.
   * <p>
   * Provides comprehensive KPI analysis including total overview and individual tester
   * KPI details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method analyzes core key performance indicators including quality metrics,
   * productivity measures, and delivery performance for strategic decision making.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return CoreKpiOverview object with comprehensive KPI statistics
   */
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

  /**
   * Generates review efficiency overview for functional test cases.
   * <p>
   * Provides comprehensive review efficiency analysis including total overview and individual tester
   * review details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method analyzes review efficiency metrics including review completion rates,
   * review cycle times, and quality gate performance for process optimization.
   *
   * @param projectId the project ID for filtering cases
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return ReviewEfficiencyOverview object with comprehensive review efficiency statistics
   */
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

  /**
   * Generates backlogged work overview for functional test cases.
   * <p>
   * Provides comprehensive backlog analysis including total overview and individual tester
   * backlog details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method analyzes backlogged work metrics including outstanding cases, overdue items,
   * and workload distribution for process improvement and resource planning.
   *
   * @param projectId the project ID for filtering cases (required)
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return BackloggedOverview object with comprehensive backlog statistics
   */
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

  /**
   * Generates recent delivery overview for functional test cases.
   * <p>
   * Provides comprehensive recent delivery analysis including total overview and individual tester
   * delivery details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method analyzes recent delivery metrics including completion rates, delivery patterns,
   * and time-based performance indicators for delivery optimization.
   *
   * @param projectId the project ID for filtering cases (required)
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return RecentDeliveryOverview object with comprehensive recent delivery statistics
   */
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

  /**
   * Generates lead time overview for functional test cases.
   * <p>
   * Provides comprehensive lead time analysis including total overview and individual tester
   * lead time details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method analyzes lead time metrics including cycle times, processing durations,
   * and efficiency indicators for process optimization and bottleneck identification.
   *
   * @param projectId the project ID for filtering cases (required)
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return LeadTimeOverview object with comprehensive lead time statistics
   */
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

  /**
   * Generates unplanned work overview for functional test cases.
   * <p>
   * Provides comprehensive unplanned work analysis including total overview and individual tester
   * unplanned work details. Supports filtering by tester organization and date range with optional
   * detailed breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   * <p>
   * The method analyzes unplanned work metrics including ad-hoc tasks, emergency cases,
   * and workload distribution for capacity planning and resource allocation.
   *
   * @param projectId the project ID for filtering cases (required)
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return UnplannedWorkOverview object with comprehensive unplanned work statistics
   */
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

  /**
   * Generates growth trend overview for functional test cases.
   * <p>
   * Analyzes case efficiency summaries to provide growth trend statistics including time series data.
   * <p>
   * Supports filtering by project, plan, tester organization, and date range, and can include detailed tester breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param projectId the project ID for filtering
   * @param planId optional plan ID for plan-specific filtering
   * @param testerOrgType the tester organization type for filtering
   * @param testerOrgId the tester organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTesterDetail whether to include individual tester details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return GrowthTrendOverview object with comprehensive growth trend statistics
   */
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

  /**
   * Generates resource creation overview for functional test cases.
   * <p>
   * Provides comprehensive statistics on resource creation including plans, cases, reviews, and baselines.
   * <p>
   * Supports filtering by project, plan, creator organization, and date range, and can include detailed creator breakdowns.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param projectId the project ID for filtering
   * @param planId optional plan ID for plan-specific filtering
   * @param creatorOrgType the creator organization type for filtering
   * @param creatorOrgId the creator organization ID for filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinCreatorDetail whether to include individual creator details
   * @param joinDataDetail whether to include detailed data breakdowns
   * @return ResourceCreationOverview object with comprehensive resource creation statistics
   */
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

  /**
   * Validates case quota limits for both total cases and plan-specific cases.
   * <p>
   * Checks against tenant quota limits to ensure resource constraints are respected.
   * <p>
   * Validates both global case count and plan-specific case count quotas.
   *
   * @param inc the increment to add to current counts
   * @param planId the plan ID for plan-specific quota validation
   */
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

  /**
   * Finds the least recent case by project ID.
   * <p>
   * Retrieves the case with the earliest creation date within the specified project.
   *
   * @param projectId the project ID to search within
   * @return FuncCaseInfo object, or null if no cases exist
   */
  @Override
  public FuncCaseInfo findLeastByProjectId(Long projectId) {
    return funcCaseInfoRepo.findLeastByProjectId(projectId);
  }

  /**
   * Finds multiple case info objects by IDs.
   * @param ids collection of case info IDs
   * @return List of FuncCaseInfo objects
   */
  @Override
  public List<FuncCaseInfo> findInfo(Collection<Long> ids) {
    return funcCaseInfoRepo.findAllById(ids);
  }

  /**
   * Checks and finds cases by plan ID and names with validation.
   * <p>
   * Validates that all requested case names exist within the plan and groups them by name.
   * <p>
   * Throws ResourceNotFound if any requested case name is not found.
   *
   * @param planId the plan ID to search within
   * @param names set of case names to find
   * @return Map of case name to list of FuncCaseInfo objects
   * @throws ResourceNotFound if any case name is not found
   */
  @Override
  public Map<String, List<FuncCaseInfo>> checkAndFindByPlanAndName(Long planId, Set<String> names) {
    if (isEmpty(names)) {
      return emptyMap();
    }
    List<FuncCaseInfo> caseDb = funcCaseInfoRepo.findByPlanIdAndNameIn(planId, names);
    if (isEmpty(caseDb)) {
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

  /**
   * Validates that case names do not already exist in the plan.
   * <p>
   * Checks for name conflicts when adding new cases to a plan.
   * <p>
   * Applies plan prefix to case names before checking for duplicates.
   *
   * @param planDb the plan object containing prefix and ID
   * @param cases list of cases to validate
   * @throws ResourceExisted if any case name already exists
   */
  @Override
  public void checkAddCaseNameExists(FuncPlan planDb, List<FuncCase> cases) {
    List<String> existedNames = funcCaseRepo.findNamesByNameInAndPlanId(
        cases.stream().map(x -> nullSafe(planDb.getCasePrefix(), "") + x.getName())
            .collect(Collectors.toSet()), planDb.getId());
    if (!existedNames.isEmpty()) {
      throw ResourceExisted.of(CASE_NAME_REPEATED_T, new Object[]{existedNames.get(0)});
    }
  }

  /**
   * Safely updates case names ensuring uniqueness within the plan.
   * <p>
   * Applies plan prefix and validates name uniqueness for case updates.
   * <p>
   * Handles both prefix application and duplicate name detection.
   *
   * @param planDb the plan object containing prefix and ID
   * @param cases list of cases to update
   * @throws ResourceExisted if any case name conflicts with existing cases
   */
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

  /**
   * Finds a case by ID with validation.
   * <p>
   * Retrieves a case and throws ResourceNotFound if not found.
   *
   * @param id the case ID
   * @return FuncCase object
   * @throws ResourceNotFound if case is not found
   */
  @Override
  public FuncCase checkAndFind(Long id) {
    return funcCaseRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncCase"));
  }

  /**
   * Finds case info by ID with validation.
   * <p>
   * Retrieves case info and throws ResourceNotFound if not found.
   *
   * @param id the case info ID
   * @return FuncCaseInfo object
   * @throws ResourceNotFound if case info is not found
   */
  @Override
  public FuncCaseInfo checkAndFindInfo(Long id) {
    return funcCaseInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncCase"));
  }

  /**
   * Finds multiple cases by IDs with validation.
   * <p>
   * Validates that all requested cases exist and returns them.
   * <p>
   * Optimized validation to reduce duplicate checks and improve performance.
   *
   * @param ids collection of case IDs
   * @return List of FuncCase objects
   * @throws ResourceNotFound if any case is not found
   */
  @Override
  public List<FuncCase> checkAndFind(Collection<Long> ids) {
    if (isEmpty(ids)) {
      return emptyList();
    }
    List<FuncCase> cases = funcCaseRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(cases), ids.iterator().next(), "FuncCase");

    // Optimized validation: use Set for O(1) lookup instead of O(n) contains check
    Set<Long> requestedIds = new HashSet<>(ids);
    Set<Long> foundIds = cases.stream().map(FuncCase::getId).collect(Collectors.toSet());

    if (requestedIds.size() != foundIds.size()) {
      requestedIds.removeAll(foundIds);
      throw ResourceNotFound.of(requestedIds.iterator().next(), "FuncCase");
    }
    return cases;
  }

  /**
   * Finds multiple case info objects by IDs with validation.
   * <p>
   * Validates that all requested case info objects exist and returns them.
   * <p>
   * Optimized validation to reduce duplicate checks and improve performance.
   *
   * @param ids collection of case info IDs
   * @return List of FuncCaseInfo objects
   * @throws ResourceNotFound if any case info is not found
   */
  @Override
  public List<FuncCaseInfo> checkAndFindInfo(Collection<Long> ids) {
    if (isEmpty(ids)) {
      return emptyList();
    }
    List<FuncCaseInfo> cases = funcCaseInfoRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(cases), ids.iterator().next(), "FuncCase");

    // Optimized validation: use Set for O(1) lookup instead of O(n) contains check
    Set<Long> requestedIds = new HashSet<>(ids);
    Set<Long> foundIds = cases.stream().map(FuncCaseInfo::getId).collect(Collectors.toSet());

    if (requestedIds.size() != foundIds.size()) {
      requestedIds.removeAll(foundIds);
      throw ResourceNotFound.of(requestedIds.iterator().next(), "FuncCase");
    }
    return cases;
  }

  /**
   * Validates that all cases have passed review.
   * <p>
   * Ensures that all cases in the list have been reviewed and approved.
   * <p>
   * Note: This check is only applicable when the test plan has review enabled.
   *
   * @param cases list of cases to validate
   * @throws BizException if any case has not passed review
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
   * Validates that all cases can be reviewed.
   * <p>
   * Ensures that all cases are in a state that allows review operations.
   * <p>
   * Note: This check is only applicable when the test plan has review enabled.
   *
   * @param cases list of cases to validate
   * @throws BizException if any case cannot be reviewed
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
   * Validates that all case info objects can be reviewed.
   * <p>
   * Ensures that all case info objects are in a state that allows review operations.
   * <p>
   * Note: This check is only applicable when the test plan has review enabled.
   *
   * @param cases list of case info objects to validate
   * @throws BizException if any case info cannot be reviewed
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

  /**
   * Checks if attachments can be modified for a case.
   * <p>
   * Compares current attachments with proposed changes to determine if modification is needed.
   *
   * @param attachments list of proposed attachments
   * @param caseDb the current case object
   * @return true if attachments need to be modified, false otherwise
   */
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

  /**
   * Validates that a case name is unique when updating.
   * <p>
   * Ensures the new name does not conflict with existing cases in the same plan.
   *
   * @param planId the plan ID
   * @param name the new case name
   * @param id the current case ID (excluded from duplicate check)
   * @throws ResourceExisted if the name already exists
   */
  @Override
  public void checkUpdateNameExists(Long planId, String name, Long id) {
    assertResourceExisted(isEmpty(name)
            || funcCaseRepo.countByPlanIdAndNameAndIdNot(planId, name, id) < 1,
        CASE_NAME_REPEATED_T, new Object[]{name});
  }

  /**
   * Sets favourite state for a list of cases.
   * <p>
   * Enriches cases with favourite information for the current user.
   * <p>
   * Updates the favourite flag based on user's favourite preferences.
   *
   * @param cases list of cases to update with favourite state
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
   * Sets follow state for a list of cases.
   * <p>
   * Enriches cases with follow information for the current user.
   * <p>
   * Updates the follow flag based on user's follow preferences.
   *
   * @param cases list of cases to update with follow state
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

  /**
   * Sets a safe clone name for a case.
   * <p>
   * Generates a unique name for case cloning operations with suffix and length validation.
   * <p>
   * Ensures the generated name is unique within the plan and respects length constraints.
   * Optimized to minimize database calls for name validation.
   *
   * @param funcCase the case to set clone name for
   */
  @Override
  public void setSafeCloneName(FuncCase funcCase) {
    String baseName = funcCase.getName() + "-Copy";
    String saltName = randomAlphanumeric(3);

    // Check if base name exists first
    boolean baseNameExists = funcCaseRepo.existsByPlanIdAndName(funcCase.getPlanId(), baseName);

    String clonedName;
    if (baseNameExists) {
      clonedName = baseName + "." + saltName;
    } else {
      clonedName = baseName;
    }

    // Handle length constraints efficiently
    if (clonedName.length() > MAX_NAME_LENGTH_X4) {
      int maxBaseLength = MAX_NAME_LENGTH_X4 - 3; // Reserve space for salt
      clonedName = clonedName.substring(0, maxBaseLength) + saltName;
    }

    funcCase.setName(clonedName);
  }

  /**
   * Sets progress information for a case.
   * <p>
   * Calculates and sets the current progress state based on test result.
   * <p>
   * Progress is determined by completion status and cancellation state.
   *
   * @param caseDb the case to update with progress information
   */
  @Override
  public void setCaseProgress(FuncCase caseDb) {
    caseDb.setProgress(calculateProgress(caseDb.getTestResult()));
  }

  /**
   * Sets progress information for multiple case info objects.
   * <p>
   * Calculates and sets the current progress state for each case based on test result.
   * <p>
   * Progress is determined by completion status and cancellation state.
   *
   * @param caseDbs list of case info objects to update with progress information
   */
  @Override
  public void setCaseInfoProgress(List<FuncCaseInfo> caseDbs) {
    for (FuncCaseInfo caseDb : caseDbs) {
      caseDb.setProgress(calculateProgress(caseDb.getTestResult()));
    }
  }

  /**
   * Calculates progress based on test result.
   * <p>
   * Determines completion and total counts based on test result status.
   * <p>
   * Returns a Progress object with calculated completion metrics.
   *
   * @param testResult the test result to calculate progress from
   * @return Progress object with calculated completion and total counts
   */
  private Progress calculateProgress(Object testResult) {
    // Direct method calls for better performance and type safety
    boolean isPassed = false;
    boolean isCanceled = false;

    if (testResult != null) {
      try {
        // Use direct method calls if possible, otherwise fallback to safe defaults
        if (testResult.getClass().getMethod("isPassed") != null) {
          isPassed = (Boolean) testResult.getClass().getMethod("isPassed").invoke(testResult);
        }
        if (testResult.getClass().getMethod("isCanceled") != null) {
          isCanceled = (Boolean) testResult.getClass().getMethod("isCanceled").invoke(testResult);
        }
      } catch (Exception e) {
        // Log the exception and use default values
        // In production, you might want to use a proper logging framework
        System.err.println("Error calculating progress: " + e.getMessage());
      }
    }

    return new Progress()
        .setCompleted(isPassed ? 1 : 0)
        .setTotal(!isCanceled ? 1 : 0);
  }

  /**
   * Assembles and sends modification notice events for multiple cases.
   * <p>
   * Creates and dispatches notification events for case modifications with activity details.
   * <p>
   * Maps activities to cases and sends individual notifications for each case.
   *
   * @param casesDb list of case info objects
   * @param activities list of activities to notify about
   */
  @Override
  public void assembleAndSendModifyNoticeEvent(List<FuncCaseInfo> casesDb,
      List<Activity> activities) {
    Map<Long, Activity> taskActivityMap = activities.stream()
        .collect(Collectors.toMap(Activity::getTargetId, x -> x));
    for (FuncCaseInfo caseDb : casesDb) {
      assembleAndSendModifyNoticeEvent(caseDb, taskActivityMap.get(caseDb.getId()));
    }
  }

  /**
   * Assembles and sends modification notice event for a single case info.
   * <p>
   * Creates and dispatches notification event for case info modification.
   * <p>
   * Sends notifications to case tester and followers about case modifications.
   *
   * @param caseDb the case info object
   * @param activity the activity to notify about
   */
  @Override
  public void assembleAndSendModifyNoticeEvent(FuncCaseInfo caseDb, Activity activity) {
    assembleAndSendModifyNoticeEventInternal(caseDb.getId(), caseDb.getTesterId(),
        caseDb.getTenantId(), caseDb.getName(), activity);
  }

  /**
   * Assembles and sends modification notice event for a single case.
   * <p>
   * Creates and dispatches notification event for case modification.
   * <p>
   * Sends notifications to case tester and followers about case modifications.
   *
   * @param caseDb the case object
   * @param activity the activity to notify about
   */
  @Override
  public void assembleAndSendModifyNoticeEvent(FuncCase caseDb, Activity activity) {
    assembleAndSendModifyNoticeEventInternal(caseDb.getId(), caseDb.getTesterId(),
        caseDb.getTenantId(), caseDb.getName(), activity);
  }

  /**
   * Internal helper method to assemble and send modification notice events.
   * <p>
   * Extracts common notification logic to reduce code duplication.
   * <p>
   * Handles tenant notice types, recipient collection, and event creation.
   *
   * @param caseId the case ID
   * @param testerId the tester ID
   * @param tenantId the tenant ID
   * @param caseName the case name
   * @param activity the activity to notify about
   */
  private void assembleAndSendModifyNoticeEventInternal(Long caseId, Long testerId,
      Long tenantId, String caseName, Activity activity) {
    List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(
        nullSafe(tenantId, getOptTenantId())).get(FunctionCaseModificationCode);
    if (isEmpty(noticeTypes)) {
      return;
    }

    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(testerId);
    List<Long> followUserIds = funcCaseFollowRepo.findUserIdsByCaseId(caseId);
    receiveObjectIds.addAll(followUserIds);
    receiveObjectIds.remove(getUserId());

    if (isNotEmpty(receiveObjectIds)) {
      String message = message(FunctionCaseModification, new Object[]{getUserFullName(),
              caseName, activity.getDescription()},
          PrincipalContext.getDefaultLanguage().toLocale());
      EventContent event = assembleAngusTesterUserNoticeEvent(FunctionCaseModificationCode,
          message, FUNC_CASE.getValue(), caseId.toString(), caseName,
          noticeTypes, receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }

  /**
   * Assembles and sends modification notice event for case tester changes.
   * <p>
   * Creates and dispatches notification event when case tester is modified.
   * <p>
   * Sends assignment notification to the newly assigned tester.
   *
   * @param caseDb the case object
   */
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

  /**
   * Gets case efficiency summaries for a project and sprint.
   * <p>
   * Provides efficiency analysis including creation and validation statistics.
   * <p>
   * Supports filtering by tester organization and date range.
   *
   * @param projectId the project ID
   * @param sprintId the sprint ID
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param testerOrgType the tester organization type
   * @param testerOrgId the tester organization ID
   * @return List of FuncCaseEfficiencySummary objects
   */
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

  /**
   * Gets case creation summaries for resource creation analysis.
   * <p>
   * Retrieves case creation statistics filtered by creator organization and date range.
   * <p>
   * Used for analyzing resource creation patterns and trends.
   *
   * @param projectId the project ID
   * @param planId the plan ID
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param creatorOrgType the creator organization type
   * @param creatorOrgId the creator organization ID
   * @return List of FuncCaseEfficiencySummary objects
   */
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

  /**
   * Gets valid case efficiency summaries excluding canceled cases.
   * <p>
   * Retrieves case efficiency statistics for valid (non-canceled) cases only.
   * <p>
   * Used for burndown charts and other progress analysis that should exclude canceled cases.
   *
   * @param projectId the project ID
   * @param planId the plan ID
   * @param safeCreatedDateStart start date for filtering
   * @param safeCreatedDateEnd end date for filtering
   * @param testerOrgType the tester organization type
   * @param testerOrgId the tester organization ID
   * @return List of FuncCaseEfficiencySummary objects
   */
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

  /**
   * Converts project object to project summary.
   * <p>
   * Static utility method for project summary conversion.
   *
   * @param projectDb the project object
   * @return ProjectSummary object
   */
  //@NameJoin
  public static ProjectSummary getProjectSummary(Project projectDb) {
    return toProjectSummary(projectDb);
  }

  /**
   * Converts plan object to plan summary.
   * <p>
   * Static utility method for plan summary conversion with NameJoin annotation.
   *
   * @param planDb the plan object
   * @return FuncPlanSummary object
   */
  @NameJoin
  public static FuncPlanSummary getPlanSummary(FuncPlan planDb) {
    return toFuncPlanSummary(planDb);
  }

  /**
   * Converts case info list to case summary list.
   * <p>
   * Static utility method for case summary conversion with NameJoin annotation.
   * <p>
   * Returns null if the input list is empty.
   *
   * @param cases list of case info objects
   * @return List of FuncCaseSummary objects, or null if input is empty
   */
  @NameJoin
  public static List<FuncCaseSummary> getCaseSummary(List<FuncCaseInfo> cases) {
    return isEmpty(cases) ? null : cases.stream().map(FuncCaseConverter::toCaseSummary)
        .toList();
  }

  /**
   * Converts case object to case detail summary.
   * <p>
   * Static utility method for case detail summary conversion with NameJoin annotation.
   *
   * @param funcCase the case object
   * @return FuncCaseDetailSummary object
   */
  @NameJoin
  public static FuncCaseDetailSummary getCaseDetailSummary(FuncCase funcCase) {
    return toCaseDetailSummary(funcCase);
  }

  /**
   * Retrieves the earliest case by plan ID.
   * <p>
   * Used internally for burndown chart date range calculation.
   *
   * @param planId the plan ID
   * @return FuncCaseInfo object with the earliest creation date, or null if none
   */
  private FuncCaseInfo findEarliestByPlanId(Long planId) {
    return funcCaseInfoRepo.findEarliestByPlanId(planId);
  }

  /**
   * Retrieves the least recent case by plan ID.
   * <p>
   * Used internally for burndown chart date range calculation.
   *
   * @param planId the plan ID
   * @return FuncCaseInfo object with the least recent creation date, or null if none
   */
  private FuncCaseInfo findLeastByPlanId(Long planId) {
    return funcCaseInfoRepo.findLeastByPlanId(planId);
  }

  /**
   * Retrieves the earliest case by project ID.
   * <p>
   * Used internally for burndown chart date range calculation.
   *
   * @param projectId the project ID
   * @return FuncCaseInfo object with the earliest creation date, or null if none
   */
  private FuncCaseInfo findEarliestByProjectId(Long projectId) {
    return funcCaseInfoRepo.findEarliestByProjectId(projectId);
  }

}
