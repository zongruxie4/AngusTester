package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisShareCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisShareQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisShareSearch;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShare;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisShareFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareViewDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler.ApisShareAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareAddVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareViewVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import java.util.Collection;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ApisShareFacadeImpl implements ApisShareFacade {

  @Resource
  private ApisShareQuery apisShareQuery;

  @Resource
  private ApisShareSearch apisShareSearch;

  @Resource
  private ApisShareCmd apisShareCmd;

  @Override
  public ApisShareAddVo add(ApisShareAddDto dto) {
    ApisShare share = ApisShareAssembler.addDtoToDomain(dto);
    return ApisShareAssembler.toShareAddVo(apisShareCmd.add(share));
  }

  @Override
  public void update(ApisShareUpdateDto dto) {
    apisShareCmd.update(ApisShareAssembler.updateDtoToDomain(dto));
  }

  @Override
  public void delete(Collection<Long> ids) {
    apisShareCmd.delete(ids);
  }

  @NameJoin
  @Override
  public ApisShareVo detail(Long id) {
    return ApisShareAssembler.toVo(apisShareQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<ApisShareVo> list(ApisShareFindDto dto) {
    Page<ApisShare> pageSpace = apisShareQuery
        .find(ApisShareAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(pageSpace, ApisShareAssembler::toVo);
  }

  @NameJoin
  @Override
  public PageResult<ApisShareVo> search(ApisShareSearchDto dto) {
    Page<ApisShare> pageSpace = apisShareSearch
        .search(ApisShareAssembler.getSearchCriteria(dto), dto.tranPage(), ApisShare.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(pageSpace, ApisShareAssembler::toVo);
  }

  @Override
  public ApisShareViewVo view(ApisShareViewDto dto) {
    ApisShare share = apisShareQuery.view(dto.getId(), dto.getPat());
    apisShareCmd.incrView(dto.getId());
    return ApisShareAssembler.toShareDetailVo(share);
  }

}
