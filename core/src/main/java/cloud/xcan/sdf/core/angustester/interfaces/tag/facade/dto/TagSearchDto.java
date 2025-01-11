package cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Getter
@Setter
@Accessors(chain = true)
public class TagSearchDto extends PageQuery {

  @NotNull
  @ApiModelProperty(required = true)
  private Long projectId;

  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
