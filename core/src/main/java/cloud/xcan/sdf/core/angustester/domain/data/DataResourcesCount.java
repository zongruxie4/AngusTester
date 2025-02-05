package cloud.xcan.sdf.core.angustester.domain.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class DataResourcesCount {

  @ApiModelProperty(value = "Total number of variable")
  private long allVariable;

  @ApiModelProperty(value = "Total number of dataset")
  private long allDataset;

  @ApiModelProperty(value = "Total number of file")
  private long allFile;

  @ApiModelProperty(value = "Total number of datasource")
  private long allDatasource;

}
