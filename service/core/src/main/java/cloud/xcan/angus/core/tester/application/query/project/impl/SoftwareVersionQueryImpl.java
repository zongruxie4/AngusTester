package cloud.xcan.angus.core.tester.application.query.project.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleTaskProgressCount0;
import static cloud.xcan.angus.core.tester.application.query.issue.impl.TaskQueryImpl.getTaskSummary;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.project.SoftwareVersionQuery;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.issue.count.ProgressCount;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskSummary;
import cloud.xcan.angus.core.tester.domain.project.version.SoftwareVersion;
import cloud.xcan.angus.core.tester.domain.project.version.SoftwareVersionRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of SoftwareVersionQuery for software version management and query operations.
 * </p>
 * <p>
 * Provides methods for software version CRUD operations, progress tracking, and task association
 * management.
 * </p>
 */
@Biz
public class SoftwareVersionQueryImpl implements SoftwareVersionQuery {

  @Resource
  private SoftwareVersionRepo softwareVersionRepo;

  @Resource
  private TaskInfoRepo taskInfoRepo;

  @Resource
  private JoinSupplier joinSupplier;

  /**
   * <p>
   * Get detailed information of a software version including progress and task statistics.
   * </p>
   * <p>
   * Retrieves the version details and assembles progress information from associated tasks. Groups
   * tasks by status for efficient data organization.
   * </p>
   *
   * @param id Software version ID
   * @return Software version with progress and task information
   */
  @Override
  public SoftwareVersion detail(Long id) {
    return new BizTemplate<SoftwareVersion>() {
      SoftwareVersion versionDb;

      @Override
      protected void checkParams() {
        versionDb = checkAndFind(id);
      }

      @Override
      protected SoftwareVersion process() {
        List<TaskInfo> tasks = taskInfoRepo.findByProjectIdAndSoftwareVersion(
            versionDb.getProjectId(), versionDb.getName());
        if (isNotEmpty(tasks)) {
          versionDb.setProgress(assembleTaskProgressCount0(tasks))
              .setTaskByStatus(joinSupplier.execute(() -> getTaskSummary(tasks)).stream()
                  .collect(Collectors.groupingBy(TaskSummary::getStatus)));
        } else {
          versionDb.setProgress(new ProgressCount()).setTaskByStatus(new HashMap<>());
        }
        return versionDb;
      }
    }.execute();
  }

  /**
   * <p>
   * List software versions with pagination and optional full-text search.
   * </p>
   * <p>
   * Retrieves paginated versions and sets progress information for each version.
   * </p>
   *
   * @param spec           Search specification
   * @param pageable       Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match          Search match parameters
   * @return Page of software versions
   */
  @Override
  public Page<SoftwareVersion> list(GenericSpecification<SoftwareVersion> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<SoftwareVersion>>() {

      @Override
      protected Page<SoftwareVersion> process() {
        Page<SoftwareVersion> pages = softwareVersionRepo.findAll(spec, pageable);
        setVersionProgress(pages.getContent());
        return pages;
      }
    }.execute();
  }

  /**
   * <p>
   * Check and find a software version by ID.
   * </p>
   *
   * @param id Software version ID
   * @return Software version entity
   */
  @Override
  public SoftwareVersion checkAndFind(Long id) {
    return softwareVersionRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ReleaseVersion"));
  }

  /**
   * <p>
   * Check and find multiple software versions by IDs.
   * </p>
   * <p>
   * Validates that all specified version IDs exist. Throws ResourceNotFound if any version is
   * missing.
   * </p>
   *
   * @param ids Collection of software version IDs
   * @return List of software version entities
   */
  @Override
  public List<SoftwareVersion> checkAndFind(Collection<Long> ids) {
    List<SoftwareVersion> versions = softwareVersionRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(versions), ids.iterator().next(), "ReleaseVersion");
    if (ids.size() != versions.size()) {
      for (SoftwareVersion version : versions) {
        assertResourceNotFound(ids.contains(version.getId()), version.getId(), "ReleaseVersion");
      }
    }
    return versions;
  }

  /**
   * <p>
   * Check if a software version already exists in a project.
   * </p>
   * <p>
   * Validates that a version with the specified name does not already exist in the project. Throws
   * ResourceExisted if the version name is already taken.
   * </p>
   *
   * @param projectId Project ID
   * @param name      Software version name
   */
  @Override
  public void checkExits(Long projectId, String name) {
    long count = softwareVersionRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(name, "ReleaseVersion");
    }
  }

  /**
   * <p>
   * Check if a software version does not exist in a project.
   * </p>
   * <p>
   * Validates that a version with the specified name exists in the project. Throws ResourceNotFound
   * if the version name does not exist.
   * </p>
   *
   * @param projectId Project ID
   * @param name      Software version name
   */
  @Override
  public void checkNotExits(Long projectId, String name) {
    long count = softwareVersionRepo.countByProjectIdAndName(projectId, name);
    if (count <= 0) {
      throw ResourceNotFound.of(name, "ReleaseVersion");
    }
  }

  /**
   * <p>
   * Set progress information for a list of software versions.
   * </p>
   * <p>
   * Efficiently loads and sets progress information for multiple versions to avoid N+1 query
   * problems. Groups tasks by software version name and assembles progress counts for each
   * version.
   * </p>
   *
   * @param versionsDb List of software versions to set progress for
   */
  @Override
  public void setVersionProgress(List<SoftwareVersion> versionsDb) {
    if (isNotEmpty(versionsDb)) {
      Set<String> versions = versionsDb.stream().map(SoftwareVersion::getName)
          .collect(Collectors.toSet());
      Map<String, List<TaskInfo>> versionTasks = taskInfoRepo.findByProjectIdAndSoftwareVersionIn(
              versionsDb.get(0).getProjectId(), versions).stream()
          .collect(Collectors.groupingBy(TaskInfo::getSoftwareVersion));
      for (SoftwareVersion versionDb : versionsDb) {
        if (versionTasks.containsKey(versionDb.getName())) {
          versionDb.setProgress(assembleTaskProgressCount0(versionTasks.get(versionDb.getName())));
        } else {
          versionDb.setProgress(new ProgressCount());
        }
      }
    }
  }

}
