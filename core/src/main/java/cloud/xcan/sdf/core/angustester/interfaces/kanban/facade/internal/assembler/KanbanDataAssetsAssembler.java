package cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.internal.assembler;

import cloud.xcan.sdf.core.angustester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesApisCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesFuncCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesScenarioCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesScriptCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesTaskCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskLastResourceCreationCount;

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
