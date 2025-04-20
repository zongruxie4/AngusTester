package cloud.xcan.angus.core.tester.domain.task.cases;

import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.spec.experimental.Entity;
import java.util.List;

public interface TaskFuncCaseAssoc<T extends Entity<T, ID>, ID> {

  Long getId();

  List<TaskInfo> getAssocTasks();

  List<FuncCaseInfo> getAssocCases();

  List<TagTarget> getTagTargets();

  T setAssocTasks(List<TaskInfo> assocTasks);

  T setAssocCases(List<FuncCaseInfo> assocCases);

  T setTagTargets(List<TagTarget> tags);

}
