package cloud.xcan.angus.core.tester.interfaces.data.facade.vo.dataset;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class DatasetExportVo {

  private String name;

  private String description;

  private Boolean extracted;

  private List<DatasetParameter> parameters;

  private DefaultExtraction extraction;
}
