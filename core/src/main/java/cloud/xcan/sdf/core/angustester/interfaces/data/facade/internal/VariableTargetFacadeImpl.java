package cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal;

import cloud.xcan.sdf.core.angustester.application.cmd.data.VariableTargetCmd;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableTargetQuery;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableTarget;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.VariableTargetFacade;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler.VariableAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.variable.VariableTargetVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
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
    return variables.stream().map(VariableAssembler::toDetailVo).collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<VariableTargetVo> listTarget(Long variableId) {
    List<VariableTarget> targets = variableTargetQuery.findTargets(variableId);
    return targets.stream().map(VariableAssembler::toTargetVo).collect(Collectors.toList());
  }

  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    return variableTargetQuery.valuePreview(targetId, targetType);
  }
}
