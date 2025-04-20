package cloud.xcan.angus.core.tester.application.converter;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTarget;
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
