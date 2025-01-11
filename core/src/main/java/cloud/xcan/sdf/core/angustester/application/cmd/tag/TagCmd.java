package cloud.xcan.sdf.core.angustester.application.cmd.tag;

import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TagCmd {

  List<IdKey<Long, Object>> add(Long projectId, Set<String> names);

  void update(List<Tag> tags);

  void delete(Collection<Long> ids);
}
