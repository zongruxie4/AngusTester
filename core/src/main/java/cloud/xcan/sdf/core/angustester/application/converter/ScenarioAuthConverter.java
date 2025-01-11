package cloud.xcan.sdf.core.angustester.application.converter;


import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.sdf.idgen.UidGenerator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ScenarioAuthConverter {

  @NotNull
  public static List<ScenarioAuth> toScenarioCreatorAuths(Set<Long> creatorIds, Long scenarioId,
      UidGenerator uidGenerator) {
    return creatorIds.stream()
        .map(creatorId -> new ScenarioAuth().setId(uidGenerator.getUID())
            .setScenarioId(scenarioId)
            .setAuthObjectType(AuthObjectType.USER)
            .setAuthObjectId(creatorId)
            .setAuths(ScenarioPermission.ALL)
            .setCreatorFlag(true)).collect(Collectors.toList());
  }

}
