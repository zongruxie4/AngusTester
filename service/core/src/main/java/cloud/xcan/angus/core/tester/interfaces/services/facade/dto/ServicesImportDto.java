package cloud.xcan.angus.core.tester.interfaces.services.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ServicesImportDto {

  @Schema(description = "Project identifier for service association")
  private Long projectId;

  @Schema(description = "Service identifier for existing service import; null indicates new service creation")
  private Long serviceId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Service name for new service creation")
  private String serviceName;

  @NotNull
  @Schema(description = "Import data source type specification", requiredMode = RequiredMode.REQUIRED)
  private ApiImportSource importSource;

  @NotNull
  @Schema(description = "Duplicate handling strategy for API import conflicts", example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(description = "Flag to delete local APIs when not present in imported data", example = "false", requiredMode = RequiredMode.REQUIRED)
  private Boolean deleteWhenNotExisted;

  @Schema(description = "API specification content in JSON or YAML format for import processing")
  private String content;

  @Schema(type = "string", format = "binary", description = "API specification file for import processing")
  private MultipartFile file;

  @Schema(description = "Multiple API specification files for batch import processing")
  private List<MultipartFile> files;

}
