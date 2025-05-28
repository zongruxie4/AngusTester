package cloud.xcan.angus.core.tester.interfaces.node.facade.dto;

import cloud.xcan.angus.core.tester.infra.iaas.InstanceChargeType;
import cloud.xcan.angus.validator.EnumPart;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author wjl
 */
@Getter
@Setter
@Accessors(chain = true)
public class NodePurchaseDto {

  @NotNull
  @EnumPart(enumClass = InstanceChargeType.class, allowableValues = "PostPaid")
  @Schema(example = "PostPaid", requiredMode = RequiredMode.REQUIRED)
  public InstanceChargeType chargeType;
  @Schema(description = "PrePaid: Unit is month, PostPaid:Unit is day", requiredMode = RequiredMode.REQUIRED)
  public Integer chargeCycle;
  @Schema(example = "10002929288887")
  private Long orderId;
  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Boolean free;
  @NotNull
  @Schema(example = "aliyun", requiredMode = RequiredMode.REQUIRED)
  private String cloudSupplier;

  @NotEmpty
  @Schema(example = "cn-shenzhen", requiredMode = RequiredMode.REQUIRED)
  private String regionId;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long nodeNum;

  @Max(value = 64)
  @NotNull
  @Schema(example = "8", requiredMode = RequiredMode.REQUIRED)
  private Integer cpu;

  @Max(value = 256)
  @NotNull
  @Schema(example = "16", requiredMode = RequiredMode.REQUIRED)
  private Integer memory; // GB

  @NotNull
  @Schema(example = "cloud_essd", requiredMode = RequiredMode.REQUIRED)
  private String sysDiskCategory;

  @Max(value = 2048)
  @NotNull
  @Schema(example = "200", requiredMode = RequiredMode.REQUIRED)
  private Integer sysDisk; // GB

  @Schema(example = "PayByTraffic")
  private String bandwidthChargeType;

  @Schema(example = "1")
  private Integer bandwidth; // MB

}
