package cloud.xcan.angus.core.tester.interfaces.data.facade.internal;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.data.VariableTargetCmd;
import cloud.xcan.angus.core.tester.application.query.data.VariableTargetQuery;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTarget;
import cloud.xcan.angus.core.tester.interfaces.data.facade.VariableTargetFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.VariableAssembler;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableTargetVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class VariableTargetFacadeImpl implements VariableTargetFacade {

  @Resource
  private VariableTargetCmd variableTargetCmd;

  @Resource
  private VariableTargetQuery variableTargetQuery;

  @Override
  public List<IdKey<Long, Object>> add(Long targetId, String targetType,
      LinkedHashSet<Long> variableIds) {
    return variableTargetCmd.add(targetId, targetType, variableIds, true);
  }

  @Override
  public void delete(Long targetId, String targetType, HashSet<Long> variableIds) {
    variableTargetCmd.delete(targetId, targetType, variableIds, true);
  }

  @NameJoin
  @Override
  public List<VariableDetailVo> list(Long targetId, String targetType) {
    List<Variable> variables = variableTargetQuery.findVariables(targetId, targetType);
    return variables.stream().map(VariableAssembler::toDetailVo).toList();
  }

  @NameJoin
  @Override
  public List<VariableTargetVo> listTarget(Long variableId) {
    List<VariableTarget> targets = variableTargetQuery.findTargets(variableId);
    return targets.stream().map(VariableAssembler::toTargetVo).toList();
  }

  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    return variableTargetQuery.valuePreview(targetId, targetType);
  }
}
