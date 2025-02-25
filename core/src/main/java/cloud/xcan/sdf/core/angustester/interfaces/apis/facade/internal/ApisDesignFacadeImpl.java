package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.sdf.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisDesignCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisDesignQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisDesignSearch;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisDesignFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignContentReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignUpdateNameDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler.ApisDesignAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.design.ApisDesignDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.design.ApisDesignVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.io.File;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ApisDesignFacadeImpl implements ApisDesignFacade {

  @Resource
  private ApisDesignCmd apisDesignCmd;

  @Resource
  private ApisDesignQuery apisDesignQuery;

  @Resource
  private ApisDesignSearch apisDesignSearch;

  @Override
  public IdKey<Long, Object> add(ApisDesignAddDto dto) {
    return apisDesignCmd.add(ApisDesignAssembler.addToDomain(dto));
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
  public void servicesGenerate(Long id) {
    apisDesignCmd.servicesGenerate(id);
  }

  @Override
  public IdKey<Long, Object> imports(ApisDesignImportDto dto) {
    return apisDesignCmd.imports(dto.getProjectId(), dto.getName(),
        dto.getContent(), dto.getFile());
  }

  @Override
  public void delete(HashSet<Long> ids) {
    apisDesignCmd.delete(ids);
  }

  @NameJoin
  @Override
  public ApisDesignDetailVo detail(Long id) {
    return ApisDesignAssembler.toDetailVo(apisDesignQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<ApisDesignVo> list(ApisDesignFindDto dto) {
    Page<ApisDesignInfo> page = apisDesignQuery.list(
        ApisDesignAssembler.getSpecification(dto), dto.tranPage(), ApisBasicInfo.class);
    return buildVoPageResult(page, ApisDesignAssembler::toVo);
  }

  @NameJoin
  @Override
  public PageResult<ApisDesignVo> search(ApisDesignSearchDto dto) {
    Page<ApisDesignInfo> page = apisDesignSearch.search(
        ApisDesignAssembler.getSearchCriteria(dto),
        dto.tranPage(), getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ApisDesignAssembler::toVo);
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(
      ApisDesignExportDto dto, HttpServletResponse response) {
    File file = apisDesignCmd.export(dto.getId(), dto.getFormat());
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM, file);
  }
}
