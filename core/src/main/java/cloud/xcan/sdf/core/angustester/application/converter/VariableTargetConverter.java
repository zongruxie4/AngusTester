package cloud.xcan.sdf.core.angustester.application.converter;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableTarget;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VariableTargetConverter {

  public static List<VariableTarget> toDomain(Long projectId, CombinedTargetType targetType,
      Long targetId, Set<Long> variableIds) {
    return variableIds.stream().map(x -> {
      VariableTarget target = new VariableTarget();
      target.setProjectId(projectId);
      target.setVariableId(x);
      target.setTargetType(targetType);
      target.setTargetId(targetId);
      return target;
    }).collect(Collectors.toList());
  }

}
