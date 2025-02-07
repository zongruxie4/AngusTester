package cloud.xcan.sdf.core.angustester.interfaces.script.facade;

import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.script.ScriptDetailVo;
import cloud.xcan.sdf.api.angustester.script.dto.ScriptAddDto;
import cloud.xcan.sdf.api.angustester.script.dto.ScriptFindDto;
import cloud.xcan.sdf.api.angustester.script.vo.AngusScriptDetailVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfoListVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfoVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfosVo;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptFormat;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.ScriptImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.ScriptReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.ScriptSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.ScriptUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.ScriptListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface ScriptFacade {

  IdKey<Long, Object> add(ScriptAddDto dto);

  void update(ScriptUpdateDto dto);

  IdKey<Long, Object> replace(ScriptReplaceDto dto);

  IdKey<Long, Object> angusAdd(Long projectId, AngusScript script);

  void angusReplace(Long id, AngusScript script);

  IdKey<Long, Object> clone(Long id);

  List<IdKey<Long, Object>> exampleImport(Long projectId);

  IdKey<Long, Object> imports(ScriptImportDto dto);

  ResponseEntity<Resource> export(Long id, ScriptFormat format, HttpServletResponse response);

  void delete(Collection<Long> ids);

  ScriptDetailVo detail(Long id);

  ScriptInfoVo info(Long id);

  List<ScriptInfosVo> infos(Set<Long> ids);

  AngusScriptDetailVo angusDetail(Long id);

  PageResult<ScriptListVo> list(ScriptFindDto dto);

  PageResult<ScriptInfoListVo> infoList(ScriptFindDto dto);

  PageResult<ScriptListVo> search(ScriptSearchDto dto);

}
