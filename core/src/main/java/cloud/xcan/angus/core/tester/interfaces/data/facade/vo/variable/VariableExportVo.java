package cloud.xcan.angus.core.tester.interfaces.data.facade.vo.variable;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class VariableExportVo {

  private String name;

  private Boolean extracted;

  private String value;

  private String description;

  private Boolean passwordValue;

  private DefaultExtraction extraction;

}
