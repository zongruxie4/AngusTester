package cloud.xcan.angus.core.tester.application.converter;


import static cloud.xcan.angus.spec.utils.JsonUtils.fromJsonObject;
import static cloud.xcan.angus.spec.utils.ObjectUtils.convert;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.indicator.Func;
import cloud.xcan.angus.api.gm.indicator.SmokeCheckSetting;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorFunc;
import cloud.xcan.angus.core.utils.GsonUtils;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.reflect.TypeToken;
import java.time.LocalDateTime;

/**
 * @author XiaoLong Liu
 */
public class IndicatorFuncConverter {

  public static IndicatorFunc toIndicatorFunc(Func default0, Long targetId,
      CombinedTargetType targetType) {
    return new IndicatorFunc()
        .setTargetType(targetType)
        .setTargetId(targetId)
        .setSmoke(default0.isSmoke())
        .setSmokeCheckSetting(default0.getSmokeCheckSetting())
        .setUserDefinedSmokeAssertion(fromJsonObject(default0.getUserDefinedSmokeAssertion(),
            new TypeReference<Assertion<HttpExtraction>>() {
            }))
        .setSecurity(default0.isSecurity())
        .setSecurityCheckSetting(default0.getSecurityCheckSetting())
        .setUserDefinedSecurityAssertion(fromJsonObject(default0.getUserDefinedSecurityAssertion(),
            new TypeReference<Assertion<HttpExtraction>>() {
            }));
  }

  public static IndicatorFunc objectArrToFunc(Object[] objects) {
    return new IndicatorFunc()
        .setId(convert(objects[0], Long.class))
        .setTargetId(convert(objects[1], Long.class))
        .setSmoke(convert(objects[2], Boolean.class))
        .setSmokeCheckSetting(isNull(objects[3]) ? null
            : GsonUtils.fromJson(objects[3].toString(), SmokeCheckSetting.class))
        .setUserDefinedSmokeAssertion(isNull(objects[4]) ? null
            : GsonUtils.fromJson(objects[4].toString(), new TypeToken<Assertion<HttpExtraction>>() {
            }.getType()))
        .setSecurity(convert(objects[5], Boolean.class))
        .setSmokeCheckSetting(isNull(objects[6]) ? null
            : GsonUtils.fromJson(objects[6].toString(), SmokeCheckSetting.class))
        .setUserDefinedSmokeAssertion(isNull(objects[7]) ? null
            : GsonUtils.fromJson(objects[7].toString(), new TypeToken<Assertion<HttpExtraction>>() {
            }.getType()))
        .setCreatedBy(convert(objects[8], Long.class))
        .setCreatedDate(convert(objects[9], LocalDateTime.class))
        .setTargetName(convert(objects[10], String.class))
        .setTargetType(convert(objects[11], CombinedTargetType.class));
  }

}
