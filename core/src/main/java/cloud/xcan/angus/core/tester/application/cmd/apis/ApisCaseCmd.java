package cloud.xcan.angus.core.tester.application.cmd.apis;

import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ApisCaseCmd {

  List<IdKey<Long, Object>> add(List<ApisCase> cases);

  void add(Long apisId, List<ApisCase> cases);

  void update(List<ApisCase> cases);

  void replace(List<ApisCase> cases);

  void rename(Long id, String name);

  void enabled(Set<Long> ids, Boolean enabled);

  void syncToScript(Long apisId);

  List<IdKey<Long, Object>> clone(Set<Long> ids);

  void delete(Collection<Long> ids);

  void deleteByApisIdIn(List<Long> apiIds);

}




