<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, notification, Toggle } from '@xcan-angus/vue-ui';
import { apis, indicator, services } from '@/api/tester';
import { CheckboxGroup } from 'ant-design-vue';
import TestForm from './testForm.vue';

const { t } = useI18n();

interface Props {
  id: string;
  visible: boolean;
  type: 'API'|'SERVICE';
  setType: 'create'|'update';
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: 'SERVICE',
  setType: 'create'
});
const emits = defineEmits<{(e: 'update:visible', value: boolean): void}>();

const perfRef = ref();
const stabilityRef = ref();
const funcRef = ref();
const checked = ref<string[]>(['perf', 'stability', 'func']);
const testTypeOpt = [
  { value: 'func', label: t('commonComp.genTestScriptModal.functionalTest') },
  { value: 'perf', label: t('commonComp.genTestScriptModal.performanceTest') },
  { value: 'stability', label: t('commonComp.genTestScriptModal.stabilityTest') }
];

const perfTestData = reactive({
  priority: 'MEDIUM',
  threads: '',
  duration: '0s',
  authFlag: false,
  rampUpInterval: undefined,
  rampUpThreads: undefined
});

const funcTestData = reactive({
  priority: 'HIGH',
  iterations: '1',
  authFlag: false,
  ignoreAssertions: false
});

const stabilityTestData = reactive({
  priority: 'LOW',
  threads: '',
  duration: '0s',
  authFlag: false
});

const perfOpen = ref(true);
const stabilityOpen = ref(true);
const funcOpen = ref(true);

// 获取测试指标
const loadTestIndicator = async () => {
  if (!props.id) {
    return;
  }
  const [perfErr, perfData] = await indicator.getDefaultPerf(props.id, props.type);
  if (perfErr) {
    notification.error(perfErr.message);
  } else {
    perfTestData.duration = perfData.data.duration;
    perfTestData.threads = perfData.data.threads;
    perfTestData.rampUpThreads = perfData.data.rampUpThreads;
    perfTestData.rampUpInterval = perfData.data.rampUpInterval;
  }
  const [stabilityErr, stabilityData] = await indicator.getDefaultStability(props.id, props.type);
  if (stabilityErr) {
    notification.error(stabilityErr.message);
  } else {
    stabilityTestData.duration = stabilityData.data.duration;
    stabilityTestData.threads = stabilityData.data.threads;
  }
};

const getTitle = computed(() => {
  return t('commonComp.genTestScriptModal.title');
});

const submitting = ref(false);
const submit = async () => {
  if (submitting.value) {
    return;
  }
  submitting.value = true;
  const perfData = perfRef.value?.getData();
  const stabilityData = stabilityRef.value?.getData();
  const funcData = funcRef.value?.getData();
  if (!perfData && !stabilityData && !funcData) {
    cancel();
    return;
  }
  const params = [perfData, stabilityData, funcData].filter(Boolean);
  if (props.setType === 'create') {
    const [error] = await (props.type === 'API' ? apis.generateTestScript(props.id, params) : services.putApiScript(props.id, params));
    submitting.value = false;
    if (error) {
      return;
    }
    notification.success('生成测试脚本成功，请在“脚本”中查看详情');
    cancel();
  } else {
    const [error] = await (props.type === 'API' ? apis.updateTestScript(props.id, params) : services.updateApiScript(props.id, params));
    submitting.value = false;
    if (error) {
      return;
    }
    notification.success('更新测试脚本成功，请在“脚本”中查看详情');
    cancel();
  }
};

const cancel = () => {
  emits('update:visible', false);
};

watch(() => props.visible, newValue => {
  if (newValue) {
    loadTestIndicator();
    checked.value = ['perf', 'stability', 'func'];
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="getTitle"
    :confirmLoading="submitting"
    :width="1180"
    @ok="submit"
    @cancel="cancel">
    <div class="flex space-x-2">
      <Icon icon="icon-tishi1" class="text-blue-icon text-3.5" />
      <span class="flex-1 text-theme-sub-content">{{ t('commonComp.genTestScriptModal.note') }}<span class="text-rule">{{ t('commonComp.genTestScriptModal.noteHighlight') }}</span></span>
    </div>
    <CheckboxGroup
      v-model:value="checked"
      :options="testTypeOpt"
      class="my-3" />
    <div class="flex mt-2">
      <Toggle
        v-if="checked.includes('func')"
        key="func"
        v-model:open="funcOpen"
        class="text-3"
        :title="t('commonComp.genTestScriptModal.functionalTestConfig')">
        <TestForm
          key="func"
          ref="funcRef"
          class="mt-2 pr-5"
          :setType="props.setType"
          :value="funcTestData"
          :type="props.type"
          testType="FUNCTIONAL" />
      </Toggle>
      <Toggle
        v-if="checked.includes('perf')"
        key="perf"
        v-model:open="perfOpen"
        class="!text-3"
        :title="t('commonComp.genTestScriptModal.performanceTestConfig')">
        <TestForm
          key="perf"
          ref="perfRef"
          class="mt-2 pr-5"
          :setType="props.setType"
          :value="perfTestData"
          :type="props.type"
          testType="PERFORMANCE" />
      </Toggle>
      <Toggle
        v-if="checked.includes('stability')"
        key="stability"
        v-model:open="stabilityOpen"
        class="text-3"
        :title="t('commonComp.genTestScriptModal.stabilityTestConfig')">
        <TestForm
          key="stability"
          ref="stabilityRef"
          class="mt-2 pr-5"
          :setType="props.setType"
          :value="stabilityTestData"
          :type="props.type"
          testType="STABILITY" />
      </Toggle>
    </div>
  </Modal>
</template>
<style scoped>
:deep(.toggle-title) {
  @apply text-3;
}
</style>
