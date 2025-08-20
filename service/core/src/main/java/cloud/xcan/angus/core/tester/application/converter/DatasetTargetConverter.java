package cloud.xcan.angus.core.tester.application.converter;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTarget;
import java.util.List;
import java.util.Set;

public class DatasetTargetConverter {

  public static List<DatasetTarget> toDomain(Long projectId, CombinedTargetType targetType,
      Long targetId, Set<Long> datasetIds) {
    return datasetIds.stream().map(x -> {
      DatasetTarget target = new DatasetTarget();
      target.setProjectId(projectId);
      target.setDatasetId(x);
      target.setTargetType(targetType);
      target.setTargetId(targetId);
      return target;
    }).toList();
  }

}
