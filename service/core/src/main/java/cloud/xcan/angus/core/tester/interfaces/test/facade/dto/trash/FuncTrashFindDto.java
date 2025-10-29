package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.trash;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.core.tester.domain.test.FuncTargetType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncTrashFindDto extends PageQuery {

  @Schema(description = "Functional test trash item identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for trash scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Deleted target name for fuzzy search and filtering")
  private String targetName;

  @NotNull
  @Schema(description = "Deleted target type for categorization filtering", requiredMode = RequiredMode.REQUIRED)
  private FuncTargetType targetType;

  @Schema(description = "Deletion timestamp for temporal filtering")
  private LocalDateTime deletedDate;

}



