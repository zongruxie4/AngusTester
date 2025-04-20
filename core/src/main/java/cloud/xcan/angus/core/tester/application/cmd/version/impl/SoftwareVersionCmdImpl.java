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
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
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

  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> add(SoftwareVersion version) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project exists
        projectQuery.checkAndFind(version.getProjectId());
        // Check the project member
        projectMemberQuery.checkMember(getUserId(), version.getProjectId());
        // Check the version name exists
        softwareVersionQuery.checkExits(version.getProjectId(), version.getName());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Save version
        IdKey<Long, Object> idKey = insert(version);

        // Save activity
        activityCmd.add(toActivity(SOFTWARE_VERSION, version, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void update(SoftwareVersion version) {
    new BizTemplate<Void>() {
      SoftwareVersion versionDb;

      @Override
      protected void checkParams() {
        // Check the version exists
        versionDb = softwareVersionQuery.checkAndFind(version.getId());
        // Check the version exists
        if (isNotEmpty(version.getName()) && !versionDb.getName().equals(version.getName())) {
          softwareVersionQuery.checkExits(versionDb.getProjectId(), version.getName());
        }
      }

      @Override
      protected Void process() {
        softwareVersionRepo.save(copyPropertiesIgnoreNull(version, versionDb));
        activityCmd.add(toActivity(SOFTWARE_VERSION, versionDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> replace(SoftwareVersion version) {
    return new BizTemplate<IdKey<Long, Object>>() {
      SoftwareVersion versionDb;

      @Override
      protected void checkParams() {
        if (nonNull(version.getId())) {
          // Check the version exists
          versionDb = softwareVersionQuery.checkAndFind(version.getId());
          // Check the version name exists
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

        softwareVersionRepo.save(copyPropertiesIgnoreTenantAuditing(version, versionDb,
            "projectId", "status"));
        activityCmd.add(toActivity(SOFTWARE_VERSION, versionDb, ActivityType.UPDATED));

        return new IdKey<Long, Object>().setId(versionDb.getId()).setKey(versionDb.getName());
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void status(Long id, SoftwareVersionStatus status) {
    new BizTemplate<Void>() {
      SoftwareVersion versionDb;

      @Override
      protected void checkParams() {
        // Check the version exists
        versionDb = softwareVersionQuery.checkAndFind(id);
      }

      @Override
      protected Void process() {
        if (versionDb.getStatus().equals(status)) {
          return null;
        }

        versionDb.setStatus(status);
        if (status.isReleased() && isNull(versionDb.getReleaseDate())) {
          versionDb.setReleaseDate(LocalDateTime.now());
        }

        activityCmd.add(
            toActivity(SOFTWARE_VERSION, versionDb, SOFTWARE_VERSION_STATUS_UPDATE, status));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void merge(Long formId, Long toId) {
    new BizTemplate<Void>() {
      SoftwareVersion formVersionDb, toVersionDb;

      @Override
      protected void checkParams() {
        // Check the version exists
        formVersionDb = softwareVersionQuery.checkAndFind(formId);
        // Check the version exists
        toVersionDb = softwareVersionQuery.checkAndFind(toId);
        // Check that the project ID is consistent
        ProtocolAssert.assertTrue(formVersionDb.getProjectId().equals(toVersionDb.getProjectId()),
            "The version project ID is not consistent");
      }

      @Override
      protected Void process() {
        // Update task version
        taskRepo.updateVersionByProjectIdAndSoftwareVersion(formVersionDb.getProjectId(),
            formVersionDb.getName(), toVersionDb.getName());

        // Update case version
        funcCaseRepo.updateVersionByProjectIdAndSoftwareVersion(formVersionDb.getProjectId(),
            formVersionDb.getName(), toVersionDb.getName());

        // Delete merge version
        softwareVersionRepo.deleteByIdIn(Set.of(formId));

        activityCmd.add(toActivity(SOFTWARE_VERSION, formVersionDb,
            SOFTWARE_VERSION_MERGE, formVersionDb.getName(), toVersionDb.getName()));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<SoftwareVersion> versionDb;

      @Override
      protected void checkParams() {
        versionDb = softwareVersionQuery.checkAndFind(ids);
      }

      @Override
      protected Void process() {
        softwareVersionRepo.deleteByIdIn(ids);

        activityCmd.addAll(toActivities(SOFTWARE_VERSION, versionDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<SoftwareVersion, Long> getRepository() {
    return softwareVersionRepo;
  }
}
