package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.tester.application.converter.MockApisConverter.toAssocMockApi;
import static cloud.xcan.angus.core.tester.application.converter.MockApisConverter.toAssocMockApisResponse;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MESSAGE_MATCH_STATUS_AND_MEDIATYPE_RESPONSE_NAME;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MESSAGE_MATCH_STATUS_RESPONSE_NAME;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static io.swagger.v3.oas.models.extension.ExtensionKey.VALUE_KEY;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.agent.message.mockservice.StartCmdParam;
import cloud.xcan.angus.agent.message.mockservice.StopCmdParam;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.domain.config.node.dns.DnsLine;
import cloud.xcan.angus.core.tester.domain.config.node.dns.DnsRecordType;
import cloud.xcan.angus.core.tester.domain.config.node.dns.NodeDomainDns;
import cloud.xcan.angus.core.tester.domain.config.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.infra.util.OAS3ExampleGenerator;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisDeleteDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisSyncDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStartDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStopDto;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.angus.mockservice.api.MockApisDeleteDto;
import cloud.xcan.angus.mockservice.api.MockApisSyncDto;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.http.ContentType;
import cloud.xcan.angus.spec.http.HttpHeader;
import cloud.xcan.angus.spec.http.HttpResponseHeader;
import cloud.xcan.angus.spec.utils.JsonUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

public class MockServiceConverter {

  public static void setReplaceInfo(MockService serviceDb, MockService service) {
    serviceDb.setName(service.getName())
        .setApisSecurity(service.getApisSecurity())
        .setApisCors(service.getApisCors())
        .setSetting(service.getSetting());
    // Cloud service edition cannot modify domain name
    if (!isCloudServiceEdition()) {
      serviceDb.setServiceDomain(service.getServiceDomain());
    }
  }

  public static StartCmdParam toStartMockServiceCmdParam(MockService service, Node node,
      String mockTesterApisServerUrl) {
    StartCmdParam cmdParam = new StartCmdParam();
    CoreUtils.copyPropertiesIgnoreNull(service.getSetting(), cmdParam);
    cmdParam.setServiceId(service.getId()).setDeviceId(service.getNodeId())
        .setTesterApisUrlPrefix(mockTesterApisServerUrl)
        .setServerIp(nonNull(node) ? node.getIp() : service.getNodeIp())
        .setServerPort(service.getServicePort());
    return cmdParam;
  }

  public static StopCmdParam mockServiceResultToToService(MockServiceInfo service) {
    return new StopCmdParam().setServiceId(service.getId()).setDeviceId(service.getNodeId());
  }

  public static MockServiceAuth toMockServiceAuth(Long authId, Long serviceId) {
    return new MockServiceAuth().setId(authId)
        .setMockServiceId(serviceId)
        .setAuthObjectType(AuthObjectType.USER)
        .setAuthObjectId(getUserId())
        .setAuths(MockServicePermission.ALL)
        .setCreator(true);
  }

  public static NodeDomainDns toMockServiceNodeDomainDns(MockService service, NodeDomain domain,
      Node nodeDb, String mockServiceDomainSuffix) {
    return new NodeDomainDns()
        .setDomainId(domain.getId())
        .setType(DnsRecordType.A)
        .setName(
            StringUtils.substringBefore(service.getServiceDomain(), "." + mockServiceDomainSuffix))
        .setLine(DnsLine.DEFAULT)
        .setStatus(NormalStatus.UNKNOWN)
        .setValue(stringSafe(nodeDb.getPublicIp(), nodeDb.getIp()))
        // 云解析DNS版本     取值范围            说明
        // 免费版            [600 - 86400]     单位秒，正整数
        // 个人版            [600 - 86400]     单位秒，正整数
        // 企业标准版         [60 - 86400]     单位秒，正整数 -> 已开通企业标准版本 -> 已过期
        // 企业旗舰版         [1 - 86400]      单位秒，正整数
        .setTtl(600);
  }

