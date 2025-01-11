package cloud.xcan.sdf.core.angustester.application.converter;


import static cloud.xcan.sdf.spec.utils.ObjectUtils.convert;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.setting.indicator.Func;
import cloud.xcan.sdf.api.gm.indicator.SmokeCheckSetting;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.core.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import java.time.LocalDateTime;

/**
 * @author xiaolong.liu
 */
public class IndicatorFuncConverter {

  public static IndicatorFunc toIndicatorFunc(Func default0, Long targetId,
      CombinedTargetType targetType) {
    return new IndicatorFunc()
        .setTargetType(targetType)
        .setTargetId(targetId)
        .setSmoke(default0.isSmoke())
        .setSmokeCheckSetting(default0.getSmokeCheckSetting())
        .setUserDefinedSmokeAssertion(default0.getUserDefinedSmokeAssertion())
        .setSecurity(default0.isSecurity())
        .setSecurityCheckSetting(default0.getSecurityCheckSetting())
        .setUserDefinedSecurityAssertion(default0.getUserDefinedSecurityAssertion());
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
