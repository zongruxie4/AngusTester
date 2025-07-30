<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { Spin, Hints, Input, AsyncComponent, Select } from '@xcan-angus/vue-ui';
import { RadioButton, RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';

const activeType = ref<'upload' | 'generated'>('upload');
const activeSource = ref('file');

const SelectFileModal = defineAsyncComponent(() => import('./SelectFileModal.vue'));

const sourceOpt = [
  { value: 'file', label: '文件' }, { value: 'dataSource', label: '数据源' }
];

const selectFileVisible = ref(false);
const selectFile = () => {
  selectFileVisible.value = true;
};
const confirmFile = () => {
};

</script>

<template>
  <Spin class="flex flex-col">
    <Hints text="为了让脚本中的参数值更加接近真实业务数据，系 统还提供了两种动态取值方式，您可根据自己的业 务需求选择参数值的取值方式 。" />
    <RadioGroup
      v-model:value="activeType"
      size="small"
      class="mt-3"
      buttonStyle="solid">
      <RadioButton value="upload">上传参数文件</RadioButton>
      <RadioButton value="generated">已生成参数文件</RadioButton>
    </RadioGroup>
    <template v-if="activeType === 'upload'"></template>
    <template v-if="activeType === 'generated'">
      <div class="mt-4">
        <div>来源</div>
        <RadioGroup
          v-model:value="activeSource"
          :options="sourceOpt" />
      </div>
      <div v-if="activeSource === 'file'" class="mt-3">
        <div>选择文件</div>
        <Input readonly>
          <template #addonAfter>
            <span class="text-3 cursor-pointer text-theme-special" @click="selectFile">选择</span>
          </template>
        </Input>
      </div>
      <div v-if="activeSource === 'dataSource'" class="mt-3">
        <div>数据源</div>
        <Select
          :action="`${TESTER}/mock/datasource`"
          :fieldName="{value: 'id', label: 'database'}" />
      </div>
    </template>
    <AsyncComponent :visible="selectFileVisible">
      <SelectFileModal
        v-model:visible="selectFileVisible"
        @ok="confirmFile" />
    </AsyncComponent>
  </Spin>
</template>
