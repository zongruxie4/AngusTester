package cloud.xcan.angus.core.tester.application.query.tag;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.ResourceTagAssoc;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.remote.search.SearchCriteria;
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

  void setEditPermission(Set<SearchCriteria> criteria, List<Tag> tags);

  void setTags(List<? extends ResourceTagAssoc<?, ?>> ress);
}
