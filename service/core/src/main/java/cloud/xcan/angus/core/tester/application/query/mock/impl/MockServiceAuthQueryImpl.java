package cloud.xcan.angus.core.tester.application.query.mock.impl;


import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NO_AUTH_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NO_AUTH_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NO_TARGET_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NO_TARGET_AUTH_CODE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfoRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuthRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author XiaoLong Liu
 */
@Biz
public class MockServiceAuthQueryImpl implements MockServiceAuthQuery {

  @Resource
  private MockServiceAuthRepo mockServiceAuthRepo;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private MockServiceInfoRepo mockServiceInfoRepo;

  @Resource
  private UserRepo userRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Boolean status(Long serviceId) {
    return new BizTemplate<Boolean>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the mock service exists
        serviceDb = mockServiceQuery.checkAndFind(serviceId);
      }

      @Override
      protected Boolean process() {
        return serviceDb.getAuth();
      }
    }.execute();
  }

  @Override
  public List<MockServicePermission> userAuth(Long serviceId, Long userId, Boolean admin) {
    return new BizTemplate<List<MockServicePermission>>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the mock service exists
        serviceDb = mockServiceQuery.checkAndFind(serviceId);
      }

      @Override
      protected List<MockServicePermission> process() {
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          return MockServicePermission.ALL;
        }
        List<MockServiceAuth> auths = findAuth(userId, serviceId);
        if (isCreator(auths)) {
          return MockServicePermission.ALL;
        }

        Set<MockServicePermission> permissions = new HashSet<>();
        if (!serviceDb.isEnabledAuth()) {
          permissions.add(MockServicePermission.VIEW);
        }

        Set<MockServicePermission> authPermissions = auths.stream()
            .map(MockServiceAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        return new ArrayList<>(authPermissions);
      }
    }.execute();
  }

  @Override
  public void check(Long serviceId, MockServicePermission permission, Long userId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        checkAuth(userId, serviceId, permission);
        return null;
      }
    }.execute();
  }

  @Override
  public Page<MockServiceAuth> find(Long serviceId,
      GenericSpecification<MockServiceAuth> spec, Pageable pageable) {
    return new BizTemplate<Page<MockServiceAuth>>() {
      @Override
      protected void checkParams() {
        checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected Page<MockServiceAuth> process() {
        spec.getCriteria().add(SearchCriteria.equal("mockServiceId", serviceId));
        return mockServiceAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public MockServiceAuth checkAndFind(Long id) {
    return mockServiceAuthRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "MockDatasourceAuth"));
  }

  @Override
  public void checkAddAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, MockServicePermission.ADD);
  }

  @Override
  public void checkViewAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, MockServicePermission.VIEW);
  }

  @Override
  public void checkModifyAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, MockServicePermission.MODIFY);
  }

  @Override
  public void checkDeleteAuth(Long userId, Long serviceId) {
    checkAuth(userId, serviceId, MockServicePermission.DELETE);
  }

  @Override
  public void checkGrantAuth(Long userId, Long serviceId) {
    // Fix:: Public apis can be modified and authorized by anyone
    checkAuth(userId, serviceId, MockServicePermission.GRANT, false, true);
  }

  @Override
  public void checkAuth(Long userId, Long apisId, MockServicePermission permission) {
    checkAuth(userId, apisId, permission, false, permission.isGrant());
  }

  @Override
  public void checkAuth(Long userId, Long datasourceId, MockServicePermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (!ignoreAdminPermission && commonQuery.isAdminUser()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.isGrant() && !mockServiceQuery
        .isAuthCtrl(datasourceId)) {
      return;
    }

    // View as base permissions
    List<MockServiceAuth> auths = findAuth(userId, datasourceId);
    if (permission.equals(MockServicePermission.VIEW)) {
      if (isEmpty(auths)) {
        throw BizException.of(MOCK_SERVICE_NO_AUTH_CODE, MOCK_SERVICE_NO_AUTH_T,
            new Object[]{permission});
      }
    }

    if (isCreator(auths)) {
      return;
    }

    if (!flatPermissions(auths).contains(permission)) {
      throw BizException.of(MOCK_SERVICE_NO_AUTH_CODE, MOCK_SERVICE_NO_AUTH_T,
          new Object[]{permission});
    }
  }

  /**
   * Verify the operation permissions of the apis
   */
  @Override
  public void batchCheckPermission(Collection<Long> serviceIds, MockServicePermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(serviceIds) || isNull(permission)) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? serviceIds : mockServiceInfoRepo.findIds0ByIdInAndAuth(serviceIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<MockServiceAuth> auths = findAuth(PrincipalContext.getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      MockServiceInfo serviceInfo = mockServiceInfoRepo.findById(firstId).orElse(null);
      throw BizException.of(MOCK_SERVICE_NO_TARGET_AUTH_CODE, MOCK_SERVICE_NO_TARGET_AUTH,
          new Object[]{permission, isNull(serviceInfo) ? firstId : serviceInfo.getName()});
    }

    Map<Long, List<MockServiceAuth>> authsMap = auths.stream()
        .filter(o -> nonNull(o.getMockServiceId()))
        .collect(Collectors.groupingBy(MockServiceAuth::getMockServiceId));
    for (Long mockServiceId : authsMap.keySet()) {
      List<MockServiceAuth> values = authsMap.get(mockServiceId);
      if (isNotEmpty(values)) {
        List<MockServicePermission> permissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (isNotEmpty(permissions) && permissions.contains(permission)) {
          continue;
        }
      }
      MockServiceInfo serviceInfo = mockServiceInfoRepo.findById(mockServiceId).orElse(null);
      throw BizException.of(MOCK_SERVICE_NO_TARGET_AUTH_CODE, MOCK_SERVICE_NO_TARGET_AUTH,
          new Object[]{permission, isNull(serviceInfo) ? mockServiceId : serviceInfo.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long serviceId, Long authObjectId, AuthObjectType authObjectType) {
    if (mockServiceAuthRepo.countByMockServiceIdAndAuthObjectIdAndAuthObjectType(serviceId,
        authObjectId, authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId,
      MockServicePermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return mockServiceAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(p -> p.getAuths().contains(permission)).map(MockServiceAuth::getMockServiceId)
        .collect(Collectors.toList());
  }

  @Override
  public List<MockServiceAuth> findAuth(Long userId, Long serviceId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return mockServiceAuthRepo.findAllByMockServiceIdAndAuthObjectIdIn(serviceId, orgIds);
  }

  @Override
  public List<MockServiceAuth> findAuth(Long userId, Collection<Long> serviceIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(serviceIds) ? mockServiceAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : mockServiceAuthRepo.findAllByMockServiceIdInAndAuthObjectIdIn(serviceIds, orgIds);
  }

  private boolean isCreator(List<MockServiceAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (MockServiceAuth auth : auths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  private Set<MockServicePermission> flatPermissions(List<MockServiceAuth> auths) {
    Set<MockServicePermission> permissions = new HashSet<>();
    for (MockServiceAuth auth : auths) {
      permissions.addAll(auth.getAuths());
    }
    return permissions;
  }
}
