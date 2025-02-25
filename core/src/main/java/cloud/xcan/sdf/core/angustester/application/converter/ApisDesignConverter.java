package cloud.xcan.sdf.core.angustester.application.converter;

import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesign;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignSource;

public class ApisDesignConverter {

  public static ApisDesign toClone(ApisDesign designDb) {
    return new ApisDesign()
        .setProjectId(designDb.getProjectId())
        .setName(designDb.getName() + "-Copy").setReleaseFlag(false)
        .setOpenapiSpecVersion(designDb.getOpenapiSpecVersion())
        .setOpenapi(designDb.getOpenapi()).setDesignSource(ApisDesignSource.MANUAL_CREATED);
  }
}
