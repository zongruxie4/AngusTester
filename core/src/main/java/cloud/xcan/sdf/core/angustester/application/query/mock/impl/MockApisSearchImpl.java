package cloud.xcan.sdf.core.angustester.application.query.mock.impl;

import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisSearch;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisSearchRepo;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceInfo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class MockApisSearchImpl implements MockApisSearch {

  @Resource
  private MockApisSearchRepo mockApisSearchRepo;

  @Resource
  private MockApisQuery mockApisQuery;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public Page<MockApis> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<MockApis> clz, String... matches) {
    return new BizTemplate<Page<MockApis>>() {
      MockServiceInfo mockServiceDb;

      @Override
      protected void checkParams() {
        // Check the view permission
        String mockServiceId = findFirstValue(criteria, "mockServiceId");
        mockServiceAuthQuery.checkViewAuth(getUserId(), Long.parseLong(mockServiceId));

        // Check the mock service exits
        mockServiceDb = mockServiceQuery.checkAndFindInfo(Long.parseLong(mockServiceId));
      }

      @Override
      protected Page<MockApis> process() {
        Page<MockApis> page = mockApisSearchRepo.find(criteria, pageable, clz, matches);
        if (page.isEmpty()) {
          return page;
        }

        // Set mock service info
        for (MockApis mockApis : page.getContent()) {
          mockApisQuery.setMockServiceInfo(mockApis, mockServiceDb);
        }
        return page;
      }
    }.execute();
  }

}
