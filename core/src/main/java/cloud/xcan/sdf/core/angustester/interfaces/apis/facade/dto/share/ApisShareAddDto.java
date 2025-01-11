package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH;

import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShareScope;
import cloud.xcan.sdf.core.angustester.domain.apis.share.DisplayOptions;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.Set;
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
public class ApisShareAddDto {

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Share name", required = true)
  private String name;

  @Length(max = DEFAULT_NAME_LENGTH_X4)
  @ApiModelProperty(value = "Share remark, it will be displayed on the sharing page to information")
  private String remark;

  @ApiModelProperty(value = "Share expired date")
  private LocalDateTime expiredDate;

  @Valid
  @NotNull
  @ApiModelProperty(value = "Share display options", required = true)
  private DisplayOptions displayOptions;

  @NotNull
  @ApiModelProperty(value = "Share scope", required = true)
  private ApisShareScope shareScope;

  @NotNull
  @ApiModelProperty(value = "Share services id", required = true)
  private Long servicesId;

  @ApiModelProperty(value = "Share apis ids, it is required when share scope is apis")
  private Set<Long> apisIds;

  @NotEmpty
  @Length(max = DEFAULT_URL_LENGTH)
  @ApiModelProperty(value = "Web front end sharing page address", required = true)
  private String baseUrl;

}
