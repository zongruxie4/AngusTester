package cloud.xcan.angus.core.tester.domain.data;

import cloud.xcan.angus.api.enums.FileResourceType;
import cloud.xcan.angus.api.enums.UseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class DataResourcesCreationCount {

  @Schema(description = "Total number of variable")
  private long allVariable;

  @Schema(description = "The number of new variable added in the last one week")
  private long variableByLast7Day;

  @Schema(description = "The number of new variable added in the last one month")
  private long variableByLast30Day;

  @Schema(description = "Total number of dataset")
  private long allDataset;

  @Schema(description = "The number of new dataset added in the last one week")
  private long datasetByLast7Day;

  @Schema(description = "The number of new dataset added in the last one month")
  private long datasetByLast30Day;

  @Schema(description = "Total number of file")
  private long allFile;

  @Schema(description = "The number of new file added in the last one week")
  private long fileByLast7Day;

  @Schema(description = "The number of new file added in the last one month")
  private long fileByLast30Day;

  @Schema(description = "Total number of datasource")
  private long allDatasource;

  @Schema(description = "The number of new datasource added in the last one week")
  private long datasourceByLast7Day;

  @Schema(description = "The number of new datasource added in the last one month")
  private long datasourceByLast30Day;

  private Map<UseStatus, Long> variableByUse;

  private Map<UseStatus, Long> datasetByUse;

  private Map<FileResourceType, Long> fileByResourceType;

  private Map<String, Long> datasourceByDb;

}
