package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.config;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_HTTP_AUTH_PARAM_NUM;

import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ServicesSyncReplaceDto {

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(example = "AAS Door Apis", required = true,
      notes = "Synchronization info naming. Services synchronization info naming, must be unique.")
  private String name;

  @NotEmpty
  @Length(max = DEFAULT_URL_LENGTH_X2)
  @ApiModelProperty(example = "http://192.168.0.101:1807/v2/api-docs?group=Api", required = true,
      notes = "Synchronize OpenAPI docs url. After configured, the apis will be read from that address to the current services.")
  private String apiDocsUrl;

  @NotNull
  @ApiModelProperty(value = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.", example = "COVER", required = true)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @ApiModelProperty(example = "false", required = true,
      notes = "The delete flag when not existed. Whether to delete a local api when it does not exist in the current synchronized data.")
  private Boolean deleteWhenNotExisted;

  @Valid
  @Length(max = MAX_HTTP_AUTH_PARAM_NUM)
  @ApiModelProperty(notes = "Authentication configuration. It is required when the synchronization url is protected.")
  private List<SimpleHttpAuth> auths;

}
