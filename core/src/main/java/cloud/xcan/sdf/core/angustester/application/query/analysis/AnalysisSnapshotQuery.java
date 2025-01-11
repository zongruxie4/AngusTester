package cloud.xcan.sdf.core.angustester.application.query.analysis;

import cloud.xcan.sdf.core.angustester.domain.analysis.snapshot.AnalysisSnapshot;

public interface AnalysisSnapshotQuery {

  AnalysisSnapshot findByAnalysisId(Long analysisId);

  AnalysisSnapshot checkAndFindByAnalysisId(Long analysisId);
}
