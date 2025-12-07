package cloud.xcan.angus.core.tester.application.cmd.test;

import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.spec.experimental.IdKey;
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

  void add0(List<FuncPlan> funcPlans);

}
