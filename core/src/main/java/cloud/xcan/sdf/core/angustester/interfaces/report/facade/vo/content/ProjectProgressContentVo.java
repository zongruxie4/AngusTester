package cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content;

import cloud.xcan.sdf.core.angustester.domain.report.record.content.ProjectProgressContent;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportDetailVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ProjectProgressContentVo {

  private ReportDetailVo report;

  private ProjectProgressContent content;

}
