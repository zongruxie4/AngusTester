<script setup lang="ts">
import { ref, onMounted, watch, defineAsyncComponent, computed, nextTick } from 'vue';
import { Form, FormItem, RadioGroup, Radio } from 'ant-design-vue';
import { Hints, Input, Select } from '@xcan-angus/vue-ui';
import { TESTER, enumLoader } from '@xcan-angus/infra';

export interface Props {
  format: string;
}

const props = withDefaults(defineProps<Props>(), {
  format: 'CVS'
});

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
  location: 'DATASPACE',
  rows: '1000',
  name: undefined
});

const spaceConfigRef = ref();
const dataSourceConfigRef = ref();
const localConfigRef = ref();
const pushConfigRef = ref();

const storeForm = computed(() => {
  switch (formState.value.location) {
    case 'DATASOURCE': return dataSourceConfigRef.value;
    case 'LOCAL': return localConfigRef.value;
    case 'PUSH_THIRD': return pushConfigRef.value;
    case 'DATASPACE': return spaceConfigRef.value;
  }
  return dataSourceConfigRef.value;
});

// - DATASPACE：数据空间存储，将数据存储到 AngusTester 应用"数据"->"空间"。
// - DATASOURCE：数据源存储，将生成数据写入到数据库。
// - LOCAL：本地存储，生成数据会以文件形式存储在执行节点，您可以进入执行详情页面点击下载数据文件。默认存储路径：${AGENT_HOME}/data/exec/[执行ID]/data.[数据格式]。
// - PUSH_THIRD：推送三方接口，支持将生成数据以文本或文件形式发送到一个 Http 接口。

// 存储方式文案提示
const storeTip = computed(() => {
  switch (formState.value.location) {
    case 'DATASOURCE': return '数据源存储，将生成数据写入到数据库。';
    case 'DATASPACE': return '数据空间存储，将数据存储到 AngusTester 应用"数据"->"空间"。';
    // eslint-disable-next-line no-template-curly-in-string
    case 'LOCAL': return '本地存储，生成数据会以文件形式存储在执行节点，您可以进入执行详情页面点击下载数据文件。默认存储路径：${AGENT_HOME}/data/exec/[执行ID]/data.[数据格式]。';
    case 'PUSH_THIRD': return '推送三方接口，支持将生成数据以文本或文件形式发送到一个 Http 接口。';
  }
  return '';
});

// 存储方式选项
let allLocationOpt: {label: string; value: string}[] = [];
const storageLocationOpt = ref<{label: string; value: string}[]>([]);
const loadStorageLoactionOpt = async () => {
  const [error, data] = await enumLoader.load('StorageLocation');
  if (error) {
    return;
  }
  allLocationOpt = data.map(i => ({ ...i, label: i.message }));
  storageLocationOpt.value = props.format !== 'SQL' ? allLocationOpt.filter(i => i.value !== 'DATASOURCE') : allLocationOpt;
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
    ? allLocationOpt.filter(i => i.value !== 'DATASOURCE')
    : allLocationOpt;
  if (oldValue === 'SQL' && formState.value.location === 'DATASOURCE') {
    formState.value.location = 'DATASPACE';
  }
});

onMounted(() => {
  loadStorageLoactionOpt();
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
      label="脚本名称"
      :rules="{required: true, message: '请输入数据脚本名称'}">
      <Input
        v-model:value="formState.name"
        placeholder="输入数据脚本的名称，限制200字符内"
        :maxLength="200"
        class="mr-2" />
    </FormItem>
    <FormItem label="执行线程数">
      <Hints text="默认1个线程，最大1000。" class="mb-2" />
      <Input
        v-model:value="formState.threads"
        dataType="number"
        :min="1"
        :max="1000"
        @blur="onThreadsBlur" />
    </FormItem>
    <FormItem label="选择Mock节点" :rules="{ required: true, message:'请选择节点' }">
      <Select
        v-model:value="formState.nodeId"
        :action="`${TESTER}/node`"
        :lazy="false"
        defaultActiveFirstOption
        :fieldNames="{value: 'id', label: 'name'}" />
    </FormItem>
    <FormItem label="执行优先级">
      <Hints text="优先级高的最先被执行，值范围：0~2147483647,值越大优先级越高。" class="mb-2" />
      <Input
        v-model:value="formState.priority"
        dataType="number"
        :max="2147483647"
        :min="0" />
    </FormItem>
    <FormItem label="遇到错误">
      <RadioGroup v-model:value="formState.onError">
        <Radio value="CONTINUE">继续</Radio>
        <Radio value="STOP">停止</Radio>
        <Radio value="STOP_NOW">立即终止</Radio>
      </RadioGroup>
    </FormItem>
    <FormItem label="存储方式">
      <Hints :text="storeTip" class="mb-1" />
      <RadioGroup
        v-model:value="formState.location"
        :options="storageLocationOpt" />
    </FormItem>
    <FormItem label="行数" class="mb-2">
      <Hints text="生成数据总行数，最大 100000000000。" class="mb-2" />
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
