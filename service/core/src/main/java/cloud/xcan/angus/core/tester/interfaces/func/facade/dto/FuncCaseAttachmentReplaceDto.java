package cloud.xcan.angus.core.tester.interfaces.func.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM;

import cloud.xcan.angus.api.pojo.Attachment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseAttachmentReplaceDto {

  @Size(max = MAX_ATTACHMENT_NUM)
  @Schema(description = "Test case supporting documents and attachments with clear functionality")
  private List<Attachment> attachments;

}
