package cloud.xcan.angus.core.tester.interfaces.data.facade.internal;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.data.DatasetTargetCmd;
import cloud.xcan.angus.core.tester.application.query.data.DatasetTargetQuery;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTarget;
import cloud.xcan.angus.core.tester.interfaces.data.facade.DatasetTargetFacade;
import cloud.xcan.angus.core.tester.interfaces.data.facade.internal.assembler.DatasetAssembler;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetTargetVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    return datasets.stream().map(DatasetAssembler::toDetailVo).toList();
  }

  @NameJoin
  @Override
  public List<DatasetTargetVo> listTarget(Long datasetId) {
    List<DatasetTarget> targets = datasetTargetQuery.findTargets(datasetId);
    return targets.stream().map(DatasetAssembler::toTargetVo).toList();
  }

  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    return datasetTargetQuery.valuePreview(targetId, targetType);
  }
}
