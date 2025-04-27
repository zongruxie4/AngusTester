package cloud.xcan.angus.core.tester.application.query.tag.impl;

import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterTag;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.domain.ResourceTagAssoc;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.domain.tag.TagRepo;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.tag.TagTargetRepo;
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
import org.springframework.data.domain.Pageable;

@Biz
public class TagQueryImpl implements TagQuery {

  @Resource
  private TagRepo tagRepo;

  @Resource
  private TagTargetRepo tagTargetRepo;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Override
  public Tag detail(Long id) {
    return new BizTemplate<Tag>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Tag process() {
        Tag tag = checkAndFind(id);
        tag.setHasEditPermission(tag.getHasEditPermission());
        return tag;
      }
    }.execute();
  }

  @Override
  public Page<Tag> find(GenericSpecification<Tag> spec, Pageable pageable) {
    return new BizTemplate<Page<Tag>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Tag> process() {
        Page<Tag> page = tagRepo.findAll(spec, pageable);
        setEditPermission(spec.getCriteria(), page.getContent());
        return page;
      }
    }.execute();
  }

  @Override
  public void checkExists(List<TagTarget> tags) {
    if (ObjectUtils.isEmpty(tags)) {
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

  @Override
  public Tag checkAndFind(Long tagId) {
    return tagRepo.findById(tagId).orElseThrow(() -> ResourceNotFound.of(tagId, "Report"));
  }

  @Override
  public List<Tag> checkAndFind(Collection<Long> tagIds) {
    List<Tag> existedTags = null;
    if (isNotEmpty(tagIds)) {
      existedTags = tagRepo.findByIdIn(tagIds);
      if (tagIds.size() == existedTags.size()) {
        return existedTags;
      }
      tagIds.removeAll(existedTags.stream().map(Tag::getId).collect(Collectors.toList()));
      assertResourceNotFound(isEmpty(tagIds), tagIds, "Report");
    }
    return existedTags;
  }

  @Override
  public Map<String, List<Tag>> checkAndFindByName(Long projectId, Set<String> names) {
    if (ObjectUtils.isEmpty(names)) {
      return emptyMap();
    }
    List<Tag> casesDb = tagRepo.findByProjectIdAndNameIn(projectId, names);
    if (ObjectUtils.isEmpty(casesDb)) {
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

  @Override
  public void checkAddNameExist(Long projectId, Set<String> names) {
    List<Tag> funcCaseTagsDb = tagRepo.findByProjectIdAndNameIn(projectId, names);
    if (isNotEmpty(funcCaseTagsDb)) {
      throw ResourceExisted.of(funcCaseTagsDb.get(0).getName(), "Report");
    }
  }

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

  @Override
  public void checkQuota(int incr) {
    long count = tagRepo.count();
    settingTenantQuotaManager.checkTenantQuota(AngusTesterTag, null, incr + count);
  }

  @Override
  public boolean hasTag(Long caseId) {
    return tagTargetRepo.countAllByTargetId(caseId) > 0;
  }

  @Override
  public boolean hasModifyTag(Long id, List<TagTarget> tagTargets) {
    Set<Long> caseTagIdsDb = tagTargetRepo.findTagIdByTargetId(id);
    // Clear tagTargets in db
    if (ObjectUtils.isEmpty(tagTargets)) {
      return !caseTagIdsDb.isEmpty();
    }
    // Change tagTargets
    Set<Long> tagsIds = tagTargets.stream().map(TagTarget::getTagId)
        .collect(Collectors.toSet());

    assertNotEmpty(tagsIds, "tagsIds is required");
    if (tagsIds.size() != caseTagIdsDb.size()) {
      return true;
    }

    tagsIds.removeAll(caseTagIdsDb);
    return !tagsIds.isEmpty();
  }

  @Override
  public void setEditPermission(Set<SearchCriteria> criteria, List<Tag> tags) {
    if (ObjectUtils.isEmpty(tags)) {
      return;
    }
    String projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
    boolean hasEditPermission = projectQuery.hasEditPermission(Long.parseLong(projectId));
    tags.forEach(module -> module.setHasEditPermission(hasEditPermission));
  }

  /**
   * Set case tags
   */
  @Override
  public void setTags(List<? extends ResourceTagAssoc<?, ?>> ress) {
    if (isNotEmpty(ress)) {
      Map<Long, List<TagTarget>> taskTagMap = tagTargetRepo.findAllByTargetIdIn(
              ress.stream().map(ResourceTagAssoc::getId).collect(Collectors.toList()))
          .stream().collect(groupingBy(TagTarget::getTargetId));
      if (ObjectUtils.isEmpty(taskTagMap)) {
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
