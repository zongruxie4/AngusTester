package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisLogQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfoRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author XiaoLong Liu
 */
@Biz
public class MockApisLogQueryImpl implements MockApisLogQuery {

  @Resource
  private MockApisLogRepo mockApisLogRepo;

  @Resource
  private MockApisLogInfoRepo mockApisLogInfoRepo;

  @Resource
  private MockApisLogSearchRepo mockApisLogSearchRepo;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public MockApisLog detail(Long id) {
    return new BizTemplate<MockApisLog>() {

      @Override
      protected MockApisLog process() {
        return mockApisLogRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "MockApiLog"));
      }
    }.execute();
  }

  @Override
  public Page<MockApisLogInfo> list(Long mockServiceId, GenericSpecification<MockApisLogInfo> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<MockApisLogInfo>>() {
      @Override
      protected void checkParams() {
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockServiceId);
      }

      @Override
      protected Page<MockApisLogInfo> process() {
        spec.getCriteria().add(SearchCriteria.equal("mockServiceId", mockServiceId));
        return fullTextSearch
            ? mockApisLogSearchRepo.find(spec.getCriteria(), pageable, MockApisLogInfo.class, match)
            : mockApisLogInfoRepo.findAll(spec, pageable);
      }
    }.execute();
  }

}
