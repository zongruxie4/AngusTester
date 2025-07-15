package cloud.xcan.angus.core.tester.interfaces.services.facade.vo.config;


import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ServicesSyncDetailVo {

  //private Long id;

  @Schema(description = "Synchronization services id")
  private Long serviceId;

  @Schema(example = "AAS Door Apis", description = "Synchronization info naming. Services synchronization info naming, must be unique")
  private String name;

  @Schema(example = "http://192.168.0.101:1807/v2/api-docs?group=Api", description = "Synchronize OpenAPI docs url. After configured, the apis will be read from that address to the current services")
  private String apiDocsUrl;

  @Schema(example = "COVER", description = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api")
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @Schema(example = "false", description = "The delete flag when not existed. Whether to delete a local api when it does not exist in the current synchronized data")
  private Boolean deleteWhenNotExisted;

  @Valid
  @Schema(description = "Authentication configuration. It is required when the synchronization url is protected")
  private List<SimpleHttpAuth> auths;

  @Schema(description = "Last synchronization success flag", example = "false")
  private Boolean syncSuccess;

  /**
   * Note:: Maximum cut out 200 characters the cause.
   */
  @Schema(description = "The reason for the last synchronization failure", example = "Api resource not found")
  private String syncFailureCause;

  @Schema(description = "Last synchronization date")
  private LocalDateTime lastSyncDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
