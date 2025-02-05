package cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.dataset;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class DatasetExportVo {

  private String name;

  private Boolean extracted;

  private List<DatasetParameter> parameters;

  private DefaultExtraction extraction;
}
