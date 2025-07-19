package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisShareAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisShareAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisShareAssembler.toShareAddVo;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisShareAssembler.toShareDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisShareAssembler.toVo;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisShareAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisShareCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisShareQuery;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShare;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisShareFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareViewDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisShareAssembler;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareAddVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareViewVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ApisShareFacadeImpl implements ApisShareFacade {

  @Resource
  private ApisShareQuery apisShareQuery;

  @Resource
  private ApisShareCmd apisShareCmd;

  @Override
  public ApisShareAddVo add(ApisShareAddDto dto) {
    ApisShare share = addDtoToDomain(dto);
    return toShareAddVo(apisShareCmd.add(share));
  }

  @Override
  public void update(ApisShareUpdateDto dto) {
    apisShareCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public void delete(Collection<Long> ids) {
    apisShareCmd.delete(ids);
  }

  @NameJoin
  @Override
  public ApisShareVo detail(Long id) {
    return toVo(apisShareQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<ApisShareVo> list(ApisShareFindDto dto) {
    Page<ApisShare> page = apisShareQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ApisShareAssembler::toVo);
  }

  @Override
  public ApisShareViewVo view(ApisShareViewDto dto) {
    ApisShare share = apisShareQuery.view(dto.getId(), dto.getPat());
    apisShareCmd.incrView(dto.getId());
    return toShareDetailVo(share);
  }

}
