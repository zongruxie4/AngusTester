package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xiaolong.liu
 */
@Valid
@ApiModel
@Getter
@Setter
@Accessors(chain = true)
public class ServicesImportDto {

  @ApiModelProperty(value = "Project ID, required when creating a service")
  private Long projectId;

  @ApiModelProperty(value = "Import services id. Importing existing projects is required, create new services if serviceId is empty.")
  private Long serviceId;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Import services name. The name is required when importing a new services.")
  private String serviceName;

  @NotNull
  @ApiModelProperty(value = "Import data source type", required = true)
  private ApiImportSource importSource;

  @NotNull
  @ApiModelProperty(value = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.", example = "COVER", required = true)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @ApiModelProperty(example = "false", required = true,
      value = "The delete flag when not existed. Whether to delete a local api when it does not exist in the current synchronized data.")
  private Boolean deleteWhenNotExisted;

  @ApiModelProperty(value = "Apis specification content. API definition string content in json or yaml format, multiple files import is not supported")
  private String content;

  @ApiModelProperty(value = "Apis specification files. API definition files in json or yaml format, multiple files need to be compressed into a zip file before uploading")
  private MultipartFile file;

  private List<MultipartFile> files;

}
