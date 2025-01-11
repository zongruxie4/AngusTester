package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShareScope;
import cloud.xcan.sdf.core.angustester.domain.apis.share.DisplayOptions;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ApisShareVo {

  private Long id;

  private String projectId;

  @ApiModelProperty(value = "Share name")
  private String name;

  @ApiModelProperty(value = "Share remark")
  private String remark;

  @ApiModelProperty(value = "Share expired date")
  private LocalDateTime expiredDate;

  @ApiModelProperty(value = "Share display options")
  private DisplayOptions displayOptions;

  @ApiModelProperty(value = "Share scope")
  private ApisShareScope shareScope;

  @ApiModelProperty(value = "Share services id")
  private Long servicesId;

  @ApiModelProperty(value = "Share apis ids, it is required when share scope is apis")
  private Set<Long> apisIds;

  @ApiModelProperty(value = "Web front end sharing page address")
  private String url;

  private Integer viewNum;

  private boolean isExpired;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
