package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisResponseQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Biz
public class MockApisResponseQueryImpl implements MockApisResponseQuery {

  @Resource
  private MockApisResponseRepo mockApisResponseRepo;

  @Resource
  private MockApisQuery mockApisQuery;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public List<MockApisResponse> findAllByApisId(Long apisId) {
    return new BizTemplate<List<MockApisResponse>>() {
      @Override
      protected void checkParams() {
        MockApis mockApis = mockApisQuery.checkAndFind(apisId);
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockApis.getMockServiceId());
      }

      @Override
      protected List<MockApisResponse> process() {
        return mockApisResponseRepo.findAllByMockApisId(apisId);
      }
    }.execute();
  }

  @Override
  public void checkAddResponseNameExists(List<MockApisResponse> apisResponses0) {
    if (isNotEmpty(apisResponses0)) {
      Map<Long, List<MockApisResponse>> apisResponsesMap = apisResponses0.stream()
          .collect(Collectors.groupingBy(MockApisResponse::getMockApisId));
      for (Entry<Long, List<MockApisResponse>> entry : apisResponsesMap.entrySet()) {
        List<MockApisResponse> mockApisResponses = entry.getValue().stream()
            .filter(ObjectUtils.duplicateByKey(MockApisResponse::getName))
            .collect(Collectors.toList());
        if (isNotEmpty(mockApisResponses)) {
          throw ResourceExisted.of(mockApisResponses.get(0).getName(), "MockApisResponse");
        }

        List<String> names = entry.getValue().stream()
            .map(MockApisResponse::getName).collect(Collectors.toList());
        List<String> existedNames = mockApisResponseRepo
            .findNamesByMockApisIdAndNameIn(entry.getKey(), names);
        if (isNotEmpty(existedNames)) {
          throw ResourceExisted.of(existedNames.get(0), "MockApisResponse");
        }
      }
    }
  }

}
