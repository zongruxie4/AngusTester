package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;

public interface FuncPlanCmd {

  IdKey<Long, Object> add(FuncPlan plan);

  void update(FuncPlan plan);

  IdKey<Long, Object> replace(FuncPlan plan);

  void start(Long id);

  void block(Long id);

  void end(Long id);

  IdKey<Long, Object> clone(Long id);

  void resultReset(HashSet<Long> ids);

  void reviewReset(HashSet<Long> ids);

  void delete(Long id);

  void delete0(List<Long> planIds);

}
