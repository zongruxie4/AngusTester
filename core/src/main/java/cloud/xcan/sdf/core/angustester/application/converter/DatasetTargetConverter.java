package cloud.xcan.sdf.core.angustester.application.converter;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetTarget;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    }).collect(Collectors.toList());
  }

}
