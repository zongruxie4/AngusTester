package cloud.xcan.angus.core.tester.application.query.analysis;

import cloud.xcan.angus.core.tester.domain.analysis.snapshot.AnalysisSnapshot;

public interface AnalysisSnapshotQuery {

  AnalysisSnapshot findByAnalysisId(Long analysisId);

  AnalysisSnapshot checkAndFindByAnalysisId(Long analysisId);
}
