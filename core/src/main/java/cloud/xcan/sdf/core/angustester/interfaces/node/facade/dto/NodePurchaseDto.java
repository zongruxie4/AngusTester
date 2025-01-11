package cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto;

import cloud.xcan.sdf.core.angustester.infra.iaas.InstanceChargeType;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author wjl
 */
@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodePurchaseDto {

  @NotNull
  @EnumPart(enumClass = InstanceChargeType.class, allowableValues = "PostPaid")
  @ApiModelProperty(example = "PostPaid", required = true)
  public InstanceChargeType chargeType;
  @ApiModelProperty(example = "1", notes = "PrePaid: Unit is month, PostPaid:Unit is day", required = true)
  public Integer chargeCycle;
  @ApiModelProperty(example = "10002929288887")
  private Long orderId;
  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Boolean freeFlag;
  @NotNull
  @ApiModelProperty(example = "aliyun", required = true)
  private String cloudSupplier;

  @NotEmpty
  @ApiModelProperty(example = "cn-shenzhen", required = true)
  private String regionId;

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long nodeNum;

  @Max(value = 64)
  @NotNull
  @ApiModelProperty(example = "8", required = true)
  private Integer cpu;

  @Max(value = 256)
  @NotNull
  @ApiModelProperty(example = "16", required = true)
  private Integer memory; // GB

  @NotNull
  @ApiModelProperty(example = "cloud_essd", required = true)
  private String sysDiskCategory;

  @Max(value = 2048)
  @NotNull
  @ApiModelProperty(example = "200", required = true)
  private Integer sysDisk; // GB

  @ApiModelProperty(example = "PayByTraffic")
  private String bandwidthChargeType;

  @ApiModelProperty(example = "1")
  private Integer bandwidth; // MB

}
