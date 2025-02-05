package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset;

import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class DatasetImportDto{

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @NotNull
  @ApiModelProperty(value = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.", example = "COVER", required = true)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @ApiModelProperty(value = "Variable specification content. Variable definition string content in json or yaml format.")
  private String content;

  @ApiModelProperty(value = "Variable specification files. Variable definition files in json or yaml format.")
  private MultipartFile file;

}