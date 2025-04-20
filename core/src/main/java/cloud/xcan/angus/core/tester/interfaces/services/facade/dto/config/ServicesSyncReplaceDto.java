package cloud.xcan.angus.core.tester.interfaces.services.facade.dto.config;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_HTTP_AUTH_PARAM_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid

@Setter
@Getter
@Accessors(chain = true)
public class ServicesSyncReplaceDto {

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(example = "AAS Door Apis", requiredMode = RequiredMode.REQUIRED,
      description = "Synchronization info naming. Services synchronization info naming, must be unique.")
  private String name;

  @NotEmpty
  @Length(max = MAX_URL_LENGTH_X2)
  @Schema(example = "http://192.168.0.101:1807/v2/api-docs?group=Api", requiredMode = RequiredMode.REQUIRED,
      description = "Synchronize OpenAPI docs url. After configured, the apis will be read from that address to the current services.")
  private String apiDocsUrl;

  @NotNull
  @Schema(description = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.", example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(example = "false", requiredMode = RequiredMode.REQUIRED,
      description = "The delete flag when not existed. Whether to delete a local api when it does not exist in the current synchronized data.")
  private Boolean deleteWhenNotExisted;

  @Valid
  @Length(max = MAX_HTTP_AUTH_PARAM_NUM)
  @Schema(description = "Authentication configuration. It is required when the synchronization url is protected.")
  private List<SimpleHttpAuth> auths;

}
