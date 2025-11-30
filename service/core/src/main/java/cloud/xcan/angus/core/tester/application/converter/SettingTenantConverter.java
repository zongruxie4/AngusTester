package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.api.commonlink.TesterConstant.CONFIG_EVALUATION_INDICATOR;
import static cloud.xcan.angus.api.commonlink.TesterConstant.CONFIG_EVENT;
import static cloud.xcan.angus.api.commonlink.TesterConstant.CONFIG_FUNCTION_INDICATOR;
import static cloud.xcan.angus.api.commonlink.TesterConstant.CONFIG_PREF_INDICATOR;
import static cloud.xcan.angus.api.commonlink.TesterConstant.CONFIG_STABILITY_INDICATOR;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static java.util.Objects.requireNonNull;

import cloud.xcan.angus.core.tester.domain.config.indicator.FuncData;
import cloud.xcan.angus.core.tester.domain.config.indicator.PerfData;
import cloud.xcan.angus.core.tester.domain.config.indicator.StabilityData;
import cloud.xcan.angus.core.tester.domain.config.tenant.SettingTenant;
import cloud.xcan.angus.core.tester.domain.config.tenant.event.TesterEvent;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

public class SettingTenantConverter {

  public static SettingTenant initTenantSetting(Long tenantId) {
    SettingTenant tenant = new SettingTenant();
    tenant.setId(tenantId);
    tenant.setTenantId(tenantId);

    LinkedHashMap<EvaluationPurpose, Integer> evaluation = getDefaultEvaluationPurpose(tenant);
    tenant.setEvaluationWeightData(evaluation);

    FuncData func = getDefaultFuncData(tenant);
    tenant.setFuncData(func);

    PerfData perf = getDefaultPerfData(tenant);
    tenant.setPerfData(perf);

    StabilityData stability = getDefaultStabilityData(tenant);
    tenant.setStabilityData(stability);

    List<TesterEvent> events = getDefaultTesterEvents(tenant);
    tenant.setTesterEventData(events);
    return tenant;
  }

  public static List<TesterEvent> getDefaultTesterEvents(SettingTenant tenant) {
    URL resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_EVENT);
    return parseSample(requireNonNull(resourceUrl),
        new TypeReference<List<TesterEvent>>() {
        }, CONFIG_EVENT);
  }

  public static StabilityData getDefaultStabilityData(SettingTenant tenant) {
    URL resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_STABILITY_INDICATOR);
    return parseSample(requireNonNull(resourceUrl),
        new TypeReference<StabilityData>() {
        }, CONFIG_STABILITY_INDICATOR);
  }

  public static PerfData getDefaultPerfData(SettingTenant tenant) {
    URL resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_PREF_INDICATOR);
    return parseSample(requireNonNull(resourceUrl),
        new TypeReference<PerfData>() {
        }, CONFIG_PREF_INDICATOR);
  }

  public static FuncData getDefaultFuncData(SettingTenant tenant) {
    URL resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_FUNCTION_INDICATOR);
    return parseSample(requireNonNull(resourceUrl),
        new TypeReference<FuncData>() {
        }, CONFIG_FUNCTION_INDICATOR);
  }

  public static LinkedHashMap<EvaluationPurpose, Integer> getDefaultEvaluationPurpose(
      SettingTenant tenant) {
    URL resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_EVALUATION_INDICATOR);
    return parseSample(requireNonNull(resourceUrl),
        new TypeReference<LinkedHashMap<EvaluationPurpose, Integer>>() {
        }, CONFIG_EVALUATION_INDICATOR);
  }

}
