package cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.dns;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.core.angustester.domain.node.dns.DnsLine;
import cloud.xcan.sdf.core.angustester.domain.node.dns.DnsRecordType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeDomainDnsUpdateDto {

  @NotNull
  @ApiModelProperty(required = true)
  private Long id;

  @ApiModelProperty(example = "1")
  private Long domainId;

  @ApiModelProperty(example = "MX")
  private DnsRecordType type;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(example = "www")
  private String name;

  private DnsLine line;

  @ApiModelProperty(example = "192.0.2.254")
  private String value;

  @Range(min = 1, max = 86400)
  @ApiModelProperty(example = "600")
  private Integer ttl;

}