  public static void assembleMockApisAndResponse(CachedUidGenerator uidGenerator,
      MockService service, List<Apis> apis, List<MockApis> mockApis,
      List<MockApisResponse> mockApisResponses) {
    for (Apis api : apis) {
      MockApis mockApi = toAssocMockApi(uidGenerator, service, api);
      api.setMockApisId(mockApi.getId());
      if (isNotEmpty(api.getResponses())) {
        int priority = api.getResponses().size();
        for (Entry<String, ApiResponse> entry : api.getResponses().entrySet()) {
          List<HttpHeader> headers = new ArrayList<>();
          if (isNotEmpty(entry.getValue().getHeaders())) {
            for (Entry<String, Header> headerEntry : entry.getValue().getHeaders().entrySet()) {
              // @DoInFuture("Write response examples to extensions during import and definition")
              String value = isEmpty(headerEntry.getValue().getExtensions())
                  ? "" : headerEntry.getValue().getExtensions()
                  .getOrDefault(VALUE_KEY, "").toString();
              headers.add(new HttpHeader(headerEntry.getKey(), value));
            }
          }

          if (isNotEmpty(entry.getValue().getContent())) {
            for (Entry<String, MediaType> mediaTypeEntry : entry.getValue().getContent()
                .entrySet()) {
              if (isEmpty(mediaTypeEntry.getKey())) {
                continue;
              }
              if (mediaTypeEntry.getKey().startsWith(ContentType.TYPE_JSON)) {
                if (headers.stream().noneMatch(
                    x -> HttpResponseHeader.Content_Type.value.equalsIgnoreCase(x.getName()))) {
                  headers.add(new HttpHeader(HttpResponseHeader.Content_Type.value,
                      mediaTypeEntry.getKey()));
                }
                String name = message(MESSAGE_MATCH_STATUS_AND_MEDIATYPE_RESPONSE_NAME,
                    new Object[]{entry.getKey(), mediaTypeEntry.getKey()});
                // @DoInFuture("Write response examples to extensions during import and definition")
                String body = getMockResponseBodyString(mediaTypeEntry, api);
                mockApisResponses.add(toAssocMockApisResponse(uidGenerator, service.getId(),
                    mockApi, name, priority--, entry, headers, body));
              } else if (mediaTypeEntry.getKey().startsWith(ContentType.TYPE_XML)) {
                // @DoInFuture("See SwaggerXmlExampleGenerator")
              }
            }
          } else {
            String name = message(MESSAGE_MATCH_STATUS_RESPONSE_NAME, new Object[]{entry.getKey()});
            mockApisResponses.add(toAssocMockApisResponse(uidGenerator, service.getId(),
                mockApi, name, priority--, entry, headers, ""));
          }
        }
      }
      mockApis.add(mockApi);
    }
  }

