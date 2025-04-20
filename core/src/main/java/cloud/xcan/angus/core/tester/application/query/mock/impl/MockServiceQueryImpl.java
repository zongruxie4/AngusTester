package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX;
import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterMockServiceApis;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_ASSOC_SERVICE_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_DOMAIN_IN_USE_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_DOMAIN_PORT_IN_USE_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NAME_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_NODE_PORT_IN_USE_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_OVER_LIMIT_IN_NODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_PORT_UNAVAILABLE_IN_AGENT_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_TOP_LEVEL_DOMAIN_ERROR_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import cloud.xcan.angus.agent.message.CheckPortCmdParam;
import cloud.xcan.angus.agent.message.CheckPortVo;
import cloud.xcan.angus.agent.message.mockservice.StatusCmdParam;
import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.api.ctrl.mockservice.MockServiceManageRemote;
import cloud.xcan.angus.api.ctrl.mockservice.dto.MockServiceStatusDto;
import cloud.xcan.angus.api.ctrl.node.NodeInfoRemote;
import cloud.xcan.angus.api.ctrl.node.dto.NodeAgentCheckPortDto;
import cloud.xcan.angus.api.ctrl.node.vo.NodeInfoDetailVo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.mock.MockResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisCount;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceAssocP;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceCount;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfoRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSource;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceStatus;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@Biz
@SummaryQueryRegister(name = "MockService", table = "mock_service",
    groupByColumns = {"created_date", "source"})
public class MockServiceQueryImpl implements MockServiceQuery {

  @Resource
  private MockServiceRepo mockServiceRepo;

  @Resource
  private MockServiceInfoRepo mockServiceInfoRepo;

  @Resource
  private MockApisResponseRepo mockApisResponseRepo;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Resource
  private MockServiceManageRemote mockServiceManageRemote;

  @Resource
  private NodeInfoRemote nodeInfoRemote;

  @Resource
  private NodeQuery nodeQuery;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private MockApisRepo mockApisRepo;

  @Resource
  private UserManager userManager;

  @Override
  public MockService detail(Long id) {
    return new BizTemplate<MockService>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the to have permission to view
        mockServiceAuthQuery.checkViewAuth(getUserId(), id);

        // Check and get service
        serviceDb = checkAndFind(id);
      }

