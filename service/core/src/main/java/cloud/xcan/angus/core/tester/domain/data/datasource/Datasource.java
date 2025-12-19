package cloud.xcan.angus.core.tester.domain.data.datasource;


import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.spec.experimental.AESValue;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;


@Entity
@Table(name = "data_datasource")
@Setter
@Getter
@Accessors(chain = true)
public class Datasource extends TenantAuditingEntity<Datasource, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  private String name;

  @Column(name = "database0")
  private String database;

  @Column(name = "driver_class_name")
  private String driverClassName;

  private String username;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private AESValue password;

  /**
   * Connection URL Syntax:
   * <p>
   * https://docs.oracle.com/cd/E17952_01/connector-j-8.0-en/connector-j-reference-jdbc-url-format.html
   * <p>
   * protocol//[hosts][/database][?properties] -> jdbc:mysql://host1:33060/sakila
   * <p>
   * Note: The format of jdbcUrl is different without database.
   */
  @Column(name = "jdbc_url")
  private String jdbcUrl;

  @Transient
  private Boolean connSuccess;
  @Transient
  private String connFailureMessage;
  @Transient
  private String modifier;
  @Transient
  private String avatar;

  public String getDecryptPassword() {
    return nonNull(password) ? password.decrypt(String.valueOf(getOptTenantId())) : null;
  }

  @Override
  public Long identity() {
    return this.id;
  }
}
