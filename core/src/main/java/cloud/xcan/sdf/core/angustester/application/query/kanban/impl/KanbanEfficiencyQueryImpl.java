package cloud.xcan.sdf.core.angustester.application.query.kanban.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.FuncCaseConverter.getCaseTesterResourcesFilter;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyCaseConverter.assembleCaseTotalOverview;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyCaseConverter.assembleTesterOverview;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyCaseConverter.assembleTesterRanking;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleAssigneeOverview;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleAssigneeRanking;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyTaskConverter.assembleTaskTotalOverview;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.getTaskAssigneeResourcesFilter;
import static cloud.xcan.sdf.core.angustester.application.query.func.impl.FuncCaseQueryImpl.getCaseSummary;
import static cloud.xcan.sdf.core.angustester.application.query.task.impl.TaskQueryImpl.getTaskSummary;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.kanban.KanbanEfficiencyQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncCaseEfficiencySummary;
import cloud.xcan.sdf.core.angustester.domain.kanban.BurnDownResourceType;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseCountOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseRanking;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseTesterOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskAssigneeOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskCountOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskRanking;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import cloud.xcan.sdf.core.angustester.domain.task.cases.CaseTestHit;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartCount;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskEfficiencySummary;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.spec.annotations.NonNullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;

@Biz
public class KanbanEfficiencyQueryImpl implements KanbanEfficiencyQuery {

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private TaskInfoRepo taskInfoRepo;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public EfficiencyTaskOverview taskEfficiencyOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, @NonNullable Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTaskList,
      boolean joinAssigneeOverview, boolean joinRanking, boolean joinBurnDownChar) {
    return new BizTemplate<EfficiencyTaskOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected EfficiencyTaskOverview process() {
        EfficiencyTaskOverview overview = new EfficiencyTaskOverview();

        Set<Long> memberIds = projectMemberQuery.getMemberIds(creatorObjectType,
            creatorObjectId, projectId);
        // Note: All project data will be querying when creator is null
        Set<SearchCriteria> allFilters = getTaskAssigneeResourcesFilter(projectId, planId,
            createdDateStart, createdDateEnd, isNull(creatorObjectId) || isNull(creatorObjectType)
                ? null : memberIds);
        List<TaskEfficiencySummary> tasks = taskRepo.findProjectionByFilters(TaskInfo.class,
            TaskEfficiencySummary.class, allFilters);

        // Add members who have tasks but have been removed from the project
        memberIds.addAll(tasks.stream().map(TaskEfficiencySummary::getAssigneeId)
            .filter(Objects::nonNull).collect(Collectors.toSet()));
        overview.setAssignees(commonQuery.getUserInfoMap(memberIds));

        if (isEmpty(tasks)) {
          overview.setTotalOverview(new EfficiencyTaskCountOverview());
          return overview;
        }

        assembleTaskTotalOverview(tasks, overview);

        if (joinAssigneeOverview || joinRanking/*be dependent*/) {
          assembleAssigneeOverview(tasks, overview);
        }

        if (joinRanking) {
          EfficiencyTaskRanking assigneeRanking = assembleAssigneeRanking(overview);
          overview.setAssigneeRanking(assigneeRanking);
        }

        if (joinBurnDownChar) {
          assembleBurnDownChart(overview, projectId, planId, creatorObjectType, creatorObjectId,
              createdDateStart, createdDateEnd);
        }

        if (joinTaskList) {
          List<TaskInfo> taskInfos = taskInfoRepo.findAllByFilters(allFilters);
          overview.setTasks(joinSupplier.execute(() -> getTaskSummary(taskInfos)));
        }
        return overview;
      }
    }.execute();
  }

  @Override
  public EfficiencyCaseOverview caseEfficiencyOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, @NonNullable Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTestHit,
      boolean joinCaseList, boolean joinTesterOverview, boolean joinRanking,
      boolean joinBurnDownChar) {
    return new BizTemplate<EfficiencyCaseOverview>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected EfficiencyCaseOverview process() {
        EfficiencyCaseOverview overview = new EfficiencyCaseOverview();

        Set<Long> memberIds = projectMemberQuery.getMemberIds(creatorObjectType,
            creatorObjectId, projectId);

        // Note: All project data will be querying when creator is null
        Set<SearchCriteria> allFilters = getCaseTesterResourcesFilter(projectId, planId,
            createdDateStart, createdDateEnd, isNull(creatorObjectId) || isNull(creatorObjectType)
                ? null : memberIds);
        List<FuncCaseEfficiencySummary> cases = funcCaseRepo.findProjectionByFilters(
            FuncCaseInfo.class, FuncCaseEfficiencySummary.class, allFilters);

        // Add members who have cases but have been removed from the project
        memberIds.addAll(cases.stream().map(FuncCaseEfficiencySummary::getTesterId)
            .filter(Objects::nonNull).collect(Collectors.toSet()));
        overview.setTesters(commonQuery.getUserInfoMap(memberIds));
        if (isEmpty(cases)) {
          overview.setTotalOverview(new EfficiencyCaseCountOverview());
          return overview;
        }

        List<CaseTestHit> testHits = joinTestHit ? taskFuncCaseQuery.findCaseHitBugs(
            cases.stream().map(FuncCaseEfficiencySummary::getId).collect(Collectors.toSet()))
            : null;

        assembleCaseTotalOverview(cases, testHits, overview);

        if (joinTesterOverview || joinRanking/*be dependent*/) {
          assembleTesterOverview(cases, testHits, overview);
        }

        if (joinRanking) {
          EfficiencyCaseRanking testerRanking = new EfficiencyCaseRanking();
          assembleTesterRanking(overview, testerRanking);

          overview.setTesterRanking(testerRanking);
        }

        if (joinBurnDownChar) {
          assembleBurnDownChart(overview, projectId, planId, creatorObjectType, creatorObjectId,
              createdDateStart, createdDateEnd);
        }

        if (joinCaseList) {
          List<FuncCaseInfo> caseInfos = funcCaseInfoRepo.findAllByFilters(allFilters);
          overview.setCases(joinSupplier.execute(() -> getCaseSummary(caseInfos)));
        }
        return overview;
      }
    }.execute();
  }

  private void assembleBurnDownChart(EfficiencyTaskOverview overview, Long projectId, Long planId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    Project projectDb = projectQuery.checkAndFind(projectId);

    Map<BurnDownResourceType, BurnDownChartCount> burnDownCharts
        = taskQuery.burndownChart(projectId, planId, creatorObjectType, creatorObjectId,
        nullSafe(createdDateStart, projectDb.getStartDate()),
        nullSafe(createdDateEnd, projectDb.getDeadlineDate()),
        false, false).getTotalBurnDownCharts();
    overview.setBurnDownCharts(burnDownCharts);

    if (isNotEmpty(overview.getAssigneeOverview())) {
      for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
        if (!AuthObjectType.USER.equals(creatorObjectType)) {
          Map<BurnDownResourceType, BurnDownChartCount> assigneeOBurnDownCharts
              = taskQuery.burndownChart(projectId, planId,
              creatorObjectType, assigneeOverview.getAssigneeId(),
              nullSafe(createdDateStart, projectDb.getStartDate()),
              nullSafe(createdDateEnd, projectDb.getDeadlineDate()),
              false, false).getTotalBurnDownCharts();
          assigneeOverview.setBurnDownCharts(assigneeOBurnDownCharts);
        }
      }
    }
  }

  private void assembleBurnDownChart(EfficiencyCaseOverview overview, Long projectId, Long planId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    Project projectDb = projectQuery.checkAndFind(projectId);

    Map<BurnDownResourceType, BurnDownChartCount> burnDownCharts = funcCaseQuery.burndownChart(
            projectId, planId, creatorObjectType, creatorObjectId,
            nullSafe(createdDateStart, projectDb.getStartDate()),
            nullSafe(createdDateEnd, projectDb.getDeadlineDate()), false, false)
        .getTotalBurnDownCharts();
    overview.setBurnDownCharts(burnDownCharts);

    if (isNotEmpty(overview.getTesterOverview())) {
      for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
        if (!AuthObjectType.USER.equals(creatorObjectType)) {
          Map<BurnDownResourceType, BurnDownChartCount> assigneeOBurnDownCharts
              = funcCaseQuery.burndownChart(projectId, planId,
                  creatorObjectType, testerOverview.getTesterId(),
                  nullSafe(createdDateStart, projectDb.getStartDate()),
                  nullSafe(createdDateEnd, projectDb.getDeadlineDate()), false, false)
              .getTotalBurnDownCharts();
          testerOverview.setBurnDownCharts(assigneeOBurnDownCharts);
        }
      }
    }
  }

}
