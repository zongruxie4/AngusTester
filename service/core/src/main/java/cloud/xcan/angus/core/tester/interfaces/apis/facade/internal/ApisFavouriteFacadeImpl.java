package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal;

import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.apis.ApisFavouriteCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteP;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.favourite.ApisFavouriteSearchDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisFavouriteAssembler;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.favourite.ApisFavouriteDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ApisFavouriteFacadeImpl implements ApisFavouriteFacade {

  @Resource
  private ApisFavouriteCmd apisFavouriteCmd;

  @Resource
  private ApisFavouriteQuery apisFavouriteQuery;

  @Override
  public IdKey<Long, Object> add(Long apisId) {
    return apisFavouriteCmd.add(ApisFavouriteAssembler.addDtoToDomain(apisId));
  }

  @Override
  public void cancel(Long apisId) {
    apisFavouriteCmd.cancel(apisId);
  }

  @Override
  public void cancelAll(Long projectId) {
    apisFavouriteCmd.cancelAll(projectId);
  }

  @Override
  public PageResult<ApisFavouriteDetailVo> search(ApisFavouriteSearchDto dto) {
    Page<ApisFavouriteP> pageResult = apisFavouriteQuery.search(dto.getProjectId(),
        dto.getApisName(), dto.tranPage());
    return buildVoPageResult(pageResult, ApisFavouriteAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return apisFavouriteQuery.count(projectId);
  }
}




