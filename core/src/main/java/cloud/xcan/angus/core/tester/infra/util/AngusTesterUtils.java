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
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.StreamUtils.copyToString;
import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.gm.indicator.SecurityCheckSetting;
import cloud.xcan.angus.api.gm.indicator.SmokeCheckSetting;
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
import cloud.xcan.angus.spec.utils.ObjectUtils;
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

public class AngusTesterUtils {

  public static @NotNull File convertImportFile(MultipartFile file) {
    File tmpPath = getImportTmpPath(ApiImportSource.OPENAPI, null);
    File importFile = new File(tmpPath.getPath() + File.separator + file.getName());
    try {
      file.transferTo(importFile);
    } catch (IOException e) {
      throw SysException.of("Transfer import file exception, cause: "
          + e.getMessage(), ExceptionLevel.ERROR);
    }
    return importFile;
  }

  public static @NotNull File writeExportFile(String name, String content) {
    File tmpPath = getExportTmpPath(null);
    File exportFile = new File(tmpPath.getPath() + File.separator + name);
    try {
      FileUtils.writeStringToFile(exportFile, content, UTF_8);
    } catch (IOException e) {
      throw SysException.of("Exception write export file, cause: "
          + e.getMessage(), ExceptionLevel.URGENT);
    }
    return exportFile;
  }

  public static void assembleIndicatorJoinTargetSql(String targetType, StringBuilder sql) {
    long tenantId = getOptTenantId();
    if (targetType.equals(CombinedTargetType.API.getValue())) {
      sql.append(" INNER JOIN apis a ON a.id = ip.target_id AND a.tenant_id = ")
          .append(tenantId);
    } else if (targetType.equals(CombinedTargetType.PROJECT.getValue()) || targetType
        .equals(CombinedTargetType.SERVICE.getValue())) {
      sql.append(" INNER JOIN services a ON a.id = ip.target_id AND a.tenant_id = ")
          .append(tenantId);
    } else if (targetType.equals(CombinedTargetType.SCENARIO.getValue())) {
      sql.append(" INNER JOIN scenario a ON a.id = ip.target_id AND a.tenant_id = ")
          .append(tenantId);
    }
  }

