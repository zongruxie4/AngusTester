package cloud.xcan.angus.core.tester.domain.report.record.content;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ObjectContent implements ReportContent {

  private final String template = OBJECT;

}
