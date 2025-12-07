package cloud.xcan.angus.core.tester.infra.util;

import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_MOCK_APIS_FILE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.assembleGrantPermissionCondition;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterInFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getFilterMatchFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.getInConditionValue;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_ASSERT_SERVER_IS_RESPONSIVE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_ASSERT_STATUS_GE_200;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_ASSERT_STATUS_IS_401_403;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_ASSERT_STATUS_IS_NOT_401_403;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_ASSERT_STATUS_IS_OK;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_ASSERT_STATUS_LESS_300;
import static cloud.xcan.angus.core.tester.infra.util.ServicesFileUtils.getExportTmpPath;
import static cloud.xcan.angus.core.tester.infra.util.ServicesFileUtils.getImportTmpPath;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.angus.model.AngusConstant.IS_SECURITY_CODE_REG;
import static cloud.xcan.angus.model.AngusConstant.NOT_SECURITY_CODE_REG;
import static cloud.xcan.angus.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.StreamUtils.copyToString;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.config.indicator.SecurityCheckSetting;
import cloud.xcan.angus.core.tester.domain.config.indicator.SmokeCheckSetting;
import cloud.xcan.angus.core.tester.domain.script.ScriptFormat;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.assertion.AssertionCondition;
import cloud.xcan.angus.model.element.assertion.AssertionType;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.http.HttpStatus;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.utils.FileUtils;
import cloud.xcan.angus.spec.utils.JsonUtils;
import cloud.xcan.angus.spec.utils.StreamUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for Angus Tester core functionality and common operations.
 * <p>
 * Provides file handling utilities, SQL assembly helpers, assertion builders, and sample data
 * processing functions.
 * <p>
 * Implements import/export file operations, authorization query building, and test case assertion
 * generation.
 * <p>
 * Supports multiple target types (API, Service, Scenario, Script) and various data formats (JSON,
 * YAML).
 */
public class AngusTesterUtils {

  /**
   * Converts a multipart file to a temporary file for import processing.
   * <p>
   * Creates a temporary directory and transfers the uploaded file to it.
   * <p>
   * Ensures proper file handling and error management during transfer.
   * <p>
   *
   * @param file the multipart file to convert
   * @return the temporary file created for import processing
   * @throws SysException if file transfer fails
   */
  public static @NotNull File convertImportFile(MultipartFile file) {
    // Create temporary directory for import processing
    File tmpPath = getImportTmpPath(ApiImportSource.OPENAPI, null);

    // Create file with original name in temporary directory
    File importFile = new File(tmpPath.getPath() + File.separator + file.getOriginalFilename());

    try {
      // Transfer uploaded file to temporary location
      file.transferTo(importFile);
    } catch (IOException e) {
      throw SysException.of("Transfer import file exception, cause: "
          + e.getMessage(), ExceptionLevel.ERROR);
    }
    return importFile;
  }

  /**
   * Writes content to a temporary export file.
   * <p>
   * Creates a temporary directory and writes the specified content to a file.
   * <p>
   * Ensures proper file encoding and error handling during write operations.
   * <p>
   *
   * @param name    the name of the export file
   * @param content the content to write to the file
   * @return the temporary file created for export
   * @throws SysException if file write operation fails
   */
  public static @NotNull File writeExportFile(String name, String content) {
    // Create temporary directory for export processing
    File tmpPath = getExportTmpPath(null);

    // Create file with specified name in temporary directory
    File exportFile = new File(tmpPath.getPath() + File.separator + name);

    try {
      // Write content to file with UTF-8 encoding
      FileUtils.writeStringToFile(exportFile, content, UTF_8);
    } catch (IOException e) {
      throw SysException.of("Exception write export file, cause: "
          + e.getMessage(), ExceptionLevel.URGENT);
    }
    return exportFile;
  }

  /**
   * Assembles SQL JOIN clauses for indicator queries based on target type.
   * <p>
   * Builds appropriate JOIN statements to connect indicators with their target entities.
   * <p>
   * Supports different target types: API, Project/Service, and Scenario.
   * <p>
   *
   * @param targetType the type of target entity (API, PROJECT, SERVICE, SCENARIO)
   * @param sql        the StringBuilder to append the JOIN clause to
   */
  public static void assembleIndicatorJoinTargetSql(String targetType, StringBuilder sql) {
    // Get current tenant ID for data isolation
    long tenantId = getOptTenantId();

    // Build JOIN clause based on target type
    if (targetType.equals(CombinedTargetType.API.getValue())) {
      // Join with APIs table
      sql.append(" INNER JOIN apis a ON a.id = ip.target_id AND a.tenant_id = ")
          .append(tenantId);
    } else if (targetType.equals(CombinedTargetType.PROJECT.getValue()) || targetType
        .equals(CombinedTargetType.SERVICE.getValue())) {
      // Join with services table (projects are treated as services)
      sql.append(" INNER JOIN services a ON a.id = ip.target_id AND a.tenant_id = ")
          .append(tenantId);
    } else if (targetType.equals(CombinedTargetType.SCENARIO.getValue())) {
      // Join with scenario table
      sql.append(" INNER JOIN scenario a ON a.id = ip.target_id AND a.tenant_id = ")
          .append(tenantId);
    }
  }

