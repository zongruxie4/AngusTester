package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share;

import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PUBLIC_TOKEN_LENGTH;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class ApisShareViewDto {

  @NotNull
  @ApiModelProperty(value = "Share id", required = true)
  private Long id;

  @NotEmpty
  @Length(max = MAX_PUBLIC_TOKEN_LENGTH)
  @ApiModelProperty(value = "Share public access token", required = true)
  private String pat;

}
