package cloud.xcan.angus.core.tester.interfaces.apis.facade;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignContentReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignExportDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignImportDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignSearchDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design.ApisDesignUpdateNameDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.design.ApisDesignDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.design.ApisDesignVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashSet;
import org.springframework.http.ResponseEntity;

public interface ApisDesignFacade {

  IdKey<Long, Object> add(ApisDesignAddDto dto);

  void updateName(ApisDesignUpdateNameDto dto);

  void replaceContent(ApisDesignContentReplaceDto dto);

  void release(Long id);

  IdKey<Long, Object> clone(Long id);

  void servicesGenerate(Long id);

  IdKey<Long, Object> imports(ApisDesignImportDto dto);

  void delete(HashSet<Long> ids);

  ApisDesignDetailVo detail(Long id);

  PageResult<ApisDesignVo> list(ApisDesignFindDto dto);

  PageResult<ApisDesignVo> search(ApisDesignSearchDto dto);

  ResponseEntity<org.springframework.core.io.Resource> export(ApisDesignExportDto dto,
      HttpServletResponse response);

}