  /**
   * Assembles authorization JOIN conditions for target queries based on target type.
   * <p>
   * Builds SQL conditions to filter resources based on user authorization permissions.
   * <p>
   * Supports different target types and handles both VIEW and GRANT permission scenarios.
   * <p>
   *
   * @param targetType the type of target entity
   * @param sql        the StringBuilder to append the condition to
   * @param criteria   the search criteria containing authorization filters
   */
  public static void assembleAuthJoinTargetCondition(String targetType, StringBuilder sql,
      Set<SearchCriteria> criteria) {
    // Extract authorization object IDs from criteria
    String authObjectIds = getFilterInFirstValue(criteria, "authObjectId");
    if (isEmpty(authObjectIds)) {
      // Admin can query all resources when authObjectIds is empty
      return;
    }

    // Validate target type is provided
    assertNotNull(targetType, "targetType is required");

    // Build grant permission filter condition
    String grantFilter = assembleGrantPermissionCondition(criteria, "a3", "GRANT");

    // Get authorization object IDs for IN clause
    String authObjectIdsInValue = getInConditionValue(authObjectIds);
    long tenantId = getOptTenantId();

    // Build authorization condition based on target type and permission level
    if (isEmpty(grantFilter)) {
      // Query resources with VIEW permission
      buildViewPermissionCondition(targetType, sql, authObjectIdsInValue, tenantId);
    } else {
      // Query resources with GRANT permission
      buildGrantPermissionCondition(targetType, sql, authObjectIdsInValue, grantFilter);
    }
  }

  /**
   * Builds SQL condition for resources with VIEW permission.
   * <p>
   * Creates UNION query to include both public resources and user-authorized resources.
   * <p>
   *
   * @param targetType           the type of target entity
   * @param sql                  the StringBuilder to append the condition to
   * @param authObjectIdsInValue the authorization object IDs for IN clause
   * @param tenantId             the current tenant ID
   */
  private static void buildViewPermissionCondition(String targetType, StringBuilder sql,
      String authObjectIdsInValue, long tenantId) {
    if (targetType.equals(CombinedTargetType.API.getValue())) {
      sql.append(
          " AND a.id IN (SELECT a1.id FROM apis a1 WHERE a1.tenant_id=" + tenantId
              + " AND (a1.auth = 0 OR a1.service_auth = 0) "
              + " UNION SELECT a2.id FROM apis a2 INNER JOIN apis_auth a3 ON a2.id = a3.apis_id "
              + " AND a3.auth_object_id IN ").append(authObjectIdsInValue).append(")");
    } else if (targetType.equals(CombinedTargetType.SERVICE.getValue())) {
      sql.append(
              " AND a.id IN (SELECT a1.id FROM services a1 WHERE a1.tenant_id=" + tenantId
                  + " AND a1.auth = 0 "
                  + " UNION SELECT a2.id FROM services a2 INNER JOIN services_auth a3 ON a2.id = a3.service_id "
                  + " AND a2.auth = 1 AND a3.auth_object_id IN ").append(authObjectIdsInValue)
          .append(")");
    } else if (targetType.equals(CombinedTargetType.SCENARIO.getValue())) {
      sql.append(
              " AND a.id IN (SELECT a1.id FROM scenario a1 WHERE a1.tenant_id=" + tenantId
                  + " AND a1.auth = 0 "
                  + " UNION SELECT a2.id FROM scenario a2 INNER JOIN scenario_auth a3 ON a2.id = a3.scenario_id "
                  + " AND a2.auth = 1 AND a3.auth_object_id IN ").append(authObjectIdsInValue)
          .append(")");
    } else if (targetType.equals(CombinedTargetType.SCRIPT.getValue())) {
      sql.append(
              " AND a.id IN (SELECT a1.id FROM script a1 WHERE a1.tenant_id=" + tenantId
                  + " AND a1.auth = 0 "
                  + " UNION SELECT a2.id FROM script a2 INNER JOIN script_auth a3 ON a2.id = a3.script_id "
                  + " AND a2.auth = 1 AND a3.auth_object_id IN ").append(authObjectIdsInValue)
          .append(")");
    }
  }

