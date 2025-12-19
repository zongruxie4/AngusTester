package cloud.xcan.angus.core.tester.application.query.project.impl;

import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterTag;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;

import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.TagQuery;
import cloud.xcan.angus.core.tester.domain.ResourceTagAssoc;
import cloud.xcan.angus.core.tester.domain.project.tag.Tag;
import cloud.xcan.angus.core.tester.domain.project.tag.TagRepo;
import cloud.xcan.angus.core.tester.domain.project.tag.TagSearchRepo;
import cloud.xcan.angus.core.tester.domain.project.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.project.tag.TagTargetRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Implementation of TagQuery for tag management and query operations.
 * </p>
 * <p>
 * Provides methods for tag CRUD operations, tag association management, and permission validation.
 * </p>
 */
@Service
public class TagQueryImpl implements TagQuery {

  @Resource
  private TagRepo tagRepo;

  @Resource
  private TagSearchRepo tagSearchRepo;

  @Resource
  private TagTargetRepo tagTargetRepo;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  /**
   * <p>
   * Get detailed information of a tag.
   * </p>
   *
   * @param id Tag ID
   * @return Tag entity with edit permission information
   */
  @Override
  public Tag detail(Long id) {
    return new BizTemplate<Tag>() {

      @Override
      protected Tag process() {
        Tag tag = checkAndFind(id);
        tag.setHasEditPermission(tag.getHasEditPermission());
        return tag;
      }
    }.execute();
  }

