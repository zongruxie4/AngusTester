[//]: # (Mock服务管理)

[//]: # (=====)

AngusTester Mock服务管理功能旨在提供一个灵活、便捷的Mock服务创建和管理平台，方便开发者在应用开发和测试过程中模拟真实的服务接口。

## 一、服务信息

在AngusTester中，每个Mock服务包含以下字段信息：

- ID：每个服务的唯一标识符，创建服务后自动分配。
- 项目ID：Mock服务所属项目ID。
- 名称：Mock服务的名称，便于用户识别和管理。
- 服务来源，标记Mock服务的创建方式：
     - 手动创建：用户手动输入相关信息创建服务。
     - 文件导入：通过导入文件形式创建服务。
     - 关联服务：基于已有接口服务进行关联并创建Mock服务。
- 导入来源，标记Mock服务的数据来源：
    - OpenAPI：符合OpenAPI规范的文档。
    - Postman：Postman导出的接口文档。
    - Angus：Angus平台生成的接口文档。
- 运行状态，指示Mock服务的当前运行状态：
    - 未启动：服务尚未启动，无法响应请求。
    - 运行中：服务已启动，能够正常处理请求。
- 是否有权限控制：标识该Mock服务是否启用权限控制，以确保只有授权用户可以访问。
- 关联服务ID：如果Mock服务关联了其他服务，此字段将会记录关联服务的ID。
- 部署节点：指定Mock服务的部署位置，便于优化服务性能和负载均衡。
- 服务端口：定义Mock服务监听的端口号，确保请求能够正确路由到相应服务。
- 服务域名：Mock服务的访问域名，用户通过此域名进行接口访问。
- 接口安全配置：用于配置接口的安全性。
- CORS配置：跨域资源共享（CORS）配置，允许浏览器跨域请求Mock接口。
- 服务配置：主要参数配置，影响Mock服务的运行时行为。
- 创建人：创建该Mock服务的用户。
- 创建时间：创建Mock服务的时间。
- 最后修改人：最后修改该Mock服务的用户。
- 最后修改时间：最后一次修改Mock服务的时间。

## 二、Mock服务操作

AngusTester Mock服务管理模块支持以下操作：

### 1、创建服务

AngusTester支持下面三种方式创建Mock服务，需要注意的事项：

- 为服务设置域名后，您可以通过域名访问Mock接口，设置的域名需要被解析到所在节点的IP。
- 服务所运行的节点，服务添加后不允许修改。
- 服务所监听的端口，服务添加后不允许修改。

### 2、添加Mock服务

用户手动输入相关信息创建服务，添加Mock服务后需要手动添加Mock接口。

操作步骤:

1. 进入AngusTester Mock页面，点击“添加Mock服务”按钮。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-add.png?fid=251751339858591932&fpt=ACQjFydlvVviKVQZk7gftRKIWM4P06hK1m9cKlHh)
2. 选择“添加Mock服务”选项。
3. 填写服务名称、域名、端口等信息。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-add-view-created.png?fid=251751339858591928&fpt=GJM1gg2BpDPOuiUca5p8wU8hlWaTPpDAy9lhIhTG)
4. 点击“确定”以创建服务。

### 3、已有服务关联导入

根据现有接口服务进行添加，用户可以选择需要Mock的接口，添加后系统将自动与接口服务建立关联。

操作步骤:

1. 进入AngusTester Mock页面，点击“添加Mock服务”按钮。
2. 选择“已有服务关联导入”选项。
3. 填写服务名称、端口等信息，并选择需要关联导入的服务。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-add-view-assoc.png?fid=251751339858591926&fpt=TM28TWidoP9umrd8ZVE2v7BURDdCWLrA6YDvzljM)
4. 点击“确定”以创建服务。

### 4、文件导入Mock

基于Swagger2.0、OpenAPI3.x、Postman2.0、Postman2.1、Angus格式数据创建Mock服务并导入接口。

操作步骤:

1. 进入AngusTester Mock页面，点击“添加Mock服务”按钮。
2. 选择“文件导入Mock”选项。
3. 填写服务名称、端口等信息，并选择需要导入接口文件。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-add-view-import.png?fid=251751339858591930&fpt=limE91WJ6DynMNkmdWMNQjZGh9dAePwRHdDYZ7Tz)
4. 点击“确定”以创建服务。

