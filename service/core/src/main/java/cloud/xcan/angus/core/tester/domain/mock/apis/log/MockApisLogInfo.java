package cloud.xcan.angus.core.tester.domain.mock.apis.log;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
public class MockApisLogInfo extends TenantEntity<MockApisLogInfo, Long> {

  @Id
  private Long id;

  @Column(name = "request_id")
  private String requestId;

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

  @Column(name = "pushback")
  private Boolean pushback;

  @Column(name = "request_date")
  private LocalDateTime requestDate;

  @Column(name = "response_status")
  private int responseStatus;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime createdDate;

  @Override
  public Long identity() {
    return this.id;
  }
}
