<script setup lang="ts">
import { ref, onMounted, inject, computed } from 'vue';
import { STORAGE, routerUtils, appContext } from '@xcan-angus/infra';
import { Select, Input, Hints } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const formState = ref({
  spaceId: undefined,
  url: '',
  parameters: [{ name: 'Content-Type', type: 'string', in: 'header', value: 'multipart/form-data' }],
  body: {
    forms: [{ name: 'bizKey', value: 'angusTesterDataFiles', format: 'string', type: 'string' }, { name: 'files', format: 'binary', type: 'string' }]
  },
  batchRows: '1'
});

const { t } = useI18n();

const formRef = ref();
// Inject project information
const projectInfo = inject('projectInfo', ref({id: ''}));
const projectId = computed(() => projectInfo.value?.id);
const appInfo = appContext.getAccessApp();

onMounted(async () => {
  // const host = await site.getUrl('files');
  // const isPrivate = await site.isPrivate();
  const storageUrl = routerUtils.getStorageFileUrl('/file/upload');
  // const _STORAGE = isPrivate ? STORAGE.replace('/storage', '') : STORAGE;
  formState.value.url = storageUrl;
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
      :label="t('genDataPlugin.spaceStoreConfig.selectSpace')"
      :rules="{required: true, message: t('genDataPlugin.spaceStoreConfig.validation.selectSpace')}">
      <Select
        v-if="projectId"
        :value="formState.spaceId"
        defaultActiveFirstOption
        showSearch
        :defaultOptions="defaultSpace"
        :lazy="false"
        :action="`${STORAGE}/space?projectId=${projectId}&appCode=${appInfo.code}&hasPermission=OBJECT_WRITE`"
        :params="selectParams"
        :fieldNames="{label: 'name', value: 'id'}"
        @change="onSpaceChaneg" />
    </FormItem>
    <FormItem :label="t('genDataPlugin.spaceStoreConfig.batchRows')">
      <Hints :text="t('genDataPlugin.spaceStoreConfig.hints.batchRows')" class="mb-2" />
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
