package cloud.xcan.sdf.core.angustester.application.query.task;

import cloud.xcan.sdf.core.angustester.domain.task.cases.CaseTestHit;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCase;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCaseAssoc;
import java.util.List;
import java.util.Set;

public interface TaskFuncCaseQuery {

  List<TaskFuncCase> findWideByTargetId(List<Long> targetIds);

  List<CaseTestHit> findCaseHitBugs(Set<Long> caseIds);

  void setAssocForTask(List<? extends TaskFuncCaseAssoc<?, ?>> assocs);

  void setAssocForCase(List<? extends TaskFuncCaseAssoc<?, ?>> cases);


}
