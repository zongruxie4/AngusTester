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

  @Schema(description = "Functional test review identifier for replacement operation", example = "1")
  private Long id;

  @Schema(description = "Functional test plan identifier for review association", example = "1")
  private Long planId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Functional test review name for identification and organization", example = "Example review", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Review owner identifier for responsibility and authority assignment", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @NotEmpty
  @Schema(description = "Review participant identifiers for collaboration and feedback", requiredMode = RequiredMode.REQUIRED)
  private LinkedHashSet<Long> participantIds;

  @Size(max = MAX_ATTACHMENT_NUM_X2)
  @Schema(description = "Review supporting documents and reference materials")
  private List<Attachment> attachments;

  @EditorContentLength
  @Schema(description = "Comprehensive review description and additional information")
  private String description;
}
