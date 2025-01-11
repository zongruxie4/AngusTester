package cloud.xcan.sdf.core.angustester.application.query.mock.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisLogSearch;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class MockApisLogSearchImpl implements MockApisLogSearch {

  @Resource
  private MockApisLogSearchRepo mockApisLogSearchRepo;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public Page<MockApisLogInfo> search(Long mockServiceId, Set<SearchCriteria> criterias,
      PageRequest pageable, Class<MockApisLogInfo> clz, String... matches) {
    return new BizTemplate<Page<MockApisLogInfo>>() {
      @Override
      protected void checkParams() {
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockServiceId);
      }

      @Override
      protected Page<MockApisLogInfo> process() {
        criterias.add(SearchCriteria.equal("mockServiceId", mockServiceId));
        return mockApisLogSearchRepo.find(criterias, pageable, clz, matches);
      }
    }.execute();
  }
}
