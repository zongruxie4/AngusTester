<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">关于服务器</div>
    <div class="flex items-center space-x-10">
      <div>
        <div class="mb-6">
          <div>定义 API 的服务器及服务器相关的动态部分，包括服务器(server)和变量(variables)两部分。其中服务器字段用于指定 API 的服务器列表，每个服务器都有一个 URL 和可选的描述信息，指示 API 的基础路径。</div>
          <div>变量用于定义 URL 中的动态部分。它允许在服务器 URL 中使用占位符，这些占位符可以在不同的环境中被替换为实际的值。主要好处如下：</div>
        </div>

        <div class="flex items-start justify-between">
          <div class="flex items-start space-x-3 w-1/3">
            <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
            <div class="space-y-1 pr-10">
              <div class="text-3.5 font-semibold">易于与CICD集成</div>
              <div>在持续集成和持续部署的工作流中，定义的服务器可以帮助自动化测试流程，使得不同环境的 API 端点可以灵活配置，符合 DevOps 的要求。</div>
            </div>
          </div>

          <div class="flex items-start space-x-3 w-1/3">
            <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
            <div class="space-y-1 pr-10">
              <div class="text-3.5 font-semibold">支持多个环境</div>
              <div>通过定义多个服务器或通过不同变量值支持不同的环境（如开发、测试、生产等）。这使得开发者能够轻松切换环境，而不必在代码中手动更改 URL。</div>
            </div>
          </div>

          <div class="flex items-start space-x-3 w-1/3">
            <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
            <div class="space-y-1 pr-10">
              <div class="text-3.5 font-semibold">支持版本控制</div>
              <div>在不同版本的 API 中，可以通过定义不同的服务器或者变量值来管理和维护版本。可以在文档中清晰地区分各个版本的服务器，避免混淆。</div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex-shrink-0">
        <div class="font-semibold text-center">完整 URL、服务器、和端点(endpoint)关系</div>
        <div class="mb-1">https://api.example.com/v1/users?role=admin&status=active</div>
        <div class="flex items-center mb-6">
          <div class="diagonal-line line"></div>
          <div style="width:112px;" class="horizontal-line line"></div>
          <div style="transform: rotate(-65deg);" class="diagonal-line line"></div>
          <div class="diagonal-line line"></div>
          <div style="width:32px;" class="horizontal-line line"></div>
          <div style="transform: rotate(-65deg);" class="diagonal-line line"></div>
          <div class="diagonal-line line"></div>
          <div style="width:125px;" class="horizontal-line line"></div>
          <div style="transform: rotate(-65deg);" class="diagonal-line line"></div>
        </div>
        <div class="font-semibold text-center">完整URL(path)</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.line {
  height: 1px ;
  background-color: rgb(82, 90, 101);
}

.diagonal-line {
  width: 10px;
  transform: rotate(65deg);
}

.horizontal-line {
  position: relative;
  transform: translateY(5px);
}

.horizontal-line::after {
  content:"server URL";
  display: block;
  position:absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
}

.horizontal-line:nth-of-type(5)::after {
  content:"endpoint";
}

.horizontal-line:nth-of-type(8)::after {
  content:"query parameters";
}
</style>
