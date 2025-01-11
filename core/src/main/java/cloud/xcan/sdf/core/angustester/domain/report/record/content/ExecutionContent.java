package cloud.xcan.sdf.core.angustester.domain.report.record.content;

import cloud.xcan.sdf.api.angusctrl.exec.vo.ExecDetailVo;
import cloud.xcan.sdf.api.angusctrl.exec.vo.ExecSampleContentVo;
import cloud.xcan.sdf.api.angusctrl.exec.vo.ExecSampleErrorContentVo;
import cloud.xcan.sdf.api.angusctrl.exec.vo.sample.ExecSampleVo;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecutionContent implements ReportContent {

  private final String template = EXECUTION_SAMPLING_RESULT;

  private ExecDetailVo exec;

  private List<ExecSampleVo> sampling;

  private List<ExecSampleErrorContentVo> samplingErrors;

  private LinkedHashMap<String, LinkedHashMap<String, Long>> latestErrorsCounter;

  private LinkedHashMap<String, LinkedHashMap<String, Long>> latestExtCounterMap1;

  private List<ExecSampleContentVo> extContents;

}
