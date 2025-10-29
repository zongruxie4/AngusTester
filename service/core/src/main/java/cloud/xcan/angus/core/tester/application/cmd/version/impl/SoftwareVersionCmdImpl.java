package cloud.xcan.angus.core.tester.application.cmd.version.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SOFTWARE_VERSION;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SOFTWARE_VERSION_MERGE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SOFTWARE_VERSION_STATUS_UPDATE;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreTenantAuditing;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.version.SoftwareVersionCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.version.SoftwareVersionQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersion;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersionRepo;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersionStatus;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Implementation of software version command operations for version management.
 *
 * <p>This class provides comprehensive functionality for managing software versions,
 * including creation, updates, status management, merging, and lifecycle control.</p>
 *
 * <p>It handles the complete version lifecycle from creation to release,
 * including version merging, status transitions, and activity logging.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Version CRUD operations with comprehensive validation</li>
 *   <li>Version status management (draft, released, deprecated)</li>
 *   <li>Version merging with data migration</li>
 *   <li>Project association and member validation</li>
 *   <li>Activity logging for audit trails</li>
 *   <li>Task and case version association updates</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Biz
public class SoftwareVersionCmdImpl extends CommCmd<SoftwareVersion, Long> implements
    SoftwareVersionCmd {

  @Resource
  private SoftwareVersionRepo softwareVersionRepo;

  @Resource
  private SoftwareVersionQuery softwareVersionQuery;

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a new software version with comprehensive validation and setup.
   *
   * <p>This method creates a new software version with extensive validation including
   * project existence, member permissions, and name uniqueness within the project.</p>
   *
   * <p>The method automatically logs version creation activity for audit purposes.</p>
   *
   * @param version the software version to add
   * @return the ID key of the created version
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> add(SoftwareVersion version) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Verify project exists and retrieve project info
        projectQuery.checkAndFind(version.getProjectId());
        // Verify user has project member permissions
        projectMemberQuery.checkMember(getUserId(), version.getProjectId());
        // Verify version name is unique within project
        softwareVersionQuery.checkExits(version.getProjectId(), version.getName());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Save software version to database
        IdKey<Long, Object> idKey = insert(version);

        // Log version creation activity for audit
        activityCmd.add(toActivity(SOFTWARE_VERSION, version, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Updates a software version with comprehensive validation.
   *
   * <p>This method updates a software version with extensive validation including
   * version existence and name uniqueness within the project.</p>
   *
   * <p>The method handles property updates and logs version update activities.</p>
   *
   * @param version the software version to update
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void update(SoftwareVersion version) {
    new BizTemplate<Void>() {
      SoftwareVersion versionDb;

      @Override
      protected void checkParams() {
        // Verify version exists and retrieve version info
        versionDb = softwareVersionQuery.checkAndFind(version.getId());
        // Verify version name is unique within project if changed
        if (isNotEmpty(version.getName()) && !versionDb.getName().equals(version.getName())) {
          softwareVersionQuery.checkExits(versionDb.getProjectId(), version.getName());
        }
      }

      @Override
      protected Void process() {
        // Update version properties while preserving null values
        softwareVersionRepo.save(copyPropertiesIgnoreNull(version, versionDb));
        // Log version update activity for audit
        activityCmd.add(toActivity(SOFTWARE_VERSION, versionDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Replaces a software version with comprehensive validation and activity logging.
   *
   * <p>This method performs a complete replacement of a software version with extensive
   * validation including version existence and name uniqueness within the project.</p>
   *
   * <p>The method handles property updates while preserving tenant auditing fields
   * and logs version replacement activities.</p>
   *
   * @param version the software version to replace
   * @return the ID key of the replaced version
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> replace(SoftwareVersion version) {
    return new BizTemplate<IdKey<Long, Object>>() {
      SoftwareVersion versionDb;

      @Override
      protected void checkParams() {
        if (nonNull(version.getId())) {
          // Verify version exists and retrieve version info
          versionDb = softwareVersionQuery.checkAndFind(version.getId());
          // Verify version name is unique within project if changed
          if (isNotEmpty(version.getName()) && !versionDb.getName().equals(version.getName())) {
            softwareVersionQuery.checkExits(versionDb.getProjectId(), version.getName());
          }
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(version.getId())) {
          return add(version);
        }

        // Update version properties while preserving tenant auditing and protected fields
        softwareVersionRepo.save(copyPropertiesIgnoreTenantAuditing(version, versionDb,
            "projectId", "status"));
        // Log version replacement activity for audit
        activityCmd.add(toActivity(SOFTWARE_VERSION, versionDb, ActivityType.UPDATED));

        return new IdKey<Long, Object>().setId(versionDb.getId()).setKey(versionDb.getName());
      }
    }.execute();
  }

  /**
   * Updates the status of a software version with comprehensive validation.
   *
   * <p>This method changes a software version status with validation including
   * version existence and automatic release date setting for released versions.</p>
   *
   * <p>The method logs version status update activities for audit purposes.</p>
   *
   * @param id the version ID to update status for
   * @param status the new status to set
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void status(Long id, SoftwareVersionStatus status) {
    new BizTemplate<Void>() {
      SoftwareVersion versionDb;

      @Override
      protected void checkParams() {
        // Verify version exists and retrieve version info
        versionDb = softwareVersionQuery.checkAndFind(id);
      }

      @Override
      protected Void process() {
        // Skip processing if status is already set to target status
        if (versionDb.getStatus().equals(status)) {
          return null;
        }

        // Update version status
        versionDb.setStatus(status);
        // Set release date automatically for released versions
        if (status.isReleased() && isNull(versionDb.getReleaseDate())) {
          versionDb.setReleaseDate(LocalDateTime.now());
        }

        // Log version status update activity for audit
        activityCmd.add(
            toActivity(SOFTWARE_VERSION, versionDb, SOFTWARE_VERSION_STATUS_UPDATE, status));
        return null;
      }
    }.execute();
  }

  /**
   * Merges two software versions with comprehensive data migration.
   *
   * <p>This method merges a source version into a target version, updating all
   * associated tasks and cases to reference the target version.</p>
   *
   * <p>The method validates project consistency and logs version merge
   * activities for audit purposes.</p>
   *
   * @param formId the source version ID to merge from
   * @param toId the target version ID to merge into
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void merge(Long formId, Long toId) {
    new BizTemplate<Void>() {
      SoftwareVersion formVersionDb, toVersionDb;

      @Override
      protected void checkParams() {
        // Verify source version exists and retrieve version info
        formVersionDb = softwareVersionQuery.checkAndFind(formId);
        // Verify target version exists and retrieve version info
        toVersionDb = softwareVersionQuery.checkAndFind(toId);
        // Verify both versions belong to the same project
        ProtocolAssert.assertTrue(formVersionDb.getProjectId().equals(toVersionDb.getProjectId()),
            "The version project ID is not consistent");
      }

      @Override
      protected Void process() {
        // Update all tasks to reference target version
        taskRepo.updateVersionByProjectIdAndSoftwareVersion(formVersionDb.getProjectId(),
            formVersionDb.getName(), toVersionDb.getName());

        // Update all cases to reference target version
        funcCaseRepo.updateVersionByProjectIdAndSoftwareVersion(formVersionDb.getProjectId(),
            formVersionDb.getName(), toVersionDb.getName());

        // Delete source version after successful migration
        softwareVersionRepo.deleteByIdIn(Set.of(formId));

        // Log version merge activity for audit
        activityCmd.add(toActivity(SOFTWARE_VERSION, formVersionDb,
            SOFTWARE_VERSION_MERGE, formVersionDb.getName(), toVersionDb.getName()));
        return null;
      }
    }.execute();
  }

  /**
   * Deletes multiple software versions with comprehensive cleanup.
   *
   * <p>This method permanently deletes multiple software versions after verifying
   * their existence. It performs batch deletion for efficiency.</p>
   *
   * <p>The method logs version deletion activities for audit purposes.</p>
   *
   * @param ids the collection of version IDs to delete
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<SoftwareVersion> versionDb;

      @Override
      protected void checkParams() {
        // Verify all versions exist and retrieve version info
        versionDb = softwareVersionQuery.checkAndFind(ids);
      }

      @Override
      protected Void process() {
        // Permanently delete all specified versions
        softwareVersionRepo.deleteByIdIn(ids);

        // Log version deletion activities for audit
        activityCmd.addAll(toActivities(SOFTWARE_VERSION, versionDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the software version repository
   */
  @Override
  protected BaseRepository<SoftwareVersion, Long> getRepository() {
    return softwareVersionRepo;
  }
}
