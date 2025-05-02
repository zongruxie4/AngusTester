package cloud.xcan.angus.core.tester.domain.mock.apis.log;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.spec.http.HttpHeader;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author XiaoLong Liu
 */
@Entity
@Table(name = "mock_apis_log")
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

  private String remote;

  @Column(name = "mock_service_id")
  private Long mockServiceId;

  @Column(name = "mock_apis_id")
  private Long mockApisId;

  private String summary;

  private String method;

  private String endpoint;

  private Boolean pushback;

  @Column(name = "pushback_request_id")
  private String pushbackRequestId;

  @Column(name = "query_parameters")
  private String queryParameters;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "request_headers")
  private List<HttpHeader> requestHeaders;

  @Column(name = "request_content_encoding")
  private String requestContentEncoding;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "request_body")
  private String requestBody;

  @Column(name = "request_date")
  private LocalDateTime requestDate;

  @Column(name = "response_status")
  private int responseStatus;

  @Type(JsonType.class)
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
