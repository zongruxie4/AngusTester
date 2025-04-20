package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisLogSearch;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class MockApisLogSearchImpl implements MockApisLogSearch {

  @Resource
  private MockApisLogSearchRepo mockApisLogSearchRepo;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public Page<MockApisLogInfo> search(Long mockServiceId, Set<SearchCriteria> criteria,
      PageRequest pageable, Class<MockApisLogInfo> clz, String... matches) {
    return new BizTemplate<Page<MockApisLogInfo>>() {
      @Override
      protected void checkParams() {
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockServiceId);
      }

      @Override
      protected Page<MockApisLogInfo> process() {
        criteria.add(SearchCriteria.equal("mockServiceId", mockServiceId));
        return mockApisLogSearchRepo.find(criteria, pageable, clz, matches);
      }
    }.execute();
  }
}
