package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCase;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ApisCaseCmd {

  List<IdKey<Long, Object>> add(List<ApisCase> cases);

  void addCases(Long apisId, List<ApisCase> cases);

  void update(List<ApisCase> cases);

  void replace(List<ApisCase> cases);

  void rename(Long id, String name);

  void enabled(Set<Long> ids, Boolean enabled);

  void syncToScript(Long apisId);

  List<IdKey<Long, Object>> clone(Set<Long> ids);

  void delete(Collection<Long> ids);

  void deleteByApisIdIn(List<Long> apiIds);

}




