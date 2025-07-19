package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisDesignAssembler.addToDomain;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisDesignAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisDesignAssembler.toDetailVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisDesignCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisDesignQuery;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisDesignFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignContentReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignExportDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignImportDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignUpdateNameDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisDesignAssembler;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.design.ApisDesignDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.design.ApisDesignVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashSet;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ApisDesignFacadeImpl implements ApisDesignFacade {

  @Resource
  private ApisDesignCmd apisDesignCmd;

  @Resource
  private ApisDesignQuery apisDesignQuery;

  @Override
  public IdKey<Long, Object> add(ApisDesignAddDto dto) {
    return apisDesignCmd.add(addToDomain(dto));
  }

  @Override
  public void updateName(ApisDesignUpdateNameDto dto) {
    apisDesignCmd.updateName(dto.getId(), dto.getName());
  }

  @Override
  public void replaceContent(ApisDesignContentReplaceDto dto) {
    apisDesignCmd.replaceContent(dto.getId(), dto.getOpenapi());
  }

  @Override
  public void release(Long id) {
    apisDesignCmd.release(id);
  }

  @Override
  public IdKey<Long, Object> clone(Long id) {
    return apisDesignCmd.clone(id);
  }

  @Override
  public void servicesAssociate(Long serviceId) {
    apisDesignCmd.servicesAssociate(serviceId);
  }

  @Override
  public void servicesGenerate(Long id) {
    apisDesignCmd.servicesGenerate(id);
  }

  @Override
  public IdKey<Long, Object> imports(ApisDesignImportDto dto) {
    return apisDesignCmd.imports(dto.getProjectId(), dto.getName(), dto.getContent(),
        dto.getFile());
  }

  @Override
  public void delete(HashSet<Long> ids) {
    apisDesignCmd.delete(ids);
  }

  @NameJoin
  @Override
  public ApisDesignDetailVo detail(Long id) {
    return toDetailVo(apisDesignQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<ApisDesignVo> list(ApisDesignFindDto dto) {
    Page<ApisDesignInfo> page = apisDesignQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ApisDesignAssembler::toVo);
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(
      ApisDesignExportDto dto, HttpServletResponse response) {
    File file = apisDesignCmd.export(dto.getId(), dto.getFormat());
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM, file);
  }
}
