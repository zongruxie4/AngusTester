package cloud.xcan.angus.core.tester.interfaces.exec.facade;

import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecSampleContent;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleErrorContentFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleExtcFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecSampleErrorContentVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleUploadResultProgressVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleVo;
import cloud.xcan.angus.remote.PageResult;
import java.util.LinkedHashMap;

public interface ExecSampleFacade {

  ExecSampleSummaryVo summaryTotal(Long id);

  PageResult<ExecSampleVo> summaryList(Long id, ExecSampleFindDto dto);

  PageResult<ExecSampleVo> scoreList(Long id, ExecSampleFindDto dto);

  PageResult<ExecSampleVo> throughputList(Long id, ExecSampleFindDto dto);

  PageResult<ExecSampleVo> threadList(Long id, ExecSampleFindDto dto);

  PageResult<ExecSampleVo> errorList(Long id, ExecSampleFindDto dto);

  PageResult<ExecSampleErrorContentVo> errorContent(Long id, ExecSampleErrorContentFindDto dto);

  LinkedHashMap<String, LinkedHashMap<String, Long>> latestErrorsCounter(Long id, Long nodeId,
      String name);

  ExecSampleUploadResultProgressVo latestUploadResultProgress(Long id);

  LinkedHashMap<String, LinkedHashMap<String, Long>> latestExtCounterMap1(Long id, Long nodeId,
      String name);

  PageResult<ExecSampleContent> extContentList(Long id, ExecSampleExtcFindDto dto);

}