### 5、启动服务

启动并运行刚创建的Mock服务或停止的Mock服务。

操作步骤:

1. 进入AngusTester Mock页面，找到需要启动的Mock服务。
2. 点击“启动”按钮运行Mock服务系统会异步启动Mock服务。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-start.png?fid=251751339858591942&fpt=BJhurj9wN3riVryt30IgeK9W2vzYvRIoXG0tmAvw)
3. 等待服务启动成功，这个过程预计需要数十秒。

### 6、停止服务

停止正则运行的Mock服务。

操作步骤:

1. 进入AngusTester Mock页面，找到需要停止的Mock服务。
2. 点击“停止”按钮运行Mock服务系统会异步停止Mock服务。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-stop.png?fid=251751339858591944&fpt=8dUEowZQguD4r0onctyypcU8s9NhZNVMJXUBRYM2)
3. 等待服务停止成功，这个过程预计需要数秒。

### 7、刷新实例

强制更新Mock服务配置和所有Mock接口信息到运作的服务实例。

操作步骤:

1. 进入AngusTester Mock页面，找到需要刷新的Mock服务。
2. 点击“刷新实例”按钮进行刷新。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-instance-update.png?fid=251751417168003251&fpt=6ktceUeKekhaHCDUyGOfAsRsH0XGvBTXJJlu2rhB)
3. 刷新成功后系统会自动提示成功。

### 8、删除Mock服务

用户可以删除不再需要的报告，以维护报告管理的整洁性。

操作步骤:

1. 进入AngusTester Mock页面，找到需要删除Mock服务。
2. 点击“删除”按钮。
3. 在弹出的确认窗口中确认删除操作。

***注意事项:***

- 删除操作不可逆，请谨慎操作。
- 确保该Mock服务不再被其他用户使用。

### 9、导出Mock脚本

导出Mock服务中所有Mock接口到脚本文件。

操作步骤:

1. 进入AngusTester Mock页面，找到需要导出的Mock服务。
2. 点击“导出”按钮系统会自动下载Mock文件。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-export.png?fid=251751417168003249&fpt=8TVDOkckmJqLwje4EVQCUzib1WV7lrM5Lp2J9T1v)
3. 下载成功可以在浏览器下载中找到该文件。

### 10、编辑基本信息

修改服务的基本信息，以便于后续管理和识别。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
3. 在“设置”->“基本信息”编辑需要修改的基本信息。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-edit-basic.png?fid=251751339858591936&fpt=VR11lo98T7lnIoBrR2SmSTvHjvQHVa7SroBXq2Ng)
4. 点击“确定”以完成修改。

### 11、修改服务配置

调整服务的配置参数，包括请求超时时间、最大并发数等，修改后，请务必保存更改，以确保配置生效。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
2. 在“设置”->“服务配置”编辑需要修改的配置信息，以下是配置参数说明：
     - 线程数：处理请求的线程数，最大10000，默认256。
     - 文件日志级别：配置请求日志信息级别。
       - NONE：无日志记录。
       - BASIC：只记录请求方法和URL以及响应状态代码和执行时间，默认。
       - HEADERS：记录基本信息以及请求和响应头。
       - FULL：记录请求和响应头、正文和元数据。
     - 记录请求日志：是否发送Mock请求日志到服务端。
     - 最大请求大小：允许的最大请求大小，默认为1000*1024*1024（1000MB）。
     - 回推线程数：处理回推请求的线程数，默认为8。
     - 回推连接超时：回推时最大连接超时，单位毫秒，默认5000。
     - 回推请求超时：回推时最大请求超时，单位毫秒，默认-1不超时。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-edit-config.png?fid=251751339858591938&fpt=TIpHvPmqdVzVG0YSIg7zx0FUSjFbmEeAMQ1s8Ii5)
3. 点击“确定”以完成修改。

***注意事项:***

- 修改后需要重启服务或刷新实例后生效。

### 12、开启接口安全

