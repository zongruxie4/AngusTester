<script setup lang="ts">
import { ref } from 'vue';
import { Input, Hints } from '@xcan-angus/vue-ui';
import { FormItem, Form } from 'ant-design-vue';

const formState = ref({
  batchRows: '1'
});

const onBatchRowsBlur = () => {
  const value = formState.value.batchRows.trim();
  if (!value) {
    formState.value.batchRows = '1';
  }
};

defineExpose({
  getData: () => {
    const { batchRows } = formState.value;
    return {
      batchRows
    };
  },
  validate: () => {
    return Promise.resolve();
  },
  setData: (data) => {
    const { batchRows } = data;
    formState.value.batchRows = batchRows;
  }
});

</script>
<template>
  <div>
    <!-- <div class="flex items-center mb-2">
      <span class="w-20">批量写入数</span>
      <Input
        v-model:value="formState.batchRows"
        class="w-80"
        dataType="number"
        :max="10000"
        :min="1"
        @blur="onBatchRowsBlur" />
      <Tooltip title="最大支持10000" placement="topLeft">
        <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
      </Tooltip>
    </div> -->
    <Form layout="vertical">
      <FormItem label="批量行数" layout="vertical">
        <!-- <Tooltip title="最大支持10000" placement="topLeft">
          <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
        </Tooltip> -->
        <Hints text="每次批量生成、存储或发送行数，默认 1，最大10000。" class="mb-2" />
        <Input
          v-model:value="formState.batchRows"
          dataType="number"
          :max="10000"
          :min="1"
          @blur="onBatchRowsBlur" />
      </FormItem>
      <FormItem label="默认储存路径" layout="vertical">
        <Hints text="生成数据文件在执行节点存储路径。" />
        <Input
          value="${AGENT_HOME}/exec/${execId}/data.${format}"
          disabled />
      </FormItem>
    </Form>
    <!-- <div class="flex items-center">
      <span class="w-20">存储路径</span>
      <Input
        class="w-80"
        value="${AGENT_HOME}/exec/${execId}/data.${format}"
        disabled />
    </div> -->
  </div>
</template>
