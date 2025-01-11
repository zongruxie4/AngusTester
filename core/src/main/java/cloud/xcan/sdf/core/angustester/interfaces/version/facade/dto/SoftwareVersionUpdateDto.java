package cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class SoftwareVersionUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Version id", required = true)
  private Long id;

  @ApiModelProperty(value = "Version name")
  private String name;

  @ApiModelProperty(value = "Version start date")
  private LocalDateTime startDate;

  @ApiModelProperty(value = "Version release date")
  private LocalDateTime releaseDate;

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value = "Version description")
  private String description;

}
