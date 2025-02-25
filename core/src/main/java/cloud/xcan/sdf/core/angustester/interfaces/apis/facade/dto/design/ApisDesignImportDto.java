package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
public class ApisDesignImportDto {

  @NotNull
  @ApiModelProperty(value = "Project ID", required = true)
  private Long projectId;

  @NotBlank
  @ApiModelProperty(value = "Design name", required = true)
  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @ApiModelProperty(value = "Apis specification content. API definition string content in json or yaml format, multiple files import is not supported")
  private String content;

  @ApiModelProperty(value = "Apis specification file. API definition file in json or yaml format, multiple files need to be compressed into a zip file before uploading")
  private MultipartFile file;

}
