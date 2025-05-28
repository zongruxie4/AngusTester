package cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.node.dns.DnsLine;
import cloud.xcan.angus.core.tester.domain.node.dns.DnsRecordType;
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
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(example = "1")
  private Long domainId;

  @Schema(example = "MX")
  private DnsRecordType type;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(example = "www")
  private String name;

  private DnsLine line;

  @Schema(example = "192.0.2.254")
  private String value;

  @Range(min = 1, max = 86400)
  @Schema(example = "600")
  private Integer ttl;

}
