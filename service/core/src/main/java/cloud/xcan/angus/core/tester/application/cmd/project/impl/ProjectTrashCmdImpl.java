package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.PROJECT;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonList;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.module.ModuleCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.tag.TagCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectTrashQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.project.ProjectRepo;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrashRepo;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for project trash management.
 * <p>
 * Provides methods for adding, clearing, restoring, and deleting project trash records.
 * <p>
 * Ensures permission checks, cascading operations, and batch processing with transaction management.
 */
@Biz
public class ProjectTrashCmdImpl extends CommCmd<ProjectTrash, Long> implements ProjectTrashCmd {

  @Resource
  private ProjectTrashRepo projectTrashRepo;
  @Resource
  private ProjectTrashQuery projectTrashQuery;
  @Resource
  private ProjectRepo projectRepo;
  @Resource
  private TagCmd tagCmd;
  @Resource
  private ModuleCmd moduleCmd;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add a batch of project trash records.
   * <p>
   * Batch inserts project trash records for persistence.
   */
  @Override
  public void add0(List<ProjectTrash> trashes) {
    batchInsert0(trashes);
  }

  /**
   * Clear a single project trash record and its associations.
   * <p>
   * Checks existence and permission, deletes trash and associated data.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clear(Long id) {
    new BizTemplate<Void>() {
      ProjectTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
        trashDb = projectTrashQuery.findMyTrashForBiz(id, "CLEAR");
      }

      @Override
      protected Void process() {
        // Delete trash
        projectTrashRepo.deleteById(id);

        deleteAssociation(singletonList(trashDb.getTargetId()));

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  /**
   * Clear all project trash records and their associations.
   * <p>
   * Deletes all trash and associated data for the current user or admin.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clearAll() {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<ProjectTrash> allTrashes = getAllTrashes();

        if (isEmpty(allTrashes)) {
          return null;
        }

        List<Long> projectIds = allTrashes.stream().map(ProjectTrash::getTargetId)
            .collect(Collectors.toList());

        // Delete all trash
        projectTrashRepo.deleteByTargetIdIn(projectIds);

        deleteAssociation(projectIds);

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  /**
   * Restore a single project from trash.
   * <p>
   * Checks existence and permission, restores project, and deletes trash record.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void back(Long id) {
    new BizTemplate<Void>() {
      ProjectTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
        trashDb = projectTrashQuery.findMyTrashForBiz(id, "BACK");
      }

      @Override
      protected Void process() {
        // Recovery project
        List<Long> projectIds = List.of(trashDb.getTargetId());
        projectRepo.updateToUndeletedStatusByIdIn(projectIds);

        // Delete trash
        projectTrashRepo.deleteById(id);

        // Add back project activity
        activityCmd.add(toActivity(PROJECT, trashDb, ActivityType.BACK, trashDb.getName()));
        return null;
      }
    }.execute();
  }

  /**
   * Restore all projects from trash.
   * <p>
   * Restores all projects and deletes all trash records for the current user or admin.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void backAll() {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<ProjectTrash> allTrashes = getAllTrashes();

        if (isNotEmpty(allTrashes)) {
          List<Long> projectIds = allTrashes.stream().map(ProjectTrash::getTargetId)
              .collect(Collectors.toList());

          // Recovery project
          projectRepo.updateToUndeletedStatusByIdIn(projectIds);

          // Delete trash
          projectTrashRepo.deleteByTargetIdIn(projectIds);
        }
        // No activity
        return null;
      }
    }.execute();
  }

  /**
   * Delete associations for a batch of projects (internal helper).
   * <p>
   * Updates project names and handles job data cleanup.
   */
  private void deleteAssociation(List<Long> projectIds) {
    if (isNotEmpty(projectIds)) {
      projectRepo.updateNameByProjectId(projectIds, String.format("-%d", currentTimeMillis()));
      // TODO Stop deleting project job data processing
    }
  }

  /**
   * Get all trash records for the current user or admin (internal helper).
   * <p>
   * Returns all trash records for the current user or all if admin.
   */
  private List<ProjectTrash> getAllTrashes() {
    return isAdmin() ? projectTrashRepo.findAll() : projectTrashRepo.findByDeletedBy(getUserId());
  }

  /**
   * Get the repository for project trash records.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<ProjectTrash, Long> getRepository() {
    return this.projectTrashRepo;
  }
}
