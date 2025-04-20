package cloud.xcan.angus.core.tester.interfaces.script.facade;

import cloud.xcan.angus.api.tester.script.ScriptDetailVo;
import cloud.xcan.angus.api.tester.script.dto.ScriptAddDto;
import cloud.xcan.angus.api.tester.script.dto.ScriptFindDto;
import cloud.xcan.angus.api.tester.script.vo.AngusScriptDetailVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfoListVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfoVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfosVo;
import cloud.xcan.angus.core.tester.domain.script.ScriptFormat;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptImportDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptSearchDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.ScriptUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.ScriptListVo;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface ScriptFacade {

  IdKey<Long, Object> add(ScriptAddDto dto);

  void update(ScriptUpdateDto dto);

  IdKey<Long, Object> replace(ScriptReplaceDto dto);

  IdKey<Long, Object> angusAdd(Long projectId, AngusScript script);

  void angusReplace(Long id, AngusScript script);

  void delete(Collection<Long> ids);

  IdKey<Long, Object> clone(Long id);

  IdKey<Long, Object> imports(ScriptImportDto dto);

  List<IdKey<Long, Object>> importExample(Long projectId);

  ScriptDetailVo detail(Long id);

  ScriptInfoVo info(Long id);

  List<ScriptInfosVo> infos(Set<Long> ids);

  AngusScriptDetailVo angusDetail(Long id);

  PageResult<ScriptListVo> list(ScriptFindDto dto);

  PageResult<ScriptInfoListVo> infoList(ScriptFindDto dto);

  PageResult<ScriptListVo> search(ScriptSearchDto dto);

  ResponseEntity<Resource> export(Long id, ScriptFormat format, HttpServletResponse response);

}
