package cloud.xcan.angus.core.tester.interfaces.version.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.version.facade.internal.assembler.SoftwareVersionAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.version.facade.internal.assembler.SoftwareVersionAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.version.facade.internal.assembler.SoftwareVersionAssembler.toAddDomain;
import static cloud.xcan.angus.core.tester.interfaces.version.facade.internal.assembler.SoftwareVersionAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.version.facade.internal.assembler.SoftwareVersionAssembler.toReplaceDomain;
import static cloud.xcan.angus.core.tester.interfaces.version.facade.internal.assembler.SoftwareVersionAssembler.toUpdateDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.version.SoftwareVersionCmd;
import cloud.xcan.angus.core.tester.application.query.version.SoftwareVersionQuery;
import cloud.xcan.angus.core.tester.application.query.version.SoftwareVersionSearch;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersion;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersionStatus;
import cloud.xcan.angus.core.tester.interfaces.version.facade.SoftwareVersionFacade;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionAddDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionFindDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionSearchDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.internal.assembler.SoftwareVersionAssembler;
import cloud.xcan.angus.core.tester.interfaces.version.facade.vo.SoftwareVersionDetailVo;
import cloud.xcan.angus.core.tester.interfaces.version.facade.vo.SoftwareVersionVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
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
    return softwareVersionCmd.add(toAddDomain(dto));
  }

  @Override
  public void update(SoftwareVersionUpdateDto dto) {
    softwareVersionCmd.update(toUpdateDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(SoftwareVersionReplaceDto dto) {
    return softwareVersionCmd.replace(toReplaceDomain(dto));
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
    return toDetailVo(softwareVersionQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<SoftwareVersionVo> list(SoftwareVersionFindDto dto) {
    Page<SoftwareVersion> page = softwareVersionQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, SoftwareVersionAssembler::toVo);
  }

  @NameJoin
  @Override
  public PageResult<SoftwareVersionVo> search(SoftwareVersionSearchDto dto) {
    Page<SoftwareVersion> page = softwareVersionSearch.search(getSearchCriteria(dto),
        dto.tranPage(), SoftwareVersion.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, SoftwareVersionAssembler::toVo);
  }
}
