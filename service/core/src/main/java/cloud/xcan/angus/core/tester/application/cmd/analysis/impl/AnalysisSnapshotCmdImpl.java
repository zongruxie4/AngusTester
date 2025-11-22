package cloud.xcan.angus.core.tester.application.cmd.analysis.impl;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.analysis.AnalysisSnapshotCmd;
import cloud.xcan.angus.core.tester.application.query.analysis.AnalysisQuery;
import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.core.tester.domain.analysis.snapshot.AnalysisSnapshot;
import cloud.xcan.angus.core.tester.domain.analysis.snapshot.AnalysisSnapshotRepo;
import jakarta.annotation.Resource;
import java.util.Collection;

@Biz
public class AnalysisSnapshotCmdImpl extends CommCmd<AnalysisSnapshot, Long> implements
    AnalysisSnapshotCmd {

  @Resource
  private AnalysisSnapshotRepo analysisSnapshotRepo;

  @Resource
  private AnalysisQuery analysisQuery;

  @Override
  public void add0(AnalysisSnapshot snapshot) {
    insert0(snapshot);
  }

  @Override
  public void update0(AnalysisSnapshot snapshot) {
    analysisSnapshotRepo.save(snapshot);
  }

  @Override
  public void replace0(AnalysisSnapshot as, Analysis analysis, Analysis analysisDb) {
    if (nonNull(as)) {
      as.setData(analysisQuery.getSnapshotDataString(analysisDb));
      update0(as);
    } else {
      // Convert from real-time to snapshot
      AnalysisSnapshot snapshot = new AnalysisSnapshot();
      snapshot.setAnalysisId(analysisDb.getId());
      snapshot.setData(analysisQuery.getSnapshotDataString(analysis));
      add0(snapshot);
    }
  }

  @Override
  public void deleteByAnalysisId(Collection<Long> ids) {
    analysisSnapshotRepo.deleteByAnalysisIdIn(ids);
  }

  @Override
  protected BaseRepository<AnalysisSnapshot, Long> getRepository() {
    return analysisSnapshotRepo;
  }
}
