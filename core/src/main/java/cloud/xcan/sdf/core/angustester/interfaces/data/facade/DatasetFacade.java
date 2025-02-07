package cloud.xcan.sdf.core.angustester.interfaces.data.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset.DatasetValuePreviewDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface DatasetFacade {

  IdKey<Long, Object> add(DatasetAddDto dto);

  void update(DatasetUpdateDto dto);

  IdKey<Long, Object> replace(DatasetReplaceDto dto);

  List<IdKey<Long, Object>> clone(HashSet<Long> ids);

  void delete(Collection<Long> ids);

  List<IdKey<Long, Object>> exampleImport(Long projectId);

  List<IdKey<Long, Object>> imports(DatasetImportDto dto);

  ResponseEntity<Resource> export(DatasetExportDto dto, HttpServletResponse response);

  DatasetDetailVo detail(Long id);

  LinkedHashMap<String, List<String>> valuePreview(DatasetValuePreviewDto dto);

  PageResult<DatasetDetailVo> list(DatasetFindDto dto);

  PageResult<DatasetDetailVo> search(DatasetSearchDto dto);

}
