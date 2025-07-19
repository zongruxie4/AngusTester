package cloud.xcan.angus.core.tester.interfaces.exec.facade;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.agent.message.runner.RunnerStopVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByArgsDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByContentDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByScriptDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecConfigReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecScriptConfigReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStopDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecInfoVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecVo;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.models.servers.Server;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface ExecFacade {

  IdKey<Long, Object> addByScriptContent(ExecAddByContentDto dto);

  IdKey<Long, Object> addByScriptArgs(ExecAddByArgsDto dto);

  IdKey<Long, Object> addByScript(ExecAddByScriptDto dto);

  void configReplace(Long id, ExecConfigReplaceDto dto);

  void scriptConfigReplace(Long id, ExecScriptConfigReplaceDto dto);

  List<RunnerRunVo> start(ExecStartDto dto);

  List<RunnerStopVo> stop(ExecStopDto dto);

  void delete(LinkedHashSet<Long> ids);

  ExecDetailVo detail(Long id);

  ExecInfoVo info(Long id, Boolean joinSampleSummary);

  List<ExecInfoVo> listInfo(Set<Long> ids, Boolean joinSampleSummary);

  List<ExecInfoVo> listInfoBySource(ScriptSource resourceType, Set<Long> resourceIds,
      Boolean joinSampleSummary);

  String script(Long id);

  List<Server> serverList(Long id);

  PageResult<ExecVo> list(ExecFindDto dto);

}
