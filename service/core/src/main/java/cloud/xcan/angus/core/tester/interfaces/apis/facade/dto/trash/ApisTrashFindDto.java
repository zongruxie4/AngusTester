package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.trash;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.commonlink.ApisTargetType;
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
public class ApisTrashFindDto extends PageQuery {

  @Schema(description = "Trash record identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for trash query scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Deleted target name for fuzzy search and filtering")
  private String targetName;

  @NotNull
  @Schema(description = "Deleted target type for classification filtering", requiredMode = RequiredMode.REQUIRED)
  private ApisTargetType targetType;

  @Schema(description = "Deletion date for temporal filtering")
  private LocalDateTime deletedDate;

}



