<script setup lang="ts">
import { ref, onMounted, watch, defineAsyncComponent, computed } from 'vue';
import { Form, FormItem, RadioGroup, Radio } from 'ant-design-vue';
import { Hints, Input, Select } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TESTER, enumUtils, StorageLocation } from '@xcan-angus/infra';

export interface Props {
  format: string;
}

const props = withDefaults(defineProps<Props>(), {
  format: 'CVS'
});

const { t } = useI18n();

const DatasourceStoreConfig = defineAsyncComponent(() => import('./DatasourceStoreConfig.vue'));
const LocalStoreConfig = defineAsyncComponent(() => import('./LocalStoreConfig.vue'));
const PushThirdStoreConfig = defineAsyncComponent(() => import('./PushThirdStoreConfig.vue'));
const SpaceStoreConfig = defineAsyncComponent(() => import('./SpaceStoreConfig.vue'));

const formRef = ref();
const formState = ref({
  threads: '1',
  nodeId: undefined,
  priority: '1000',
  onError: 'CONTINUE',
  location: StorageLocation.DATASOURCE,
  rows: '1000',
  name: undefined
});

const spaceConfigRef = ref();
const dataSourceConfigRef = ref();
const localConfigRef = ref();
const pushConfigRef = ref();

const storeForm = computed(() => {
  switch (formState.value.location) {
    case StorageLocation.DATASOURCE:
      return dataSourceConfigRef.value;
    case StorageLocation.LOCAL:
      return localConfigRef.value;
    case StorageLocation.PUSH_THIRD:
      return pushConfigRef.value;
    case StorageLocation.DATASPACE:
      return spaceConfigRef.value;
  }
  return dataSourceConfigRef.value;
});

// 存储方式文案提示
const storeTip = computed(() => {
  switch (formState.value.location) {
    case StorageLocation.DATASOURCE:
      return t('genDataPlugin.excuteConfig.storageTips.datasource');
    case StorageLocation.DATASPACE:
      return t('genDataPlugin.excuteConfig.storageTips.dataspace');
    case StorageLocation.LOCAL:
      return t('genDataPlugin.excuteConfig.storageTips.local');
    case StorageLocation.PUSH_THIRD:
      return t('genDataPlugin.excuteConfig.storageTips.pushThird');
  }
  return '';
});

// 存储方式选项
let allLocationOpt: { label: string; value: string }[] = [];
const storageLocationOpt = ref<{ label: string; value: string }[]>([]);
const loadStorageLocationOpt = () => {
  const data = enumUtils.enumToMessages(StorageLocation);
  allLocationOpt = data.map(i => ({ ...i, label: i.message }));
  storageLocationOpt.value = props.format !== 'SQL' ? allLocationOpt.filter(i => i.value !== StorageLocation.DATASOURCE) : allLocationOpt;
};

const validate = () => {
  return Promise.all([formRef.value.validate(), storeForm.value.validate()]);
};

const getData = () => {
  const storeData = storeForm.value.getData();
  return { ...formState.value, ...storeData };
};

const getCurrentData = () => {
  const storeData = storeForm.value.getCurrentData ? storeForm.value.getCurrentData() : storeForm.value.getData();
  return { ...formState.value, ...storeData };
};
const setData = (data) => {
  if (data) {
    Object.keys(formState.value).forEach(key => {
      formState.value[key] = data[key];
      delete data[key];
    });
    setTimeout(() => {
      storeForm.value.setData(data);
    }, 100);
  }
};

const onRowsBlur = () => {
  const value = formState.value.rows.trim();
  if (!value) {
    formState.value.rows = '1';
  }
};

const onThreadsBlur = () => {
  const value = formState.value.threads.trim();
  if (!value) {
    formState.value.threads = '1';
  }
};

watch(() => props.format, (newValue, oldValue) => {
  storageLocationOpt.value = newValue !== 'SQL'
    ? allLocationOpt.filter(i => i.value !== StorageLocation.DATASOURCE)
    : allLocationOpt;
  if (oldValue === 'SQL' && formState.value.location === StorageLocation.DATASOURCE) {
    formState.value.location = 'DATASPACE';
  }
});

onMounted(() => {
  loadStorageLocationOpt();
});

