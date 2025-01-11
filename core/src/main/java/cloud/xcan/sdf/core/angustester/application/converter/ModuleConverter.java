package cloud.xcan.sdf.core.angustester.application.converter;

import cloud.xcan.sdf.core.angustester.domain.module.Module;

public class ModuleConverter {

  public static Module setReplaceInfo(Module moduleDb, Module module) {
    moduleDb.setPid(module.getPid())
        .setName(module.getName())
        .setSequence(module.getSequence());
    return moduleDb;
  }
}
