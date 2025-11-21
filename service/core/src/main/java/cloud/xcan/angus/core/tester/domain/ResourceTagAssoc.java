package cloud.xcan.angus.core.tester.domain;

import cloud.xcan.angus.core.tester.domain.project.tag.TagTarget;
import cloud.xcan.angus.spec.experimental.Entity;
import java.util.List;

public interface ResourceTagAssoc<T extends Entity<T, ID>, ID> {

  Long getId();

  List<TagTarget> getTagTargets();

  T setTagTargets(List<TagTarget> tags);

}
