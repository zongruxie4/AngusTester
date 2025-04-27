<script setup lang="ts">
const tipItems = [
  {
    title: '变量',
    content: '定义一个可以在整个测试过程中共享和重复使用的常量值。'
  },
  {
    title: '数据集',
    content: '定义一组相关的数据参数，通常为实际的测试数据，在执行时动态改变值。'
  },
  {
    title: 'Mock函数',
    content: '通过在测试中引用 mock 函数，在执行时动态生成对应参数值或随机值。'
  },
  {
    title: '采样提取变量',
    content: '从上一个采样的请求或响应中提取一个值，作为后续采样的参数值，每次迭代时重新提取。'
  }
];
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">欢迎进入Angus数据！</div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/1.png" />
      <div>数据可以帮助您生成、管理业务数据和测试数据，并且基于数据生成参数化变量或数据集。</div>
    </div>
    <div class="mt-2 mr-5">
      <div class="font-semibold mb-1.5">AngusTester 提供了四种参数化方式，以满足不同的需求：</div>
      <div
        v-for="item in tipItems"
        :key="item.title"
        class="mb-1">
        <span class="font-semibold">{{ item.title }}: </span>
        <span>{{ item.content }}</span>
      </div>
    </div>
  </div>
</template>
