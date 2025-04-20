package cloud.xcan.angus.core.tester.application.cmd.apis.impl;


import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.commonlink.ApisTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTrashQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrashRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ApisTrashCmdImpl extends CommCmd<ApisTrash, Long> implements ApisTrashCmd {

  @Resource
  private ApisTrashRepo apisTrashRepo;

  @Resource
  private ApisTrashQuery apisTrashQuery;

  @Resource
  private ApisRepo apisRepo;

  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;

  @Resource
  private ApisCmd apisCmd;

  @Resource
  private ServicesRepo servicesRepo;

  @Resource
  private ServicesCmd servicesCmd;

  @Resource
  private VariableRepo variablesRepo;

  @Resource
  private ActivityCmd activityCmd;

  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void add0(List<ApisTrash> trashes) {
    batchInsert0(trashes);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clear(Long id) {
    new BizTemplate<Void>() {
      ApisTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
        trashDb = apisTrashQuery.findMyTrashForBiz(id, "CLEAR");
      }

      @Override
      protected Void process() {
        // Delete trash
        apisTrashRepo.deleteById(id);

        // Delete association data
        deleteAssociation(Collections.singletonList(trashDb));

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clearAll(Long projectId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        // Find existed trash
        List<ApisTrash> allTrashes = getAllTrashesByProject(projectId);

        if (isEmpty(allTrashes)) {
          return null;
        }

        // Delete all trash
        apisTrashRepo.deleteByTargetIdIn(allTrashes.stream().map(ApisTrash::getTargetId).collect(
            Collectors.toList()));

        // Delete association data
        deleteAssociation(allTrashes);

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void back(Long id) {
    new BizTemplate<Void>() {
      ApisTrash trashDb;

      @Override
      protected void checkParams() {
        trashDb = apisTrashQuery.findMyTrashForBiz(id, "BACK");
      }

      @Override
      protected Void process() {
        if (trashDb.getTargetType().isService()) {
          backServices(Collections.singletonList(trashDb));
          // Add back activity
          activityCmd.add(toActivity(SERVICE, trashDb, ActivityType.BACK, trashDb.getName()));
        } else if (trashDb.getTargetType().isApi()) {
          backApisAndServices(Collections.singletonList(trashDb));
          // Add back activity
          activityCmd.add(toActivity(API, trashDb, ActivityType.BACK, trashDb.getName()));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void backAll(Long projectId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        List<ApisTrash> allTrashes = getAllTrashesByProject(projectId);

        if (isNotEmpty(allTrashes)) {
          Map<ApisTargetType, List<ApisTrash>> trashMap = allTrashes.stream()
              .collect(Collectors.groupingBy(ApisTrash::getTargetType));
          for (ApisTargetType apisTargetType : trashMap.keySet()) {
            if (apisTargetType.isService()) {
              backServices(trashMap.get(ApisTargetType.SERVICE));
            } else if (apisTargetType.isApi()) {
              backApisAndServices(trashMap.get(ApisTargetType.API));
            }
          }
        }
        // No activity
        return null;
      }
    }.execute();
  }

  private void deleteAssociation(List<ApisTrash> trashes) {
    List<Long> allApiIds = new ArrayList<>();

    List<Long> serviceIds = trashes.stream()
        .filter(d -> d.getTargetType().isService()).map(ApisTrash::getTargetId)
        .collect(Collectors.toList());
    if (isNotEmpty(serviceIds)) {
      // Clean up subordinates apis trash
      List<Long> apiIds = apisRepo.findAll0IdByServiceIdIn(serviceIds);
      if (isNotEmpty(apiIds)) {
        allApiIds.addAll(apiIds);
      }

      servicesCmd.delete0(serviceIds);
      apisTrashRepo.deleteByTargetIdIn(serviceIds);
    }

    List<Long> apiIds = trashes.stream().filter(d -> d.getTargetType().isApi())
        .map(ApisTrash::getTargetId).collect(Collectors.toList());
    if (isNotEmpty(apiIds)) {
      allApiIds.addAll(apiIds);
    }

    if (isNotEmpty(allApiIds)) {
      apisCmd.delete0(allApiIds);
      apisTrashRepo.deleteByTargetIdIn(allApiIds);
    }
  }

  private void backServices(List<ApisTrash> trashes) {
    if (isEmpty(trashes)) {
      return;
    }
    List<Long> backServiceIds = trashes.stream().filter(x -> x.getTargetType().isService())
        .map(ApisTrash::getTargetId).collect(Collectors.toList());
    if (isEmpty(backServiceIds)) {
      return;
    }
    // Update undeleted service status
    servicesRepo.updateToUndeletedStatusByIdIn(backServiceIds);
    // Cancel deleted service and parent project
    apisTrashRepo.deleteByTargetIdIn(backServiceIds);
    // Update apis project delete flag
    apisRepo.updateServiceDeleteStatusByService(backServiceIds, false);
  }

  private void backApisAndServices(List<ApisTrash> trashes) {
    if (isEmpty(trashes)) {
      return;
    }
    List<Long> backApiIds = trashes.stream().filter(x -> x.getTargetType().isApi())
        .map(ApisTrash::getTargetId).collect(Collectors.toList());
    if (isEmpty(backApiIds)) {
      return;
    }
    // Update undeleted apis status
    apisRepo.updateToUndeletedStatusByIdIn(backApiIds);
    // Delete apis trash
    apisTrashRepo.deleteByTargetIdIn(backApiIds);

    // Cancel parent project and service, Include logic deleted projects
    List<ApisBaseInfo> apiInfos = apisBaseInfoRepo.findAll0ByIdIn(backApiIds);
    if (isNotEmpty(apiInfos)) {
      // Get all parent project and service
      List<Long> serviceIds = apiInfos.stream()
          .map(ApisBaseInfo::getServiceId).collect(Collectors.toList());
      // Include logic deleted
      Collection<Services> services = servicesRepo.findAll0ByIdIn(serviceIds);

      // Cancel deleted parent service
      List<Long> backServiceIds = services.stream().map(Services::getId)
          .collect(Collectors.toList());
      if (isNotEmpty(backServiceIds)) {
        // Update undeleted service status
        servicesRepo.updateToUndeletedStatusByIdIn(backServiceIds);
        // Cancel deleted service
        apisTrashRepo.deleteByTargetIdIn(backServiceIds);
        // Update apis parent delete flag
        apisRepo.updateServiceDeleteStatusByService(backServiceIds, false);
      }
    }
  }

  private List<ApisTrash> getAllTrashesByProject(Long projectId) {
    Long currentUserId = getUserId();
    List<ApisTrash> trashDbs;
    if (isNull(projectId)) {
      trashDbs = isAdmin() ? apisTrashRepo.findAll()
          : apisTrashRepo.findByCreatedBy(currentUserId);
    } else {
      trashDbs = isAdmin() ? apisTrashRepo.findByProjectId(projectId)
          : apisTrashRepo.findByProjectIdAndCreatedBy(projectId, currentUserId);
    }
    return trashDbs;
  }

  @Override
  protected BaseRepository<ApisTrash, Long> getRepository() {
    return this.apisTrashRepo;
  }
}
