package cloud.xcan.sdf.core.angustester.domain.task.cases;

import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTarget;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.spec.experimental.Entity;
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
