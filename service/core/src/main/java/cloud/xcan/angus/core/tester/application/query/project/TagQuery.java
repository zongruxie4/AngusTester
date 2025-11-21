package cloud.xcan.angus.core.tester.application.query.project;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.ResourceTagAssoc;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TagQuery {

  Tag detail(Long id);

  Page<Tag> list(GenericSpecification<Tag> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  void checkExists(List<TagTarget> tags);

  Tag checkAndFind(Long tagId);

  List<Tag> checkAndFind(Collection<Long> tagIds);

  Map<String, List<Tag>> checkAndFindByName(Long projectId, Set<String> tags);

  Set<Long> getTargetIdsById(Set<Long> tagIds);

  void checkAddNameExist(Long projectId, Set<String> names);

  void checkUpdateNameExists(Long projectId, Collection<Tag> tags);

  void checkQuota(int incr);

  boolean hasTag(Long caseId);

  boolean hasModifyTag(Long id, List<TagTarget> tagTargets);

  void setEditPermission(Set<SearchCriteria> criteria, List<Tag> tags);

  void setTags(List<? extends ResourceTagAssoc<?, ?>> ress);

}
