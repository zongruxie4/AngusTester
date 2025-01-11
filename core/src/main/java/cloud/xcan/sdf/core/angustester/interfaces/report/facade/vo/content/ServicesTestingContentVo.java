package cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.content;

import cloud.xcan.sdf.core.angustester.domain.report.record.content.ServicesTestingContent;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.ReportDetailVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServicesTestingContentVo {

  private ReportDetailVo report;

  private ServicesTestingContent content;

}
