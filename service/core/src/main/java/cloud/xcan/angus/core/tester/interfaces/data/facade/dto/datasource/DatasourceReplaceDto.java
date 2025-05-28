package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource;

import static cloud.xcan.angus.model.AngusConstant.MAX_DATABASE_JDBC_URL_LENGTH;
import static cloud.xcan.angus.model.AngusConstant.MAX_DATABASE_PASSWORD_LENGTH;
import static cloud.xcan.angus.model.AngusConstant.MAX_DATABASE_USERNAME_LENGTH;
import static cloud.xcan.angus.model.AngusConstant.MAX_DATASOURCE_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class DatasourceReplaceDto {

  //@NotNull
  @Schema(description="Modify database id. Create a new database when the value is null")
  private Long id;

  @Schema(description = "Project id, required when creating a new dataset")
  private Long projectId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(example = "Mysql-Conn-001", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotBlank
  @Length(max = MAX_DATASOURCE_NAME_LENGTH)
  @Schema(description = "Database data source type", example = "MYSQL", requiredMode = RequiredMode.REQUIRED)
  private String database;

  @Length(max = MAX_NAME_LENGTH_X2)
  private String driverClassName;

  @Length(max = MAX_DATABASE_USERNAME_LENGTH)
  @Schema(example = "admin")
  private String username;

  @Length(max = MAX_DATABASE_PASSWORD_LENGTH)
  @Schema(example = "xcan@123")
  private String password;

  /**
   * Connection URL Syntax:
   * <p>
   * https://docs.oracle.com/cd/E17952_01/connector-j-8.0-en/connector-j-reference-jdbc-url-format.html
   * <p>
   * protocol//[hosts][/database][?properties] -> jdbc:mysql://host1:33060/sakila
   * <p>
   * Note: The format of jdbcUrl is different without database.
   */
  @NotBlank
  @Length(max = MAX_DATABASE_JDBC_URL_LENGTH)
  @Schema(example = "jdbc:msql://localhost:3306/angusmock", requiredMode = RequiredMode.REQUIRED)
  private String jdbcUrl;

}
