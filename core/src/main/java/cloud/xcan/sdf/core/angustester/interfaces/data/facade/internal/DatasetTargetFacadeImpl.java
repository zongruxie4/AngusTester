package cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal;

import cloud.xcan.sdf.core.angustester.application.cmd.data.DatasetTargetCmd;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasetTargetQuery;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetTarget;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.DatasetTargetFacade;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler.DatasetAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetTargetVo;
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
public class DatasetTargetFacadeImpl implements DatasetTargetFacade {

  @Resource
  private DatasetTargetCmd datasetTargetCmd;

  @Resource
  private DatasetTargetQuery datasetTargetQuery;

  @Override
  public List<IdKey<Long, Object>> add(Long targetId, String targetType,
      LinkedHashSet<Long> datasetIds) {
    return datasetTargetCmd.add(targetId, targetType, datasetIds, true);
  }

  @Override
  public void delete(Long targetId, String targetType, HashSet<Long> datasetIds) {
    datasetTargetCmd.delete(targetId, targetType, datasetIds, true);
  }

  @NameJoin
  @Override
  public List<DatasetDetailVo> list(Long targetId, String targetType) {
    List<Dataset> datasets = datasetTargetQuery.findDatasets(targetId, targetType);
    return datasets.stream().map(DatasetAssembler::toDetailVo).collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<DatasetTargetVo> listTarget(Long datasetId) {
    List<DatasetTarget> targets = datasetTargetQuery.findTargets(datasetId);
    return targets.stream().map(DatasetAssembler::toTargetVo).collect(Collectors.toList());
  }

  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    return datasetTargetQuery.valuePreview(targetId, targetType);
  }
}
