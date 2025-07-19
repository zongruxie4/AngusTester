package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_APIS_ASSOC_APIS_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_APIS_NAME_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_APIS_OPERATION_EXISTED_T;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisOperationP;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSearchRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.http.PathMatchers;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
@SummaryQueryRegister(name = "MockApis", table = "mock_apis",
    groupByColumns = {"created_date", "method", "source"},
    aggregateColumns = {"id", "request_num", "pushback_num", "simulate_error_num",
        "success_num", "exception_num"})
public class MockApisQueryImpl implements MockApisQuery {

  @Resource
  private MockApisRepo mockApisRepo;

  @Resource
  private MockApisResponseRepo apisResponseRepo;

  @Resource
  private MockApisSearchRepo mockApisSearchRepo;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public MockApis detail(Long id) {
    return new BizTemplate<MockApis>() {
      MockApis mockApisDb;
      MockServiceInfo mockServiceDb;
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the mock apis exists
        mockApisDb = checkAndFind(id);

        // Check the view permission
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockApisDb.getMockServiceId());

        // Check the mock service exits
        mockServiceDb = mockServiceQuery.checkAndFindInfo(mockApisDb.getMockServiceId());

        // Check the apis exists
        if (nonNull(mockApisDb.getAssocApisId())) {
          apisDb = apisQuery.findBaseByIdIn(mockApisDb.getAssocApisId());
        }
      }

