package cloud.xcan.angus.core.tester.application.cmd.task;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import java.util.Collection;
import java.util.Set;

public interface TaskFuncCaseCmd {

  void addAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds);

  void replaceAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds);

  void updateAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds);

  void deleteAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds);

  void replaceAssocTask0(CombinedTargetType targetType, Long targetId, Set<Long> taskIds);

  void replaceAssocCase0(CombinedTargetType targetType, Long targetId, Set<Long> caseIds);

  void deleteByTargetIds(Collection<Long> targetIds);
}
