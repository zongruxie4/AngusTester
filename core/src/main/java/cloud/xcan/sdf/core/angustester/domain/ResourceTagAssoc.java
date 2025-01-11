package cloud.xcan.sdf.core.angustester.domain;

import cloud.xcan.sdf.core.angustester.domain.tag.TagTarget;
import cloud.xcan.sdf.spec.experimental.Entity;
import java.util.List;

public interface ResourceTagAssoc<T extends Entity<T, ID>, ID> {

  Long getId();

  List<TagTarget> getTagTargets();

  T setTagTargets(List<TagTarget> tags);

}
