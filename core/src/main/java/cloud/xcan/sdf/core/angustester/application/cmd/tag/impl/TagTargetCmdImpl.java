package cloud.xcan.sdf.core.angustester.application.cmd.tag.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.TagTargetConverter.toCaseTagTargets;
import static cloud.xcan.sdf.core.angustester.application.converter.TagTargetConverter.toUpdateCaseTag;
import static cloud.xcan.sdf.core.angustester.application.converter.TagTargetConverter.toUpdateTaskTag;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.tag.TagTargetCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.activity.SimpleTaskActivityResource;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTarget;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTargetRepo;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class TagTargetCmdImpl extends CommCmd<TagTarget, Long> implements TagTargetCmd {

  @Resource
  private TagTargetRepo tagTargetRepo;

  @Resource
  private TagQuery tagQuery;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Override
  public void add0(List<TagTarget> tags) {
    batchInsert0(tags);
  }

  /**
   * External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTaskTarget(List<TagTarget> tags) {
    assertNotEmpty(tags, "tags is required");

    List<Tag> tagDbs = tagQuery.checkAndFind(tags.stream()
        .map(TagTarget::getTagId).collect(Collectors.toList()));

    // Check the maximum limit of tag? It is a full modification, and the parameter verification is enough

    List<TagTarget> tagTargetDbs = tagTargetRepo
        .findAllByTargetIdIn(tags.stream().map(TagTarget::getTargetId).collect(Collectors.toSet()));

    // Exclude existing assignees
    List<TagTarget> newAddTags = new ArrayList<>(tags);
    if (isNotEmpty(tagTargetDbs)) {
      CoreUtils.removeAll(newAddTags, tagTargetDbs);

      // Tags to be deleted
      // Fix: Unspecified tags are deleted for task
      List<TagTarget> deletedTags = deleteUnsetTags(tags, tagTargetDbs);
      if (isNotEmpty(deletedTags)) {
        tagTargetRepo.deleteAll(deletedTags);
      }
    }

    if (isNotEmpty(newAddTags)) {
      // Add tags
      batchInsert0(newAddTags);
    }

    // Add tags activity
    activityCmd.batchAdd(toActivities(TASK,
        tags.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                // Deduplicate by taskId
                new TreeSet<>(Comparator.comparing(TagTarget::getTargetId))), ArrayList::new))
            .stream().map(a -> new SimpleTaskActivityResource()
                .setId(a.getTargetId()).setTaskId(a.getTargetId()).setName(a.getTargetName())
            ).collect(Collectors.toList()),
        ActivityType.TAG, tagDbs.stream().map(Tag::getName).collect(Collectors.joining(","))));
  }

  /**
   * External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTaskTarget0(List<TagTarget> tags) {
    assertNotEmpty(tags, "tags is required");

    // Check the maximum limit of tag? It is a full modification, and the parameter verification is enough

    List<TagTarget> tagDbs = tagTargetRepo.findAllByTargetIdIn(tags.stream()
        .map(TagTarget::getTargetId).collect(Collectors.toSet()));

    // Exclude existing assignees
    List<TagTarget> newAddTags = new ArrayList<>(tags);
    if (isNotEmpty(tagDbs)) {
      newAddTags.removeAll(tagDbs);

      // Tags to be deleted
      // Fix: Unspecified tags are deleted for task
      List<TagTarget> deletedTags = deleteUnsetTags(tags, tagDbs);
      if (isNotEmpty(deletedTags)) {
        tagTargetRepo.deleteAll(deletedTags);
      }
    }

    if (isNotEmpty(newAddTags)) {
      // Add tags
      batchInsert0(newAddTags);
    }
  }

  /**
   * Note: Tags can be cleared.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTaskTags(Long taskId, Set<Long> tagIds) {
    // Check the test task exists
    TaskInfo taskDb = taskQuery.checkAndFindInfo(taskId);

    // Clear tags, do not log activity when database does not exist
    if (ObjectUtils.isEmpty(tagIds)) {
      if (tagQuery.hasTag(taskId)) {
        delete0ByTaskIds(Collections.singleton(taskId));
        // Add clear tags activity
        activityCmd.add(toActivity(TASK, taskDb, ActivityType.TAG_CLEAR));
      }
      return;
    }

    // Add or update tags
    if (isNotEmpty(tagIds)) {
      replaceTaskTarget(toUpdateTaskTag(tagIds, taskDb));
    }
  }

  /**
   * External calling biz must ensure data authed and secured!
   * <p>
   * Important:: Update operations should not call this method.
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTaskTags0(Task taskDb, List<TagTarget> tagTargets) {
    assertNotNull(taskDb, "taskDb is required");

    // Empty tags
    delete0ByTaskIds(Collections.singleton(taskDb.getId()));

    // Add or update tags
    if (isNotEmpty(tagTargets)) {
      replaceTaskTarget0(toUpdateTaskTag(tagTargets.stream().map(TagTarget::getTagId)
          .collect(Collectors.toSet()), taskDb));
    }
  }

  /**
   * External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0ByTaskIds(Collection<Long> taskIds) {
    tagTargetRepo.deleteByTargetIdIn(taskIds);
  }

  @Override
  public void addCase(List<FuncCase> cases) {
    List<TagTarget> tagTargets = getFuncCaseTagTargets(cases);
    if (ObjectUtils.isNotEmpty(tagTargets)) {
      tagQuery.checkAndFind(tagTargets.stream()
          .map(TagTarget::getTagId).collect(Collectors.toList()));
      batchInsert0(tagTargets);
    }
  }

  @Override
  public void updateCase(List<FuncCase> cases) {
    List<TagTarget> tagTargets = getFuncCaseTagTargets(cases);
    if (ObjectUtils.isNotEmpty(tagTargets)) {
      tagQuery.checkAndFind(tagTargets.stream()
          .map(TagTarget::getTagId).collect(Collectors.toList()));
      replace0(tagTargets);
    }
  }

  @Override
  public void replaceCase(List<FuncCase> cases) {
    delete0ByCaseIds(cases.stream().map(FuncCase::getId).collect(Collectors.toList()));
    addCase(cases);
  }

  /**
   * External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceCaseTargets(List<TagTarget> tags) {
    assertNotEmpty(tags, "tags is required");

    List<Tag> tagDbs = tagQuery.checkAndFind(
        tags.stream().map(TagTarget::getTagId).collect(Collectors.toList()));

    // Check the maximum limit of tag? It is a full modification, and the parameter verification is enough

    List<TagTarget> caseTagTargetDbs = tagTargetRepo.findAllByTargetIdIn(
        tags.stream().map(TagTarget::getTargetId).collect(Collectors.toSet()));

    // Exclude existing tags
    List<TagTarget> newAddTags = new ArrayList<>(tags);
    if (isNotEmpty(caseTagTargetDbs)) {
      CoreUtils.removeAll(newAddTags, caseTagTargetDbs);

      // Tags to be deleted
      // Fix: Unspecified tags are deleted for task
      List<TagTarget> deletedTags = deleteUnsetTags(tags, caseTagTargetDbs);
      if (isNotEmpty(deletedTags)) {
        tagTargetRepo.deleteAll(deletedTags);
      }
    }

    if (isNotEmpty(newAddTags)) {
      // Add tags
      batchInsert0(newAddTags);
    }

    // Add tags activity
    activityCmd.batchAdd(toActivities(FUNC_CASE,
        tags.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                // Deduplicate by taskId
                new TreeSet<>(Comparator.comparing(TagTarget::getTargetId))), ArrayList::new))
            .stream().map(a -> new SimpleTaskActivityResource()
                .setId(a.getTargetId()).setTaskId(a.getTargetId()).setName(a.getTargetName())
            ).collect(Collectors.toList()),
        ActivityType.TAG, tagDbs.stream().map(Tag::getName)
            .collect(Collectors.joining(","))));
  }

  /**
   * Note: Tags can be cleared.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceCaseTags(Long caseId, Collection<Long> tagIds) {
    // Check the test tag exists
    FuncCaseInfo tagDb = funcCaseQuery.checkAndFindInfo(caseId);

    // Clear tags, do not log activity when database does not exist
    if (ObjectUtils.isEmpty(tagIds)) {
      if (tagQuery.hasTag(caseId)) {
        delete0ByCaseIds(Collections.singleton(caseId));
        // Add clear tags activity
        activityCmd.add(toActivity(FUNC_CASE, tagDb, ActivityType.TAG_CLEAR));
      }
      return;
    }

    // Add or update tags
    if (isNotEmpty(tagIds)) {
      replaceCaseTargets(toUpdateCaseTag(tagIds, tagDb));
    }
  }

  /**
   * External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0ByCaseIds(Collection<Long> caseIds) {
    tagTargetRepo.deleteByTargetIdIn(caseIds);
  }

  /**
   * External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replace0(List<TagTarget> tagTargets) {
    assertNotEmpty(tagTargets, "tags is required");

    // Check the maximum limit of tag? It is a full modification, and the parameter verification is enough

    List<TagTarget> tagDbs = tagTargetRepo.findAllByTargetIdIn(
        tagTargets.stream().map(TagTarget::getTargetId).collect(Collectors.toSet()));

    // Exclude existing assignees
    List<TagTarget> newAddTags = new ArrayList<>(tagTargets);
    if (ObjectUtils.isNotEmpty(tagDbs)) {
      newAddTags.removeAll(tagDbs);

      // Tags to be deleted
      // Fix: Unspecified tags are deleted for task
      List<TagTarget> deletedTags = deleteUnsetTags(tagTargets, tagDbs);
      if (ObjectUtils.isNotEmpty(deletedTags)) {
        tagTargetRepo.deleteAll(deletedTags);
      }
    }

    if (ObjectUtils.isNotEmpty(newAddTags)) {
      // Add tags
      batchInsert0(newAddTags);
    }
  }

  /**
   * External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0(Collection<Long> tagIds) {
    tagTargetRepo.deleteByTagIdIn(tagIds);
  }

  @Override
  public void deleteByTargetIdIn(List<Long> targetIds) {
    tagTargetRepo.deleteByTargetIdIn(targetIds);
  }

  private List<TagTarget> deleteUnsetTags(List<TagTarget> tags, List<TagTarget> tagDbs) {
    List<TagTarget> deletedTags = new ArrayList<>();
    List<Long> tagIds = tags.stream().map(TagTarget::getTagId).collect(Collectors.toList());
    for (TagTarget tagDb : tagDbs) {
      if (!tagIds.contains(tagDb.getTagId())) {
        deletedTags.add(tagDb);
      }
    }
    return deletedTags;
  }

  public static List<TagTarget> getFuncCaseTagTargets(List<FuncCase> cases) {
    List<TagTarget> tagTargets = new ArrayList<>();
    for (FuncCase case0 : cases) {
      List<TagTarget> targets = toCaseTagTargets(case0.getId(), case0.getTagIds());
      if (isNotEmpty(targets)) {
        tagTargets.addAll(targets);
      }
    }
    return tagTargets;
  }

  @Override
  protected BaseRepository<TagTarget, Long> getRepository() {
    return this.tagTargetRepo;
  }
}
