package cloud.xcan.angus.core.tester.domain.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class DataResourcesCount {

  @Schema(description = "Total number of variable")
  private long allVariable;

  @Schema(description = "Total number of dataset")
  private long allDataset;

  @Schema(description = "Total number of file")
  private long allFile;

  @Schema(description = "Total number of datasource")
  private long allDatasource;

}
