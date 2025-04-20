package cloud.xcan.angus.core.tester.interfaces.data.facade.internal;

import cloud.xcan.angus.core.tester.application.query.data.DatasetTargetQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableTargetQuery;
import cloud.xcan.angus.core.tester.interfaces.data.facade.DataParameterizationFacade;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DataParameterizationFacadeImpl implements DataParameterizationFacade {

  @Resource
  private VariableTargetQuery variableTargetQuery;

  @Resource
  private DatasetTargetQuery datasetTargetQuery;

  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    Map<String, String> valuePreview = new LinkedHashMap<>();
    valuePreview.putAll(variableTargetQuery.valuePreview(targetId, targetType));
    valuePreview.putAll(datasetTargetQuery.valuePreview(targetId, targetType));
    return valuePreview;
  }
}