  /**
   * <p>
   * List tags with pagination and optional full-text search.
   * </p>
   * <p>
   * Supports both regular database queries and full-text search operations. Sets edit permissions
   * for all returned tags based on project permissions.
   * </p>
   *
   * @param spec           Search specification
   * @param pageable       Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match          Search match parameters
   * @return Page of tags
   */
  @Override
  public Page<Tag> list(GenericSpecification<Tag> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<Tag>>() {
      @Override
      protected Page<Tag> process() {
        Page<Tag> page = fullTextSearch
            ? tagSearchRepo.find(spec.getCriteria(), pageable, Tag.class, match)
            : tagRepo.findAll(spec, pageable);
        setEditPermission(spec.getCriteria(), page.getContent());
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Check if all specified tag targets exist in the database.
   * </p>
   * <p>
   * Validates that all tag IDs in the tag targets correspond to existing tags. Throws
   * ResourceNotFound if any tag does not exist.
   * </p>
   *
   * @param tags List of tag targets to validate
   */
  @Override
  public void checkExists(List<TagTarget> tags) {
    if (isEmpty(tags)) {
      return;
    }
    List<Long> tagsIds = tags.stream().map(TagTarget::getTagId)
        .collect(Collectors.toList());
    List<Long> existedTagsIds = tagRepo.findIdByIdIn(tagsIds);
    if (tagsIds.size() == existedTagsIds.size()) {
      return;
    }
    if (isNotEmpty(existedTagsIds)) {
      tagsIds.removeAll(existedTagsIds);
    }
    if (isNotEmpty(tagsIds)) {
      throw ResourceNotFound.of(tagsIds.get(0).toString(), "Report");
    }
  }

  /**
   * <p>
   * Check and find a tag by ID.
   * </p>
   *
   * @param tagId Tag ID
   * @return Tag entity
   */
  @Override
  public Tag checkAndFind(Long tagId) {
    return tagRepo.findById(tagId).orElseThrow(() -> ResourceNotFound.of(tagId, "Report"));
  }

  /**
   * <p>
   * Check and find multiple tags by IDs.
   * </p>
   * <p>
   * Validates that all specified tag IDs exist. Throws ResourceNotFound if any tag is missing.
   * </p>
   *
   * @param tagIds Collection of tag IDs
   * @return List of tag entities
   */
  @Override
  public List<Tag> checkAndFind(Collection<Long> tagIds) {
    List<Tag> existedTags = null;
    if (isNotEmpty(tagIds)) {
      existedTags = tagRepo.findByIdIn(tagIds);
      if (tagIds.size() == existedTags.size()) {
        return existedTags;
      }
      // tagIds.removeAll(existedTags.stream().map(Tag::getId).toList()) -> Fix: UnsupportedOperationException
      tagIds.removeAll(existedTags.stream().map(Tag::getId).collect(Collectors.toList()));
      assertResourceNotFound(isEmpty(tagIds), tagIds, "Report");
    }
    return existedTags;
  }

  /**
   * <p>
   * Check and find tags by names within a project.
   * </p>
   * <p>
   * Validates that all specified tag names exist in the project. Returns a map grouped by tag name.
   * Throws ResourceNotFound if any tag name is missing.
   * </p>
   *
   * @param projectId Project ID
   * @param names     Set of tag names
   * @return Map of tag name to list of tags
   */
  @Override
  public Map<String, List<Tag>> checkAndFindByName(Long projectId, Set<String> names) {
    if (isEmpty(names)) {
      return emptyMap();
    }
    List<Tag> casesDb = tagRepo.findByProjectIdAndNameIn(projectId, names);
    if (isEmpty(casesDb)) {
      throw ResourceNotFound.of(names.iterator().next(), "CaseTag");
    }
    if (names.size() != casesDb.size()) {
      Collection<String> namesDb = casesDb.stream()
          .map(Tag::getName).collect(Collectors.toSet());
      names.removeAll(namesDb);
      throw ResourceNotFound.of(names.iterator().next(), "CaseTag");
    }
    return casesDb.stream().collect(Collectors.groupingBy(Tag::getName));
  }

  /**
   * Get associated target ids.
   *
   * @param tagIds Tag ids
   * @return Target ids
   */
  @Override
  public Set<Long> getTargetIdsById(Set<Long> tagIds) {
    return tagTargetRepo.getTargetIdsByTagIdIn(tagIds);
  }

  /**
   * <p>
   * Check if tag names already exist in a project when adding new tags.
   * </p>
   *
   * @param projectId Project ID
   * @param names     Set of tag names to check
   */
  @Override
  public void checkAddNameExist(Long projectId, Set<String> names) {
    List<Tag> funcCaseTagsDb = tagRepo.findByProjectIdAndNameIn(projectId, names);
    if (isNotEmpty(funcCaseTagsDb)) {
      throw ResourceExisted.of(funcCaseTagsDb.get(0).getName(), "Report");
    }
  }

  /**
   * <p>
   * Check if tag names already exist in a project when updating tags.
   * </p>
   * <p>
   * Validates that updated tag names do not conflict with existing tags in the same project.
   * </p>
   *
   * @param projectId Project ID
   * @param tags      Collection of tags to update
   */
  @Override
  public void checkUpdateNameExists(Long projectId, Collection<Tag> tags) {
    List<Tag> tagsDb = tagRepo.findByProjectIdAndNameIn(projectId,
        tags.stream().map(Tag::getName).collect(Collectors.toList()));
    if (isEmpty(tagsDb)) {
      return;
    }
    for (Tag tag : tags) {
      for (Tag tagDb : tagsDb) {
        if (tag.getName().equalsIgnoreCase(tagDb.getName()) && !tag.getId().equals(tagDb.getId())) {
          throw ResourceExisted.of(tagDb.getName(), "Report");
        }
      }
    }
  }

  /**
   * <p>
   * Check tenant quota for tag creation.
   * </p>
   *
   * @param incr Number of tags to be added
   */
  @Override
  public void checkQuota(int incr) {
    long count = tagRepo.count();
    settingTenantQuotaManager.checkTenantQuota(AngusTesterTag, null, incr + count);
  }

  /**
   * <p>
   * Check if a resource has any tags associated with it.
   * </p>
   *
   * @param caseId Resource ID
   * @return true if the resource has tags, false otherwise
   */
  @Override
  public boolean hasTag(Long caseId) {
    return tagTargetRepo.countAllByTargetId(caseId) > 0;
  }

  /**
   * <p>
   * Check if tag modifications are needed for a resource.
   * </p>
   * <p>
   * Compares existing tags with new tag targets to determine if modifications are required. Returns
   * true if tags need to be modified, false if no changes are needed.
   * </p>
   *
   * @param id         Resource ID
   * @param tagTargets New tag targets to compare
   * @return true if modifications are needed, false otherwise
   */
  @Override
  public boolean hasModifyTag(Long id, List<TagTarget> tagTargets) {
    Set<Long> caseTagIdsDb = tagTargetRepo.findTagIdByTargetId(id);
    // Clear tagTargets in db
    if (isEmpty(tagTargets)) {
      return !caseTagIdsDb.isEmpty();
    }
    // Change tagTargets
    Set<Long> tagsIds = tagTargets.stream().map(TagTarget::getTagId).collect(Collectors.toSet());

    assertNotEmpty(tagsIds, "tagsIds is required");
    if (tagsIds.size() != caseTagIdsDb.size()) {
      return true;
    }

    tagsIds.removeAll(caseTagIdsDb);
    return !tagsIds.isEmpty();
  }

  /**
   * <p>
   * Set edit permissions for tags based on project permissions.
   * </p>
   * <p>
   * Extracts project ID from search criteria and sets edit permission for all tags based on whether
   * the user has edit permission for the project.
   * </p>
   *
   * @param criteria Search criteria containing project ID
   * @param tags     List of tags to set permissions for
   */
  @Override
  public void setEditPermission(Set<SearchCriteria> criteria, List<Tag> tags) {
    if (isEmpty(tags)) {
      return;
    }
    String projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
    assert projectId != null;
    boolean hasEditPermission = projectQuery.hasEditPermission(Long.parseLong(projectId));
    tags.forEach(module -> module.setHasEditPermission(hasEditPermission));
  }

  /**
   * <p>
   * Set tags for a list of resources.
   * </p>
   * <p>
   * Efficiently loads and sets tag information for multiple resources to avoid N+1 query problems.
   * Groups tag targets by resource ID and sets tag names for each target.
   * </p>
   *
   * @param ress List of resources that can be associated with tags
   */
  @Override
  public void setTags(List<? extends ResourceTagAssoc<?, ?>> ress) {
    if (isNotEmpty(ress)) {
      Map<Long, List<TagTarget>> taskTagMap = tagTargetRepo.findAllByTargetIdIn(
              ress.stream().map(ResourceTagAssoc::getId).collect(Collectors.toList()))
          .stream().collect(groupingBy(TagTarget::getTargetId));
      if (isEmpty(taskTagMap)) {
        return;
      }
      ress.forEach(x -> x.setTagTargets((taskTagMap.get(x.getId()))));
      List<Tag> tags = tagRepo.findByIdIn(
          taskTagMap.values().stream().flatMap(Collection::stream).map(TagTarget::getTagId)
              .collect(Collectors.toList()));
      if (isNotEmpty(tags)) {
        Map<Long, Tag> tagMaps = tags.stream().collect((Collectors.toMap(Tag::getId, x -> x)));
        ress.stream().map(ResourceTagAssoc::getTagTargets)
            .filter(ObjectUtils::isNotEmpty).flatMap(Collection::stream)
            .forEach(x -> x.setTagName(tagMaps.get(x.getTagId()).getName()));
      }
    }
  }
}
