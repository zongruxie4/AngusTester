package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;


import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class TaskConfirmorReplaceDto {

  @ApiModelProperty(value = "Confirmor id, allow clear confirmor by empty value")
  private Long confirmorId;

}
