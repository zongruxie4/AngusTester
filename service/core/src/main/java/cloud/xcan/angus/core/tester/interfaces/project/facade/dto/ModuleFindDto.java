package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.AbstractQuery;
import cloud.xcan.angus.remote.OrderSort;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Getter
@Setter
@Accessors(chain = true)
public class ModuleFindDto extends AbstractQuery {
  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(name = "Parent module ID")
  private Long pid;

  @Length(max = MAX_NAME_LENGTH)
  private String name;

  private Integer sequence;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Override
  @JsonIgnore
  @Schema(hidden = true)
  public String getDefaultOrderBy() {
    return "sequence";
  }

  @Override
  @JsonIgnore
  @Schema(hidden = true)
  public OrderSort getDefaultOrderSort() {
    return OrderSort.ASC;
  }

}
