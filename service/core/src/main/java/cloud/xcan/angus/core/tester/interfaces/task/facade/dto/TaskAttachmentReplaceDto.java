package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;

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
public class TaskAttachmentReplaceDto {

  @Size(max = MAX_ATTACHMENT_NUM)
  @Schema(description = "Task attachments, allow clear attachments by empty value")
  private List<Attachment> attachments;

}
