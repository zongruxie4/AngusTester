package cloud.xcan.angus.core.tester.domain;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class CombinedTarget implements ActivityResource {

  private CombinedTargetType targetType;

  private Long targetId;

  private String targetName;

  private Scenario scenarioInfo;

  private Services services;

  private ApisBaseInfo apisBaseInfo;

  private Services apisParent;

  private FuncCaseInfo caseInfo;

  private FuncPlan caseParent;

  private FuncPlan plan;

  private TaskInfo taskInfo;

  private TaskSprint taskParent;

  private TaskSprint sprint;

  //PROJECT, SERVICE, API, SCENARIO, DIR/*SCENARIO DIR*/, TASK, VARIABLE, FUNC_DIR, PLAN, CASE;

  @Override
  public Long getId() {
    if (CombinedTargetType.SCENARIO.equals(targetType)) {
      return scenarioInfo.getId();
    } else if (CombinedTargetType.API.equals(targetType)) {
      return apisBaseInfo.getId();
    } else if (CombinedTargetType.SERVICE.equals(targetType)) {
      return services.getId();
    } else if (CombinedTargetType.TASK.equals(targetType)) {
      return taskInfo.getId();
    } else if (CombinedTargetType.TASK_SPRINT.equals(targetType)) {
      return sprint.getId();
    } else if (CombinedTargetType.FUNC_CASE.equals(targetType)) {
      return caseInfo.getId();
    } else if (CombinedTargetType.FUNC_PLAN.equals(targetType)) {
      return plan.getId();
    }
    return DEFAULT_ROOT_PID;
  }

  @Override
  public String getName() {
    if (CombinedTargetType.SCENARIO.equals(targetType)) {
      return scenarioInfo.getName();
    } else if (CombinedTargetType.API.equals(targetType)) {
      return apisBaseInfo.getName();
    } else if (CombinedTargetType.SERVICE.equals(targetType)) {
      return services.getName();
    } else if (CombinedTargetType.TASK.equals(targetType)) {
      return taskInfo.getName();
    } else if (CombinedTargetType.TASK_SPRINT.equals(targetType)) {
      return sprint.getName();
    } else if (CombinedTargetType.FUNC_CASE.equals(targetType)) {
      return caseInfo.getName();
    } else if (CombinedTargetType.FUNC_PLAN.equals(targetType)) {
      return plan.getName();
    }
    return "";
  }

  @Override
  public Long getParentId() {
    if (CombinedTargetType.SCENARIO.equals(targetType)) {
      return scenarioInfo.getParentId();
    } else if (CombinedTargetType.API.equals(targetType)) {
      return apisBaseInfo.getParentId();
    } else if (CombinedTargetType.SERVICE.equals(targetType)) {
      return services.getParentId();
    } else if (CombinedTargetType.TASK.equals(targetType)) {
      return taskInfo.getParentId();
    } else if (CombinedTargetType.TASK_SPRINT.equals(targetType)) {
      return sprint.getParentId();
    } else if (CombinedTargetType.FUNC_CASE.equals(targetType)) {
      return caseInfo.getParentId();
    } else if (CombinedTargetType.FUNC_PLAN.equals(targetType)) {
      return plan.getParentId();
    }
    return DEFAULT_ROOT_PID;
  }

  @Override
  public Long getProjectId() {
    if (CombinedTargetType.SCENARIO.equals(targetType)) {
      return scenarioInfo.getProjectId();
    } else if (CombinedTargetType.API.equals(targetType)) {
      return apisBaseInfo.getProjectId();
    } else if (CombinedTargetType.SERVICE.equals(targetType)) {
      return services.getProjectId();
    } else if (CombinedTargetType.TASK.equals(targetType)) {
      return taskInfo.getProjectId();
    } else if (CombinedTargetType.TASK_SPRINT.equals(targetType)) {
      return sprint.getProjectId();
    } else if (CombinedTargetType.FUNC_CASE.equals(targetType)) {
      return caseInfo.getProjectId();
    } else if (CombinedTargetType.FUNC_PLAN.equals(targetType)) {
      return plan.getProjectId();
    }
    return DEFAULT_ROOT_PID;
  }
}
