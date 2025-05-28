package cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.node.dns.DnsLine;
import cloud.xcan.angus.core.tester.domain.node.dns.DnsRecordType;
import cloud.xcan.angus.validator.IPv4;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@Accessors(chain = true)
public class NodeDomainDnsAddDto {

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long domainId;

  @NotNull
  @Schema(example = "MX", requiredMode = RequiredMode.REQUIRED)
  private DnsRecordType type;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(example = "www", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private DnsLine line;

  @IPv4
  @NotBlank
  @Schema(example = "192.0.2.254", requiredMode = RequiredMode.REQUIRED)
  private String value;

  @Range(min = 1, max = 86400)
  @Schema(example = "600")
  private Integer ttl;

}
