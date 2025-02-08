package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH;

import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShareScope;
import cloud.xcan.sdf.core.angustester.domain.apis.share.DisplayOptions;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class ApisShareUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Share id", required = true)
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Share name")
  private String name;

  @Length(max = DEFAULT_NAME_LENGTH_X4)
  @ApiModelProperty(value = "Share remark, it will be displayed on the sharing page to information")
  private String remark;

  @ApiModelProperty(value = "Share expired date")
  private LocalDateTime expiredDate;

  @Valid
  @ApiModelProperty(value = "Share display options")
  private DisplayOptions displayOptions;

  @ApiModelProperty(value = "Share scope")
  private ApisShareScope shareScope;

  @ApiModelProperty(value = "Share services id")
  private Long servicesId;

  @ApiModelProperty(value = "Share apis ids, it is required when share scope is apis")
  private Set<Long> apisIds;

  @Length(max = DEFAULT_URL_LENGTH)
  @ApiModelProperty(value = "Web front end sharing page address")
  private String baseUrl;

}
