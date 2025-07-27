package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Accessors(chain = true)
public class ApisDesignImportDto {

  @NotNull
  @Schema(description = "Project identifier for API design import destination", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotBlank
  @Schema(description = "API design name for identification and organization", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH)
  private String name;

  @Schema(description = "OpenAPI specification content in JSON or YAML format for direct import")
  private String content;

  @Schema(description = "OpenAPI specification file in JSON or YAML format for file-based import",
      type = "string", format = "binary")
  private MultipartFile file;

}
