<script setup lang="ts">
import { Popover } from 'ant-design-vue';
import { Icon, Hints } from '@xcan-angus/vue-ui';
</script>
<template>
  <Popover
    destroyTooltipOnHide
    placement="left"
    :trigger="['click']">
    <div class="flex-shrink-0 flex items-center cursor-pointer w-4 h-7">
      <Icon icon="icon-jieshaoshuoming" class="text-4 text-theme-text-hover" />
    </div>
    <template #content>
      <div style="max-height: 60vh; overflow: auto;" class="leading-4.5 text-3 text-theme-content">
        <div class="mb-4">
          <Hints text="执行条件表达式" class="mb-2 !font-semibold !text-theme-content" />
          <div class="mb-3">用于控制该条断言是否执行，当满足条件表达式或条件表达式为空时断言会被执行。</div>
        </div>
        <ol class="pl-4 space-y-3">
          <li style="list-style-type: circle;" class="space-y-2 text-theme-sub-content">
            <div class="text-theme-content font-semibold">条件表达式格式</div>
            <div>{变量名} + 运算符 + 值，例如：{id}=100001。</div>
          </li>
          <li style="list-style-type: circle;" class="space-y-2">
            <div class="font-semibold">格式中参数说明</div>
            <ul style="padding-left: 16px;list-style-type: disc;" class="space-y-2 text-theme-sub-content">
              <li>“变量”为当前接口或父级项目/服务或全局中定义的变量，其中变量引用符号{}可以省略。</li>
              <li>支持“运算符”包括：=、!=、>、>=、&lt;、&lt;=。</li>
              <li>操作符右侧“值”只能是常量。</li>
            </ul>
          </li>
        </ol>
      </div>
    </template>
  </Popover>
</template>
<style scoped>
pre code {
  font-size: 13px;
  line-height: 18px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
