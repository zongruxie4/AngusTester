package cloud.xcan.angus.core.tester.interfaces.kanban.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesApisCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesFuncCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesScenarioCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesScriptCount;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesTaskCount;
import cloud.xcan.angus.core.tester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskLastResourceCreationCount;

public class KanbanDataAssetsAssembler {

  public static ResourcesFuncCount toCount(FuncLastResourceCreationCount count) {
    return new ResourcesFuncCount().setAllPlan(count.getAllPlan())
        .setAllReview(count.getAllReview()).setAllBaseline(count.getAllBaseline())
        .setAllCase(count.getAllCase()).setPlanByStatus(count.getPlanByStatus())
        .setCaseByTestResult(count.getCaseByTestResult())
        .setCaseByReviewStatus(count.getCaseByReviewStatus())
        .setCaseByPriority(count.getCaseByPriority());
  }

  public static ResourcesApisCount toCount(ApisResourcesCreationCount count) {
    return new ResourcesApisCount().setAllService(count.getAllService())
        .setAllApis(count.getAllApis())
        .setAllUnarchivedApis(count.getAllUnarchivedApis())
        .setServicesByStatus(count.getServicesByStatus())
        .setApisByStatus(count.getApisByStatus())
        .setApisByMethod(count.getApisByMethod());
  }

  public static ResourcesScenarioCount toCount(ScenarioResourcesCreationCount count) {
    return new ResourcesScenarioCount().setAllSce(count.getAllSce())
        .setSceByScriptType(count.getSceByScriptType())
        .setSceByPluginName(count.getSceByPluginName());
  }

  public static ResourcesTaskCount toCount(TaskLastResourceCreationCount count) {
    return new ResourcesTaskCount().setAllSprint(count.getAllSprint())
        .setAllMeeting(count.getAllMeeting())
        .setAllTask(count.getAllTask())
        .setSprintByStatus(count.getSprintByStatus())
        .setTaskByStatus(count.getTaskByStatus())
        .setTaskByType(count.getTaskByType())
        .setTaskByPriority(count.getTaskByPriority());
  }

  public static ResourcesScriptCount toCount(ScriptResourcesCreationCount count) {
    return new ResourcesScriptCount().setAllScript(count.getAllScript())
        .setScriptByScriptType(count.getScriptByScriptType())
        .setScriptByPluginName(count.getScriptByPluginName());
  }
}
