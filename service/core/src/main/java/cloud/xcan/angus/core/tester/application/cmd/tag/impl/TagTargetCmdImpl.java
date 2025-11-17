package cloud.xcan.angus.core.tester.application.cmd.tag.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.TagTargetConverter.toCaseTagTargets;
import static cloud.xcan.angus.core.tester.application.converter.TagTargetConverter.toUpdateCaseTag;
import static cloud.xcan.angus.core.tester.application.converter.TagTargetConverter.toUpdateTaskTag;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.tag.TagTargetCmd;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.activity.SimpleTaskActivityResource;
import cloud.xcan.angus.core.tester.domain.issue.Task;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.tag.TagTargetRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.utils.CoreUtils;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of tag target command operations for tag association management.
 *
 * <p>This class provides comprehensive functionality for managing tag associations
 * with various targets including tasks, functional cases, and other entities.</p>
 *
 * <p>It handles the complete lifecycle of tag associations from creation
 * to deletion, including bulk operations, relationship management,
 * and activity logging for audit purposes.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Tag-target association management</li>
 *   <li>Bulk tag operations for tasks and cases</li>
 *   <li>Tag replacement and cleanup operations</li>
 *   <li>Activity logging for audit trails</li>
 *   <li>External business integration support</li>
 * </ul></p>
 */
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

  /**
   * Adds tag targets without validation (internal use).
   *
   * <p>This method performs bulk insertion of tag targets without
   * validation checks. It is intended for internal use only.</p>
   *
   * @param tags the list of tag targets to add
   */
  @Override
  public void add0(List<TagTarget> tags) {
    batchInsert0(tags);
  }

  /**
   * Replaces task tag associations with comprehensive processing.
   *
   * <p>This method performs complete replacement of tag associations for tasks,
   * including adding new associations and removing unspecified ones.</p>
   *
   * <p>External calling business logic must ensure data authorization and security.</p>
   *
   * @param tags the list of tag targets to replace
   * @throws IllegalArgumentException if validation fails
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTaskTarget(List<TagTarget> tags) {
    assertNotEmpty(tags, "tags is required");

    // Verify tags exist and retrieve them
    List<Tag> tagDbs = tagQuery.checkAndFind(tags.stream()
        .map(TagTarget::getTagId).toList());

    // TODO: Add maximum tag limit validation if needed

    // Retrieve existing tag targets
    List<TagTarget> tagTargetDbs = tagTargetRepo
        .findAllByTargetIdIn(tags.stream().map(TagTarget::getTargetId).collect(Collectors.toSet()));

    // Identify new tags to add
    List<TagTarget> newAddTags = new ArrayList<>(tags);
    if (isNotEmpty(tagTargetDbs)) {
      CoreUtils.removeAll(newAddTags, tagTargetDbs);

      // Identify tags to be deleted (unspecified tags are removed)
      List<TagTarget> deletedTags = deleteUnsetTags(tags, tagTargetDbs);
      if (isNotEmpty(deletedTags)) {
        tagTargetRepo.deleteAll(deletedTags);
      }
    }

    if (isNotEmpty(newAddTags)) {
      // Add new tag associations
      batchInsert0(newAddTags);
    }

    // Add tags activity
    activityCmd.addAll(toActivities(TASK,
        tags.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                // Deduplicate by taskId
                new TreeSet<>(Comparator.comparing(TagTarget::getTargetId))), ArrayList::new))
            .stream().map(a -> new SimpleTaskActivityResource()
                .setId(a.getTargetId()).setTaskId(a.getTargetId()).setName(a.getTargetName())
            ).toList(),
        ActivityType.TAG, tagDbs.stream().map(Tag::getName).collect(Collectors.joining(","))));
  }

  /**
   * Replaces task tag associations without activity logging.
   *
   * <p>This method performs complete replacement of tag associations for tasks
   * without logging activities. It is intended for internal use.</p>
   *
   * <p>External calling business logic must ensure data authorization and security.</p>
   *
   * @param tags the list of tag targets to replace
   * @throws IllegalArgumentException if validation fails
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTaskTarget0(List<TagTarget> tags) {
    assertNotEmpty(tags, "tags is required");

    // TODO: Add maximum tag limit validation if needed

    // Retrieve existing tag targets
    List<TagTarget> tagDbs = tagTargetRepo.findAllByTargetIdIn(tags.stream()
        .map(TagTarget::getTargetId).collect(Collectors.toSet()));

    // Identify new tags to add
    List<TagTarget> newAddTags = new ArrayList<>(tags);
    if (isNotEmpty(tagDbs)) {
      newAddTags.removeAll(tagDbs);

      // Identify tags to be deleted (unspecified tags are removed)
      List<TagTarget> deletedTags = deleteUnsetTags(tags, tagDbs);
      if (isNotEmpty(deletedTags)) {
        tagTargetRepo.deleteAll(deletedTags);
      }
    }

    if (isNotEmpty(newAddTags)) {
      // Add new tag associations
      batchInsert0(newAddTags);
    }
  }

  /**
   * Replaces tags for a specific task with validation.
   *
   * <p>This method replaces all tags associated with a task. If no tag IDs
   * are provided, all tags are cleared from the task.</p>
   *
   * <p>The method logs tag replacement and clearing activities for audit purposes.</p>
   *
   * @param taskId the ID of the task
   * @param tagIds the set of tag IDs to associate with the task (can be empty to clear all)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTaskTags(Long taskId, Set<Long> tagIds) {
    // Verify task exists and retrieve task info
    TaskInfo taskDb = taskQuery.checkAndFindInfo(taskId);

    // Handle tag clearing (when no tag IDs provided)
    if (isEmpty(tagIds)) {
      if (tagQuery.hasTag(taskId)) {
        delete0ByTaskIds(Collections.singleton(taskId));
        // Log tag clearing activity
        activityCmd.add(toActivity(TASK, taskDb, ActivityType.TAG_CLEAR));
      }
      return;
    }

    // Replace tags with new associations
    if (isNotEmpty(tagIds)) {
      replaceTaskTarget(toUpdateTaskTag(tagIds, taskDb));
    }
  }

  /**
   * Replaces task tags without validation (internal use).
   *
   * <p>This method performs tag replacement for tasks without validation
   * and activity logging. It is intended for internal use only.</p>
   *
   * <p>Important: Update operations should not call this method.
   * External calling business logic must ensure data authorization and security.</p>
   *
   * @param taskDb the task entity
   * @param tagTargets the list of tag targets to associate
   * @throws IllegalArgumentException if validation fails
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceTaskTags0(Task taskDb, List<TagTarget> tagTargets) {
    assertNotNull(taskDb, "taskDb is required");

    // Clear existing tags first
    delete0ByTaskIds(Collections.singleton(taskDb.getId()));

    // Add new tag associations if provided
    if (isNotEmpty(tagTargets)) {
      replaceTaskTarget0(toUpdateTaskTag(tagTargets.stream().map(TagTarget::getTagId)
          .collect(Collectors.toSet()), taskDb));
    }
  }

  /**
   * Deletes tag associations for multiple tasks (internal use).
   *
   * <p>This method removes all tag associations for the specified tasks
   * without validation or activity logging.</p>
   *
   * <p>External calling business logic must ensure data authorization and security.</p>
   *
   * @param taskIds the collection of task IDs to delete tag associations for
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0ByTaskIds(Collection<Long> taskIds) {
    tagTargetRepo.deleteByTargetIdIn(taskIds);
  }

  /**
   * Adds tag associations for functional cases.
   *
   * <p>This method creates tag associations for multiple functional cases
   * after validating that the tags exist.</p>
   *
   * @param cases the list of functional cases to add tags for
   */
  @Override
  public void addCase(List<FuncCase> cases) {
    List<TagTarget> tagTargets = getFuncCaseTagTargets(cases);
    if (isNotEmpty(tagTargets)) {
      tagQuery.checkAndFind(tagTargets.stream()
          .map(TagTarget::getTagId).toList());
      batchInsert0(tagTargets);
    }
  }

  /**
   * Updates tag associations for functional cases.
   *
   * <p>This method updates tag associations for multiple functional cases
   * after validating that the tags exist.</p>
   *
   * @param cases the list of functional cases to update tags for
   */
  @Override
  public void updateCase(List<FuncCase> cases) {
    List<TagTarget> tagTargets = getFuncCaseTagTargets(cases);
    if (isNotEmpty(tagTargets)) {
      tagQuery.checkAndFind(tagTargets.stream()
          .map(TagTarget::getTagId).toList());
      replace0(tagTargets);
    }
  }

  /**
   * Replaces tag associations for functional cases.
   *
   * <p>This method completely replaces tag associations for multiple
   * functional cases by first deleting existing associations
   * and then adding new ones.</p>
   *
   * @param cases the list of functional cases to replace tags for
   */
  @Override
  public void replaceCase(List<FuncCase> cases) {
    delete0ByCaseIds(cases.stream().map(FuncCase::getId).toList());
    addCase(cases);
  }

  /**
   * Replaces case tag associations with comprehensive processing.
   *
   * <p>This method performs complete replacement of tag associations for cases,
   * including adding new associations and removing unspecified ones.</p>
   *
   * <p>External calling business logic must ensure data authorization and security.</p>
   *
   * @param tags the list of tag targets to replace
   * @throws IllegalArgumentException if validation fails
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceCaseTargets(List<TagTarget> tags) {
    assertNotEmpty(tags, "tags is required");

    List<Tag> tagDbs = tagQuery.checkAndFind(
        tags.stream().map(TagTarget::getTagId).toList());

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
    activityCmd.addAll(toActivities(FUNC_CASE,
        tags.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                // Deduplicate by taskId
                new TreeSet<>(Comparator.comparing(TagTarget::getTargetId))), ArrayList::new))
            .stream().map(a -> new SimpleTaskActivityResource()
                .setId(a.getTargetId()).setTaskId(a.getTargetId()).setName(a.getTargetName())
            ).toList(),
        ActivityType.TAG, tagDbs.stream().map(Tag::getName)
            .collect(Collectors.joining(","))));
  }

  /**
   * Replaces tags for a specific functional case with validation.
   *
   * <p>This method replaces all tags associated with a functional case. If no tag IDs
   * are provided, all tags are cleared from the case.</p>
   *
   * <p>The method logs tag replacement and clearing activities for audit purposes.</p>
   *
   * @param caseId the ID of the functional case
   * @param tagIds the collection of tag IDs to associate with the case (can be empty to clear all)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceCaseTags(Long caseId, Collection<Long> tagIds) {
    // Verify functional case exists and retrieve case info
    FuncCaseInfo tagDb = funcCaseQuery.checkAndFindInfo(caseId);

    // Handle tag clearing (when no tag IDs provided)
    if (isEmpty(tagIds)) {
      if (tagQuery.hasTag(caseId)) {
        delete0ByCaseIds(Collections.singleton(caseId));
        // Log tag clearing activity
        activityCmd.add(toActivity(FUNC_CASE, tagDb, ActivityType.TAG_CLEAR));
      }
      return;
    }

    // Replace tags with new associations
    if (isNotEmpty(tagIds)) {
      replaceCaseTargets(toUpdateCaseTag(tagIds, tagDb));
    }
  }

  /**
   * Deletes tag associations for multiple functional cases (internal use).
   *
   * <p>This method removes all tag associations for the specified functional cases
   * without validation or activity logging.</p>
   *
   * <p>External calling business logic must ensure data authorization and security.</p>
   *
   * @param caseIds the collection of case IDs to delete tag associations for
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0ByCaseIds(Collection<Long> caseIds) {
    tagTargetRepo.deleteByTargetIdIn(caseIds);
  }

  /**
   * Replaces tag targets without activity logging (internal use).
   *
   * <p>This method performs complete replacement of tag targets without
   * validation or activity logging. It is intended for internal use.</p>
   *
   * <p>External calling business logic must ensure data authorization and security.</p>
   *
   * @param tagTargets the list of tag targets to replace
   * @throws IllegalArgumentException if validation fails
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
    if (isNotEmpty(tagDbs)) {
      newAddTags.removeAll(tagDbs);

      // Tags to be deleted
      // Fix: Unspecified tags are deleted for task
      List<TagTarget> deletedTags = deleteUnsetTags(tagTargets, tagDbs);
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
   * Deletes tag targets by tag IDs (internal use).
   *
   * <p>This method removes all tag targets associated with the specified tag IDs
   * without validation or activity logging.</p>
   *
   * <p>External calling business logic must ensure data authorization and security.</p>
   *
   * @param tagIds the collection of tag IDs to delete targets for
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0(Collection<Long> tagIds) {
    tagTargetRepo.deleteByTagIdIn(tagIds);
  }

  /**
   * Deletes tag targets by target IDs.
   *
   * <p>This method removes all tag targets associated with the specified target IDs.</p>
   *
   * @param targetIds the list of target IDs to delete tag associations for
   */
  @Override
  public void deleteByTargetIdIn(List<Long> targetIds) {
    tagTargetRepo.deleteByTargetIdIn(targetIds);
  }

  /**
   * Identifies tag targets to be deleted based on unspecified tags.
   *
   * <p>This method compares existing tag targets with new tag targets
   * and identifies which existing targets should be deleted because
   * their tags are not included in the new specification.</p>
   *
   * @param tags the new tag targets specification
   * @param tagDbs the existing tag targets in database
   * @return list of tag targets to be deleted
   */
  private List<TagTarget> deleteUnsetTags(List<TagTarget> tags, List<TagTarget> tagDbs) {
    List<TagTarget> deletedTags = new ArrayList<>();
    List<Long> tagIds = tags.stream().map(TagTarget::getTagId).toList();
    for (TagTarget tagDb : tagDbs) {
      if (!tagIds.contains(tagDb.getTagId())) {
        deletedTags.add(tagDb);
      }
    }
    return deletedTags;
  }

  /**
   * Converts functional cases to tag targets.
   *
   * <p>This method extracts tag associations from functional cases
   * and converts them to tag target entities for database operations.</p>
   *
   * @param cases the list of functional cases
   * @return list of tag targets for the cases
   */
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

  /**
   * Returns the repository instance for this command.
   *
   * @return the tag target repository
   */
  @Override
  protected BaseRepository<TagTarget, Long> getRepository() {
    return this.tagTargetRepo;
  }
}
