package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisAssembler.toDetailVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisCmd;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisSearch;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisSearchDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisAssembler;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisDetailVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * @author XiaoLong Liu
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
  public List<IdKey<Long, Object>> add(List<MockApisAddDto> dto) {
    List<MockApis> apis = dto.stream().map(MockApisAssembler::addDtoToDomain)
        .collect(Collectors.toList());
    return mockApisCmd.add(apis, true);
  }

  @Override
  public void update(List<MockApisUpdateDto> dto) {
    List<MockApis> mockApis = dto.stream()
        .map(MockApisAssembler::updateDtoToDomain).collect(Collectors.toList());
    mockApisCmd.update(mockApis);
  }

  @Override
  public List<IdKey<Long, Object>> replace(List<MockApisReplaceDto> dto) {
    List<MockApis> mockApis = dto.stream()
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
    return toDetailVo(mockApisQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<MockApisListVo> list(MockApisFindDto dto) {
    GenericSpecification<MockApis> spec = getSpecification(dto);
    // mockServiceId filter is required
    boolean queryAll = spec.getCriteria().size() <= 1;
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