      @Override
      protected MockApis process() {
        // Set mock service info
        setMockServiceInfo(mockApisDb, mockServiceDb);

        // Set inconsistent operation info
        if (nonNull(mockApisDb.getAssocApisId())) {
          setInconsistentOperationInfo(mockApisDb, apisDb);
        }

        // Set mock apis responses
        mockApisDb.setResponses(apisResponseRepo.findAllByMockApisId(id));

        // Set apis deleted status
        mockApisDb.setAssocApisDeleted(isNull(mockApisDb.getAssocApisId()) || nonNull(apisDb));
        return mockApisDb;
      }
    }.execute();
  }

  @Override
  public List<MockApis> info0(Long mockServiceId, String method, String endpoint) {
    return new BizTemplate<List<MockApis>>() {
      @Override
      protected void checkParams() {
        // NOOP -> Call by open2p
      }

      @Override
      protected List<MockApis> process() {
        List<MockApisOperationP> operations = mockApisRepo
            .findAllOperationByMockServiceId(mockServiceId);
        if (isNotEmpty(operations)) {
          if (isEmpty(method) || isEmpty(endpoint)) {
            // Find all mock apis
            List<MockApis> mockApis = mockApisRepo.findByMockServiceId(mockServiceId);
            if (isNotEmpty(mockApis)) {
              Map<Long, List<MockApisResponse>> mockApisResponsesMap = apisResponseRepo
                  .findByMockServiceId(mockServiceId).stream()
                  .collect(Collectors.groupingBy(MockApisResponse::getMockApisId));
              if (isNotEmpty(mockApisResponsesMap)) {
                for (MockApis mockApi : mockApis) {
                  mockApi.setResponses(mockApisResponsesMap.get(mockApi.getId()));
                }
              }
            }
            return mockApis;
          } else {
            // Find one mock apis
            Optional<MockApisOperationP> operation = operations.stream()
                .filter(x -> method.equalsIgnoreCase(x.getMethod())
                    && PathMatchers.getPathMatcher().match(x.getEndpoint(), endpoint))
                .findFirst();
            if (operation.isPresent()) {
              MockApis mockApis = checkAndFind(operation.get().getId());
              List<MockApisResponse> mockApisResponses = apisResponseRepo
                  .findAllByMockApisId(operation.get().getId());
              mockApis.setResponses(mockApisResponses);
              return List.of(mockApis);
            }
          }
        }
        return null;
      }
    }.execute();
  }

  @Override
  public Page<MockApis> list(GenericSpecification<MockApis> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<MockApis>>() {
      MockServiceInfo mockServiceDb;

      @Override
      protected void checkParams() {
        // Check the view permission
        String mockServiceId = findFirstValue(spec.getCriteria(), "mockServiceId");
        mockServiceAuthQuery.checkViewAuth(getUserId(), Long.parseLong(mockServiceId));

        // Check the mock service exits
        mockServiceDb = mockServiceQuery.checkAndFindInfo(Long.parseLong(mockServiceId));
      }

      @Override
      protected Page<MockApis> process() {
        Page<MockApis> page = fullTextSearch
            ? mockApisSearchRepo.find(spec.getCriteria(), pageable, MockApis.class, match)
            : mockApisRepo.findAll(spec, pageable);
        if (page.isEmpty()) {
          return page;
        }

        for (MockApis mockApis : page.getContent()) {
          setMockServiceInfo(mockApis, mockServiceDb);
        }
        return page;
      }
    }.execute();
  }

  @Override
  public MockApis checkAndFind(Long id) {
    return mockApisRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "MockApis"));
  }

  @Override
  public List<MockApis> checkAndFind(Collection<Long> reqApisIds) {
    List<MockApis> services = mockApisRepo.findAllById(reqApisIds);
    Set<Long> serviceIds = services.stream().map(MockApis::getId).collect(Collectors.toSet());
    reqApisIds.removeAll(serviceIds);
    assertResourceNotFound(isEmpty(reqApisIds), reqApisIds, "MockApis");
    return services;
  }

  @Override
  public void checkAddNameExists(Long serviceId, List<MockApis> apis) {
    List<String> existedNames = mockApisRepo.findSummariesByMockServiceIdAndSummaryIn(
        serviceId, apis.stream().map(MockApis::getName).collect(Collectors.toSet()));
    assertResourceExisted(existedNames.isEmpty(), existedNames, "MockApis");
  }

  @Override
  public void checkUpdateNameExists(Long serviceId, List<MockApis> apis) {
    // Note: Update name is optional
    Set<String> reqNames = apis.stream().map(MockApis::getName).collect(Collectors.toSet());
    if (isEmpty(reqNames)) {
      return;
    }
    List<MockApis> existedApis = mockApisRepo
        .findAllByMockServiceIdAndSummaryIn(serviceId, reqNames);
    if (isNotEmpty(existedApis)) {
      for (MockApis existedApi : existedApis) {
        for (MockApis api : apis) {
          assertResourceExisted(!Objects.equals(existedApi.getName(), api.getName())
                  || existedApi.getId().equals(api.getId()), MOCK_APIS_NAME_EXISTED_T,
              new Object[]{api.getName()});
        }
      }
    }
  }

  @Override
  public void checkAddOperationExists(Long serviceId, List<MockApis> apis) {
    // Note: Update method and endpoint is required
    List<MockApis> existedApis = mockApisRepo.findByMockServiceIdAndEndpointIn(
        serviceId, apis.stream().map(MockApis::getEndpoint).collect(Collectors.toSet()));
    if (isNotEmpty(existedApis)) {
      for (MockApis existedApi : existedApis) {
        for (MockApis api : apis) {
          assertResourceExisted(!existedApi.sameIdentityAs(api),
              MOCK_APIS_OPERATION_EXISTED_T, new Object[]{api.getMethod(), api.getEndpoint()});
        }
      }
    }
  }

  @Override
  public void checkUpdateOperationExists(Long serviceId, List<MockApis> apis) {
    List<MockApis> existedApis = mockApisRepo.findByMockServiceIdAndEndpointIn(
        serviceId, apis.stream().map(MockApis::getEndpoint).collect(Collectors.toSet()));
    if (isNotEmpty(existedApis)) {
      for (MockApis existedApi : existedApis) {
        for (MockApis api : apis) {
          assertResourceExisted(!existedApi.sameIdentityAs(api)
                  || existedApi.getId().equals(api.getId()), MOCK_APIS_OPERATION_EXISTED_T,
              new Object[]{api.getMethod(), api.getEndpoint()});
        }
      }
    }
  }

  /**
   * Check that only a maximum of apis can be added to a mock service
   */
  @Override
  public void checkServiceApisQuota(MockService mockServiceDb, int incr) {
    long count = mockApisRepo.countAllByMockServiceId(mockServiceDb.getId());
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterMockServiceApis,
        Set.of(mockServiceDb.getId()), incr + count);
  }

  @Override
  public void checkAssocApisExists(Apis apisDb) {
    ProtocolAssert.assertTrue(!mockApisRepo.existsByAssocApisId(apisDb.getId()),
        MOCK_APIS_ASSOC_APIS_EXISTED_T, new Object[]{apisDb.getName()});
  }

  @Override
  public void checkAssocApisExists(ApisBaseInfo apisDb) {
    ProtocolAssert.assertTrue(!mockApisRepo.existsByAssocApisId(apisDb.getId()),
        MOCK_APIS_ASSOC_APIS_EXISTED_T, new Object[]{apisDb.getName()});
  }

  @Override
  public void checkAssocApissExists(List<MockApis> mockApis) {
    List<Long> apisIds = mockApis.stream().filter(x -> nonNull(x.getAssocApisId()))
        .map(MockApis::getAssocApisId).collect(Collectors.toList());
    if (isNotEmpty(apisIds)) {
      List<ApisBaseInfo> apis = apisQuery.checkAndFindBaseInfos(apisIds);
      if (isNotEmpty(apis)) {
        for (ApisBaseInfo api : apis) {
          ProtocolAssert.assertTrue(!mockApisRepo.existsByAssocApisId(api.getId()),
              MOCK_APIS_ASSOC_APIS_EXISTED_T, new Object[]{api.getName()});
        }
      } else {
        throw ResourceNotFound.of(apisIds.get(0), "Apis");
      }
    }
  }

  @Override
  public void setSafeCloneName(MockApis apis) {
    String saltName = randomAlphanumeric(3);
    String clonedName = mockApisRepo.existsByMockServiceIdAndSummary(apis.getMockServiceId(),
        apis.getName() + "-Copy") ? apis.getName() + "-Copy." + saltName : apis.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_OPENAPI_SUMMARY_LENGTH ? clonedName.substring(0,
        MAX_OPENAPI_SUMMARY_LENGTH - 3) + saltName : clonedName;
    apis.setSummary(clonedName);
    apis.setEndpoint(stringSafe(apis.getEndpoint()) + "-Copy." + saltName);
  }

  @Override
  public void setMockServiceInfo(MockApis mockApisDb, MockServiceInfo mockServiceDb) {
    mockApisDb.setMockServiceName(mockServiceDb.getName())
        .setMockServiceDomainUrl(mockServiceDb.getServiceDomainUrl())
        .setMockServiceHostUrl(mockServiceDb.getServiceHostUrl());
  }

  private void setInconsistentOperationInfo(MockApis mockApisDb, ApisBaseInfo apisDb) {
    if (!Objects.equals(apisDb.getEndpoint(), mockApisDb.getEndpoint()) ||
        !Objects.equals(apisDb.getMethod(), mockApisDb.getMethod())) {
      mockApisDb.setInconsistentOperation(true)
          .setApisEndpoint(apisDb.getEndpoint()).setApisMethod(apisDb.getMethod());
    } else {
      mockApisDb.setInconsistentOperation(false);
    }
  }
}
