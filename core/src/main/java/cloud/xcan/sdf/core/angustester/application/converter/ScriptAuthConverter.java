package cloud.xcan.sdf.core.angustester.application.converter;


import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuth;
import cloud.xcan.sdf.idgen.UidGenerator;
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
            .setCreatorFlag(true))
        .collect(Collectors.toList());
  }

}
