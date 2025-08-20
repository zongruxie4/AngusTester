package cloud.xcan.angus.core.tester.application.converter;


import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuth;
import cloud.xcan.angus.idgen.UidGenerator;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ScriptAuthConverter {

  @NotNull
  public static List<ScriptAuth> toScriptCreatorAuths(Collection<Long> creatorIds, Long scriptId,
      UidGenerator uidGenerator) {
    return creatorIds.stream()
        .map(creatorId -> new ScriptAuth().setId(uidGenerator.getUID())
            .setScriptId(scriptId)
            .setAuthObjectType(AuthObjectType.USER)
            .setAuthObjectId(creatorId)
            .setAuths(ScriptPermission.ALL)
            .setCreator(true))
        .toList();
  }

}
