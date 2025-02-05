package cloud.xcan.sdf.core.angustester.application.cmd.data;

import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;

public interface VariableTargetCmd {

  List<IdKey<Long, Object>> add(Long targetId, String targetType, Set<Long> variableIds,
      boolean saveActivity);

  void delete(Long targetId, String targetType, Set<Long> variableIds, boolean saveActivity);

}
