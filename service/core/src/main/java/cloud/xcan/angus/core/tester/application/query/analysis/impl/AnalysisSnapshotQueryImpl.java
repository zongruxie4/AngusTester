package cloud.xcan.angus.core.tester.application.query.analysis.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.tester.application.query.analysis.AnalysisSnapshotQuery;
import cloud.xcan.angus.core.tester.domain.analysis.snapshot.AnalysisSnapshot;
import cloud.xcan.angus.core.tester.domain.analysis.snapshot.AnalysisSnapshotRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;

@Biz
public class AnalysisSnapshotQueryImpl implements AnalysisSnapshotQuery {

  @Resource
  private AnalysisSnapshotRepo analysisSnapshotRepo;

  @Override
  public AnalysisSnapshot findByAnalysisId(Long analysisId) {
    return analysisSnapshotRepo.findByAnalysisId(analysisId).orElse(null);
  }

  @Override
  public AnalysisSnapshot checkAndFindByAnalysisId(Long analysisId) {
    return analysisSnapshotRepo.findByAnalysisId(analysisId)
        .orElseThrow(() -> ResourceNotFound.of(analysisId, "AnalysisSnapshot"));
  }

}
