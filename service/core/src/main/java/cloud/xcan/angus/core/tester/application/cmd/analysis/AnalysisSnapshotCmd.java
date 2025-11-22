package cloud.xcan.angus.core.tester.application.cmd.analysis;

import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.core.tester.domain.analysis.snapshot.AnalysisSnapshot;
import java.util.Collection;

public interface AnalysisSnapshotCmd {

  void add0(AnalysisSnapshot snapshot);

  void update0(AnalysisSnapshot snapshot);

  void replace0(AnalysisSnapshot as, Analysis analysis, Analysis analysisDb);

  void deleteByAnalysisId(Collection<Long> ids);
}
