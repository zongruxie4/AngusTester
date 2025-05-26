package cloud.xcan.angus.core.tester.application.converter;

import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesign;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignSource;

public class ApisDesignConverter {

  public static ApisDesign toClone(ApisDesign designDb) {
    return new ApisDesign()
        .setProjectId(designDb.getProjectId())
        .setName(designDb.getName() + "-Copy").setReleased(false)
        .setOpenapiSpecVersion(designDb.getOpenapiSpecVersion())
        .setOpenapi(designDb.getOpenapi()).setDesignSource(ApisDesignSource.MANUAL_CREATED);
  }
}
