package cloud.xcan.angus.core.tester.interfaces.config.facade.dto;

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
  @Schema(description = "Cloud instance billing type for cost management", example = "PostPaid", requiredMode = RequiredMode.REQUIRED)
  public InstanceChargeType chargeType;
  @Schema(description = "Billing cycle duration; PrePaid: Unit is month, PostPaid: Unit is day", requiredMode = RequiredMode.REQUIRED)
  public Integer chargeCycle;
  @Schema(description = "Order identifier for purchase tracking and billing", example = "10002929288887")
  private Long orderId;
  @NotNull
  @Schema(description = "Flag indicating whether this is a free node for cost management", requiredMode = RequiredMode.REQUIRED)
  private Boolean free;
  @NotNull
  @Schema(description = "Cloud service provider for resource provisioning", example = "aliyun", requiredMode = RequiredMode.REQUIRED)
  private String cloudSupplier;

  @NotEmpty
  @Schema(description = "Cloud region identifier for resource deployment location", example = "cn-shenzhen", requiredMode = RequiredMode.REQUIRED)
  private String regionId;

  @NotNull
  @Schema(description = "Number of nodes to purchase for batch resource provisioning", requiredMode = RequiredMode.REQUIRED)
  private Long nodeNum;

  @Max(value = 64)
  @NotNull
  @Schema(description = "CPU cores for node performance specification", example = "8", requiredMode = RequiredMode.REQUIRED)
  private Integer cpu;

  @Max(value = 256)
  @NotNull
  @Schema(description = "Memory capacity in GB for node performance specification", example = "16", requiredMode = RequiredMode.REQUIRED)
  private Integer memory; // GB

  @NotNull
  @Schema(description = "System disk category for storage performance specification", example = "cloud_essd", requiredMode = RequiredMode.REQUIRED)
  private String sysDiskCategory;

  @Max(value = 2048)
  @NotNull
  @Schema(description = "System disk size in GB for storage capacity", example = "200", requiredMode = RequiredMode.REQUIRED)
  private Integer sysDisk; // GB

  @Schema(description = "Bandwidth billing type for network cost management", example = "PayByTraffic")
  private String bandwidthChargeType;

  @Schema(description = "Bandwidth capacity in MB for network performance", example = "1")
  private Integer bandwidth; // MB

}
