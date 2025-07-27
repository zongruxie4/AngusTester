package cloud.xcan.angus.core.tester.interfaces.tag.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

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
public class TagUpdateDto {

  @NotNull
  @Schema(description = "Tag identifier for modification", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(example = "Sprint-1", description = "Updated tag name for improved classification", requiredMode = RequiredMode.REQUIRED)
  private String name;

}
