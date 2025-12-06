package cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import cloud.xcan.angus.core.tester.domain.project.template.content.TemplateContent;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class TemplateAddDto {

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Test template name for identification and management", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Test template type (ISSUE, TEST_PLAN or TEST_CASE)", requiredMode = RequiredMode.REQUIRED)
  private TemplateType templateType;

  @NotNull
  @Schema(description = "Test template content with specific structure based on template type", requiredMode = RequiredMode.REQUIRED)
  private TemplateContent templateContent;

}

