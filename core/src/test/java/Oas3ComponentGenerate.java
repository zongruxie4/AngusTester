import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Oas3ComponentGenerate {

  public static void main(String[] args) throws JsonProcessingException {
    // private Map<String, ApiResponse> responses = null;
    ApiResponse response = new ApiResponse();
    response.description("Default operation");
    Map<String, Header> headers = new HashMap<>();
    Header header = new Header();
    header.setDescription("calls per hour allowed by the user");
    Schema schema = new Schema();
    schema.setType("integer");
    schema.setFormat("int32");
    header.setSchema(schema);
    headers.put("X-Rate-Limit", header);
    header = new Header();
    header.setDescription("date in UTC when token expires");
    schema = new Schema();
    schema.setType("string");
    schema.setFormat("date-time");
    header.setSchema(schema);
    headers.put("X-Expires-After", header);
    response.setHeaders(headers);
    Content content = new Content();
    MediaType mediaType = new MediaType();
    schema = new Schema();
    schema.set$ref("#/components/schemas/User");
    mediaType.setSchema(schema);
    content.addMediaType("application/json", mediaType);
    content.addMediaType("application/xml", mediaType);
    response.setContent(content);
    // responses.put("default", response);
    System.out.println(OPENAPI_MAPPER.writeValueAsString(response));

    // private Map<String, Parameter> parameters = null;
    // Map<String, Parameter> parameters = new HashMap<>();
    Parameter parameter = new Parameter();
    Schema items = new Schema();

    // A header parameter of array values (over design)
    // @DoFuture("If the header has an array type, convert it to a comma separated string during import")
    // parameter = new Parameter();
    // parameter.setName("array");
    // parameter.in("header");
    // parameter.description("array values to be passed as a header, values separated by `,`");
    // Schema schema = new Schema();
    // schema.setType("array"); // Multiple values separated by `,` in the header
    // Schema items = new Schema();
    // items.setType("integer");
    // items.setFormat("int64");
    // schema.setItems(items);
    // parameter.setSchema(schema);
    // parameter.addExtension(ExtensionKey.VALUE_KEY, List.of("1", "2", "3"));
    // headers.add(parameter);

    // An optional query parameter of a string value, allowing multiple values by repeating the query parameter:
    parameter = new Parameter();
    // A path parameter of a string value:
    parameter.setName("username");
    parameter.in("path");
    parameter.description("username to fetch");
    parameter.setRequired(true);
    schema = new Schema();
    schema.setType("string");
    parameter.setSchema(schema);
    System.out.println(OPENAPI_MAPPER.writeValueAsString(parameter));
    // An optional query parameter of a string value, allowing multiple values by repeating the query parameter:
    parameter = new Parameter();
    // A path parameter of a string value:
    parameter.setName("id");
    parameter.in("query");
    parameter.description("ID of the object to fetch");
    parameter.setRequired(false);
    schema = new Schema();
    schema.setType("array"); // Multiple value query in form with same parameter name
    items = new Schema();
    items.setType("string");
    schema.setItems(items);
    parameter.setSchema(schema);
    System.out.println(OPENAPI_MAPPER.writeValueAsString(parameter));
    // A free-form query parameter, allowing undefined parameters of a specific type:
    parameter = new Parameter();
    // A path parameter of a string value:
    parameter.setName("freeForm");
    parameter.in("query");
    parameter.setRequired(true);
    schema = new Schema();
    schema.setType("object");
    Schema additionalProperties = new Schema<>();
    additionalProperties.setType("integer");
    schema.setAdditionalProperties(additionalProperties);
    parameter.setSchema(schema);
    System.out.println(OPENAPI_MAPPER.writeValueAsString(parameter));

    // private Map<String, Example> examples = null;
    Example example = new Example();
    example.setSummary("A foo example");
    example.setValue("{\"foo\": \"bar\"}");
    System.out.println(OPENAPI_MAPPER.writeValueAsString(example));

    // NOOP: private Map<String, RequestBody> requestBodies = null;

    // private Map<String, Header> headers = null;
    header = new Header();
    // header.setName("X-Rate-Limit");
    header.setDescription("calls per hour allowed by the user");
    schema = new Schema();
    schema.setType("integer");
    schema.setFormat("int32");
    header.setSchema(schema);
    System.out.println(OPENAPI_MAPPER.writeValueAsString(header));
    header = new Header();
    // header.setName("multipleValueHeader");
    header.setDescription("multiple value header");
    schema = new Schema();
    schema.setType("array"); // Multiple values separated by `;` in the header
    items = new Schema();
    items.setType("integer");
    items.setFormat("int64");
    schema.setItems(items);
    header.setSchema(schema);
    System.out.println(OPENAPI_MAPPER.writeValueAsString(header));

    // private Map<String, SecurityScheme> securitySchemes = null;
    // multipleValuesApiKey
    SecurityScheme securityScheme = new SecurityScheme();
    securityScheme.setDescription("Multiple values apiKey auth");
    securityScheme.name("api_key_01");
    securityScheme.type(Type.APIKEY);
    securityScheme.in(In.HEADER);
    SecurityScheme ext = new SecurityScheme();
    ext.name("api_key_02");
    ext.type(Type.APIKEY);
    ext.in(In.HEADER);
    SecurityScheme ext2 = new SecurityScheme();
    ext2.name("api_key_03");
    ext2.type(Type.APIKEY);
    ext2.in(In.HEADER);
    securityScheme.addExtension("x-xc-apiKey", List.of(ext, ext2));
    System.out.println(OPENAPI_MAPPER.writeValueAsString(securityScheme));

    // basicAuth
    securityScheme = new SecurityScheme();
    securityScheme.setDescription("Basic auth");
    // securityScheme.name("basicAuth");
    securityScheme.type(Type.HTTP);
    securityScheme.scheme("basic");
    System.out.println(OPENAPI_MAPPER.writeValueAsString(securityScheme));

    // jwtBearerAuth
    securityScheme = new SecurityScheme();
    securityScheme.setDescription("JWT Bearer auth");
    // securityScheme.name("jwtBearerAuth");
    securityScheme.type(Type.HTTP);
    securityScheme.scheme("bearer");
    securityScheme.bearerFormat("JWT");
    System.out.println(OPENAPI_MAPPER.writeValueAsString(securityScheme));

    // implicitOAuth2Auth
    securityScheme = new SecurityScheme();
    securityScheme.setDescription("Implicit OAuth2 auth");
    // securityScheme.name("implicitOAuth2Auth");
    securityScheme.type(Type.OAUTH2);
    securityScheme.scheme("oauth2");
    OAuthFlows flows = new OAuthFlows();
    OAuthFlow flow = new OAuthFlow();
    flow.setAuthorizationUrl("https://example.com/api/oauth/dialog");
    Scopes scopes = new Scopes();
    scopes.addString("write:pets", "modify pets in your account");
    scopes.addString("read:pets", "read your pets");
    flow.setScopes(scopes);
    flows.setImplicit(flow);
    securityScheme.flows(flows);
    System.out.println(OPENAPI_MAPPER.writeValueAsString(securityScheme));

    // private Map<String, Link> links = null;

    // private Map<String, Callback> callbacks = null;

    // private java.util.Map<String, Object> extensions = null;

  }

}
