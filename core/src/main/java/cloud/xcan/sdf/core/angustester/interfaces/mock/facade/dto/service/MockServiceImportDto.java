package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service;

import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xiaolong.liu
 */
@Valid
@ApiModel
@Getter
@Setter
@Accessors(chain = true)
public class MockServiceImportDto {

  @NotNull
  @ApiModelProperty(notes = "Import mock service id.", required = true)
  private Long mockServiceId;

  @NotNull
  @ApiModelProperty(value = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.", example = "COVER", required = true)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @ApiModelProperty(example = "false", required = true,
      notes = "The delete flag when not existed. Whether to delete a local api when it does not exist in the current synchronized data.")
  private Boolean deleteWhenNotExisted;

  @ApiModelProperty(notes = "Apis specification content. API definition string content in json or yaml format, multiple files import is not supported")
  private String content;

  @ApiModelProperty(notes = "Apis specification files. API definition files in json or yaml format, multiple files need to be compressed into a zip file before uploading")
  private MultipartFile file;

}
