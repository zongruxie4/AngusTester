package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM;

import cloud.xcan.sdf.api.pojo.Attachment;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseAttachmentReplaceDto {

  @Size(max = MAX_ATTACHMENT_NUM)
  @ApiModelProperty(value = "Case attachments, allow clear attachments")
  private List<Attachment> attachments;

}
