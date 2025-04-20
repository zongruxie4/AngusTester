package cloud.xcan.angus.core.tester.application.query.data;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.BaseTarget;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTarget;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VariableTargetQuery {

  List<Variable> findVariables(Long targetId, String targetType);

  Map<Long, List<Variable>> findVariables(Collection<Long> targetId, String targetType, Map<Long, Long> caseApiMap);

  List<VariableTarget> findTargets(Long variableId);

  Map<String, String> valuePreview(Long targetId, String targetType);

  Set<Long> findVariableIdByServiceId(Long serviceId);

  void checkTargetPermission(Long targetId, String targetType);

  void checkTargetQuota(int inc, Long targetId, CombinedTargetType targetType);

  void setTargetName(List<? extends BaseTarget> targets);


}
