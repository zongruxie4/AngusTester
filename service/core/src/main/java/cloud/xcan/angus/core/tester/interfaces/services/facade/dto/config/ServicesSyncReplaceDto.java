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

@Getter
@Setter
@Accessors(chain = true)
public class ServicesSyncReplaceDto {

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Synchronization configuration name for identification and management", example = "AAS Door Apis", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotEmpty
  @Length(max = MAX_URL_LENGTH_X2)
  @Schema(description = "OpenAPI documentation URL for API synchronization source", example = "http://192.168.0.101:1807/v2/api-docs?group=Api", requiredMode = RequiredMode.REQUIRED)
  private String apiDocsUrl;

  @NotNull
  @Schema(description = "Duplicate handling strategy for API synchronization conflicts", example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(description = "Flag to delete local APIs when not present in synchronized data", example = "false", requiredMode = RequiredMode.REQUIRED)
  private Boolean deleteWhenNotExisted;

  @Valid
  @Length(max = MAX_HTTP_AUTH_PARAM_NUM)
  @Schema(description = "Authentication configuration for protected synchronization URLs")
  private List<SimpleHttpAuth> auths;

}
