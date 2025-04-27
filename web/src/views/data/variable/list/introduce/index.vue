<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">关于变量</div>
    <div class="mb-6">
      <div>用于定义一个可以被重复的使用的值，定义变量后可以使用变量占位符 {变量名} 来引用变量。变量使得测试和维护变得更加高效和灵活，同时能够提升测试的可重复性和可扩展性。</div>
      <div>注意：在执行采样前，变量占位符会被替换成对应的变量值，并且执行过程中值不会改变。AngusTester 变量除了支持定义静态变量外，还支持下面三种方式提取一个变量：</div>
    </div>

    <div class="flex items-start justify-between">
      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">文件提取</div>
          <div>从文件中提取一个值作为变量值。如：读取秘钥。</div>
        </div>
      </div>

      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">Http提取</div>
          <div>从Http请求中提取一个值作为变量值。如：动态获取一个令牌。</div>
        </div>
      </div>

      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">Jdbc提取</div>
          <div>根据SQL从数据源表中提取指定的变量值，如：商品当前报价。</div>
        </div>
      </div>
    </div>
  </div>
</template>
