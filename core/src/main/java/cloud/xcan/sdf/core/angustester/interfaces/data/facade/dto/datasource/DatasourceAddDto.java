package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource;

import static cloud.xcan.angus.model.AngusConstant.MAX_DATABASE_JDBC_URL_LENGTH;
import static cloud.xcan.angus.model.AngusConstant.MAX_DATABASE_PASSWORD_LENGTH;
import static cloud.xcan.angus.model.AngusConstant.MAX_DATABASE_USERNAME_LENGTH;
import static cloud.xcan.angus.model.AngusConstant.MAX_DATASOURCE_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class DatasourceAddDto {

  @NotNull
  @ApiModelProperty(value = "Project id", example = "1", required = true)
  private Long projectId;

  @NotBlank
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(example = "Mysql-Conn-001", required = true)
  private String name;

  @NotBlank
  @Length(max = MAX_DATASOURCE_NAME_LENGTH)
  @ApiModelProperty(value = "Database data source type", example = "MYSQL", required = true)
  private String database;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String driverClassName;

  @Length(max = MAX_DATABASE_USERNAME_LENGTH)
  @ApiModelProperty(example = "admin")
  private String username;

  @Length(max = MAX_DATABASE_PASSWORD_LENGTH)
  @ApiModelProperty(example = "xcan@123")
  private String passd;

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
  @ApiModelProperty(example = "jdbc:msql://localhost:3306/angusmock", required = true)
  private String jdbcUrl;

}
