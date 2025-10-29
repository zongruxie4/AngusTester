package cloud.xcan.angus.core.tester.application.query.issue;

import cloud.xcan.angus.core.tester.domain.issue.cases.CaseTestHit;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCase;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCaseAssoc;
import java.util.List;
import java.util.Set;

public interface TaskFuncCaseQuery {

  List<TaskFuncCase> findWideByTargetId(List<Long> targetIds);

  List<CaseTestHit> findCaseHitBugs(Set<Long> caseIds);

  void setAssocForTask(List<? extends TaskFuncCaseAssoc<?, ?>> assocs);

  void setAssocForCase(List<? extends TaskFuncCaseAssoc<?, ?>> cases);


}