  /**
   * Builds SQL condition for resources with GRANT permission.
   * <p>
   * Creates query to include only resources where user has GRANT permission.
   * <p>
   *
   * @param targetType           the type of target entity
   * @param sql                  the StringBuilder to append the condition to
   * @param authObjectIdsInValue the authorization object IDs for IN clause
   * @param grantFilter          the grant permission filter condition
   */
  private static void buildGrantPermissionCondition(String targetType, StringBuilder sql,
      String authObjectIdsInValue, String grantFilter) {
    if (targetType.equals(CombinedTargetType.API.getValue())) {
      sql.append(
              " AND a.id IN (SELECT a2.id FROM apis a2 INNER JOIN apis_auth a3 "
                  + "   ON a2.id = a3.apis_id AND a3.auth_object_id IN ")
          .append(authObjectIdsInValue).append(grantFilter).append(")");
    } else if (targetType.equals(CombinedTargetType.SERVICE.getValue())) {
      sql.append(
              " AND a.id IN (SELECT a2.id FROM services a2 INNER JOIN services_auth a3 "
                  + "   ON a2.id = a3.service_id AND a3.auth_object_id IN ")
          .append(authObjectIdsInValue).append(grantFilter).append(")");
    } else if (targetType.equals(CombinedTargetType.SCENARIO.getValue())) {
      sql.append(
              " AND a.id IN (SELECT a2.id FROM scenario a2 INNER JOIN scenario_auth a3 "
                  + "   ON a2.id = a3.scenario_id AND a3.auth_object_id IN ")
          .append(authObjectIdsInValue).append(grantFilter).append(")");
    } else if (targetType.equals(CombinedTargetType.SCRIPT.getValue())) {
      sql.append(
              " AND a.id IN (SELECT a2.id FROM script a2 INNER JOIN script_auth a3 "
                  + "   ON a2.id = a3.script_id AND a3.auth_object_id IN ")
          .append(authObjectIdsInValue).append(grantFilter).append(")");
    }
  }

  /**
   * Assembles full-text search conditions for target name matching.
   * <p>
   * Builds MySQL MATCH AGAINST clauses for efficient full-text search on target names.
   * <p>
   * Supports different search fields based on target type for optimized search results.
   * <p>
   *
   * @param criteria        the search criteria containing target name filter
   * @param targetTypeValue the type of target entity for field selection
   * @param sql             the StringBuilder to append the condition to
   */
  public static void assembleTargetNameMatchCondition(Set<SearchCriteria> criteria,
      String targetTypeValue, StringBuilder sql) {
    // Extract search value from criteria
    String searchValue = getFilterMatchFirstValue(criteria, "targetName");
    if (isNotEmpty(searchValue)) {
      if (targetTypeValue.equalsIgnoreCase(CombinedTargetType.API.getValue())) {
        // Use full-text search on API summary, operation ID, and merged search fields
        sql.append(" AND MATCH(").append("a.summary, a.operation_id, a.ext_search_merge")
            .append(") AGAINST ( \"").append(searchValue).append("\" IN BOOLEAN MODE)");
      } else if (targetTypeValue.equalsIgnoreCase(CombinedTargetType.SCENARIO.getValue())) {
        // Use full-text search on scenario name, description, and merged search fields
        sql.append(" AND MATCH(").append("a.name, a.description, a.ext_search_merge")
            .append(") AGAINST ( \"").append(searchValue).append("\" IN BOOLEAN MODE)");
      } else {
        // Use full-text search on target name for other entity types
        sql.append(" AND MATCH(").append("a.name")
            .append(") AGAINST ( \"").append(searchValue).append("\" IN BOOLEAN MODE)");
      }
    }
  }

  /**
   * Assembles LIKE conditions for target name matching using pattern matching.
   * <p>
   * Builds SQL LIKE clauses for partial string matching on target names.
   * <p>
   * Used when full-text search is not available or for simple pattern matching.
   * <p>
   *
   * @param criteria the search criteria containing target name filter
   * @param sql      the StringBuilder to append the condition to
   */
  public static void assembleTargetNameLikeCondition(Set<SearchCriteria> criteria,
      StringBuilder sql) {
    // Extract search value from criteria
    String searchValue = getFilterMatchFirstValue(criteria, "targetName");
    if (isNotEmpty(searchValue)) {
      // Build LIKE condition for partial name matching
      sql.append(" AND a.name like \"%%").append(searchValue).append("%%\"");
    }
  }

