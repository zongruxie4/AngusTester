package cloud.xcan.angus.core.tester.application.cmd.data;

import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;

public interface VariableTargetCmd {

  List<IdKey<Long, Object>> add(Long targetId, String targetType, Set<Long> variableIds,
      boolean saveActivity);

  void delete(Long targetId, String targetType, Set<Long> variableIds, boolean saveActivity);

}
