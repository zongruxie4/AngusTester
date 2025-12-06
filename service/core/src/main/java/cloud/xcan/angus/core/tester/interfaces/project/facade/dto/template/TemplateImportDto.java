package cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template;

import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for template import operation.
 * Supports single template import in Excel, CSV or JSON format.
 */
@Getter
@Setter
@Accessors(chain = true)
public class TemplateImportDto {

  @NotNull
  @Schema(description = "Template type (ISSUE, TEST_PLAN, or TEST_CASE)", requiredMode = RequiredMode.REQUIRED)
  private TemplateType templateType;

  @NotBlank
  @Schema(description = "Template name for identification", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(type = "string", format = "binary", description = "Template import file in Excel, CSV or JSON format containing single template data", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile file;

}