defineExpose({
  validateForm: validate,
  getData,
  setData,
  getCurrentData
});
</script>
<template>
  <Form
    ref="formRef"
    :model="formState"
    layout="vertical"
    class="w-full"
    size="small">
    <FormItem
      name="name"
      :label="t('genDataPlugin.excuteConfig.scriptName')"
      :rules="{required: true, message: t('genDataPlugin.excuteConfig.validation.scriptNameRequired')}">
      <Input
        v-model:value="formState.name"
        :placeholder="t('genDataPlugin.excuteConfig.placeholders.scriptName')"
        :maxLength="200"
        class="mr-2" />
    </FormItem>
    <FormItem :label="t('genDataPlugin.excuteConfig.execThreads')">
      <Hints :text="t('genDataPlugin.excuteConfig.hints.execThreads')" class="mb-2" />
      <Input
        v-model:value="formState.threads"
        dataType="number"
        :min="1"
        :max="1000"
        @blur="onThreadsBlur" />
    </FormItem>
    <FormItem :label="t('genDataPlugin.excuteConfig.selectMockNode')" :rules="{ required: true, message: t('genDataPlugin.excuteConfig.validation.selectNode') }">
      <Select
        v-model:value="formState.nodeId"
        :action="`${TESTER}/node`"
        :lazy="false"
        defaultActiveFirstOption
        :fieldNames="{value: 'id', label: 'name'}" />
    </FormItem>
    <FormItem :label="t('genDataPlugin.excuteConfig.execPriority')">
      <Hints :text="t('genDataPlugin.excuteConfig.hints.execPriority')" class="mb-2" />
      <Input
        v-model:value="formState.priority"
        dataType="number"
        :max="2147483647"
        :min="0" />
    </FormItem>
    <FormItem :label="t('genDataPlugin.excuteConfig.onError')">
      <RadioGroup v-model:value="formState.onError">
        <Radio value="CONTINUE">{{ t('genDataPlugin.excuteConfig.errorActions.continue') }}</Radio>
        <Radio value="STOP">{{ t('genDataPlugin.excuteConfig.errorActions.stop') }}</Radio>
        <Radio value="STOP_NOW">{{ t('genDataPlugin.excuteConfig.errorActions.stopNow') }}</Radio>
      </RadioGroup>
    </FormItem>
    <FormItem :label="t('genDataPlugin.excuteConfig.storageMethod')">
      <Hints :text="storeTip" class="mb-1" />
      <RadioGroup
        v-model:value="formState.location"
        :options="storageLocationOpt" />
    </FormItem>
    <FormItem :label="t('genDataPlugin.excuteConfig.rows')" class="mb-2">
      <Hints :text="t('genDataPlugin.excuteConfig.hints.rows')" class="mb-2" />
      <div class="flex items-center mb-4">
        <Input
          v-model:value="formState.rows"
          dataType="number"
          :min="1"
          :max="100000000000"
          @blur="onRowsBlur" />

        <!-- <Tooltip title="行数最大支持千亿行" placement="topLeft">
          <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
        </Tooltip> -->
      </div>
    </FormItem>
    <!-- <div class="mb-2">
      <span class="w-20">行数</span>
      <Input
        v-model:value="formState.rows"
        dataType="number"
        class="w-80"
        :min="1"
        :max="100000000000"
        @blur="onRowsBlur" />
      <Tooltip title="行数最大支持千亿行" placement="topLeft">
        <Icon icon="icon-tishi1" class="text-blue-tips text-3.5 ml-1" />
      </Tooltip>
    </div> -->
  </Form>
  <template v-if="formState.location === 'DATASOURCE'">
    <DatasourceStoreConfig
      ref="dataSourceConfigRef"
      class="text-3" />
  </template>
  <template v-if="formState.location === 'LOCAL'">
    <LocalStoreConfig
      ref="localConfigRef"
      class="text-3" />
  </template>
  <template v-if="formState.location === 'PUSH_THIRD'">
    <PushThirdStoreConfig
      ref="pushConfigRef"
      class="text-3" />
  </template>
  <template v-if="formState.location === 'DATASPACE'">
    <SpaceStoreConfig
      ref="spaceConfigRef"
      class="text-3" />
  </template>
</template>
