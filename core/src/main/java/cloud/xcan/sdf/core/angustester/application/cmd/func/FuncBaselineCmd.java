package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaseline;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface FuncBaselineCmd {

  IdKey<Long, Object> add(FuncBaseline baseline);

  void update(FuncBaseline baseline);

  void update0(FuncBaseline baseline);

  IdKey<Long, Object> replace(FuncBaseline baseline);

  void establish(Long id);

  void delete(Collection<Long> ids);

}
