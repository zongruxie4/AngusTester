package cloud.xcan.angus.core.tester.interfaces.func.facade.dto;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseImportDto {

  @NotNull
  @Schema(description = "Target functional test plan identifier for case import", requiredMode = RequiredMode.REQUIRED)
  private Long planId;

  @NotNull
  @Schema(description = "Duplicate handling strategy: COVER overrides existing cases, IGNORE skips duplicates",
      example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(type = "string", format = "binary", description = "Excel file containing test case data for import", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile file;

}
