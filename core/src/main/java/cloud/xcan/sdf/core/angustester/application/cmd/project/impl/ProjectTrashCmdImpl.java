package cloud.xcan.sdf.core.angustester.application.cmd.project.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.PROJECT;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonList;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.project.ProjectTrashCmd;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectTrashQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectRepo;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrashRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ProjectTrashCmdImpl extends CommCmd<ProjectTrash, Long> implements ProjectTrashCmd {

  @Resource
  private ProjectTrashRepo projectTrashRepo;

  @Resource
  private ProjectTrashQuery projectTrashQuery;

  @Resource
  private ProjectRepo projectRepo;

  @Resource
  private ActivityCmd activityCmd;

  @Override
  public void add0(List<ProjectTrash> trashes) {
    batchInsert0(trashes);
  }

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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clearAll() {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void backAll() {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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

  private void deleteAssociation(List<Long> projectIds) {
    if (ObjectUtils.isNotEmpty(projectIds)) {
      projectRepo.updateNameByProjectId(projectIds, String.format("-%d", currentTimeMillis()));
      // TODO Do physical deletion
    }
  }

  private List<ProjectTrash> getAllTrashes() {
    return isAdmin() ? projectTrashRepo.findAll() : projectTrashRepo.findByDeletedBy(getUserId());
  }

  @Override
  protected BaseRepository<ProjectTrash, Long> getRepository() {
    return this.projectTrashRepo;
  }
}