package cloud.xcan.sdf.core.angustester.application.cmd.project.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.PROJECT;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.ProjectConverter.toTaskTrash;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyProperties;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.project.ProjectCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.project.ProjectMemberCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.project.ProjectTrashCmd;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ProjectCmdImpl extends CommCmd<Project, Long> implements ProjectCmd {

  @Resource
  private ProjectRepo projectRepo;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ProjectMemberCmd projectMemberCmd;

  @Resource
  private ProjectTrashCmd trashProjectCmd;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private CommonQuery commonQuery;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Project project) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project quota
        projectQuery.checkQuota(1);

        // Check the name replication
        projectQuery.checkAddNameExists(project.getName());

        // Check the members exists
        commonQuery.checkOrgExists(project.getMemberTypeIds());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Save project
        IdKey<Long, Object> idKey = insert(project);

        // Add owner to members
        addOwnerToMembers(project, project.getOwnerId());
        // Save members
        projectMemberCmd.add0(idKey.getId(), project.getMemberTypeIds());

        // Add created project activity
        activityCmd.add(toActivity(PROJECT, project, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Project project) {
    new BizTemplate<Void>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Check the project exists
        projectDb = projectQuery.checkAndFind(project.getId());

        // Check the only allow owner and admin to modify
        projectQuery.checkModifyPermission(projectDb);

        // Check the name replication
        if (isNotEmpty(project.getName())) {
          projectQuery.checkUpdateNameExists(project.getId(), project.getName());
        }

        // Check the members exists
        commonQuery.checkOrgExists(project.getMemberTypeIds());
      }

      @Override
      protected Void process() {
        // Update members
        if (isNotEmpty(project.getMemberTypeIds())) {
          addOwnerToMembers(project, projectDb);
          projectMemberCmd.replace0(projectDb.getId(), project.getMemberTypeIds());
        }

        // Save project
        projectRepo.save(copyPropertiesIgnoreNull(project, projectDb));

        // Add created project activity
        activityCmd.add(toActivity(PROJECT, project, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Project project) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        if (nonNull(project.getId())) {
          // Check the project exists
          projectDb = projectQuery.checkAndFind(project.getId());

          // Check the only allow owner and admin to modify
          projectQuery.checkModifyPermission(projectDb);

          // Check the name replication
          if (isNotEmpty(project.getName())) {
            projectQuery.checkUpdateNameExists(project.getId(), project.getName());
          }

          // Check the members exists
          commonQuery.checkOrgExists(project.getMemberTypeIds());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(project.getId())) {
          return add(project);
        }

        // Replace members
        addOwnerToMembers(project, projectDb);
        projectMemberCmd.replace0(projectDb.getId(), project.getMemberTypeIds());

        // Save project
        projectRepo.save(copyProperties(project, projectDb));

        // Add created project activity
        activityCmd.add(toActivity(PROJECT, project, ActivityType.UPDATED));
        return new IdKey<Long, Object>().setId(projectDb.getId()).setKey(projectDb.getName());
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Check the project existed and authed
        projectDb = projectRepo.findById(id).orElse(null);

        if (nonNull(projectDb)) {
          // Check the only allow owner and admin to delete
          projectQuery.checkModifyPermission(projectDb);
        }
      }

      @Override
      protected Void process() {
        if (isNull(projectDb)) {
          return null;
        }

        // Logic delete
        // After the project is deleted, filter and exclude the associated sub project and task!
        projectDb.setDeletedFlag(true).setDeletedBy(getUserId())
            .setDeletedDate(LocalDateTime.now());
        projectRepo.save(projectDb);

        // TODO Logical deletion of associated resources

        // Add project to ProjectTrash
        trashProjectCmd.add0(singletonList(toTaskTrash(projectDb)));

        // Add delete project activity
        activityCmd.add(toActivity(PROJECT, projectDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Physically delete, External calling biz must ensure data authed and secured!
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0(List<Long> ids) {
    List<Long> projectIds = projectRepo.findId0ByIdIn(ids);
    if (ObjectUtils.isEmpty(projectIds)) {
      return;
    }

    // Delete project
    projectRepo.deleteAllByIdIn(projectIds);

    // TODO Delete associated resources

    // TODO Delete projects from ProjectTrash

  }

  private static void addOwnerToMembers(Project project, Long ownerId) {
    addOwnerToMembers0(project, ownerId);
  }

  private static void addOwnerToMembers(Project project, Project projectDb) {
    Long ownerId = isNull(project.getOwnerId()) ? projectDb.getOwnerId() : project.getOwnerId();
    addOwnerToMembers0(project, ownerId);
  }

  private static void addOwnerToMembers0(Project project, Long ownerId) {
    LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> members = project.getMemberTypeIds();
    if (members.containsKey(OrgTargetType.USER)){
      members.get(OrgTargetType.USER).add(ownerId);
    }else {
      LinkedHashSet<Long> memberSet = new LinkedHashSet<>();
      memberSet.add(ownerId);
      project.getMemberTypeIds().put(OrgTargetType.USER, memberSet);
    }
  }

  @Override
  protected BaseRepository<Project, Long> getRepository() {
    return this.projectRepo;
  }
}




