package cloud.xcan.sdf.core.angustester.interfaces.version.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.version.SoftwareVersionCmd;
import cloud.xcan.sdf.core.angustester.application.query.version.SoftwareVersionQuery;
import cloud.xcan.sdf.core.angustester.application.query.version.SoftwareVersionSearch;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersion;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersionStatus;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.SoftwareVersionFacade;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.internal.assembler.SoftwareVersionAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.vo.SoftwareVersionDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.vo.SoftwareVersionVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SoftwareVersionFacadeImpl implements SoftwareVersionFacade {

  @Resource
  private SoftwareVersionCmd softwareVersionCmd;

  @Resource
  private SoftwareVersionQuery softwareVersionQuery;

  @Resource
  private SoftwareVersionSearch softwareVersionSearch;

  @Override
  public IdKey<Long, Object> add(SoftwareVersionAddDto dto) {
    SoftwareVersion version = SoftwareVersionAssembler.toAddDomain(dto);
    return softwareVersionCmd.add(version);
  }

  @Override
  public void update(SoftwareVersionUpdateDto dto) {
    SoftwareVersion version = SoftwareVersionAssembler.toUpdateDomain(dto);
    softwareVersionCmd.update(version);
  }

  @Override
  public IdKey<Long, Object> replace(SoftwareVersionReplaceDto dto) {
    SoftwareVersion version = SoftwareVersionAssembler.toReplaceDomain(dto);
    return softwareVersionCmd.replace(version);
  }

  @Override
  public void status(Long id, SoftwareVersionStatus status) {
    softwareVersionCmd.status(id, status);
  }

  @Override
  public void merge(Long formId, Long toId) {
    softwareVersionCmd.merge(formId, toId);
  }

  @Override
  public void delete(Collection<Long> ids) {
    softwareVersionCmd.delete(ids);
  }

  @NameJoin
  @Override
  public SoftwareVersionDetailVo detail(Long id) {
    SoftwareVersion version = softwareVersionQuery.detail(id);
    return SoftwareVersionAssembler.toDetailVo(version);
  }

  @NameJoin
  @Override
  public PageResult<SoftwareVersionVo> list(SoftwareVersionFindDto dto) {
    Page<SoftwareVersion> page = softwareVersionQuery
        .find(SoftwareVersionAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, SoftwareVersionAssembler::toVo);
  }

  @NameJoin
  @Override
  public PageResult<SoftwareVersionVo> search(SoftwareVersionSearchDto dto) {
    Page<SoftwareVersion> page = softwareVersionSearch
        .search(SoftwareVersionAssembler.getSearchCriteria(dto), dto.tranPage(),
            SoftwareVersion.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, SoftwareVersionAssembler::toVo);
  }
}
