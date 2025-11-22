package cloud.xcan.angus.core.tester.application.cmd.analysis;

import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;

public interface AnalysisCmd {

  IdKey<Long, Object> add(Analysis analysis);

  void update(Analysis analysis);

  IdKey<Long, Object> replace(Analysis analysis);

  void refresh(Long id);

  void delete(Collection<Long> ids);

}