设置访问接口接口的安全策略，确保设置符合安全要求，以保护服务不受未授权访问，默认未开启。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
2. 在“设置”->“接口安全”中，开启“接口安全”选项，并配置访问接口所需的授权信息，最多允许添加10个。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-edit-apisecurity.png?fid=251751339858591934&fpt=hnQ5Hb3taZ3tA0ssi6wkiBcyltelwxDFhrF7SxFv)
3. 点击“保存”以创建服务。

***注意事项:***

- 开启配置后访问接口必须携带授权参数才可以访问接口。
- 修改后需要重启服务或刷新实例后生效。

### 13、开启跨域设置

配置跨域资源共享（CORS）策略，指定允许访问的域名，确保用户能够顺利访问接口，同时保持安全性，默认开启。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
2. 在“设置”->“跨域设置”中，开启“是否开启”选项，并修改跨域策略，以下是配置参数说明：
    - 是否开启：启用跨域访问限制配置，默认为不启用。
    - 允许的域：指定哪些网站可以参与跨来源资源共享，默认设置*，通过响应头Access-Control-Allow-Origin返回。
    - 允许访问凭证：指定第三方站点可能能够执行特权操作并检索敏感信息，默认为true，通过响应头Access-Control-Allow-Credentials返回。
    - 允许的请求方法：指定允许的HTTP方法，默认为GET、POST、PUT、PATCH、DELETE、OPTIONS、HEAD，通过响应头Access-Control-Allow-Methods返回。
    - 允许的请求头：指定允许客户端使用的HTTP请求标头，默认不指定，通过响应头Access-Control-Allow-Headers返回。
    - 可访问的响应头：指定允许访问响应中的那些标头字段，这些字段不包括浏览器可以访问的默认头，默认不指定，通过响应头Access-Control-Expose-Headers返回。
    ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-edit-cors.png?fid=251751339858591940&fpt=AY3BiJ0xjGPMs4DfEMfVdWukyUQ4kMQVm71vsH14)
3. 点击“确定”以完成修改。

***注意事项:***

- 修改后需要重启服务生效。

### 14、查看Mock接口

查看已创建的Mock接口的列表，可以查看每个接口的详细信息，包括请求和响应示例。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
2. 点击“接口”查看接口列表和Mock接口详细信息。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-apis.png?fid=251751417168003247&fpt=F64hh0E9ZRkswC9uVjDgZDZpHEOSmUwYbFGIwCcW)

### 15、查看请求记录

提供对部分请求的详细记录，包括时间、请求参数等。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
2. 点击“请求”查看所有对该服务的请求记录，包括请求时间、请求详情等。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-apis-logs.png?fid=251751417168003245&fpt=RQ6zg3wvqypXank0vS7fZK6gbkno5Dr484KbUnwi)

### 16、查看服务日志

服务的日志信息，了解服务的运行状态和错误信息。日志记录有助于快速定位问题并进行故障排查。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
2. 点击“日志”查看服务的运行日志，以便排查问题。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-logs.png?fid=251751417168003253&fpt=tBNAPbJDxiBUfNzUFEJg8CxFWYfqbKhys0cm2wJ5)

### 17、查看活动记录

有重要活动记录，包括用户操作、系统事件等。通过查看活动记录，您可以追踪服务的操作历史，确保操作的透明性和安全性。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
2. 点击“活动”查看Mock服务的活动历史，包括创建、修改等操作。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-activity.png?fid=251751417168003243&fpt=HTTvl0pF1J2TfbEf9U15deEQSTHz11j3dwgJwGc1)

### 18、查看服务资源监控

监控服务的资源使用情况，如 CPU、内存等。通过监控，您可以及时发现潜在的性能瓶颈，并进行优化和扩容调整。

操作步骤:

1. 进入AngusTester Mock页面，点击服务名进入服务详情。
2. 点击“监控”查看查看Mock服务的资源使用情况和性能指标。
   ![](https://bj-c1-prod-files.xcan.cloud/storage/pubapi/v1/file/service-monitor.png?fid=251751417168003255&fpt=km1G3oKvEE0uhztoA4O6muriThhgQ1I9bRy1T0T2)
