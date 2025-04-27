package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.core.tester.domain.task.cases.CaseTestHit;
import cloud.xcan.angus.core.tester.domain.task.cases.TaskFuncCase;
import cloud.xcan.angus.core.tester.domain.task.cases.TaskFuncCaseAssoc;
import java.util.List;
import java.util.Set;

public interface TaskFuncCaseQuery {

  List<TaskFuncCase> findWideByTargetId(List<Long> targetIds);

  List<CaseTestHit> findCaseHitBugs(Set<Long> caseIds);

  void setAssocForTask(List<? extends TaskFuncCaseAssoc<?, ?>> assocs);

  void setAssocForCase(List<? extends TaskFuncCaseAssoc<?, ?>> cases);


}
