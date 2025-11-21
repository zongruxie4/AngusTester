package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TAG;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_TAG_FILE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.CREATED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.UPDATED;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.TagCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.TagQuery;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.tag.Tag;
import cloud.xcan.angus.core.tester.domain.project.tag.TagRepo;
import cloud.xcan.angus.core.tester.domain.project.tag.TagTargetRepo;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.spec.experimental.IdKey;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of tag command operations for tag management.
 *
 * <p>This class provides comprehensive functionality for managing tags,
 * including creation, updates, deletion, and example import operations.</p>
 *
 * <p>It handles the complete lifecycle of tag management from creation
 * to deletion, including permission validation, quota management,
 * and activity logging for audit purposes.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Tag CRUD operations with comprehensive validation</li>
 *   <li>Bulk tag operations with project-level constraints</li>
 *   <li>Example tag import functionality</li>
 *   <li>Permission and quota management</li>
 *   <li>Activity logging for audit trails</li>
 * </ul></p>
 */
@Biz
public class TagCmdImpl extends CommCmd<Tag, Long> implements TagCmd {

  @Resource
  private TagRepo tagRepo;
  @Resource
  private TagTargetRepo tagTargetRepo;
  @Resource
  private TagQuery tagQuery;
  @Resource
  private ProjectQuery projectQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds multiple tags to a project with comprehensive validation.
   *
   * <p>This method creates multiple tags for a project with proper
   * permission validation, name uniqueness checks, and quota management.</p>
   *
   * <p>The method logs tag creation activities for audit purposes.</p>
   *
   * @param projectId the ID of the project to add tags to
   * @param names the set of tag names to create
   * @return list of ID keys for the created tags
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(Long projectId, Set<String> names) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // Verify project exists and user has edit permissions
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);
        // Verify tag names don't already exist
        tagQuery.checkAddNameExist(projectId, names);
        // Verify quota limits are not exceeded
        tagQuery.checkQuota(names.size());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<Tag> tags = names.stream()
            .map(name -> new Tag().setProjectId(projectId).setName(name))
            .toList();
        List<IdKey<Long, Object>> idKeys = batchInsert(tags, "name");

        activityCmd.addAll(toActivities(TAG, tags, CREATED, activityParams(tags)));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Updates multiple tags with comprehensive validation.
   *
   * <p>This method updates multiple tags with proper permission validation
   * and name uniqueness checks. All tags must belong to the same project.</p>
   *
   * <p>The method logs tag update activities for audit purposes.</p>
   *
   * @param tags the list of tags to update
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<Tag> tags) {
    new BizTemplate<Void>() {
      List<Tag> tagsDb;

      @Override
      protected void checkParams() {
        // Verify tags exist and retrieve them
        tagsDb = tagQuery.checkAndFind(tags.stream().map(Tag::getId).toList());
        Set<Long> projectIds = tagsDb.stream().map(Tag::getProjectId)
            .collect(Collectors.toSet());
        // Verify all tags belong to the same project
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only batch adding tags with one project is allowed");
        Long projectId = projectIds.iterator().next();

        // Verify user has edit permissions for the project
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);

        // Verify tag names don't conflict with existing tags
        tagQuery.checkUpdateNameExists(projectId, tags);
      }

      @Override
      protected Void process() {
        batchUpdate0(CoreUtils.batchCopyPropertiesIgnoreNull(tags, tagsDb));

        activityCmd.addAll(toActivities(TAG, tagsDb, UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Imports example tags into a project.
   *
   * <p>This method imports predefined example tags from sample files
   * to help users get started with the system.</p>
   *
   * <p>Note: When API calls are not user-initiated, tenant and user information
   * must be injected into the PrincipalContext for proper authorization.</p>
   *
   * @param projectId the ID of the project to import examples into
   * @return list of ID keys for the imported tags
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {

      @Override
      protected List<IdKey<Long, Object>> process() {
        URL resourceUrl = this.getClass().getResource("/samples/tag/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_TAG_FILE);
        List<Tag> tags = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<List<Tag>>() {
            }, SAMPLE_TAG_FILE);
        for (Tag tag : tags) {
          tag.setProjectId(projectId);
        }
        return batchInsert(tags, "name");
      }
    }.execute();
  }

  /**
   * Deletes multiple tags with comprehensive cleanup.
   *
   * <p>This method deletes multiple tags and their associated relationships.
   * All tags must belong to the same project for batch deletion.</p>
   *
   * <p>The method logs tag deletion activities for audit purposes.</p>
   *
   * @param ids the collection of tag IDs to delete
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<Tag> tagsDb;

      @Override
      protected void checkParams() {
        // Verify tags exist and retrieve them
        tagsDb = tagQuery.checkAndFind(ids);

        // Verify all tags belong to the same project
        Set<Long> projectIds = tagsDb.stream().map(Tag::getProjectId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only batch adding modules with one project is allowed");
        Long projectId = projectIds.iterator().next();

        // Verify user has edit permissions for the project
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);
      }

      @Override
      protected Void process() {
        // Delete tag relationships first
        tagTargetRepo.deleteByTagIdIn(ids);

        // Delete the tags themselves
        tagRepo.deleteByIdIn(ids);

        // Log tag deletion activities
        activityCmd.addAll(toActivities(TAG, tagsDb, DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the tag repository
   */
  @Override
  protected BaseRepository<Tag, Long> getRepository() {
    return this.tagRepo;
  }
}
