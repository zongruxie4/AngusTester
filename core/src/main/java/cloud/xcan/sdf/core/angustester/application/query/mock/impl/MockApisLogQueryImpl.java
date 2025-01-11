package cloud.xcan.sdf.core.angustester.application.query.mock.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisLogQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author xiaolong.liu
 */
@Biz
public class MockApisLogQueryImpl implements MockApisLogQuery {

  @Resource
  private MockApisLogRepo mockApisLogRepo;

  @Resource
  private MockApisLogInfoRepo mockApisLogInfoRepo;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public MockApisLog detail(Long id) {
    return new BizTemplate<MockApisLog>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected MockApisLog process() {
        return mockApisLogRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "MockApiLog"));
      }
    }.execute();
  }

  @Override
  public Page<MockApisLogInfo> list(Long mockServiceId,
      GenericSpecification<MockApisLogInfo> spec, PageRequest pageable) {
    return new BizTemplate<Page<MockApisLogInfo>>() {
      @Override
      protected void checkParams() {
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockServiceId);
      }

      @Override
      protected Page<MockApisLogInfo> process() {
        spec.getCriterias().add(SearchCriteria.equal("mockServiceId", mockServiceId));
        return mockApisLogInfoRepo.findAll(spec, pageable);
      }
    }.execute();
  }

}
