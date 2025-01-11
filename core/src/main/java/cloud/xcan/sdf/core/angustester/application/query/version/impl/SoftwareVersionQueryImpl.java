package cloud.xcan.sdf.core.angustester.application.query.version.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.assembleTaskProgressCount0;
import static cloud.xcan.sdf.core.angustester.application.query.task.impl.TaskQueryImpl.getTaskSummary;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.version.SoftwareVersionQuery;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSummary;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersion;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersionRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class SoftwareVersionQueryImpl implements SoftwareVersionQuery {

  @Resource
  private SoftwareVersionRepo softwareVersionRepo;

  @Resource
  private TaskInfoRepo taskInfoRepo;

  @Resource
  private JoinSupplier joinSupplier;

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

  @Override
  public Page<SoftwareVersion> find(GenericSpecification<SoftwareVersion> spec, PageRequest pageable) {
    return new BizTemplate<Page<SoftwareVersion>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<SoftwareVersion> process() {
        Page<SoftwareVersion> pages = softwareVersionRepo.findAll(spec, pageable);
        setVersionProgress(pages.getContent());
        return pages;
      }
    }.execute();
  }

  @Override
  public SoftwareVersion checkAndFind(Long id) {
    return softwareVersionRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ReleaseVersion"));
  }

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

  @Override
  public void checkExits(Long projectId, String name) {
    long count = softwareVersionRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(name, "ReleaseVersion");
    }
  }

  @Override
  public void checkNotExits(Long projectId, String name) {
    long count = softwareVersionRepo.countByProjectIdAndName(projectId, name);
    if (count <= 0) {
      throw ResourceNotFound.of(name, "ReleaseVersion");
    }
  }

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
        }else {
          versionDb.setProgress(new ProgressCount());
        }
      }
    }
  }

}