  @Nullable
  public static String getMockResponseBodyString(Entry<String, MediaType> mediaTypeEntry,
      Apis apis) {
    String body = isEmpty(mediaTypeEntry.getValue().getExtensions()) ? "" :
        mediaTypeEntry.getValue().getExtensions().getOrDefault(VALUE_KEY, "").toString();
    try {
      if (isEmpty(body)) {
        Object example = nonNull(mediaTypeEntry.getValue().getExample())
            ? mediaTypeEntry.getValue().getExample()
            : isNotEmpty(mediaTypeEntry.getValue().getExamples()) ? mediaTypeEntry
                .getValue().getExamples().values().iterator().next() : null;
        if (mediaTypeEntry.getKey().startsWith(ContentType.TYPE_JSON)) {
          if (nonNull(example)) {
            body = JsonUtils.toJson(example);
          } else if (nonNull(mediaTypeEntry.getValue().getSchema())) {
            String key = mediaTypeEntry.getValue().getSchema().get$ref();
            Schema schema = isNotEmpty(key) && isNotEmpty(apis.getResolvedRefModels())
                && apis.getResolvedRefModels().containsKey(key) ?
                JsonUtils.convert(apis.getResolvedRefModels().get(key), Schema.class)
                : mediaTypeEntry.getValue().getSchema();
            if (nonNull(schema)) {
              OAS3ExampleGenerator exampleGenerator = new OAS3ExampleGenerator(null);
              List<Map<String, String>> maps = exampleGenerator.generate(null, null, schema);
              if (nonNull(maps.get(0)) && nonNull(maps.get(0).get(OAS3ExampleGenerator.EXAMPLE))) {
                body = maps.get(0).get(OAS3ExampleGenerator.EXAMPLE);
              }
            }
          }
        }
        /*
        else if (mediaTypeEntry.getKey().startsWith(ContentType.TYPE_XML)
            || mediaTypeEntry.getKey().startsWith(ContentType.APPLICATION_XML)) {
          if (nonNull(example)) {
            // @DoInFuture("See SwaggerXmlExampleGenerator")
          } else if (nonNull(mediaTypeEntry.getValue().getSchema())) {
            // @DoInFuture("See SwaggerXmlExampleGenerator")
          }
        }*/
      }
    } catch (Exception e) {
      throw ProtocolException.of(ExceptionUtils.getMessage(e));
    }
    return body;
  }

  public static MockServiceStartDto toMockServiceStartDto(Map<Long, Node> nodeMap,
      List<MockService> serviceDbs, String mockTesterApisServerUrl) {
    return new MockServiceStartDto().setBroadcast(true)
        .setCmdParams(serviceDbs.stream()
            .map(x -> toStartMockServiceCmdParam(x, nodeMap.get(x.getNodeId()),
                mockTesterApisServerUrl))
            .toList());
  }

  public static MockServiceStopDto toMockServiceStopDto(List<MockServiceInfo> serviceDbs) {
    return new MockServiceStopDto().setBroadcast(true)
        .setCmdParams(serviceDbs.stream()
            .map(MockServiceConverter::mockServiceResultToToService)
            .toList());
  }

  public static MockServiceApisSyncDto toMockServiceApisSyncDto(MockService service,
      List<MockApis> apis) {
    return new MockServiceApisSyncDto().setBroadcast(true)
        .setCmdParams(new MockApisSyncDto()
            .setMockServiceId(service.getId())
            .setMockServiceIp(service.getNodeIp())
            .setMockServicePort(service.getServicePort())
            .setDeviceId(service.getNodeId())
            .setSyncAllMockApis(false)
            .setSyncMockService(false)
            .setSyncMockApisOperations(apis.stream()
                .map(x -> String.format("%s:%s", x.getMethod().getValue(), x.getEndpoint()))
                .toList()));
  }

  public static MockServiceApisDeleteDto toMockServiceApisDeleteDto(MockService service,
      List<MockApis> apis) {
    return new MockServiceApisDeleteDto().setBroadcast(true)
        .setCmdParams(new MockApisDeleteDto()
            .setMockServiceId(service.getId())
            .setMockServiceIp(service.getNodeIp())
            .setMockServicePort(service.getServicePort())
            .setDeviceId(service.getNodeId())
            .setDeleteAllMockApis(false)
            .setDeleteMockApisOperations(apis.stream()
                .map(x -> String.format("%s:%s", x.getMethod().getValue(), x.getEndpoint()))
                .toList()));
  }

  public static MockServiceApisSyncDto toMockServiceApisSyncDto(MockService service,
      boolean syncAllMockApis) {
    return new MockServiceApisSyncDto().setBroadcast(true)
        .setCmdParams(new MockApisSyncDto()
            .setMockServiceId(service.getId())
            .setMockServiceIp(service.getNodeIp())
            .setMockServicePort(service.getServicePort())
            .setDeviceId(service.getNodeId())
            .setSyncAllMockApis(syncAllMockApis)
            .setSyncMockService(true));
  }

}
