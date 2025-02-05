package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class DatasourceSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", example = "1", required = true)
  private Long projectId;

  private String name;

  private String database;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "lastModifiedDate";
  }
}



