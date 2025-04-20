package cloud.xcan.angus.core.tester.application.converter;

import cloud.xcan.angus.core.tester.domain.module.Module;

public class ModuleConverter {

  public static Module setReplaceInfo(Module moduleDb, Module module) {
    moduleDb.setPid(module.getPid())
        .setName(module.getName())
        .setSequence(module.getSequence());
    return moduleDb;
  }
}
