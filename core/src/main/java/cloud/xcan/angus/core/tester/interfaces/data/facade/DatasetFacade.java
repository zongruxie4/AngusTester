package cloud.xcan.angus.core.tester.interfaces.data.facade;

import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetExportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetImportDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetSearchDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset.DatasetValuePreviewDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset.DatasetDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface DatasetFacade {

  IdKey<Long, Object> add(DatasetAddDto dto);

  void update(DatasetUpdateDto dto);

  IdKey<Long, Object> replace(DatasetReplaceDto dto);

  List<IdKey<Long, Object>> clone(HashSet<Long> ids);

  List<IdKey<Long, Object>> imports(DatasetImportDto dto);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

  DatasetDetailVo detail(Long id);

  LinkedHashMap<String, List<String>> valuePreview(DatasetValuePreviewDto dto);

  PageResult<DatasetDetailVo> list(DatasetFindDto dto);

  PageResult<DatasetDetailVo> search(DatasetSearchDto dto);

  ResponseEntity<Resource> export(DatasetExportDto dto, HttpServletResponse response);

}
