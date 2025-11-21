package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
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
public class TagFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for tag filtering and organization", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Tag identifier for specific tag lookup")
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Tag name for partial matching search")
  private String name;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Tag creation timestamp for timeline filtering")
  private LocalDateTime createdDate;

}
