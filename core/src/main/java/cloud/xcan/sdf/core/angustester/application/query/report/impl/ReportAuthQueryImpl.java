package cloud.xcan.sdf.core.angustester.application.query.report.impl;

import static cloud.xcan.sdf.api.message.CommProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.FUNC_PLAN_NO_AUTH;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.FUNC_PLAN_NO_AUTH_CODE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.hasPolicy;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isTenantSysAdmin;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.commonlink.user.UserRepo;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportQuery;
import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.core.angustester.domain.report.ReportRepo;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportAuth;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportAuthCurrent;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportAuthRepo;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

@Biz
public class ReportAuthQueryImpl implements ReportAuthQuery {

  @Resource
  private ReportAuthRepo reportAuthRepo;

  @Resource
  private ReportQuery reportQuery;

  @Resource
  private ReportRepo reportRepo;

  @Resource
  private UserRepo userRepo;

  @Override
  public Boolean status(Long reportId) {
    return new BizTemplate<Boolean>() {
      Report reportDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportDb = reportQuery.checkAndFind(reportId);
      }

      @Override
      protected Boolean process() {
        return reportDb.getAuthFlag();
      }
    }.execute();
  }

  @Override
  public List<ReportPermission> userAuth(Long reportId, Long userId, Boolean adminFlag) {
    return new BizTemplate<List<ReportPermission>>() {
      Report reportDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportDb = reportQuery.checkAndFind(reportId);
      }

      @Override
      protected List<ReportPermission> process() {
        if (Objects.nonNull(adminFlag) && adminFlag && isAdminUser()) {
          return ReportPermission.ALL;
        }

        List<ReportAuth> auths = findAuth(userId, reportId);
        if (isCreator(auths)) {
          return ReportPermission.ALL;
        }

        return auths.stream()
            .map(ReportAuth::getAuths).flatMap(Collection::stream).distinct()
            .collect(Collectors.toList());
      }
    }.execute();
  }

  @Override
  public ReportAuthCurrent currentUserAuth(Long reportId, Boolean adminFlag) {
    return new BizTemplate<ReportAuthCurrent>() {
      Report reportDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportDb = reportQuery.checkAndFind(reportId);
      }

      @Override
      protected ReportAuthCurrent process() {
        ReportAuthCurrent authCurrent = new ReportAuthCurrent();
        authCurrent.setReportAuthFlag(reportDb.getAuthFlag());

        if (Objects.nonNull(adminFlag) && adminFlag && isAdminUser()) {
          authCurrent.addPermissions(ReportPermission.ALL);
          return authCurrent;
        }

        List<ReportAuth> auths = findAuth(getUserId(), reportId);
        if (isCreator(auths)) {
          authCurrent.addPermissions(ReportPermission.ALL);
          return authCurrent;
        }

        List<ReportPermission> authPermissions = auths.stream()
            .map(ReportAuth::getAuths).flatMap(Collection::stream).distinct()
            .collect(Collectors.toList());
        authCurrent.addPermissions(authPermissions);
        return authCurrent;
      }
    }.execute();
  }

  @Override
  public Map<Long, ReportAuthCurrent> currentUserAuths(HashSet<Long> reportIds, Boolean adminFlag) {
    return new BizTemplate<Map<Long, ReportAuthCurrent>>() {
      List<Report> reportsDb;

      @Override
      protected void checkParams() {
        // Check the report exists
        reportsDb = reportQuery.checkAndFind(reportIds);
      }

      @Override
      protected Map<Long, ReportAuthCurrent> process() {
        Map<Long, ReportAuthCurrent> authCurrentMap = new HashMap<>();
        if (nonNull(adminFlag) && adminFlag && isAdminUser()) {
          for (Report report : reportsDb) {
            ReportAuthCurrent authCurrent = new ReportAuthCurrent();
            authCurrent.setReportAuthFlag(report.getAuthFlag());
            authCurrent.addPermissions(ReportPermission.ALL);
            authCurrentMap.put(report.getId(), authCurrent);
          }
          return authCurrentMap;
        }

        Set<Long> currentCreatorIds = reportsDb.stream()
            .filter(x -> x.getCreatedBy().equals(getUserId())).map(Report::getId)
            .collect(Collectors.toSet());
        if (isNotEmpty(currentCreatorIds)) {
          for (Report report : reportsDb) {
            if (currentCreatorIds.contains(report.getId())) {
              ReportAuthCurrent authCurrent = new ReportAuthCurrent();
              authCurrent.setReportAuthFlag(report.getAuthFlag());
              authCurrent.addPermissions(ReportPermission.ALL);
              authCurrentMap.put(report.getId(), authCurrent);
            }
          }
        }

        Set<Long> remainIds = new HashSet<>(reportIds);
        remainIds.removeAll(currentCreatorIds);
        if (isNotEmpty(remainIds)) {
          Map<Long, List<ReportAuth>> reportAuthsMap = findAuth(getUserId(), remainIds)
              .stream().collect(Collectors.groupingBy(ReportAuth::getReportId));
          for (Report report : reportsDb) {
            if (remainIds.contains(report.getId())) {
              ReportAuthCurrent authCurrent = new ReportAuthCurrent();
              Set<ReportPermission> permissions = new HashSet<>();
              List<ReportAuth> scriptAuths = reportAuthsMap.get(report.getId());
              if (isNotEmpty(scriptAuths)) {
                Set<ReportPermission> authPermissions = scriptAuths.stream()
                    .map(ReportAuth::getAuths).flatMap(Collection::stream)
                    .collect(Collectors.toSet());
                permissions.addAll(authPermissions);
              }
              authCurrent.addPermissions(permissions);
              authCurrent.setReportAuthFlag(report.getAuthFlag());
              authCurrentMap.put(report.getId(), authCurrent);
            }
          }
        }
        return authCurrentMap;
      }
    }.execute();
  }

  @Override
  public void check(Long reportId, ReportPermission permission, Long userId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        checkAuth(userId, reportId, permission);
        return null;
      }
    }.execute();
  }

  @Override
  public Page<ReportAuth> find(Specification<ReportAuth> spec, List<String> reportIds,
      Pageable pageable) {
    return new BizTemplate<Page<ReportAuth>>() {
      @Override
      protected void checkParams() {
        if (isEmpty(reportIds)) {
          throw CommProtocolException.of(PARAM_MISSING_T, PARAM_MISSING_KEY,
              new Object[]{"reportId"});
        }
      }

      @Override
      protected Page<ReportAuth> process() {
        return reportAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public ReportAuth checkAndFind(Long id) {
    return reportAuthRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ReportAuth"));
  }

  //VIEW, MODIFY, GENERATE, DELETE, GRANT, SHARE, EXPORT

  @Override
  public void checkViewReportAuth(Long userId, Long reportId) {
    checkAuth(userId, reportId, ReportPermission.VIEW);
  }

  @Override
  public void checkModifyReportAuth(Long userId, Long reportId) {
    checkAuth(userId, reportId, ReportPermission.MODIFY);
  }

  @Override
  public void checkGenerateReportAuth(Long userId, Long reportId) {
    checkAuth(userId, reportId, ReportPermission.GENERATE);
  }

  @Override
  public void checkDeleteReportAuth(Long userId, Long reportId) {
    checkAuth(userId, reportId, ReportPermission.DELETE);
  }

  @DoInFuture("Execution sampling data is not pubapi permission")
  @Override
  public void checkShareReportAuth(Long userId, Long reportId) {
    //checkAuth(userId, reportId, ReportPermission.SHARE);
  }

  @Override
  public void checkShareExportAuth(Long userId, Long reportId) {
    checkAuth(userId, reportId, ReportPermission.EXPORT);
  }

  @Override
  public void checkGrantAuth(Long userId, Long reportId) {
    checkAuth(userId, reportId, ReportPermission.GRANT, false, true);
  }

  @Override
  public void checkAuth(Long userId, Long reportId, ReportPermission permission) {
    checkAuth(userId, reportId, permission, false, permission.isGrant());
  }

  @Override
  public void checkAuth(Long userId, Long reportId, ReportPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (!ignoreAdminPermission && isAdminUser() || !isUserAction()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.isGrant() && !reportQuery.isAuthCtrl(reportId)) {
      return;
    }

    List<ReportAuth> reportAuths = findAuth(userId, reportId);

    if (isCreator(reportAuths)) {
      return;
    }
    if (!findDirAction(reportAuths).contains(permission)) {
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH, new Object[]{permission});
    }
  }

  /**
   * Verify the operation permissions of the scenarioDir
   */
  @Override
  public void batchCheckPermission(Collection<Long> reportIds, ReportPermission permission) {
    if (isAdminUser() || isEmpty(reportIds) || Objects.isNull(permission) || !isUserAction()) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? reportIds : reportRepo.findIds0ByIdInAndAuthFlag(reportIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<ReportAuth> auths = findAuth(PrincipalContext.getUserId(), authIds);
    if (CollectionUtils.isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      Report report = reportRepo.findById(firstId).orElse(null);
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH,
          new Object[]{permission, Objects.isNull(report) ? firstId : report.getName()});
    }

    Map<Long, List<ReportAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getReportId()))
        .collect(Collectors.groupingBy(ReportAuth::getReportId));
    for (Long reportId : authMap.keySet()) {
      List<ReportAuth> values = authMap.get(reportId);
      if (isNotEmpty(values)) {
        List<ReportPermission> reportPermissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (isNotEmpty(reportPermissions) && reportPermissions.contains(permission)) {
          continue;
        }
      }
      Report report = reportRepo.findById(reportId).orElse(null);
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH,
          new Object[]{permission, Objects.isNull(report) ? reportId : report.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long reportId, Long authObjectId, AuthObjectType authObjectType) {
    if (reportAuthRepo.countByReportIdAndAuthObjectIdAndAuthObjectType(reportId, authObjectId,
        authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId, ReportPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return reportAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(a -> a.getAuths().contains(permission)).map(ReportAuth::getReportId).collect(
            Collectors.toList());
  }

  @Override
  public List<ReportAuth> findAuth(Long userId, Long reportId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return reportAuthRepo.findAllByReportIdAndAuthObjectIdIn(reportId, orgIds);
  }

  @Override
  public List<ReportAuth> findAuth(Long userId, Collection<Long> reportIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(reportIds) ? reportAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : reportAuthRepo.findAllByReportIdInAndAuthObjectIdIn(reportIds, orgIds);
  }

  @Override
  public List<ReportPermission> getUserAuth(Long reportId, Long userId) {
    if (isAdminUser()) {
      return ReportPermission.ALL;
    }

    List<ReportAuth> scenarioAuths = findAuth(userId, reportId);
    if (isEmpty(scenarioAuths)) {
      return null;
    }
    if (isCreator(scenarioAuths)) {
      return ReportPermission.ALL;
    }

    return scenarioAuths.stream().map(ReportAuth::getAuths).flatMap(Collection::stream)
        .distinct().collect(Collectors.toList());
  }

  @Override
  public boolean isAdminUser() {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN) || isTenantSysAdmin();
  }

  @Override
  public boolean isCreator(Long userId, Long reportId) {
    List<ReportAuth> scenarioDirAuths = findAuth(userId, reportId);
    return isCreator(scenarioDirAuths);
  }

  private boolean isCreator(List<ReportAuth> reportAuths) {
    if (reportAuths.isEmpty()) {
      return false;
    }
    for (ReportAuth reportAuth : reportAuths) {
      if (reportAuth.getCreatorFlag()) {
        return true;
      }
    }
    return false;
  }

  private Set<ReportPermission> findDirAction(List<ReportAuth> reportAuths) {
    Set<ReportPermission> actions = new HashSet<>();
    for (ReportAuth reportAuth : reportAuths) {
      actions.addAll(reportAuth.getAuths());
    }
    return actions;
  }
}
