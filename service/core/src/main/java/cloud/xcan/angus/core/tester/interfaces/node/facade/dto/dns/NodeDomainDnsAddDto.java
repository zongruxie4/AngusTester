package cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.config.node.dns.DnsLine;
import cloud.xcan.angus.core.tester.domain.config.node.dns.DnsRecordType;
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
  @Schema(description = "Domain identifier for DNS record association", requiredMode = RequiredMode.REQUIRED)
  private Long domainId;

  @NotNull
  @Schema(description = "DNS record type defining the record format and purpose", example = "MX", requiredMode = RequiredMode.REQUIRED)
  private DnsRecordType type;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "DNS record name for subdomain or host identification", example = "www", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "DNS line configuration for geographic or network routing", requiredMode = RequiredMode.REQUIRED)
  private DnsLine line;

  @IPv4
  @NotBlank
  @Schema(description = "DNS record value containing the actual data (IP address, domain, etc.)", example = "192.0.2.254", requiredMode = RequiredMode.REQUIRED)
  private String value;

  @Range(min = 1, max = 86400)
  @Schema(description = "Time-to-live value in seconds for DNS record caching", example = "600")
  private Integer ttl;

}
