package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisFavouriteCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisFavouriteQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.favourite.ApisFavouriteP;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisFavouriteFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.favourite.ApisFavouriteSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler.ApisFavouriteAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.favourite.ApisFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
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




