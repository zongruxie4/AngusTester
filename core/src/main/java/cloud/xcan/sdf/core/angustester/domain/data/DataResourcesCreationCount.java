package cloud.xcan.sdf.core.angustester.domain.data;

import cloud.xcan.sdf.api.enums.FileResourceType;
import cloud.xcan.sdf.api.enums.UseStatus;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class DataResourcesCreationCount {

  @ApiModelProperty(value = "Total number of variable")
  private long allVariable;

  @ApiModelProperty(value = "The number of new variable added in the last one week")
  private long variableByLast7Day;

  @ApiModelProperty(value = "The number of new variable added in the last one month")
  private long variableByLast30Day;

  @ApiModelProperty(value = "Total number of dataset")
  private long allDataset;

  @ApiModelProperty(value = "The number of new dataset added in the last one week")
  private long datasetByLast7Day;

  @ApiModelProperty(value = "The number of new dataset added in the last one month")
  private long datasetByLast30Day;

  @ApiModelProperty(value = "Total number of file")
  private long allFile;

  @ApiModelProperty(value = "The number of new file added in the last one week")
  private long fileByLast7Day;

  @ApiModelProperty(value = "The number of new file added in the last one month")
  private long fileByLast30Day;

  @ApiModelProperty(value = "Total number of datasource")
  private long allDatasource;

  @ApiModelProperty(value = "The number of new datasource added in the last one week")
  private long datasourceByLast7Day;

  @ApiModelProperty(value = "The number of new datasource added in the last one month")
  private long datasourceByLast30Day;

  private Map<UseStatus, Long> variableByUse;

  private Map<UseStatus, Long> datasetByUse;

  private Map<FileResourceType, Long> fileByResourceType;

  private Map<String, Long> datasourceByDb;

}
