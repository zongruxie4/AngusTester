package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.mock.MockResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceCount;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceStatus;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.domain.services.Services;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface MockServiceQuery {

  MockService detail(Long id);

  MockService detailByProjectId(Long projectId);

  MockService info(Long id);

  MockService info0(Long id);

  Set<Long> assocApisIdsList(Long mockServiceId);

  void check(Long id);

  Page<MockServiceInfo> find(GenericSpecification<MockServiceInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  List<MockServiceInfo> findByNodeId(Long id);

  MockServiceCount countStatistics(Long mockServiceId);

  MockResourcesCreationCount creationStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  Set<Long> assocApisList(Long id, Long angusProjectId);

  MockService findByProjectId(Long projectId);

  MockService checkAndFind(Long id);

  MockServiceInfo checkAndFindInfo(Long id);

  List<MockService> checkAndFind(Set<Long> ids);

  List<MockServiceInfo> checkAndFindInfo(Set<Long> ids);

  void checkNameExists(String name);

  void checkNodeServiceNum(long nodeId, long existedNodeNum);

  void checkUpdateDomain(String serviceDomain, Long nodeId);

  void checkNodeAndPortAvailable(Node nodeDb, int port);

  void checkAssocProjectExists(Services projectDb);

  void checkValidDomainAndPort(MockService service);

  void setMockServiceInfoCurrentAuths(List<MockServiceInfo> mockServices);

  void setNodeInfo(List<MockService> services);

  void setInfoNodeInfo(List<MockServiceInfo> services);

  void setMockServiceCurrentAuths(List<MockService> mockServices);

  void setMockServiceStatus(List<MockService> mockServices);

  void setMockServiceInfoStatus(List<MockServiceInfo> mockServices);

  Map<Long, MockServiceStatus> getMockServiceStatus(List<MockService> services);

  Map<Long, MockServiceStatus> getMockServiceInfoStatus(List<MockServiceInfo> services);

  Map<Long, Long> findProjectMockIdsMap(Set<Long> projectIds);

  Boolean isAuthCtrl(Long id);

}
