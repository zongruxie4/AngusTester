package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.mock;

import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ApisAssocMockApisAddDto {

  @NotNull
  @ApiModelProperty(value = "Associate Mock Service id", example = "1", required = true)
  private Long mockServiceId;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @ApiModelProperty(value = "Mock apis name", example = "This is mock apis name")
  private String summary;

}
