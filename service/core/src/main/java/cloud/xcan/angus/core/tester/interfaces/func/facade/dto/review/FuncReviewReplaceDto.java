package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncReviewReplaceDto {

  @Schema(description = "Review id", example = "1")
  private Long id;

  @Schema(description = "Plan id", example = "1")
  private Long planId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Review name, Brief Overview of the review, supporting up to 200 characters.", example = "Example review", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Review owner id", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @NotEmpty
  @Schema(description = "Review participant ids", requiredMode = RequiredMode.REQUIRED)
  private LinkedHashSet<Long> participantIds;

  @Size(max = MAX_ATTACHMENT_NUM_X2)
  @Schema(description =
      "Review attachments. Additional documents and information, such as requirement specifications, reference materials and standards, "
          + "system architecture diagrams, testing specifications, technical documents, etc.")
  private List<Attachment> attachments;

  @EditorContentLength
  @Schema(description =
      "Other review information. This is the other description of the testing review. Additional details such as testing strategies, "
          + "risk assessment, and management. ")
  private String description;
}
