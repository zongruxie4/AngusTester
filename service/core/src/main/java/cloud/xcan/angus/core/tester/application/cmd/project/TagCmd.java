package cloud.xcan.angus.core.tester.application.cmd.project;

import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TagCmd {

  List<IdKey<Long, Object>> add(Long projectId, Set<String> names);

  void update(List<Tag> tags);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

}
