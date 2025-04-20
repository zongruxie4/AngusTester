package cloud.xcan.angus.core.tester.interfaces.data.facade;

import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetTargetVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public interface DatasetTargetFacade {

  List<IdKey<Long, Object>> add(Long targetId, String targetType, LinkedHashSet<Long> datasetIds);

  void delete(Long targetId, String targetType, HashSet<Long> datasetIds);

  List<DatasetDetailVo> list(Long targetId, String targetType);

  List<DatasetTargetVo> listTarget(Long datasetId);

  Map<String, String> valuePreview(Long targetId, String targetType);

}
