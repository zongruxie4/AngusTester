package cloud.xcan.angus.core.tester.interfaces.data.facade;

import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableExportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableImportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable.VariableValuePreviewDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface VariableFacade {

  IdKey<Long, Object> add(VariableAddDto dto);

  void update(VariableUpdateDto dto);

  IdKey<Long, Object> replace(VariableReplaceDto dto);

  List<IdKey<Long, Object>> clone(HashSet<Long> ids);

  List<IdKey<Long, Object>> imports(VariableImportDto dto);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

  VariableDetailVo detail(Long id);

  String valuePreview(VariableValuePreviewDto dto);

  PageResult<VariableDetailVo> list(VariableFindDto dto);

  ResponseEntity<Resource> export(VariableExportDto dto, HttpServletResponse response);

}
