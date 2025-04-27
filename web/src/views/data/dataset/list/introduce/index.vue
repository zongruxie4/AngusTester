<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">关于数据集</div>
    <div class="mb-6">
      <div>用于定义一组相关的数据参数值，通常为实际的测试数据，如：用户数据、订单数据、帐户数据等，定义数据集后可以使用参数名占位符 {参数名} 来引用数据集参数。</div>
      <div>注意：数据集在执行采样前加载，在采样过程中动态改变参数值。AngusTester 支持下面三种方式定义数据集：</div>
    </div>

    <div class="flex items-start justify-between">
      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">静态数据集</div>
          <div>使用Mock函数和常量值定义一组值作为数据集。</div>
        </div>
      </div>

      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">文件提取</div>
          <div>从一个文件中提取一组参数值作为数据集。</div>
        </div>
      </div>

      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">Jdbc提取</div>
          <div>根据一个SQL语句从数据源表中提取一组值作为数据集。</div>
        </div>
      </div>
    </div>
  </div>
</template>
