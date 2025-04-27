<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
import { STORAGE, site } from '@xcan-angus/tools';
import { Select, Input, Hints } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';

const formState = ref({
  spaceId: undefined,
  url: '',
  parameters: [{ name: 'Content-Type', type: 'string', in: 'header', value: 'multipart/form-data' }],
  body: {
    forms: [{ name: 'bizKey', value: 'angusTesterDataFiles', format: 'string', type: 'string' }, { name: 'files', format: 'binary', type: 'string' }]
  },
  batchRows: '1'
});

const formRef = ref();
const projectInfo = inject('projectInfo', ref({ id: '' }));
const appInfo = inject('appInfo', ref({ code: '' }));

onMounted(async () => {
  const host = await site.getUrl('files');
  const isPrivate = await site.isPrivate();
  const _STORAGE = isPrivate ? STORAGE.replace('/storage', '') : STORAGE;
  formState.value.url = `${host}${_STORAGE}/file/upload`;
});

const defaultSpace = ref();
const onSpaceChaneg = (value, opt) => {
  formState.value.spaceId = value;
  defaultSpace.value = { [value]: opt };
};

const validate = () => {
  return formRef.value.validate();
};

const onBatchRowsBlur = () => {
  const value = formState.value.batchRows.trim();
  if (!value) {
    formState.value.batchRows = '1';
  }
};

const selectParams = { appCode: 'AngusTester' };

defineExpose({
  getData: () => {
    const { spaceId, body, url, parameters, batchRows } = formState.value;
    return {
      batchRows,
      storeRequest: {
        url,
        parameters,
        body: {
          forms: [...body.forms, { name: 'spaceId', format: 'string', type: 'string', value: spaceId }]
        }
      }
    };
  },

  getCurrentData: () => {
    const { batchRows, spaceId } = formState.value;
    return {
      batchRows,
      spaceId,
      defaultSpace
    };
  },
  validate,
  setData: (data) => {
    formState.value.batchRows = data.batchRows || '1';
    formState.value.spaceId = data.spaceId || undefined;
    defaultSpace.value = data.defaultSpace || undefined;
  }
});

</script>
<template>
  <Form
    ref="formRef"
    :model="formState"
    layout="vertical">
    <FormItem
      name="spaceId"
      label="选择空间"
      :rules="{required: true, message: '请选择空间'}">
      <Select
        :value="formState.spaceId"
        defaultActiveFirstOption
        showSearch
        :defaultOptions="defaultSpace"
        :lazy="false"
        :action="`${STORAGE}/space/search?projectId=${projectInfo.id}&appCode=${appInfo.code}&hasPermission=OBJECT_WRITE`"
        :params="selectParams"
        :fieldNames="{label: 'name', value: 'id'}"
        @change="onSpaceChaneg" />
    </FormItem>
    <FormItem label="批量行数">
      <Hints text="每次批量生成、存储或发送行数，默认 1，最大10000。" class="mb-2" />
      <div class="flex items-center">
        <Input
          v-model:value="formState.batchRows"
          dataType="number"
          :max="10000"
          :min="1"
          @blur="onBatchRowsBlur" />
        <!-- <Tooltip title="最大支持10000" placement="topLeft">
          <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
        </Tooltip> -->
      </div>
    </FormItem>
  </Form>
</template>
