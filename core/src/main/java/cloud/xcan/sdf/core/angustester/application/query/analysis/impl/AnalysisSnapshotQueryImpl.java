package cloud.xcan.sdf.core.angustester.application.query.analysis.impl;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisSnapshotQuery;
import cloud.xcan.sdf.core.angustester.domain.analysis.snapshot.AnalysisSnapshot;
import cloud.xcan.sdf.core.angustester.domain.analysis.snapshot.AnalysisSnapshotRepo;
import cloud.xcan.sdf.core.biz.Biz;
import javax.annotation.Resource;

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