  public static void assembleAuthJoinTargetCondition(String targetType, StringBuilder sql,
      Set<SearchCriteria> criteria) {
    String authObjectIds = getFilterInFirstValue(criteria, "authObjectId");
    if (ObjectUtils.isEmpty(authObjectIds)) {
      // Admin query all resource when authObjectIds is empty
      return;
    }
    assertNotNull(targetType, "targetType is required");
    String grantFilter = assembleGrantPermissionCondition(criteria, "a3", "GRANT");
    // Non-Admin query own resource when authObjectIds is not empty
    String authObjectIdsInValue = getInConditionValue(authObjectIds);
    long tenantId = getOptTenantId();
    // @formatter:off
    if (ObjectUtils.isEmpty(grantFilter)){
      // Query has `VIEW` permission resource
      if (targetType.equals(CombinedTargetType.API.getValue())) {
        sql.append(
            " AND a.id IN (SELECT a1.id FROM apis a1 WHERE a1.tenant_id=" + tenantId + " AND (a1.auth = 0 OR a1.service_auth = 0) "
                + " UNION SELECT a2.id FROM apis a2 INNER JOIN apis_auth a3 ON a2.id = a3.apis_id "
                +     " AND a3.auth_object_id IN ").append(authObjectIdsInValue).append(")");
      } else if (targetType.equals(CombinedTargetType.SERVICE.getValue())) {
        sql.append(
            " AND a.id IN (SELECT a1.id FROM services a1 WHERE a1.tenant_id=" + tenantId + " AND a1.auth = 0 "
                + " UNION SELECT a2.id FROM services a2 INNER JOIN services_auth a3 ON a2.id = a3.service_id "
                +     " AND a2.auth = 1 AND a3.auth_object_id IN ").append(authObjectIdsInValue).append(")");
      } else if (targetType.equals(CombinedTargetType.SCENARIO.getValue())) {
        sql.append(
            " AND a.id IN (SELECT a1.id FROM scenario a1 WHERE a1.tenant_id=" + tenantId + " AND a1.auth = 0 "
                + " UNION SELECT a2.id FROM scenario a2 INNER JOIN scenario_auth a3 ON a2.id = a3.scenario_id "
                +     " AND a2.auth = 1 AND a3.auth_object_id IN ").append(authObjectIdsInValue).append(")");
      } else if (targetType.equals(CombinedTargetType.SCRIPT.getValue())) {
        sql.append(
            " AND a.id IN (SELECT a1.id FROM script a1 WHERE a1.tenant_id=" + tenantId + " AND a1.auth = 0 "
                + " UNION SELECT a2.id FROM script a2 INNER JOIN script_auth a3 ON a2.id = a3.script_id "
                +     " AND a2.auth = 1 AND a3.auth_object_id IN ").append(authObjectIdsInValue).append(")");
      }
    }else {
      // Query has `GRANT` permission resource
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
    // @formatter:on
  }

  public static void assembleTargetNameMatchCondition(Set<SearchCriteria> criteria,
      String targetTypeValue, StringBuilder sql) {
    String searchValue = getFilterMatchFirstValue(criteria, "targetName");
    if (ObjectUtils.isNotEmpty(searchValue)) {
      if (targetTypeValue.equalsIgnoreCase(CombinedTargetType.API.getValue())) {
        sql.append(" AND MATCH(").append("a.summary, a.operation_id, a.ext_search_merge")
            .append(") AGAINST ( \"").append(searchValue).append("\" IN BOOLEAN MODE)");
      } else if (targetTypeValue.equalsIgnoreCase(CombinedTargetType.SCENARIO.getValue())) {
        sql.append(" AND MATCH(").append("a.name, a.description, a.ext_search_merge")
            .append(") AGAINST ( \"").append(searchValue).append("\" IN BOOLEAN MODE)");
      } else {
        sql.append(" AND MATCH(").append("a.name")
            .append(") AGAINST ( \"").append(searchValue).append("\" IN BOOLEAN MODE)");
      }
    }
  }

  public static void assembleTargetNameLikeCondition(Set<SearchCriteria> criteria,
      StringBuilder sql) {
    // Assemble non mainClass match Conditions
    String searchValue = getFilterMatchFirstValue(criteria, "targetName");
    if (ObjectUtils.isNotEmpty(searchValue)) {
      sql.append(" AND a.name like \"%%").append(searchValue).append("%%\"");
    }
  }

  public static List<Assertion<HttpExtraction>> assembleSmokeCaseAssertion(
      SmokeCheckSetting smokeCheckSetting, Assertion<HttpExtraction> userDefinedSmokeAssertion) {
    SmokeCheckSetting safeSetting = nullSafe(smokeCheckSetting, SmokeCheckSetting.API_AVAILABLE);
    List<Assertion<HttpExtraction>> assertions = new ArrayList<>();
    switch (safeSetting) {
      case SERVICE_AVAILABLE:
        Assertion<HttpExtraction> assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_SERVER_IS_RESPONSIVE))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.GREATER_THAN)
            .setExpected("0");
        assertions.add(assertion);
        return assertions;
      case API_AVAILABLE:
        assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_GE_200))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.GREATER_THAN_EQUAL)
            .setExpected(HttpStatus.OK.getValue());
        assertions.add(assertion);

        assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_LESS_300))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.LESS_THAN)
            .setExpected(HttpStatus.MULTIPLE_CHOICES.getValue());
        assertions.add(assertion);
        return assertions;
      default:
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

  public static List<Assertion<HttpExtraction>> assembleSecurityCaseAssertion(
      SecurityCheckSetting securityCheckSetting,
      Assertion<HttpExtraction> userDefinedSecurityAssertion) {
    SecurityCheckSetting safeSetting = nullSafe(securityCheckSetting,
        SecurityCheckSetting.NOT_SECURITY_CODE);
    List<Assertion<HttpExtraction>> assertions = new ArrayList<>();
    switch (safeSetting) {
      case NOT_SECURITY_CODE:
        Assertion<HttpExtraction> assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_IS_NOT_401_403))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.REG_MATCH)
            .setExpression(NOT_SECURITY_CODE_REG);
        assertions.add(assertion);
        return assertions;
      case IS_SECURITY_CODE:
        assertion = new Assertion<>();
        assertion.setName(MessageHolder.message(APIS_CASE_ASSERT_STATUS_IS_401_403))
            .setEnabled(true)
            .setType(AssertionType.STATUS)
            .setAssertionCondition(AssertionCondition.REG_MATCH)
            .setExpression(IS_SECURITY_CODE_REG);
        assertions.add(assertion);
        return assertions;
      default:
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

  @SneakyThrows
  public static @NotNull ResponseEntity<Resource> getResourceResponseEntity(
      String fileName, ScriptFormat format, Object obj) {
    String content = format.isYaml()
        ? AngusParser.YAML_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
        : AngusParser.JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    byte[] contentBytes = content.getBytes();
    InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(contentBytes));
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
        fileName + format.getFileSuffix(), contentBytes.length, resource);
  }

  public static <T> T parseSample(URL resourceUrl, TypeReference<T> type, String exampleFile) {
    try {
      String content = copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
      return JsonUtils.convert(content, type);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + exampleFile,
          e.getMessage());
    }
  }

  public static String readSample(URL resourceUrl, String exampleFile) {
    try {
      return copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + exampleFile,
          e.getMessage());
    }
  }

  public static @NotNull String readExampleServicesContent(Class<?> resClz, String servicesFile) {
    URL resourceUrl = resClz.getResource("/samples/services/"
        + getDefaultLanguage().getValue() + "/" + servicesFile);
    String content;
    try {
      content = StreamUtils.copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + servicesFile, e.getMessage());
    }
    return content;
  }

  public static @NotNull String readExampleScriptContent(Class<?> resClz, String scriptFile) {
    try {
      URL resourceUrl = resClz.getResource("/samples/script/"
          + getDefaultLanguage().getValue() + "/" + scriptFile);
      assert resourceUrl != null;
      return copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + scriptFile, e.getMessage());
    }
  }

  public static @NotNull String readExampleMockApisContent(Class<?> resClz, String scriptFile) {
    try {
      URL resourceUrl = resClz.getResource("/samples/mock/"
          + getDefaultLanguage().getValue() + "/" + SAMPLE_MOCK_APIS_FILE);
      assert resourceUrl != null;
      return copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw SysException.of("Couldn't read sample file " + scriptFile, e.getMessage());
    }
  }
}
