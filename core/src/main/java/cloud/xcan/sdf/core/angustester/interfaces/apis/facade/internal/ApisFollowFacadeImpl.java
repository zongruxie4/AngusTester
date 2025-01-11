package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisFollowCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisFollowQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.follow.ApisFollowP;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisFollowFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.follow.ApisFollowSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler.ApisFollowAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.follow.ApisFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ApisFollowFacadeImpl implements ApisFollowFacade {

  @Resource
  private ApisFollowCmd apisFollowCmd;

  @Resource
  private ApisFollowQuery apisFollowQuery;

  @Override
  public IdKey<Long, Object> add(Long apiId) {
    return apisFollowCmd.add(ApisFollowAssembler.addDtoToDomain(apiId));
  }

  @Override
  public void cancel(Long apisId) {
    apisFollowCmd.cancel(apisId);
  }

  @Override
  public void cancelAll(Long projectId) {
    apisFollowCmd.cancelAll(projectId);
  }

  @Override
  public PageResult<ApisFollowDetailVo> search(ApisFollowSearchDto dto) {
    Page<ApisFollowP> pageResult = apisFollowQuery.search(dto.getProjectId(),
        dto.getApisName(), dto.tranPage());
    return buildVoPageResult(pageResult, ApisFollowAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return apisFollowQuery.count(projectId);
  }

}




