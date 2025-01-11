package cloud.xcan.sdf.core.angustester.application.query.tag;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.ResourceTagAssoc;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTarget;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagQuery {

  Tag detail(Long id);

  Page<Tag> find(GenericSpecification<Tag> spec, Pageable pageable);

  void checkExists(List<TagTarget> tags);

  Tag checkAndFind(Long tagId);

  List<Tag> checkAndFind(Collection<Long> tagIds);

  Map<String, List<Tag>> checkAndFindByName(Long projectId, Set<String> tags);

  void checkAddNameExist(Long projectId, Set<String> names);

  void checkUpdateNameExists(Long projectId, Collection<Tag> tags);

  void checkQuota(int incr);

  boolean hasTag(Long caseId);

  boolean hasModifyTag(Long id, List<TagTarget> tagTargets);

  void setEditPermissionFlag(Set<SearchCriteria> criteria, List<Tag> tags);

  void setTags(List<? extends ResourceTagAssoc<?, ?>> ress);
}
