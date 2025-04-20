package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.apis.ApisUnarchivedCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisUnarchivedQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisUnarchivedSearch;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisUnarchivedFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedSearchDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisUnarchivedAssembler;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisUnarchivedDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisUnarchivedListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ApisUnarchivedFacadeImpl implements ApisUnarchivedFacade {

  @Resource
  private ApisUnarchivedCmd apisUnarchivedCmd;

  @Resource
  private ApisUnarchivedQuery apisUnarchivedQuery;

  @Resource
  private ApisUnarchivedSearch apisUnarchivedSearch;

  @Override
  public List<IdKey<Long, Object>> add(List<ApisUnarchivedAddDto> dtos) {
    return apisUnarchivedCmd.add(
        dtos.stream().map(ApisUnarchivedAssembler::addDtoToDomain).collect(Collectors.toList()));
  }

  @Override
  public void update(List<ApisUnarchivedUpdateDto> dtos) {
    apisUnarchivedCmd.update(
        dtos.stream().map(ApisUnarchivedAssembler::updateDtoToDomain).collect(Collectors.toList()));
  }

  @Override
  public void delete(Long id) {
    apisUnarchivedCmd.delete(id, false);
  }

  @Override
  public void rename(Long id, String name) {
    apisUnarchivedCmd.rename(id, name);
  }

  @Override
  public void deleteAll() {
    apisUnarchivedCmd.delete(null, true);
  }

  @Override
  public ApisUnarchivedDetailVo detail(Long id) {
    return ApisUnarchivedAssembler.toApisUnarchivedDetailVo(apisUnarchivedQuery.detail(id));
  }

  @Override
  public Long count(Long projectId) {
    return apisUnarchivedQuery.count(projectId);
  }

  @Override
  public PageResult<ApisUnarchivedListVo> list(ApisUnarchivedFindDto dto) {
    Page<ApisUnarchived> apisPage = apisUnarchivedQuery
        .find(ApisUnarchivedAssembler.getSpecification(dto), dto.tranPage(), ApisUnarchived.class);
    return buildVoPageResult(apisPage, ApisUnarchivedAssembler::toApisUnarchivedListVo);
  }

  @Override
  public PageResult<ApisUnarchivedListVo> search(ApisUnarchivedSearchDto dto) {
    Page<ApisUnarchived> apisPage = apisUnarchivedSearch
        .search(ApisUnarchivedAssembler.getSearchCriteria(dto), dto.tranPage(),
            ApisUnarchived.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(apisPage, ApisUnarchivedAssembler::toApisUnarchivedListVo);
  }
}
