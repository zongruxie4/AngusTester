package cloud.xcan.angus.core.tester.application.converter;

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
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URL;
import java.util.List;

public class SettingTenantConverter {

  public static SettingTenant initTenantSetting(Long tenantId) {
    SettingTenant tenant = new SettingTenant();
    tenant.setId(tenantId);
    tenant.setTenantId(tenantId);

    URL resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_FUNCTION_INDICATOR);
    FuncData func = parseSample(requireNonNull(resourceUrl),
        new TypeReference<FuncData>() {
        }, CONFIG_FUNCTION_INDICATOR);
    tenant.setFuncData(func);

    resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_PREF_INDICATOR);
    PerfData perf = parseSample(requireNonNull(resourceUrl),
        new TypeReference<PerfData>() {
        }, CONFIG_PREF_INDICATOR);
    tenant.setPerfData(perf);

    resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_STABILITY_INDICATOR);
    StabilityData stability = parseSample(requireNonNull(resourceUrl),
        new TypeReference<StabilityData>() {
        }, CONFIG_STABILITY_INDICATOR);
    tenant.setStabilityData(stability);

    resourceUrl = tenant.getClass().getResource("/config/"
        + getDefaultLanguage().getValue() + "/" + CONFIG_EVENT);
    List<TesterEvent> events = parseSample(requireNonNull(resourceUrl),
        new TypeReference<List<TesterEvent>>() {
        }, CONFIG_EVENT);
    tenant.setTesterEventData(events);
    return tenant;
  }

}
