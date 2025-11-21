package cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.config.node.dns.DnsLine;
import cloud.xcan.angus.core.tester.domain.config.node.dns.DnsRecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@Accessors(chain = true)
public class NodeDomainDnsUpdateDto {

  @NotNull
  @Schema(description = "DNS record identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(description = "Domain identifier for DNS record association", example = "1")
  private Long domainId;

  @Schema(description = "DNS record type defining the record format and purpose", example = "MX")
  private DnsRecordType type;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "DNS record name for subdomain or host identification", example = "www")
  private String name;

  @Schema(description = "DNS line configuration for geographic or network routing")
  private DnsLine line;

  @Schema(description = "DNS record value containing the actual data (IP address, domain, etc.)", example = "192.0.2.254")
  private String value;

  @Range(min = 1, max = 86400)
  @Schema(description = "Time-to-live value in seconds for DNS record caching", example = "600")
  private Integer ttl;

}
