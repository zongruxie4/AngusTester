package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.converter.DefaultModelHttpConverter.toComponents;
import static cloud.xcan.angus.core.converter.DefaultModelHttpConverter.toPathItem;
import static cloud.xcan.angus.core.converter.oas3.SchemaToHttpConverter.toParameters;
import static cloud.xcan.angus.core.converter.oas3.SchemaToHttpConverter.toServer;
import static cloud.xcan.angus.core.converter.oas3.SchemaToHttpConverter.toSetting;
import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_DEFAULT_NAME;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_SECURITY_NAME;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_CASE_SMOKE_NAME;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleSecurityCaseAssertion;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.assembleSmokeCaseAssertion;
import static cloud.xcan.angus.core.utils.AngusUtils.overrideExecServerParameter;
import static cloud.xcan.angus.model.AngusConstant.DEFAULT_ACTION_ON_EOF;
import static cloud.xcan.angus.model.AngusConstant.DEFAULT_SHARING_MODE;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_WEBSOCKET_NAME;
import static cloud.xcan.angus.spec.utils.JsonUtils.JSON;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static io.swagger.v3.oas.models.extension.ExtensionKey.REQUEST_SETTING_KEY;
import static io.swagger.v3.oas.models.extension.ExtensionKey.WS_MESSAGE_KEY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.converter.oas3.SchemaToHttpConverter;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.angus.model.AngusConstant;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.element.http.setting.HttpSetting;
import cloud.xcan.angus.model.element.websocket.WebSocket;
import cloud.xcan.angus.model.element.websocket.WebSocketMessageMode;
import cloud.xcan.angus.model.element.websocket.setting.WebSocketSetting;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.angus.spec.locale.MessageHolder;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.extension.OpenAPIUtils;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ApisToAngusModelConverter {

  @SneakyThrows
  public static void assembleAddApisScript(Apis apisDb, Map<String, Server> serverMap,
      IndicatorFunc indicatorFunc, Map<ApisCaseType, List<ApisCase>> typeCasesMap, Script script,
      List<Variable> variables, List<Dataset> datasets) {
    script.setProjectId(apisDb.getProjectId());
    script.setServiceId(apisDb.getServiceId());
    script.setName(apisDb.getSummary() + "-" + script.getType().getMessage());
    script.setAuth(nullSafe(script.getAuth(), false));

    List<cloud.xcan.angus.model.element.dataset.Dataset> angusDatasets = isEmpty(datasets) ? null
        : datasets.stream().map(DatasetConverter::toAngusDataset).toList();

    if (apisDb.getProtocol().isHttp()) {
      List<Http> https = new ArrayList<>();
      script.setPlugin(PLUGIN_HTTP_NAME);
      script.getAngusScript().setPlugin(PLUGIN_HTTP_NAME);

      Http http = toHttp(apisDb);
      overrideExecServerParameter(serverMap, http);
      http.setDatasets(angusDatasets);

      // http.setVariables(angusCurrentVariables); ??
      HttpSetting setting = nullSafe(toSetting(apisDb.getExtensions()), HttpSetting.DEFAULT);
      if (nonNull(http.getRequest())) {
        http.getRequest().setSetting(setting);
      }
      http.getRequest().getExtensions().remove(REQUEST_SETTING_KEY);

      if (script.getType().isFunctionalTesting()) {
        https.addAll(assembleFuncCases(indicatorFunc, http, typeCasesMap, angusDatasets));
      } else {
        https.add(http);
      }

      Arguments arguments = assembleScriptArguments(script, setting);
      if (isNull(script.getAngusScript().getTask())) {
        script.getAngusScript().setTask(Task.newBuilder().arguments(arguments)
            .pipelines(https).build());
      } else {
        script.getAngusScript().getTask().setArguments(arguments).setPipelines(https);
      }
    } else {
      script.setPlugin(PLUGIN_WEBSOCKET_NAME);
      script.getAngusScript().setPlugin(PLUGIN_WEBSOCKET_NAME);
      WebSocket webSocket = toWebsocket(apisDb);
      webSocket.setDatasets(angusDatasets);

      // webSocket.setVariables(angusCurrentVariables); ??
      WebSocketSetting setting = nullSafe(webSocket.getSetting(), WebSocketSetting.DEFAULT);
      webSocket.getExtensions().remove(REQUEST_SETTING_KEY);

      Arguments arguments = assembleScriptArguments(script, setting);

      if (isNull(script.getAngusScript().getTask())) {
        script.getAngusScript().setTask(Task.newBuilder().arguments(arguments)
            .pipelines(List.of(webSocket)).build());
      } else {
        script.getAngusScript().getTask().setArguments(arguments);
        script.getAngusScript().getTask().setArguments(arguments).setPipelines(List.of(webSocket));
      }
    }

    List<cloud.xcan.angus.model.element.variable.Variable> angusVariables =
        isNotEmpty(variables) ? variables.stream()
            .map(ApisToAngusModelConverter::toAngusVariable).toList() : null;
    overrideExecServerParameter(serverMap, angusVariables);
    script.getAngusScript().getConfiguration().setVariables(angusVariables);
  }

  public static List<Http> assembleFuncCases(IndicatorFunc indicatorFunc, Http http,
      Map<ApisCaseType, List<ApisCase>> typeCasesMap,
      List<cloud.xcan.angus.model.element.dataset.Dataset> angusDatasets)
      throws CloneNotSupportedException {

    List<Http> https = new ArrayList<>();
    if (typeCasesMap.isEmpty() || !typeCasesMap.containsKey(ApisCaseType.SMOKE)) {
      Http smokeCase = (Http) http.clone();
      smokeCase.setName(MessageHolder.message(APIS_CASE_SMOKE_NAME))
          .setCaseId(getBean(CachedUidGenerator.class).getUID()).setPersistent(false)
          .setEnabled(indicatorFunc.isSmoke()).setCaseType(ApisCaseType.SMOKE)
          .setAssertions(assembleSmokeCaseAssertion(indicatorFunc.getSmokeCheckSetting(),
              indicatorFunc.getUserDefinedSmokeAssertion()));
      smokeCase.setDatasets(angusDatasets);
      https.add(smokeCase);
    } else {
      // Only allow one smoke testing case
      Http case0 = ApisToAngusModelConverter.toHttp(typeCasesMap.get(ApisCaseType.SMOKE).get(0));
      case0.setDatasets(angusDatasets);
      https.add(case0);
    }

    if (typeCasesMap.isEmpty() || !typeCasesMap.containsKey(ApisCaseType.SECURITY)) {
      Http securityCase = (Http) http.clone();
      securityCase.setName(MessageHolder.message(APIS_CASE_SECURITY_NAME))
          .setCaseId(getBean(CachedUidGenerator.class).getUID()).setPersistent(false)
          .setEnabled(indicatorFunc.isSecurity()).setCaseType(ApisCaseType.SECURITY)
          .setAssertions(assembleSecurityCaseAssertion(indicatorFunc.getSecurityCheckSetting(),
              indicatorFunc.getUserDefinedSecurityAssertion()));
      securityCase.setDatasets(angusDatasets);
      https.add(securityCase);
    } else {
      // Only allow one smoke testing case
      Http case0 = ApisToAngusModelConverter.toHttp(typeCasesMap.get(ApisCaseType.SECURITY).get(0));
      case0.setDatasets(angusDatasets);
      https.add(case0);
    }

    if (typeCasesMap.containsKey(ApisCaseType.USER_DEFINED)) {
      for (ApisCase apisCase : typeCasesMap.get(ApisCaseType.USER_DEFINED)) {
        Http caseDb = ApisToAngusModelConverter.toHttp(apisCase);
        caseDb.setName(MessageHolder.message(APIS_CASE_DEFAULT_NAME));
        caseDb.setPersistent(true);
        https.add(caseDb);
      }
    }

    // Add default test cases when smoking and safety testing are not enabled
    /* if (https.isEmpty()) {
      Http defaultCase = (Http) http.clone();
      defaultCase.setName(MessageHolder.message(APIS_CASE_DEFAULT_NAME))
          .setCaseId(getBean(CachedUidGenerator.class).getUID()).setPersistent(false)
          .setCaseType(ApisCaseType.USER_DEFINED);
      https.add(defaultCase);
    }*/
    return https;
  }

  private static @NotNull Arguments assembleScriptArguments(Script script, HttpSetting setting) {
    Arguments arguments;
    if (isNull(script.getAngusScript().getTask())) {
      arguments = Arguments.of(Map.of(AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS, true,
          HttpSetting.ARGUMENT_KEY_NAME, setting));
    } else {
      arguments = script.getAngusScript().getTask().getArguments();
      if (!arguments.containsKey(AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS)) {
        arguments.put(AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS, true);
      }
      if (!arguments.containsKey(HttpSetting.ARGUMENT_KEY_NAME)) {
        arguments.put(HttpSetting.ARGUMENT_KEY_NAME, setting);
      }
    }
    return arguments;
  }

  private static @NotNull Arguments assembleScriptArguments(Script script,
      WebSocketSetting setting) {
    Arguments arguments;
    if (isNull(script.getAngusScript().getTask())) {
      arguments = Arguments.of(Map.of(AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS, true,
          HttpSetting.ARGUMENT_KEY_NAME, setting));
    } else {
      arguments = script.getAngusScript().getTask().getArguments();
      if (!arguments.containsKey(AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS)) {
        arguments.put(AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS, true);
      }
      if (!arguments.containsKey(WebSocketSetting.ARGUMENT_KEY_NAME)) {
        arguments.put(WebSocketSetting.ARGUMENT_KEY_NAME, setting);
      }
    }
    return arguments;
  }

  @NotNull
  public static Arguments getScriptTaskArguments(Boolean ignoreAssertions) {
    Arguments arguments = new Arguments();
    arguments.put(AngusConstant.ARGUMENT_KEY_NAME_IGNORE_ASSERTIONS,
        ignoreAssertions);
    return arguments;
  }

  public static cloud.xcan.angus.model.element.variable.Variable toAngusVariable(Variable value) {
    return cloud.xcan.angus.model.element.variable.Variable.newBuilder()
        .name(value.getName())
        .value(value.getValue())
        .passwordValue(value.getPasswordValue())
        .extraction(value.getExtraction())
        .build();
  }

  public static Http toHttp(Apis apisDb) {
    Operation operation = new Operation();
    operation.method = apisDb.getMethod().name();
    operation.currentServer = isNotEmpty(apisDb.getCurrentServer())
        && apisDb.getCurrentServer().isValidUrl() ? apisDb.getCurrentServer()
        : isNotEmpty(apisDb.getAvailableServers()) ? apisDb.getAvailableServers().stream()
            .filter(Server::isValidUrl).findFirst().orElse(null) : null;
    operation.endpoint = apisDb.getEndpoint();
    operation.authentication = apisDb.isAuthSchemaRef()
        ? apisDb.getRefAuthentication() : apisDb.getAuthentication();
    operation.parameters(apisDb.getParameters());
    operation.requestBody(apisDb.getRequestBody());
    // Fix:: The responses is required for OAS parser
    // operation.responses(ApiResponses.default0());
    operation.extensions(apisDb.getExtensions());
    try {
      if (isNotEmpty(apisDb.getResolvedRefModels())) {
        // Parser uses options as a way to customize the behavior while parsing:
        ParseOptions parseOptions = new ParseOptions();
        // - When remote or relative references are found in the parsed document, parser will attempt to:
        // 1. resolve the reference in the remote or relative location
        // 2. parse the resolved reference
        // 3. add the resolved "component" (e.g. parameter, schema, response, etc.) to the resolved `OpenAPI` POJO components section
        // 4. replace the remote/relative reference with a local reference,  e.g. : `#/components/schemas/NameOfRemoteSchema`.
        parseOptions.setResolve(true);
        // you might need to have all local references removed replacing the reference with the content of the referenced element.
        parseOptions.setResolveFully(true);
        // In some scenarios, you might need to have all schemas defined inline (e.g. a response schema) moved to the `components/schemas` section and replaced with a reference to the newly added schema within `components/schemas`
        // parseOptions.setFlatten(true); // In V3
        // From a swagger string
        OpenAPI openapi = new OpenAPI();
        openapi.setPaths(new Paths().addPathItem(apisDb.getEndpoint(), toPathItem(operation)));
        openapi.setComponents(toComponents(apisDb.getResolvedRefModels()));
        SwaggerParseResult result = new io.swagger.parser.OpenAPIParser().readContents(
            Json31.converterMapper().writeValueAsString(openapi), null, parseOptions);
        // Note:: The resolution of result. getMessages () may also be successful if it is not empty, such as the exception information in converting Swagger 2 to OpenAPI 3
        operation = OpenAPIUtils.flatPaths(openapi.getPaths())
            .get(operation.method + ":" + stringSafe(operation.endpoint));
      }
    } catch (Exception e) {
      // Ignore when parsing exceptions and continue generating scripts.
      log.error("Parsing apis[{}] OpenAPI components exception, cause: {}", apisDb.getId(),
          e.getMessage());
    }

    return Http.newBuilder()
        .name(apisDb.getSummary())
        .description(apisDb.getDescription())
        .enabled(true)
        .apisId(apisDb.getId())
        .request(SchemaToHttpConverter.toRequest(operation))
        .assertions(apisDb.getAssertions())
        // .variables(apisDb.getVariables()) ??
        .actionOnEOF(nullSafe(apisDb.getDatasetActionOnEOF(), DEFAULT_ACTION_ON_EOF))
        .sharingMode(nullSafe(apisDb.getDatasetSharingMode(), DEFAULT_SHARING_MODE))
        .build();
  }

  public static WebSocket toWebsocket(Apis apisDb) {
    WebSocketSetting setting = isEmpty(apisDb.getExtensions()) ||
        !apisDb.getExtensions().containsKey(REQUEST_SETTING_KEY) ? null :
        JSON.convertValue(apisDb.getExtensions().get(REQUEST_SETTING_KEY), WebSocketSetting.class);
    Object message = nonNull(apisDb.getRequestBody())
        && nonNull(apisDb.getRequestBody().getExtensions())
        ? apisDb.getRequestBody().getExtensions().get(WS_MESSAGE_KEY) : null;
    Server server = nonNull(apisDb.getCurrentServer())
        && isNotEmpty(apisDb.getCurrentServer().getUrl()) ? apisDb.getCurrentServer()
        : nonNull(apisDb.getAvailableServers()) ? apisDb.getAvailableServers().get(0) : null;
    return WebSocket.newBuilder()
        .name(apisDb.getSummary())
        .description(apisDb.getDescription())
        .enabled(true)
        .apisId(apisDb.getId())
        .server(toServer(server))
        .endpoint(apisDb.getEndpoint())
        .parameters(toParameters(apisDb.getParameters()))
        .mode(WebSocketMessageMode.SEND_AND_RECEIVE)
        .message(nonNull(message) ? message.toString() : "")
        // .assertions(apisDb.getAssertions()) ??
        // .variables(apisDb.getVariables()) ??
        .actionOnEOF(nullSafe(apisDb.getDatasetActionOnEOF(), DEFAULT_ACTION_ON_EOF))
        .sharingMode(nullSafe(apisDb.getDatasetSharingMode(), DEFAULT_SHARING_MODE))
        .setting(setting)
        .build();
  }

  public static Http toHttp(ApisCase caseDb) {
    Operation operation = new Operation();
    operation.method = caseDb.getMethod().name();
    operation.currentServer = caseDb.getCurrentServer();
    operation.endpoint = caseDb.getEndpoint();
    operation.authentication = caseDb.isAuthSchemaRef()
        ? caseDb.getRefAuthentication() : caseDb.getAuthentication();
    operation.parameters(caseDb.getParameters());
    operation.requestBody(caseDb.getRequestBody());
    // Fix:: The responses is required for OAS parser
    // operation.responses(ApiResponses.default0());
    try {
      if (isNotEmpty(caseDb.getResolvedRefModels())) {
        // Parser uses options as a way to customize the behavior while parsing:
        ParseOptions parseOptions = new ParseOptions();
        // - When remote or relative references are found in the parsed document, parser will attempt to:
        // 1. resolve the reference in the remote or relative location
        // 2. parse the resolved reference
        // 3. add the resolved "component" (e.g. parameter, schema, response, etc.) to the resolved `OpenAPI` POJO components section
        // 4. replace the remote/relative reference with a local reference,  e.g. : `#/components/schemas/NameOfRemoteSchema`.
        parseOptions.setResolve(true);
        // you might need to have all local references removed replacing the reference with the content of the referenced element.
        parseOptions.setResolveFully(true);
        // In some scenarios, you might need to have all schemas defined inline (e.g. a response schema) moved to the `components/schemas` section and replaced with a reference to the newly added schema within `components/schemas`
        // parseOptions.setFlatten(true); // In V3
        // From a swagger string
        OpenAPI openapi = new OpenAPI();
        openapi.setPaths(new Paths().addPathItem(caseDb.getEndpoint(), toPathItem(operation)));
        openapi.setComponents(toComponents(caseDb.getResolvedRefModels()));
        SwaggerParseResult result = new io.swagger.parser.OpenAPIParser().readContents(
            Json31.converterMapper().writeValueAsString(openapi), null, parseOptions);
        // Note:: The resolution of result. getMessages () may also be successful if it is not empty, such as the exception information in converting Swagger 2 to OpenAPI 3
        operation = OpenAPIUtils.flatPaths(openapi.getPaths())
            .get(operation.method + ":" + stringSafe(operation.endpoint));
      }
    } catch (Exception e) {
      // Ignore when parsing exceptions and continue generating scripts.
      log.error("Parsing apis[{}] OpenAPI components exception, cause: {}", caseDb.getId(),
          e.getMessage());
    }

    return Http.newBuilder()
        .name(caseDb.getName())
        .description(caseDb.getDescription())
        .enabled(nullSafe(caseDb.getEnabled(), true))
        .caseId(caseDb.getId())
        .apisId(caseDb.getApisId())
        .request(SchemaToHttpConverter.toRequest(operation))
        .assertions(caseDb.getAssertions())
        // .variables(apisDb.getVariables()) ??
        .actionOnEOF(nullSafe(caseDb.getDatasetActionOnEOF(), DEFAULT_ACTION_ON_EOF))
        .sharingMode(nullSafe(caseDb.getDatasetSharingMode(), DEFAULT_SHARING_MODE))
        .build();
  }
}
