package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import java.util.LinkedHashSet;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class DatasetExportDto extends PageQuery {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Dataset ids. Note: All the datasets wil be exported if ids are empty.")
  @Size(max = DEFAULT_BATCH_SIZE)
  private LinkedHashSet<Long> ids;

  @ApiParam(name = "format", value = "Script format, default yml")
  private ScriptFormat format;

}



