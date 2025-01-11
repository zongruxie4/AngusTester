package cloud.xcan.sdf.core.angustester.application.cmd.analysis;

import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.domain.analysis.snapshot.AnalysisSnapshot;
import java.util.Collection;

public interface AnalysisSnapshotCmd {

  void add0(AnalysisSnapshot snapshot);

  void update0(AnalysisSnapshot snapshot);

  void replace0(AnalysisSnapshot as, Analysis analysis, Analysis analysisDb);

  void deleteByAnalysisId(Collection<Long> ids);
}
