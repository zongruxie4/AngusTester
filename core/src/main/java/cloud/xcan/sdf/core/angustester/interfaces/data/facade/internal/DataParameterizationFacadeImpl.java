package cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal;

import cloud.xcan.sdf.core.angustester.application.query.data.DatasetTargetQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableTargetQuery;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.DataParameterizationFacade;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Resource;
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
