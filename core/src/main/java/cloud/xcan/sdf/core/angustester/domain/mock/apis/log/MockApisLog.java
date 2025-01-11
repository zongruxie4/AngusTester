package cloud.xcan.sdf.core.angustester.domain.mock.apis.log;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.sdf.spec.http.HttpHeader;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author xiaolong.liu
 */
@Entity
@Table(name = "mock_apis_log")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Setter
@Getter
@Accessors(chain = true)
public class MockApisLog extends TenantEntity<MockApisLog, Long> {

  @Id
  private Long id;

  @Column(name = "request_id")
  private String requestId;

  @Column(name = "exception_message")
  private String exceptionMessage;

  @Column
  private String remote;

  @Column(name = "mock_service_id")
  private Long mockServiceId;

  @Column(name = "mock_apis_id")
  private Long mockApisId;

  private String summary;

  @Column
  private String method;

  @Column
  private String endpoint;

  @Column(name = "pushback_flag")
  private Boolean pushbackFlag;

  @Column(name = "pushback_request_id")
  private String pushbackRequestId;

  @Column(name = "query_parameters")
  private String queryParameters;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "request_headers")
  private List<HttpHeader> requestHeaders;

  @Column(name = "request_content_encoding")
  private String requestContentEncoding;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "request_body")
  private String requestBody;

  @Column(name = "request_date")
  private LocalDateTime requestDate;

  @Column(name = "response_status")
  private int responseStatus;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "response_headers")
  private List<HttpHeader> responseHeaders;

  @Column(name = "response_body")
  private String responseBody;

  @Column(name = "response_date")
  private LocalDateTime responseDate;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime createdDate;

  @Override
  public Long identity() {
    return this.id;
  }
}
