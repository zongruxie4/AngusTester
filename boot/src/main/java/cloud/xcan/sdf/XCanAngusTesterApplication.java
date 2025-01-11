/*
 * Copyright (c) 2021   XCan Company
 *
 *        http://www.xcan.company
 *
 * Licensed under the XCBL(XCan Business License) Version 1.0.
 * Detail XCBL license at:
 *
 * http://www.xcan.company/licenses/XCBL-1.0
 */
package cloud.xcan.sdf;

import cloud.xcan.sdf.security.resauth.EnableResServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableResServer
@EnableFeignClients(basePackages = {
    "cloud.xcan.sdf.api.gm",
    "cloud.xcan.sdf.api.store",
    "cloud.xcan.sdf.api.expense",
    "cloud.xcan.sdf.api.storage",
    "cloud.xcan.sdf.api.angustester",
    "cloud.xcan.sdf.api.angusctrl"
})
@EnableDiscoveryClient
@SpringBootApplication
public class XCanAngusTesterApplication {

  public static void main(String[] args) {
    SpringApplication.run(XCanAngusTesterApplication.class, args);
  }

}
