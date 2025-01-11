OpenAPI Specification 3.1.0
===============================================

# 介绍

OpenAPI Specification（OAS）定义了一个标准与语言无关的HTTP API接口，允许人类和计算机在没有访问源代码、文档或通过网络流量
检查的情况下发现和理解服务的能力。当正确定义时，消费者可以使用最少的实现逻辑理解和与远程服务进行交互。OpenAPI 规范可用于：

- 设计优先的用户，使用Swagger Codegen为您的API生成服务器存根。
- 被文档生成工具用于显示API或使用Swagger UI生成的交互式API文档，让您的用户可以直接在浏览器中尝试API调用 - 调试。
- 利用代码生成工具生成客户端代码（使用Swagger Codegen以40多种语言为您的API生成客户端库）- Code Snippet。
- 使用规范将API相关工具连接到您的API，如：测试工具生成测试用例等等。

> 完整的 OpenAPI 规范可以在 GitHub 上找到：[OpenAPI 3.0 规范](https://github.com/OAI/OpenAPI-Specification.git)

# 定义

## OpenAPI Document（OpenAPI文档）

一个用于定义或描述API或API元素的自包含或组合资源。`OpenAPI文档必须至少包含一个paths字段、一个components字段或一个webhooks字段。OpenAPI文档使用并符合OpenAPI规范`。

## Path Templating（路径模板）

路径模板是指使用由花括号 ({}) 分隔的模板表达式，将 URL 路径的一部分标记为可使用路径参数替换。

路径中的每个模板表达式必须对应于包含在路径项本身和/或路径项的每个操作中的路径参数。一个异常是如果路径项为空，例如由于 ACL 约束，则不需要匹配的路径参数。

这些路径参数的值不得包含 [RFC3986](https://datatracker.ietf.org/doc/html/rfc3986) 描述的任何未转义的“通用语法”字符：正斜杠 (/)、问号 ( ?) 或哈希 ( #)。

## Media Types（媒体类型）

媒体类型定义分布在多个资源中。媒体类型定义应该符合 [RFC6838](https://datatracker.ietf.org/doc/html/rfc6838)。

## HTTP Status Codes（HTTP状态码）

HTTP 状态代码用于指示已执行操作的状态。可用的状态代码由 [RFC7231](https://datatracker.ietf.org/doc/html/rfc7231) 定义，注册的状态代码列在IANA状态代码注册表中。

# Specification（规范）

## Versions（版本）

OpenAPI 规范使用major.minor.patch版本控制方案。major.minor 版本字符串的一部分指定 OAS 功能集。
.patch版本解决本文档中的错误或提供说明，而不是功能集。支持 OAS 3.1 的工具应该与所有 OAS 3.1.* 版本兼容。
工具不应考虑补丁版本，例如，不区分3.1.0和3.1.1。

minor有时可能会在OAS版本中进行非向后兼容的更改，据信其影响相对于所提供的好处而言较低。

与 OAS 3.*.* 兼容的 OpenAPI 文档包含一个必填openapi字段，用于指定它使用的 OAS 版本。

## Format（格式）

符合 OpenAPI 规范的 OpenAPI 文档本身就是一个 JSON 对象，可以用 JSON 或 YAML 格式表示。

规范中的所有字段名称都区分大小写。这包括在 Map 中用作键的所有字段，除非明确指出键不区分大小写。

该模式公开了两种类型的字段：固定字段（具有声明的名称）和模式化字段（为字段名称声明正则表达式模式）。

模式字段在包含对象中必须具有唯一的名称。

为了保留在YAML和JSON格式之间转换能力，建议使用 YAML 1.2版。额外的限制：

- 标签必须限制在 [JSON模式规则集](https://yaml.org/spec/1.2-old/spec.html#id2803231) 允许的范围内。
- YAML映射中使用的键必须限制为标量字符串，如：[YAML Failsafe 模式规则集](https://yaml.org/spec/1.2-old/spec.html#id2802346) 所定义。

注意：虽然API可以由OpenAPI文档以YAML或JSON格式定义，但API请求体和响应体以及其他内容不是必须的对于JSON或YAML。

> API 规范可以用 YAML 或 JSON 编写，该格式对人和机器都易于学习和阅读。

## Document Structure（文档结构）

OpenAPI 文档可以由单个文档组成，也可以根据作者的判断分为多个相互关联的部分。在后一种情况下，使用Reference Objects和Schema Object $ref关键字。
   
建议将根 OpenAPI 文档命名为：openapi.json或openapi.yaml.
   
## Data Types（数据类型）

OAS 中的数据类型基于JSON Schema Specification Draft 2020-12支持的类型。请注意，integer作为一种类型也受支持，
它被定义为不带分数或指数部分的 JSON 数字。模型是使用Schema Object定义的，它是 JSON Schema Specification Draft 2020-12 的超集。

正如JSON Schema Validation vocabulary所定义的，数据类型可以有一个可选的修饰符属性：format。OAS 定义了额外的格式来提供原始数据类型的详细信息。

OAS 定义的格式是：

type|	format|	说明
---|---|---
integer|	int32|	带符号的 32 位
integer|	int64|	有符号的 64 位
number|	float|	
number	|double|	
string	|password|	提示 UI 模糊输入

## Rich Text Formatting（富文本格式）

在整个规范中，description 字段被标记为支持 CommonMark 降价格式。在 OpenAPI 工具呈现富文本的地方，它必须至少
支持 [CommonMark 0.27](https://github.com/commonmark/commonmark-spec/tree/0.27) 描述的降价语法，
工具可以选择忽略一些 CommonMark 特性来解决安全问题。

# OpenAPI Object Parameters （OpenApi对象参数）

<https://swagger.io/specification/>

- `openapi`: OpenAPI版本号，必需。
- `info`: 一个包含API信息的对象，必需。
- `servers`: 一个包含API服务器定义的数组，可选。
- `paths`: 一个包含路径定义的对象，必需。
- `components`: 一个包含API组件定义的对象，可选。
- `security`: 一个包含安全方案名称的数组，可选。
- `tags`: 一个包含标签定义的数组，可选。
- `externalDocs`: 一个包含外部文档定义的对象，可选。

## Info Object Parameters（元信息参数）

> info 参数用于指定 API 的元数据，例如 API 的标题、版本、描述等。

- `title`: API的标题，必需。
- `description`: API的描述，可支持 CommonMark 语法格式富文本，可选。
- `termsOfService`: API 的服务条款 URL，可选。
- `version`: API的版本号，必需。
- `contact`: 一个包含API联系人信息的对象，可选。
  - `name`：联系人的姓名。
  - `url`：联系人的 URL 地址。
  - `email`：联系人的电子邮件地址。
- `license`: 一个包含API许可证信息的对象，可选。
  - `name`：许可证名称，例如 Apache 2.0,必需。
  - `url`：指向许可证文本的 URL，可选。
  - `extensions`：扩展字段，供应商扩展或其他元数据，可选。
  
配置示例：

```yaml
info:
  title: My API
  description: This is a sample API
  version: 1.0.0
  contact:
    name: API Team
    url: https://example.com/contact
    email: api@example.com
  license:
    name: Apache 2.0
    url: https://www.apache.org/licenses/LICENSE-2.0.html
```

## Server Object Parameters（服务器对象参数）

> servers 参数用于指定 API 的服务器地址和相关信息，相比 OAS2 可以定义多个服务器对象（OSA2 使用 schemes, host and basePath 来定义服务器）。
  
- `url`: API服务器的URL，必需。
- `description`: API服务器的描述，可支持 CommonMark 语法格式富文本，可选。
- `variables`: 用于定义一个或多个参数，以便在服务器 URL 中使用，可选。
  - `enum`: 一个包含枚举值的数组，可选。
  - `default`: 默认值，必需。
  - `description`: 变量的描述，可选。
  
***API Server and Base URL***

```txt
https://api.example.com/v1/users?role=admin&status=active
\________________________/\____/ \______________________/
         server URL       endpoint    query parameters
                            path
```

***Server URL Format***

```txt
scheme://host[:port][/path]
```

主机可以是名称或IP地址（IPv4或IPv6）。OpenAPI 3.0也支持来自OpenAPI 2.0的WebSocket方案ws://和wss://。

有效的服务器URL示例：

```txt
https://api.example.com
https://api.example.com:8443/v1/reports
http://localhost:3025/v1
http://10.0.81.36/v1
ws://api.example.com/v1
wss://api.example.com/v1
/v1/reports
/
//api.example.com
```

如果未提供服务器阵列或服务器阵列为空，则服务器URL默认为 /：

```yaml
servers:
  - url: /
```
         
配置示例：

```yaml
servers:
  - url: https://api.example.com/{version}
    description: Production server
    variables:
      version:
        enum:
          - v1
          - v2
        default: v1
        description: API version
```

## Path Item Object Parameters（路径项对象参数）

- `$ref`: 引用到一个Path Item Object，可选。
- `summary`: 路径操作的简短描述，可选。 
- `description`: 路径操作的详细描述，可支持 CommonMark 语法格式富文本，可选。
- `get`: 一个包含GET操作定义的对象，可选。
- `put`: 一个包含PUT操作定义的对象，可选。
- `post`: 一个包含POST操作定义的对象，可选。
- `delete`: 一个包含DELETE操作定义的对象，可选。
- `options`: 一个包含OPTIONS操作定义的对象，可选。
- `head`: 一个包含HEAD操作定义的对象，可选。
- `patch`: 一个包含PATCH操作定义的对象，可选。
- `trace`: 一个包含TRACE操作定义的对象，可选。
- `servers`: 一个包含服务器定义的数组，可选。
- `parameters`: 一个包含参数定义的数组，可选。

> 注意：Swagger2 没有 trace 方法。

### Operation Object Parameters（操作对象参数/接口对象参数）

- `tags`: 一个包含标签名称的数组，可选。
- `summary`: 操作的简短描述，可选。
- `description`: 操作的详细描述，可选。
- `externalDocs`: 一个包含外部文档定义的对象，可选。
- `operationId`: 操作的ID，可选。
- `parameters`: 一个包含参数定义的数组，可选。
  - `name`: 参数名称，必需。
  - `in`: 参数位置，必需。
  - `description`: 参数描述，可选。
  - `required`: 标记参数是否必需，可选。
  - `deprecated`: 标记参数是否已弃用，可选。
  - `allowEmptyValue`: 标记参数是否允许空值，可选。
  - `style`: 参数样式，可选。
  - `explode`: 标记是否分离数组或对象的参数，可选。
  - `allowReserved`: 标记是否允许保留字符，可选。
  - `schema`: 参数的数据模型，必需。
  - `example`: 参数的示例值，可选。
  - `examples`: 一个包含参数示例的对象，可选。
- `requestBody`: 包含请求体定义的对象，可选。
- `responses`: 一个包含响应定义的对象，必需。
- `callbacks`: 一个包含回调定义的对象，可选。
- `deprecated`: 是否废弃，可选。
- `security`: 一个包含安全方案列表的数组，可选。
- `servers`: 一个包含API服务器信息的数组，可选。
- `x-*`: 可以自定义的扩展字段，可选。
  - x-status: 接口状态
  - x-owner-id: 负责人ID
  - x-owner-fullanme: 负责人名称

#### Request Body Object Parameters

- `description`: 请求体的描述，可选。
- `content`: 一个包含请求体内容的对象，必需。
- `required`: 标记请求体是否必需，可选。

##### Media Type Object Parameters

- `schema`: 媒体类型的数据模型，可选。
- `example`: 媒体类型的示例值，可选。
- `examples`: 一个包含媒体类型示例的对象，可选。
- `encoding`: 一个包含媒体类型编码的对象，可选。

###### Encoding Object Parameters

- `contentType`: 编码内容类型，必需。
- `headers`: 一个包含编码头信息的对象，可选。
- `style`: 编码样式，可选。
- `explode`: 标记是否分离数组或对象的参数，可选。
- `allowReserved`: 标记是否允许保留字符，可选。

#### Responses Object Parameters

- `{httpStatusCode}`: 一个包含API响应的对象，例如200、404、500等等，必需。

##### Response Object Parameters

- `description`: 响应的描述，必需。
- `headers`: 一个包含响应头信息的对象，可选。
- `content`: 一个包含响应内容的对象，可选。
- `links`: 一个包含响应链接的对象，可选。

###### Header Object Parameters

- `description`: 头信息的描述，可选。
- `required`: 标记头信息是否必需，可选。
- `deprecated`: 标记头信息是否已弃用，可选。
- `allowEmptyValue`: 标记头信息是否允许空值，可选。
- `style`: 头信息样式，可选。
- `explode`: 标记是否分离数组或对象的头信息，可选。
- `allowReserved`: 标记是否允许保留字符，可选。

###### Link Object Parameters（链接对象参数） 

- `operationRef`: 需要执行的API操作的引用，可选。
- `operationId`: 需要执行的API操作的ID，可选。
- `parameters`: 一个包含链接参数的对象，可选。
- `requestBody`: 需要在链接中传递的请求体，可选。
- `description`: 链接的描述，可选。
- `server`: API服务器的URL，可选。

#### Callback Object Parameters（回调对象参数） ？

- `{callbackName}`: 一个包含回调操作的对象，必需。

##### Callback Object Parameters（回调对象参数） ？

- `{eventName}`: 一个包含回调操作的对象，必需。

## Components Object Parameters（组件对象参数）

> 在 API 规范中用于定义和组织参数来减少规范参数冗余，并提高规范的可读性和可维护性；除非从components对象外的属性显式引用它们，否则在components对象中定义的所有对象都不会对API产生影响。

> 对应AngusTester数据模型，OAS2.x Models，OAS3.x Schema。

- `schemas`: 用于保存可重复使用的模型对象，可选。
- `responses`: 用于保存可重复使用的响应对象，可选。
- `parameters`: 用于保存可重复使用的参数对象，可选。
- `examples`: 用于保存可重复使用的示例对象，可选。
- `requestBodies`: 用于保存可重复使用的请求体对象，可选。
- `headers`: 用于保存可重复使用的头对象，可选。
- `securitySchemes`: 用于保存可重复使用的安全模型对象，可选。
- `links`: 用于保存可重复使用的链接对象，可选。
- `callbacks`: 用于保存可重复使用的回调对象，可选。
- `pathItems`: 用于保存可重复使用的Path对象（接口），可选。

```yaml
components:
  schemas:
    GeneralError:
      type: object
      properties:
        code:
          type: integer
          format: int32
        message:
          type: string
    Category:
      type: object
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
    Tag:
      type: object
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
  parameters:
    skipParam:
      name: skip
      in: query
      description: number of items to skip
      required: true
      schema:
        type: integer
        format: int32
    limitParam:
      name: limit
      in: query
      description: max records to return
      required: true
      schema:
        type: integer
        format: int32
  examples:
    ErrorExample:
      value:
        message: An error occurred
        code: 500
    FooExample:
      summary: A foo example
      value: {"foo": "bar"}
    XmlExample:
      summary: This is an example in XML (概要) # 指向文字示例的URI。这提供了引用JSON或YAML文档中无法轻松包含的示例的能力。value字段和externalValue字段是互斥的。
      description: This is an example in XML (详细描述) 
      externalValue: 'https://example.org/examples/address-example.xml'
  requestBodies:
    CreateUserRequest:
      required: true
      content:
        application/json:
          schema:
            type: object
            properties:
              name:
                type: string
              email:
                type: string
                format: email
              phone:
                type: string
                format: phone
            required:
              - name
              - email
  headers:
    RequestIdHeader:
      description: Unique identifier for a request
      schema:
        type: string
      example: "12345"
  responses:
    NotFound:
      description: Entity not found.
    IllegalInput:
      description: Illegal input for operation.
    GeneralError:
      description: General Error
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/GeneralError'
  securitySchemes:
    api_key:
      type: apiKey
      name: api_key
      in: header
    petstore_auth:
      type: oauth2
      flows: 
        implicit:
          authorizationUrl: https://example.org/api/oauth/dialog
          scopes:
            write:pets: modify pets in your account
            read:pets: read your pets
```

## Security Scheme Object Parameters (安全方案的对象参数)

- `type`: 安全方案的类型，包括apiKey、http、oauth2、openIdConnect四种类型，必需。
- `description`: 安全方案的描述，可选。
- `name`: 当type为apiKey时，定义用于传递API密钥的参数名称，当type为http时必需。
- `in`: 当type为apiKey时，定义API密钥的传递位置，可选值包括query、header、cookie，当type为http时必需。
- `scheme`: 当type为http时，定义HTTP身份验证方案，包括basic、bearer、digest、hooba、mutualTLS等，当type为http时必需。
- `bearerFormat`: 当type为http且scheme为bearer时，定义Bearer Token的格式，当type为http且scheme为bearer时必需。
- `flows`: 当type为oauth2时，定义OAuth2的四种授权流程，包括implicit、password、clientCredentials、authorizationCode，当type为oauth2时必需。
- `openIdConnectUrl`: 当type为openIdConnect时，定义OpenID Connect授权服务器的URL，当type为openIdConnect时必需。

配置示例：

<https://swagger.io/docs/specification/authentication/>

- Basic Authentication

```yaml
securitySchemes:
  basicAuth:
    type: http
    scheme: basic
```

- Bearer Token

```yaml
securitySchemes:
  bearerAuth:
    type: http
    scheme: bearer
    bearerFormat: JWT
```

- API Key in Query Parameter

```yaml
securitySchemes:
  apiKeyQuery:
    type: apiKey
    name: api_key
    in: query
```

- API Key in Header

```yaml
securitySchemes:
  apiKeyHeader:
    type: apiKey
    name: X-API-Key
    in: header
```

## OAuth Flows Object Parameters (OAuth安全方案的对象参数)

<https://swagger.io/docs/specification/authentication/oauth2/>

- `implicit`: 隐式授权流程，可选。
- `password`: 密码授权流程，可选。
- `clientCredentials`: 客户端凭证授权流程，可选。
- `authorizationCode`: 授权码授权流程，可选。
- `authorizationUrl`: 授权服务器的URL，必需。
- `tokenUrl`: 令牌服务器的URL，当flow为authorizationCode、clientCredentials、password时必需。
- `refreshUrl`: 刷新令牌的URL，当flow为authorizationCode、clientCredentials、password时可选。
- `scopes`: 一个包含作用域定义的对象，可选。

配置示例：

- OAuth 2.0 Implicit Grant

```yaml
securitySchemes:
  oauth2:
    type: oauth2
    flows:
      implicit:
        authorizationUrl: https://example.com/oauth/authorize
        scopes:
          read:pets: read your pets
          write:pets: modify pets in your account
```

- OAuth 2.0 Authorization Code Grant

```yaml
securitySchemes:
  oauth2:
    type: oauth2
    flows:
      authorizationCode:
        authorizationUrl: https://example.com/oauth/authorize
        tokenUrl: https://example.com/oauth/token
        scopes:
          read:pets: read your pets
          write:pets: modify pets in your account
```

- OAuth 2.0 Resource Owner Password Credentials Grant

```yaml
securitySchemes:
  oauth2:
    type: oauth2
    flows:
      password:
        tokenUrl: https://example.com/oauth/token
        scopes:
          read:pets: read your pets
          write:pets: modify pets in your account
```

- OAuth 2.0 Client Credentials Grant

```yaml
securitySchemes:
  oauth2:
    type: oauth2
    flows:
      clientCredentials:
        tokenUrl: https://example.com/oauth/token
        scopes:
          read:pets: read your pets
          write:pets: modify pets in your account
```

# 附录

### Swagger规范和OpenAPI规范关系 

`OpenAPI规范是Swagger规范的演进版本，OpenAPI规范取代了Swagger规范，成为了当前使用最广泛的RESTful API描述规范。`

Swagger是一种用于描述RESTful Web服务的规范，它定义了API的基本结构，包括API的路径、请求方法、参数、响应、错误等。Swagger最初由Tony Tam在2010年创建，并在2015年被SmartBear Software收购。Swagger规范是一个开放的、可扩展的、独立于编程语言和Web框架的规范。

OpenAPI规范是Swagger规范的下一代版本，它由OpenAPI Initiative维护。OpenAPI规范包含了Swagger规范的所有特性，并且增加了一些新的特性，如支持更多的HTTP方法、支持更多的数据类型、支持JSON Schema等。此外，OpenAPI规范还定义了API文档的标准格式，可以使用JSON或YAML格式来描述API。
