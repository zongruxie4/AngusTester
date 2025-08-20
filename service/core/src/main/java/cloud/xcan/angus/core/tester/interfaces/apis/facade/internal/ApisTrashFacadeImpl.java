package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisTrashAssembler.getSpecification;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.ApisTargetType;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTrashCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTrashQuery;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.trash.ApisTrashFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisTrashAssembler;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.trash.ApisTrashDetailVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ApisTrashFacadeImpl implements ApisTrashFacade {

  @Resource
  private ApisTrashCmd apisTrashCmd;

  @Resource
  private ApisTrashQuery apisTrashQuery;

  @Resource
  private ApisQuery apisQuery;

  @Override
  public void clear(Long id) {
    apisTrashCmd.clear(id);
  }

  @Override
  public void clearAll(Long projectId) {
    apisTrashCmd.clearAll(projectId);
  }

  @Override
  public void back(Long id) {
    apisTrashCmd.back(id);
  }

  @Override
  public void backAll(Long projectId) {
    apisTrashCmd.backAll(projectId);
  }

  @Override
  public Long count(Long projectId) {
    return apisTrashQuery.count(projectId);
  }

  @Override
  public PageResult<ApisTrashDetailVo> list(ApisTrashFindDto dto) {
    Page<ApisTrash> page = apisTrashQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    if (page.isEmpty()) {
      return PageResult.empty();
    }
    PageResult<ApisTrashDetailVo> detailPage
        = buildVoPageResult(page, ApisTrashAssembler::toDetailVo);
    if (ApisTargetType.API.equals(dto.getTargetType())) {
      setApiMethodAndUri(page, detailPage);
    }
    return detailPage;
  }

  private void setApiMethodAndUri(Page<ApisTrash> trashPage, PageResult<ApisTrashDetailVo> page) {
    List<ApisTrash> trash = trashPage.getContent();
    List<ApisBaseInfo> apis = apisQuery.findBase0ByIdIn(
        trash.stream().map(ApisTrash::getTargetId).toList());
    Map<Long, ApisBaseInfo> apisMap = apis.stream()
        .collect(Collectors.toMap(ApisBaseInfo::getId, Function.identity()));
    for (ApisTrashDetailVo apisDetailVo : page.getList()) {
      ApisBaseInfo a = apisMap.get(apisDetailVo.getTargetId());
      if (isNotEmpty(a)) {
        apisDetailVo.setProtocol(a.getProtocol());
        apisDetailVo.setMethod(a.getMethod());
        apisDetailVo.setEndpoint(a.getEndpoint());
      }
    }
  }

}




