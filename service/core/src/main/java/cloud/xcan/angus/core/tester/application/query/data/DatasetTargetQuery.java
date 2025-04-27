package cloud.xcan.angus.core.tester.application.query.data;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTarget;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DatasetTargetQuery {

  List<Dataset> findDatasets(Long targetId, String targetType);

  Map<Long, List<Dataset>> findDatasets(Collection<Long> targetIds, String targetType,
      Map<Long, Long> caseApiMap);

  List<DatasetTarget> findTargets(Long datasetId);

  Map<String, String> valuePreview(Long targetId, String targetType);

  void checkTargetQuota(int inc, Long targetId, CombinedTargetType targetType);

}
