package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal;


import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecAssembler.toExecDetailVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.agent.message.runner.RunnerStopVo;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecFacade;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByArgsDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByContentDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecAddByScriptDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecConfigReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecScriptConfigReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStopDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecAssembler;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecInfoVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecVo;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ExecFacadeImpl implements ExecFacade {

  @Resource
  private ExecCmd execCmd;

  @Resource
  private ExecQuery execQuery;

  @Override
  public IdKey<Long, Object> addByScriptContent(ExecAddByContentDto dto) {
    return execCmd.addByLocalScriptContent(dto.getProjectId(), dto.getName(),
        dto.getScriptContent(), dto.getTrial());
  }

  @Override
  public IdKey<Long, Object> addByScriptArgs(ExecAddByArgsDto dto) {
    return execCmd.addByLocalScriptArgs(dto.getProjectId(), dto.getName(), dto.getScriptType(),
        dto.getPlugin(), dto.getConfiguration(), dto.getTask(), dto.getTrial());
  }

  @Override
  public IdKey<Long, Object> addByScript(ExecAddByScriptDto dto) {
    return execCmd.addByRemoteScript(dto.getName(), dto.getScriptId(), dto.getScriptType(),
        dto.getConfiguration(), dto.getArguments(), dto.getTrial());
  }

  @Override
  public void configReplace(Long id, ExecConfigReplaceDto dto) {
    execCmd.configReplace(id, dto.getName(), dto.getIterations(), dto.getDuration(),
        dto.getThread(), dto.getPriority(), dto.getIgnoreAssertions(), dto.getUpdateTestResult(),
        dto.getStartMode(), dto.getStartAtDate(), dto.getReportInterval());
  }

  @Override
  public void scriptConfigReplace(Long id, ExecScriptConfigReplaceDto dto) {
    execCmd.scriptConfigReplace(id, dto.getName(), dto.getScriptType(), dto.getConfiguration(),
        dto.getArguments());
  }

  @Override
  public List<RunnerRunVo> start(ExecStartDto dto) {
    return execCmd.start(dto);
  }

  @Override
  public List<RunnerStopVo> stop(ExecStopDto dto) {
    return execCmd.stop(dto);
  }

  @Override
  public void delete(LinkedHashSet<Long> ids) {
    execCmd.delete(ids);
  }

  @NameJoin
  @Override
  public ExecDetailVo detail(Long id) {
    return toExecDetailVo(execQuery.detail(id));
  }

  @Override
  public ExecInfoVo info(Long id, Boolean joinSampleSummary) {
    List<ExecInfo> execInfos = execQuery.listInfo(Set.of(id), joinSampleSummary);
    return isEmpty(execInfos) ? null : execInfos.stream().map(ExecAssembler::toExecInfoVo)
        .toList().get(0);
  }

  @Override
  public List<ExecInfoVo> listInfo(Set<Long> ids, Boolean joinSampleSummary) {
    List<ExecInfo> execInfos = execQuery.listInfo(ids, joinSampleSummary);
    return isEmpty(execInfos) ? null : execInfos.stream().map(ExecAssembler::toExecInfoVo)
        .toList();
  }

  @Override
  public List<ExecInfoVo> listInfoBySource(ScriptSource resourceType, Set<Long> resourceIds,
      Boolean joinSampleSummary) {
    List<ExecInfo> execInfos = execQuery.listInfoBySource(resourceType, resourceIds,
        joinSampleSummary);
    return isEmpty(execInfos) ? null : execInfos.stream().map(ExecAssembler::toExecInfoVo)
        .toList();
  }

  @Override
  public String script(Long id) {
    return execQuery.script(id);
  }

  @Override
  public List<Server> serverList(Long id) {
    return execQuery.findServers(id);
  }

  @NameJoin
  @Override
  public PageResult<ExecVo> list(ExecFindDto dto) {
    Page<ExecInfo> page = execQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ExecAssembler::toExecVo);
  }

}
