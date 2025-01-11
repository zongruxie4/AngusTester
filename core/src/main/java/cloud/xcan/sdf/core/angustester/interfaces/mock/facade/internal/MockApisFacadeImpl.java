package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockApisCmd;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisSearch;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.MockApisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockApisAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.MockApisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.MockApisListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * @author xiaolong.liu
 */
@Component
public class MockApisFacadeImpl implements MockApisFacade {

  @Resource
  private MockApisCmd mockApisCmd;

  @Resource
  private MockApisQuery mockApisQuery;

  @Resource
  private MockApisSearch mockApisSearch;

  @Override
  public List<IdKey<Long, Object>> add(List<MockApisAddDto> dtos) {
    List<MockApis> apis = dtos.stream().map(MockApisAssembler::addDtoToDomain)
        .collect(Collectors.toList());
    return mockApisCmd.add(apis, true);
  }

  @Override
  public void update(List<MockApisUpdateDto> dtos) {
    List<MockApis> mockApis = dtos.stream()
        .map(MockApisAssembler::updateDtoToDomain).collect(Collectors.toList());
    mockApisCmd.update(mockApis);
  }

  @Override
  public List<IdKey<Long, Object>> replace(List<MockApisReplaceDto> dtos) {
    List<MockApis> mockApis = dtos.stream()
        .map(MockApisAssembler::replaceDtoToDomain).collect(Collectors.toList());
    return mockApisCmd.replace(mockApis);
  }

  @Override
  public IdKey<Long, Object> clone(Long id) {
    return mockApisCmd.clone(id);
  }

  @Override
  public void move(HashSet<Long> ids, Long targetServiceId) {
    mockApisCmd.move(ids, targetServiceId);
  }

  @Override
  public void instanceSync(Long id) {
    mockApisCmd.instanceSync(id);
  }

  @Override
  public IdKey<Long, Object> copyApisAdd(Long mockServiceId, Long apisId) {
    return mockApisCmd.copyApisAdd(mockServiceId, apisId);
  }

  @Override
  public IdKey<Long, Object> assocApisAdd(Long mockServiceId, Long apisId) {
    return mockApisCmd.assocApisAdd(mockServiceId, apisId);
  }

  @Override
  public void assocApisUpdate(Long mockApisId, Long apisId) {
    mockApisCmd.assocApisUpdate(mockApisId, apisId);
  }

  @Override
  public void assocDelete(HashSet<Long> ids) {
    mockApisCmd.assocDelete(ids);
  }

  @Override
  public void delete(Collection<Long> ids) {
    mockApisCmd.delete(ids);
  }

  @NameJoin
  @Override
  public MockApisDetailVo detail(Long id) {
    MockApis apis = mockApisQuery.detail(id);
    return MockApisAssembler.toDetailVo(apis);
  }

  @NameJoin
  @Override
  public PageResult<MockApisListVo> list(MockApisFindDto dto) {
    GenericSpecification<MockApis> spec = MockApisAssembler.getSpecification(dto);
    // mockServiceId filter is required
    boolean queryAll = spec.getCriterias().size() <= 1;
    Page<MockApis> page = mockApisQuery.find(spec, dto.tranPage());
    if (page.isEmpty()) {
      PrincipalContext.addExtension("queryAllEmpty", queryAll);
      return PageResult.empty();
    }
    return buildVoPageResult(page, MockApisAssembler::toApisListVo);
  }

  @Override
  public PageResult<MockApisListVo> search(MockApisSearchDto dto) {
    Set<SearchCriteria> filters = MockApisAssembler.getSearchCriteria(dto);
    boolean queryAll = filters.size() <= 1;
    Page<MockApis> page = mockApisSearch
        .search(filters, dto.tranPage(), MockApis.class, getMatchSearchFields(dto.getClass()));
    if (page.isEmpty()) {
      PrincipalContext.addExtension("queryAllEmpty", queryAll);
      return PageResult.empty();
    }
    return buildVoPageResult(page, MockApisAssembler::toApisListVo);
  }

}