      @Override
      protected MockService process() {
        // Set mock service status
        setMockServiceStatus(Collections.singletonList(serviceDb));

        // Set the current user service permissions flag
        setMockServiceCurrentAuths(Collections.singletonList(serviceDb));

        // Set node info
        setNodeInfo(List.of(serviceDb));
        return serviceDb;
      }
    }.execute();
  }

  @Override
  public MockService detailByProjectId(Long projectId) {
    return new BizTemplate<MockService>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Find mock service by projectId
        serviceDb = findByProjectId(projectId);
        if (isNull(serviceDb)) {
          return;
        }

        // Check the to have permission to view
        mockServiceAuthQuery.checkViewAuth(getUserId(), serviceDb.getId());
      }

      @Override
      protected MockService process() {
        if (isNull(serviceDb)) {
          return null;
        }

        // Set mock service status
        setMockServiceStatus(Collections.singletonList(serviceDb));

        // Set the current user service permissions flag
        setMockServiceCurrentAuths(Collections.singletonList(serviceDb));
        return serviceDb;
      }
    }.execute();
  }

  @Override
  public MockService info(Long id) {
    return new BizTemplate<MockService>() {
      @Override
      protected void checkParams() {
        // Check the to have permission to view
        // mockServiceAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected MockService process() {
        return mockServiceRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "MockService"));
      }
    }.execute();
  }

  @Override
  public MockService info0(Long id) {
    return new BizTemplate<MockService>() {
      @Override
      protected void checkParams() {
        // Check the to have permission to view
        // mockServiceAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected MockService process() {
        return mockServiceRepo.findById(id).orElse(null);
      }
    }.execute();
  }

  @Override
  public Set<Long> assocApisIdsList(Long id) {
    return new BizTemplate<Set<Long>>() {
      @Override
      protected void checkParams() {
        // Check the to have permission to view
        // mockServiceAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected Set<Long> process() {
        return mockApisRepo.findAllIdsByMockServiceId(id);
      }
    }.execute();
  }

  @Override
  public void check(Long id) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        if (!mockServiceRepo.existsById(id)) {
          throw ResourceNotFound.of(id, "MockService");
        }
        return null;
      }
    }.execute();
  }

  @Override
  public Page<MockServiceInfo> find(GenericSpecification<MockServiceInfo> spec,
      Pageable pageable) {
    return new BizTemplate<Page<MockServiceInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<MockServiceInfo> process() {
        Page<MockServiceInfo> page = mockServiceInfoRepo.findAll(spec, pageable);
        if (page.isEmpty()) {
          return page;
        }

        // Set mock service status
        setMockServiceInfoStatus(page.getContent());

        // Set the current user service permissions
        setMockServiceInfoCurrentAuths(page.getContent());

        // Set node info
        setInfoNodeInfo(page.getContent());
        return page;
      }
    }.execute();
  }

  @Override
  public List<MockServiceInfo> findByNodeId(Long nodeId) {
    return new BizTemplate<List<MockServiceInfo>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<MockServiceInfo> process() {
        List<MockServiceInfo> infos = mockServiceInfoRepo.findByNodeId(nodeId);

        // Set mock service status
        setMockServiceInfoStatus(infos);
        return infos;
      }
    }.execute();
  }

  @Override
  public MockServiceCount countStatistics(Long mockServiceId) {
    return new BizTemplate<MockServiceCount>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected MockServiceCount process() {
        MockServiceCount count = new MockServiceCount();
        MockApisCount apisCount;
        if (nonNull(mockServiceId)) {
          count.setApisNum(mockApisRepo.countAllByMockServiceId(mockServiceId));
          apisCount = mockApisRepo.countAllNum(mockServiceId);
        } else {
          count.setApisNum(mockApisRepo.countAll());
          apisCount = mockApisRepo.countAllNum();
        }
        // Fix:: JPA Null return value from advice does not match primitive return type for: MockApisCount
        // Modify primitive long to Long.
        count.setRequestNum(nullSafe(apisCount.getRequestNum(), 0L));
        count.setPushbackNum(nullSafe(apisCount.getPushbackNum(), 0L));
        count.setSimulateErrorNum(nullSafe(apisCount.getSimulateErrorNum(), 0L));
        count.setSuccessNum(nullSafe(apisCount.getSuccessNum(), 0L));
        count.setExceptionNum(nullSafe(apisCount.getExceptionNum(), 0L));
        return count;
      }
    }.execute();
  }

  @Override
  public MockResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<MockResourcesCreationCount>() {

      final MockResourcesCreationCount result = new MockResourcesCreationCount();

      Set<Long> createdBys = null;

      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected MockResourcesCreationCount process() {
        // Find all when condition is null, else find by condition
        if (nonNull(creatorObjectType)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        Set<SearchCriteria> allFilters = getCommonResourcesStatsFilter(
            projectId, createdDateStart, createdDateEnd, createdBys);

        // Number of statistical apis
        countService(result, allFilters);
        countApi(result, allFilters);
        countResponse(result, allFilters);
        allFilters.add(SearchCriteria.equal("pushback", 1));
        countPushback(result, allFilters);
        return result;
      }
    }.execute();
  }

  @Override
  public Set<Long> assocApisList(Long id, Long angusProjectId) {
    return new BizTemplate<Set<Long>>() {
      @Override
      protected void checkParams() {
        // Check the mock service exists
        check(id);

        // Check the to have permission to view mock service
        mockServiceAuthQuery.checkViewAuth(getUserId(), id);

        // Check the Angus project exists
        servicesQuery.check(angusProjectId);

        // Check the to have permission to view mock service
        servicesAuthQuery.checkViewAuth(getUserId(), angusProjectId);
      }

      @Override
      protected Set<Long> process() {
        // Query project has permission apis
        long maxApisNum = commonQuery.findTenantQuota(AngusTesterMockServiceApis).getQuota();
        Page<ApisBasicInfo> apisExist = apisQuery.find0(angusProjectId,
            PageRequest.of(0, (int) maxApisNum, Sort.by(Direction.DESC, "id")),
            ApisBasicInfo.class);
        if (!apisExist.hasContent()) {
          return null;
        }

        // Query the interface under the Mock service
        Set<Long> mockApisIds = mockApisRepo.findAllByMockServiceIdAndSource(id,
                MockServiceSource.ASSOC_SERVICE).stream()
            .map(MockApis::getAssocApisId)
            .filter(Objects::nonNull).collect(toSet());

        return apisExist.getContent().stream().map(ApisBasicInfo::getId)
            .filter(mockApisIds::contains).collect(toSet());
      }
    }.execute();
  }

  @Override
  public MockService findByProjectId(Long projectId) {
    return mockServiceRepo.findByAssocServiceId(projectId).orElse(null);
  }

  @Override
  public MockService checkAndFind(Long id) {
    return mockServiceRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "MockService"));
  }

  @Override
  public MockServiceInfo checkAndFindInfo(Long id) {
    return mockServiceInfoRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "MockService"));
  }

  @Override
  public List<MockService> checkAndFind(Set<Long> ids) {
    List<MockService> mockService = mockServiceRepo.findAllById(ids);
    Set<Long> serviceIds = mockService.stream().map(MockService::getId).collect(toSet());
    ids.removeAll(serviceIds);
    ProtocolAssert.assertResourceNotFound(isEmpty(ids), ids, "MockService");
    return mockService;
  }

  @Override
  public List<MockServiceInfo> checkAndFindInfo(Set<Long> ids) {
    List<MockServiceInfo> mockService = mockServiceInfoRepo.findAllById(ids);
    Set<Long> serviceIds = mockService.stream().map(MockServiceInfo::getId).collect(toSet());
    ids.removeAll(serviceIds);
    ProtocolAssert.assertResourceNotFound(isEmpty(ids), ids, "MockService");
    return mockService;
  }

  @Override
  public void checkNameExists(String name) {
    long count = mockServiceRepo.countByName(name);
    ProtocolAssert.assertResourceExisted(count < 1,
        MOCK_SERVICE_NAME_EXISTED_T, new Object[]{name});
  }

  /**
   * Limit the maximum number of services that can be created nodes is memory/1GB, but create at
   * least two.
   */
  @Override
  public void checkNodeServiceNum(long nodeId, long existedNodeNum) {
    if (existedNodeNum >= 2) {
      NodeInfoDetailVo detailVo = nodeInfoRemote.detail(nodeId, false).orElseContentThrow();
      if (nonNull(detailVo.getInfo())
          /* Allow detailVo.getInfo().getMemTotal() / 1024 / 1024 / 1024 + 1 nodes */
          && detailVo.getInfo().getMemTotal() / 1024 / 1024 / 1024 < existedNodeNum) {
        throw ProtocolException.of(MOCK_SERVICE_OVER_LIMIT_IN_NODE);
      }
    }
  }

  @Override
  public void checkValidDomainAndPort(MockService service) {
    String serviceDomain = service.getServiceDomain();
    if (isNotEmpty(serviceDomain)) {
      if (isCloudServiceEdition()) {
        // Check the service domain must end with angusmock.cloud
        ProtocolAssert.assertTrue(serviceDomain.endsWith(MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX),
            MOCK_SERVICE_TOP_LEVEL_DOMAIN_ERROR_T, new Object[]{MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX});
      }
      // Check the domain not used by any other nodes (Excluding nodeId)
      boolean existedDomain = mockServiceRepo.existsByServiceDomainAndNodeIdNot(
          serviceDomain, service.getNodeId());
      ProtocolAssert.assertTrue(!existedDomain, MOCK_SERVICE_DOMAIN_IN_USE_T,
          new Object[]{service.getNodeId()});
      // Check the domain and port not used by any services
      boolean existedDomainAndPort = mockServiceRepo.existsByServiceDomainAndServicePort(
          serviceDomain, service.getServicePort());
      ProtocolAssert.assertTrue(!existedDomainAndPort, MOCK_SERVICE_DOMAIN_PORT_IN_USE_T,
          new Object[]{serviceDomain, String.valueOf(service.getServicePort())});
    }
  }

  @Override
  public void checkUpdateDomain(String serviceDomain, Long nodeId) {
    if (isNotEmpty(serviceDomain)) {
      if (isCloudServiceEdition()) {
        // Check the service domain must end with angusmock.cloud
        ProtocolAssert.assertTrue(serviceDomain.endsWith(MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX),
            MOCK_SERVICE_TOP_LEVEL_DOMAIN_ERROR_T, new Object[]{MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX});
      }
      // Check the domain not used by any other nodes (Excluding nodeId)
      boolean existedDomain = mockServiceRepo.existsByServiceDomainAndNodeIdNot(
          serviceDomain, nodeId);
      ProtocolAssert.assertTrue(!existedDomain, MOCK_SERVICE_DOMAIN_IN_USE_T,
          new Object[]{nodeId});
    }
  }

  @Override
  public void checkNodeAndPortAvailable(Node nodeDb, int port) {
    // Check the port not used by any other services
    boolean existedNodePort = mockServiceRepo.existsByNodeIdAndServicePort(nodeDb.getId(), port);
    ProtocolAssert.assertTrue(!existedNodePort, MOCK_SERVICE_NODE_PORT_IN_USE_T,
        new Object[]{nodeDb.getId(), port});

    // Check the available in node by agent
    CheckPortVo result = checkPort(nodeDb.getId(), nodeDb.getIp(), port);
    if (!result.isSuccess()) {
      throw ProtocolException.of(MOCK_SERVICE_PORT_UNAVAILABLE_IN_AGENT_T,
          new Object[]{port, nodeDb.getId()}, new RuntimeException(result.getMessage()));
    }
  }

  @Override
  public void checkAssocProjectExists(Services projectDb) {
    ProtocolAssert.assertTrue(!mockServiceRepo.existsByAssocServiceId(projectDb.getId()),
        MOCK_SERVICE_ASSOC_SERVICE_EXISTED_T, new Object[]{projectDb.getName()});
  }

  @Override
  public void setNodeInfo(List<MockService> services) {
    if (isNotEmpty(services)) {
      Map<Long, Node> nodeMap = nodeQuery.findNodeMap(services.stream()
          .map(MockService::getNodeId).collect(Collectors.toList()));
      for (MockService service : services) {
        Node node = nodeMap.get(service.getNodeId());
        if (nonNull(node)) {
          service.setNodeIp(node.getIp());
          service.setNodePublicIp(node.getPublicIp());
        } else {
          service.setNodeIp("localhost");
          service.setNodePublicIp("localhost");
        }
      }
    }
  }

  @Override
  public void setInfoNodeInfo(List<MockServiceInfo> services) {
    if (isNotEmpty(services)) {
      Map<Long, Node> nodeMap = nodeQuery.findNodeMap(services.stream()
          .map(MockServiceInfo::getNodeId).collect(Collectors.toList()));
      for (MockServiceInfo service : services) {
        Node node = nodeMap.get(service.getNodeId());
        if (nonNull(node)) {
          service.setNodeIp(node.getIp());
          service.setNodePublicIp(node.getPublicIp());
        } else {
          service.setNodeIp("localhost");
          service.setNodePublicIp("localhost");
        }
      }
    }
  }

  @Override
  public void setMockServiceCurrentAuths(List<MockService> mockServices) {
    if (isNotEmpty(mockServices)) {
      List<MockService> doAuthServices = new ArrayList<>();
      for (MockService service : mockServices) {
        if (!service.getAuth() || commonQuery.isAdminUser()) {
          service.setCurrentAuths(new HashSet<>(MockServicePermission.ALL));
        } else {
          doAuthServices.add(service);
        }
      }

      if (isNotEmpty(doAuthServices)) {
        Map<Long, List<MockServiceAuth>> authServiceMap = mockServiceAuthQuery.findAuth(
                getUserId(), mockServices.stream().map(MockService::getId).collect(Collectors.toList()))
            .stream().collect(Collectors.groupingBy(MockServiceAuth::getMockServiceId));
        for (MockService doAuthService : doAuthServices) {
          if (authServiceMap.containsKey(doAuthService.getId())) {
            doAuthService.setCurrentAuths(authServiceMap.get(doAuthService.getId()).stream()
                .map(MockServiceAuth::getAuths).flatMap(Collection::stream)
                .collect(toSet()));
          }
        }
      }
    }
  }

  @Override
  public void setMockServiceInfoCurrentAuths(List<MockServiceInfo> mockServices) {
    if (isNotEmpty(mockServices)) {
      List<MockServiceInfo> doAuthServices = new ArrayList<>();
      for (MockServiceInfo service : mockServices) {
        if (!service.getAuth() || commonQuery.isAdminUser()) {
          service.setCurrentAuths(new HashSet<>(MockServicePermission.ALL));
        } else {
          doAuthServices.add(service);
        }
      }

      if (isNotEmpty(doAuthServices)) {
        Map<Long, List<MockServiceAuth>> authServiceMap = mockServiceAuthQuery.findAuth(
                getUserId(), mockServices.stream().map(MockServiceInfo::getId)
                    .collect(Collectors.toList()))
            .stream().collect(Collectors.groupingBy(MockServiceAuth::getMockServiceId));
        for (MockServiceInfo doAuthService : doAuthServices) {
          if (authServiceMap.containsKey(doAuthService.getId())) {
            doAuthService.setCurrentAuths(authServiceMap.get(doAuthService.getId()).stream()
                .map(MockServiceAuth::getAuths).flatMap(Collection::stream)
                .collect(toSet()));
          }
        }
      }
    }
  }

  @Override
  public void setMockServiceStatus(List<MockService> mockServices) {
    if (isNotEmpty(mockServices)) {
      Map<Long, MockServiceStatus> serviceStatusMap = getMockServiceStatus(mockServices);
      for (MockService service : mockServices) {
        service.setServiceStatus(
            serviceStatusMap.getOrDefault(service.getId(), MockServiceStatus.NOT_STARTED));
      }
    }
  }

  @Override
  public void setMockServiceInfoStatus(List<MockServiceInfo> mockServices) {
    if (isNotEmpty(mockServices)) {
      Map<Long, MockServiceStatus> serviceStatusMap = getMockServiceInfoStatus(mockServices);
      for (MockServiceInfo service : mockServices) {
        service.setServiceStatus(serviceStatusMap.getOrDefault(service.getId(),
            MockServiceStatus.NOT_STARTED));
      }
    }
  }

  /**
   * Get mock service status
   */
  @Override
  public Map<Long, MockServiceStatus> getMockServiceStatus(List<MockService> services) {
    MockServiceStatusDto dto = new MockServiceStatusDto()
        .setBroadcast(true).setCmdParams(services.stream().map(x -> new StatusCmdParam()
                .setDeviceId(x.getNodeId()).setServiceId(x.getId())
                .setServerIp(x.getNodeIp()).setServerPort(x.getServicePort()))
            .collect(Collectors.toList()));
    List<StatusVo> result = mockServiceManageRemote.status(dto).orElseContentThrow();
    Map<Long, MockServiceStatus> serviceStatusMap = new HashMap<>(services.size());
    if (isNotEmpty(result)) {
      Map<Long, List<StatusVo>> serviceStatusVoMap = result.stream()
          .collect(Collectors.groupingBy(StatusVo::getServiceId));
      for (MockService service : services) {
        if (serviceStatusVoMap.containsKey(service.getId())
            && serviceStatusVoMap.get(service.getId()).stream().anyMatch(StatusVo::isSuccess)) {
          serviceStatusMap.put(service.getId(), MockServiceStatus.RUNNING);
        } else {
          serviceStatusMap.put(service.getId(), MockServiceStatus.NOT_STARTED);
        }
      }
    }
    return serviceStatusMap;
  }

  /**
   * Get mock service status
   */
  @Override
  public Map<Long, MockServiceStatus> getMockServiceInfoStatus(List<MockServiceInfo> services) {
    MockServiceStatusDto dto = new MockServiceStatusDto()
        .setBroadcast(true).setCmdParams(
            services.stream().map(x -> new StatusCmdParam()
                    .setDeviceId(x.getNodeId()).setServiceId(x.getId())
                    .setServerIp(x.getNodeIp()).setServerPort(x.getServicePort()))
                .collect(Collectors.toList()));
    List<StatusVo> result = mockServiceManageRemote.status(dto).orElseContentThrow();
    Map<Long, MockServiceStatus> serviceStatusMap = new HashMap<>(services.size());
    if (isNotEmpty(result)) {
      Map<Long, List<StatusVo>> serviceStatusVoMap = result.stream()
          .collect(Collectors.groupingBy(StatusVo::getServiceId));
      for (MockServiceInfo service : services) {
        if (serviceStatusVoMap.containsKey(service.getId())
            && serviceStatusVoMap.get(service.getId()).stream().anyMatch(StatusVo::isSuccess)) {
          serviceStatusMap.put(service.getId(), MockServiceStatus.RUNNING);
        } else {
          serviceStatusMap.put(service.getId(), MockServiceStatus.NOT_STARTED);
        }
      }
    }
    return serviceStatusMap;
  }

  @Override
  public Map<Long, Long> findProjectMockIdsMap(Set<Long> projectIds) {
    if (isEmpty(projectIds)) {
      return Collections.emptyMap();
    }
    return mockServiceRepo.findServiceMockIdsMap(projectIds).stream().collect(Collectors.toMap(
        MockServiceAssocP::getAssocServiceId, MockServiceAssocP::getId));
  }

  @Override
  public Boolean isAuthCtrl(Long id) {
    Boolean auth = mockServiceRepo.findAuthById(id);
    return auth == null || auth;
  }

  private CheckPortVo checkPort(Long nodeId, String nodeIp, int port) {
    List<CheckPortVo> result = nodeInfoRemote.checkPort(
            new NodeAgentCheckPortDto().setBroadcast(true)
                .setCmdParams(List.of(new CheckPortCmdParam().setDeviceId(nodeId)
                    .setServerIp(nodeIp).setServerPort(port))))
        .orElseContentThrow();
    return result.get(0);
  }

  private void countService(MockResourcesCreationCount result, Set<SearchCriteria> allFilters) {
    result.setAllService(mockServiceRepo.countAllByFilters(allFilters));
  }

  private void countApi(MockResourcesCreationCount result, Set<SearchCriteria> allFilters) {
    result.setAllApi(mockApisRepo.countAllByFilters(allFilters));
  }

  private void countResponse(MockResourcesCreationCount result, Set<SearchCriteria> allFilters) {
    result.setAllResponse(mockApisResponseRepo.countAllByFilters(allFilters));
  }

  private void countPushback(MockResourcesCreationCount result, Set<SearchCriteria> allFilters) {
    result.setAllPushback(mockApisResponseRepo.countAllByFilters(allFilters));
  }
}
