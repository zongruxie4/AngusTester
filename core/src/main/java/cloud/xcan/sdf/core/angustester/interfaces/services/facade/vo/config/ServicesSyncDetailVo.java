package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.config;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ServicesSyncDetailVo {

  //private Long id;

  @ApiModelProperty(value = "Synchronization services id.")
  private Long serviceId;

  @ApiModelProperty(example = "AAS Door Apis", notes = "Synchronization info naming. Services synchronization info naming, must be unique.")
  private String name;

  @ApiModelProperty(example = "http://192.168.0.101:1807/v2/api-docs?group=Api", notes = "Synchronize OpenAPI docs url. After configured, the apis will be read from that address to the current services.")
  private String apiDocsUrl;

  @ApiModelProperty(example = "COVER", notes = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.")
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @ApiModelProperty(example = "false", notes = "The delete flag when not existed. Whether to delete a local api when it does not exist in the current synchronized data.")
  private Boolean deleteWhenNotExisted;

  @Valid
  @ApiModelProperty(notes = "Authentication configuration. It is required when the synchronization url is protected.")
  private List<SimpleHttpAuth> auths;

  @ApiModelProperty(value = "Last synchronization success flag.", example = "false")
  private Boolean syncSuccessFlag;

  /**
   * Note:: Maximum cut out 200 characters the cause.
   */
  @ApiModelProperty(value = "The reason for the last synchronization failure", example = "Api resource not found")
  private String syncFailureCause;

  @ApiModelProperty(value = "Last synchronization date.")
  private LocalDateTime lastSyncDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
