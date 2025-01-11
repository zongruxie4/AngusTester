package cloud.xcan.sdf.core.angustester.domain.mock.apis.log;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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

  @Column(name = "pushback_flag")
  private Boolean pushbackFlag;

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
