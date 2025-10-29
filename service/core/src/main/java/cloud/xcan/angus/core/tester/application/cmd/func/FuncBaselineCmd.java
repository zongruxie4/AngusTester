package cloud.xcan.angus.core.tester.application.cmd.func;

import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaseline;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;

public interface FuncBaselineCmd {

  IdKey<Long, Object> add(FuncBaseline baseline);

  void update(FuncBaseline baseline);

  void update0(FuncBaseline baseline);

  IdKey<Long, Object> replace(FuncBaseline baseline);

  void establish(Long id);

  void delete(Collection<Long> ids);

}
