package cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.AbstractQuery;
import cloud.xcan.sdf.api.OrderSort;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ModuleFindDto extends AbstractQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @ApiModelProperty(name = "Parent module ID")
  private Long pid;

  private Integer sequence;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;


  @Override
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  public String getDefaultOrderBy() {
    return "sequence";
  }

  @Override
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  public OrderSort getDefaultOrderSort() {
    return OrderSort.ASC;
  }
}
