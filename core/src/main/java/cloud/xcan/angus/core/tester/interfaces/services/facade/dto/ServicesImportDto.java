package cloud.xcan.angus.core.tester.interfaces.services.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author XiaoLong Liu
 */@Valid
@Getter
@Setter
@Accessors(chain = true)
public class ServicesImportDto {

  @Schema(description = "Project ID, required when creating a service")
  private Long projectId;

  @Schema(description = "Import services id. Importing existing projects is required, create new services if serviceId is empty.")
  private Long serviceId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Import services name. The name is required when importing a new services.")
  private String serviceName;

  @NotNull
  @Schema(description = "Import data source type", requiredMode = RequiredMode.REQUIRED)
  private ApiImportSource importSource;

  @NotNull
  @Schema(description = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.", example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(example = "false", requiredMode = RequiredMode.REQUIRED,
      description = "The delete flag when not existed. Whether to delete a local api when it does not exist in the current synchronized data.")
  private Boolean deleteWhenNotExisted;

  @Schema(description = "Apis specification content. API definition string content in json or yaml format, multiple files import is not supported")
  private String content;

  @Schema(description = "Apis specification file. API definition file in json or yaml format, multiple files need to be compressed into a zip file before uploading")
  private MultipartFile file;

  private List<MultipartFile> files;

}
