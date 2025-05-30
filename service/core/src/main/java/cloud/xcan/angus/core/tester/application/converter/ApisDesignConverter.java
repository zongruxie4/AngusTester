package cloud.xcan.angus.core.tester.application.converter;

import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesign;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignSource;
import cloud.xcan.angus.core.tester.domain.services.Services;

public class ApisDesignConverter {

  public static ApisDesign toClone(ApisDesign designDb) {
    return new ApisDesign()
        .setProjectId(designDb.getProjectId())
        .setName(designDb.getName() + "-Copy").setReleased(false)
        .setOpenapiSpecVersion(designDb.getOpenapiSpecVersion())
        .setOpenapi(designDb.getOpenapi()).setDesignSource(ApisDesignSource.MANUAL_CREATED);
  }

  public static ApisDesign assocToDomain(Services services) {
    return new ApisDesign().setProjectId(services.getProjectId())
        .setName(services.getName()).setReleased(true)
        .setOpenapiSpecVersion("3.0.1")
        .setDesignSourceId(services.getId())
        .setDesignSource(ApisDesignSource.SYNCHRONOUS_SERVICE);
  }

}
