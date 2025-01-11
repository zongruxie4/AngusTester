package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisLogQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisLogSearch;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisLogFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log.MockApisLogFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log.MockApisLogSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockApisLogAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log.MockApisLogDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log.MockApisLogListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class MockApisLogFacadeImpl implements MockApisLogFacade {

  @Resource
  private MockApisLogQuery mockApisLogQuery;

  @Resource
  private MockApisLogSearch mockApisLogSearch;

  @NameJoin
  @Override
  public MockApisLogDetailVo detail(Long id) {
    MockApisLog log = mockApisLogQuery.detail(id);
    return MockApisLogAssembler.toApiLogDetailVo(log);
  }

  @Override
  public PageResult<MockApisLogListVo> list(Long mockServiceId, MockApisLogFindDto dto) {
    Page<MockApisLogInfo> mockApisPage = mockApisLogQuery
        .list(mockServiceId, MockApisLogAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(mockApisPage, MockApisLogAssembler::toApisLogListVo);
  }

  @Override
  public PageResult<MockApisLogListVo> search(Long mockServiceId, MockApisLogSearchDto dto) {
    Page<MockApisLogInfo> page = mockApisLogSearch
        .search(mockServiceId, MockApisLogAssembler.getSearchCriteria(dto), dto.tranPage(),
            MockApisLogInfo.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, MockApisLogAssembler::toApisLogListVo);
  }

}