  /**
   * Assembles smoke test assertions based on smoke check settings.
   * <p>
   * Creates appropriate HTTP assertions for different smoke test scenarios.
   * <p>
   * Supports service availability, API availability, and custom user-defined assertions.
   * <p>
   *
   * @param smokeCheckSetting         the smoke check configuration
   * @param userDefinedSmokeAssertion optional user-defined assertion
   * @return list of assembled assertions for smoke testing
   */
  public static List<Assertion<HttpExtraction>> assembleSmokeCaseAssertion(
      SmokeCheckSetting smokeCheckSetting, Assertion<HttpExtraction> userDefinedSmokeAssertion) {
    // Use default setting if none provided
    SmokeCheckSetting safeSetting = nullSafe(smokeCheckSetting, SmokeCheckSetting.API_AVAILABLE);
    List<Assertion<HttpExtraction>> assertions = new ArrayList<>();

    switch (safeSetting) {
      case SERVICE_AVAILABLE:
        // Create assertion to check if server is responsive (status > 0)
        Assertion<HttpExtraction> assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_SERVER_IS_RESPONSIVE))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.GREATER_THAN)
            .setExpected("0");
        assertions.add(assertion);
        return assertions;

      case API_AVAILABLE:
        // Create assertion to check if API returns success status (200-299)
        assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_GE_200))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.GREATER_THAN_EQUAL)
            .setExpected(HttpStatus.OK.getValue());
        assertions.add(assertion);

        // Create assertion to check if status is less than 300 (no redirection)
        assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_LESS_300))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.LESS_THAN)
            .setExpected(HttpStatus.MULTIPLE_CHOICES.getValue());
        assertions.add(assertion);
        return assertions;

      default:
        // Use user-defined assertion or create default OK status assertion
        if (nonNull(userDefinedSmokeAssertion)) {
          assertions.add(userDefinedSmokeAssertion);
        } else {
          assertion = new Assertion<>();
          assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_IS_OK))
              .setEnabled(true)
              .setType(AssertionType.STATUS)
              .setAssertionCondition(AssertionCondition.EQUAL)
              .setExpected(HttpStatus.OK.getValue());
          assertions.add(assertion);
        }
        return assertions;
    }
  }

  /**
   * Assembles security test assertions based on security check settings.
   * <p>
   * Creates appropriate HTTP assertions for different security test scenarios.
   * <p>
   * Supports checking for security codes (401/403) and custom user-defined assertions.
   * <p>
   *
   * @param securityCheckSetting         the security check configuration
   * @param userDefinedSecurityAssertion optional user-defined assertion
   * @return list of assembled assertions for security testing
   */
  public static List<Assertion<HttpExtraction>> assembleSecurityCaseAssertion(
      SecurityCheckSetting securityCheckSetting,
      Assertion<HttpExtraction> userDefinedSecurityAssertion) {
    // Use default setting if none provided
    SecurityCheckSetting safeSetting = nullSafe(securityCheckSetting,
        SecurityCheckSetting.NOT_SECURITY_CODE);
    List<Assertion<HttpExtraction>> assertions = new ArrayList<>();

    switch (safeSetting) {
      case NOT_SECURITY_CODE:
        // Create assertion to check that response is NOT a security code (401/403)
        Assertion<HttpExtraction> assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_IS_NOT_401_403))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.REG_MATCH)
            .setExpression(NOT_SECURITY_CODE_REG);
        assertions.add(assertion);
        return assertions;

      case IS_SECURITY_CODE:
        // Create assertion to check that response IS a security code (401/403)
        assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_IS_401_403))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.REG_MATCH)
            .setExpression(IS_SECURITY_CODE_REG);
        assertions.add(assertion);
        return assertions;

      default:
        // Use user-defined assertion or create default non-security code assertion
        if (nonNull(userDefinedSecurityAssertion)) {
          assertions.add(userDefinedSecurityAssertion);
        } else {
          assertion = new Assertion<>();
          assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_IS_NOT_401_403))
              .setEnabled(true)
              .setType(AssertionType.STATUS)
              .setAssertionCondition(AssertionCondition.REG_MATCH)
              .setExpression(NOT_SECURITY_CODE_REG);
          assertions.add(assertion);
        }
        return assertions;
    }
  }

  /**
   * Creates a ResponseEntity for downloading resources in various formats.
   * <p>
   * Converts objects to JSON or YAML format and creates downloadable response entities.
   * <p>
   * Supports different script formats and provides proper content type headers.
   * <p>
   *
   * @param fileName the base name for the downloaded file
   * @param format   the script format (JSON or YAML) for content serialization
   * @param obj      the object to serialize and download
   * @return ResponseEntity containing the downloadable resource
   */
  @SneakyThrows
  public static @NotNull ResponseEntity<Resource> getResourceResponseEntity(
      String fileName, ScriptFormat format, Object obj) {
    // Serialize object to JSON or YAML format based on script format
    String content = format.isYaml()
        ? AngusParser.YAML_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
        : AngusParser.JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);

    // Convert content to bytes for streaming
    byte[] contentBytes = content.getBytes();
    InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(contentBytes));

    // Build and return download response entity
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
        fileName + format.getFileSuffix(), contentBytes.length, resource);
  }

  /**
   * Parses a sample file from resources and converts it to the specified type.
   * <p>
   * Reads content from a resource URL and deserializes it using JSON utilities.
   * <p>
   * Provides proper error handling for file reading and parsing operations.
   * <p>
   *
   * @param resourceUrl the URL of the resource file to parse
   * @param type        the TypeReference for deserialization
   * @param exampleFile the name of the example file for error reporting
   * @return the parsed object of the specified type
   * @throws SysException if file reading or parsing fails
   */
  public static <T> T parseSample(URL resourceUrl, TypeReference<T> type, String exampleFile) {
    try {
      // Read content from resource URL
      String content = copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
      // Convert content to specified type using JSON utilities
      return JsonUtils.convert(content, type);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + exampleFile,
          e.getMessage());
    }
  }

  /**
   * Reads a sample file from resources and returns its content as a string.
   * <p>
   * Reads content from a resource URL and returns it as a UTF-8 encoded string.
   * <p>
   * Provides proper error handling for file reading operations.
   * <p>
   *
   * @param resourceUrl the URL of the resource file to read
   * @param exampleFile the name of the example file for error reporting
   * @return the content of the file as a string
   * @throws SysException if file reading fails
   */
  public static String readSample(URL resourceUrl, String exampleFile) {
    try {
      // Read content from resource URL and convert to string
      return copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + exampleFile,
          e.getMessage());
    }
  }

  /**
   * Reads example services content from resources based on current language.
   * <p>
   * Loads sample service files from language-specific resource directories.
   * <p>
   * Provides proper error handling for file reading operations.
   * <p>
   *
   * @param resClz       the class for resource loading
   * @param servicesFile the name of the services file to read
   * @return the content of the example services file
   * @throws SysException if file reading fails
   */
  public static @NotNull String readExampleServicesContent(Class<?> resClz, String servicesFile) {
    // Build resource path with language-specific directory
    URL resourceUrl = resClz.getResource("/samples/services/"
        + getDefaultLanguage().getValue() + "/" + servicesFile);

    String content;
    try {
      // Read content from resource URL
      content = StreamUtils.copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + servicesFile, e.getMessage());
    }
    return content;
  }

  /**
   * Reads example script content from resources based on current language.
   * <p>
   * Loads sample script files from language-specific resource directories.
   * <p>
   * Provides proper error handling and resource validation.
   * <p>
   *
   * @param resClz     the class for resource loading
   * @param scriptFile the name of the script file to read
   * @return the content of the example script file
   * @throws SysException if file reading fails
   */
  public static @NotNull String readExampleScriptContent(Class<?> resClz, String scriptFile) {
    try {
      // Build resource path with language-specific directory
      URL resourceUrl = resClz.getResource("/samples/script/"
          + getDefaultLanguage().getValue() + "/" + scriptFile);

      // Validate resource URL exists
      assert resourceUrl != null;

      // Read content from resource URL
      return copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + scriptFile, e.getMessage());
    }
  }

  /**
   * Reads example mock APIs content from resources based on current language.
   * <p>
   * Loads sample mock API files from language-specific resource directories.
   * <p>
   * Uses predefined mock APIs file constant for consistent file naming.
   * <p>
   *
   * @param resClz     the class for resource loading
   * @param scriptFile the name parameter (not used, kept for compatibility)
   * @return the content of the example mock APIs file
   * @throws SysException if file reading fails
   */
  public static @NotNull String readExampleMockApisContent(Class<?> resClz, String scriptFile) {
    try {
      // Build resource path with language-specific directory using constant file name
      URL resourceUrl = resClz.getResource("/samples/mock/"
          + getDefaultLanguage().getValue() + "/" + SAMPLE_MOCK_APIS_FILE);

      // Validate resource URL exists
      assert resourceUrl != null;

      // Read content from resource URL
      return copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + scriptFile, e.getMessage());
    }
  }
}
