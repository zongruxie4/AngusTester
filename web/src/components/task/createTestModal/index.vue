<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, reactive, ref, watch } from 'vue';
import { http, TESTER } from '@xcan-angus/tools';
import { CheckboxGroup, Form, FormItem } from 'ant-design-vue';
import { Icon, Modal, notification, Select, Toggle } from '@xcan-angus/vue-ui';
import { project } from '@/api/tester';

import { FormData } from './PropsType';

const TestForm = defineAsyncComponent(() => import('./testForm.vue'));

interface Props {
  visible: boolean,
  infoText: string,
  type: 'API' | 'SERVICE' | 'SCENARIO';
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  infoText: '',
  type: 'API',
  id: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void,
  (e: 'update:id', val: string | undefined): void,
}>();

const projectInfo = inject('projectInfo', ref({ id: '' }));
const members = ref([]);

const sprintFormRef = ref();
const projectSelectRef = ref(); // 任务项目选择组件ref
const pref = ref();// 性能测试form ref

const sprintData = reactive({
  id: undefined,
  activeKey: ['FUNCTIONAL']
});

const perfForm = ref<FormData>({
  deadlineDate: '',
  priority: 'MEDIUM',
  assigneeId: undefined,
  testType: 'PERFORMANCE'
});

const fref = ref();// 功能测试form ref
const funcForm = ref<FormData>({
  deadlineDate: '',
  priority: 'MEDIUM',
  assigneeId: undefined,
  testType: 'FUNCTIONAL'
});

const sref = ref();// 稳定性测试form ref
const stablityForm = ref<FormData>({
  deadlineDate: '',
  priority: 'MEDIUM',
  assigneeId: undefined,
  testType: 'STABILITY'
});

const toggleOpenConfig = reactive({
  func: true,
  perf: true,
  stability: true
});

const radioList = [
  { label: '功能测试', value: 'FUNCTIONAL' },
  { label: '性能测试', value: 'PERFORMANCE' },
  { label: '稳定性测试', value: 'STABILITY' }];
const confirmLoading = ref<boolean>(false);
const formData = computed(() => {
  const result = [];
  if (sprintData.activeKey.includes('PERFORMANCE')) {
    result.push(perfForm.value);
  }

  if (sprintData.activeKey.includes('FUNCTIONAL')) {
    result.push(funcForm.value);
  }

  if (sprintData.activeKey.includes('STABILITY')) {
    result.push(stablityForm.value);
  }

  return result;
});

const loadMembers = async () => {
  const [error, { data }] = await project.getMemberUser(projectInfo.value.id);
  if (error) {
    return;
  }
  members.value = data || [];
};

const validateTestType = () => {
  if (!sprintData.activeKey.length) {
    // eslint-disable-next-line prefer-promise-reject-errors
    return Promise.reject('请选择测试类型');
  }
  return Promise.resolve();
};

const validate = () => {
  let result = true;
  if (pref.value) {
    if (!pref.value.validate()) {
      toggleOpenConfig.perf = true;
      result = false;
    }
  }
  if (fref.value) {
    if (!fref.value.validate()) {
      toggleOpenConfig.func = true;
      result = false;
    }
  }
  if (sref.value) {
    if (!sref.value.validate()) {
      toggleOpenConfig.stability = true;
      result = false;
    }
  }

  return result;
};

const ok = async () => {
  const isValid = validate();
  sprintFormRef.value.validate();
  if (!isValid) {
    return;
  }

  if (!sprintData.id) {
    return;
  }

  confirmLoading.value = true;
  const [error] = await http.put(okUrl.value, formData.value);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success('生成任务成功');
  cancel();
};

const cancel = () => {
  sprintData.id = undefined;
  sprintData.activeKey = ['FUNCTIONAL'];
  projectSelectRef.value?.clearAll();
  confirmLoading.value = false;
  perfForm.value = {
    deadlineDate: '',
    priority: 'MEDIUM',
    assigneeId: undefined,
    testType: 'PERFORMANCE'
  };
  funcForm.value = {
    deadlineDate: '',
    priority: 'MEDIUM',
    assigneeId: undefined,
    testType: 'FUNCTIONAL'
  };
  stablityForm.value = {
    deadlineDate: '',
    priority: 'MEDIUM',
    assigneeId: undefined,
    testType: 'STABILITY'
  };
  emit('update:visible', false);
  emit('update:id', undefined);
};

const resetFormValidate = () => {
  sprintFormRef.value?.clearValidate();
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      resetFormValidate();
      return;
    }

    setTimeout(() => {
      fref.value?.reset();
      pref.value?.reset();
      sref.value?.reset();
    });
  }, { immediate: true });
  watch(() => projectInfo.value, () => {
    if (!members.value.length && projectInfo.value?.id) {
      loadMembers();
    }
  }, { immediate: true });
});

const okUrl = computed(() => {
  switch (props.type) {
    case 'API':
      return `${TESTER}/apis/${props.id}/test/task/generate?taskSprintId=${sprintData.id}`;
    case 'SERVICE':
      return `${TESTER}/services/${props.id}/test/task/generate?taskSprintId=${sprintData.id}`;
    case 'SCENARIO':
      return `${TESTER}/scenario/${props.id}/test/task/generate?taskSprintId=${sprintData.id}`;
    default:
      return '';
  }
});
</script>
<template>
  <Modal
    class="create-task-modal-container"
    title="生成测试任务"
    :width="1000"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="ok"
    @cancel="cancel">
    <div v-if="!!infoText" class="mt-3 mb-5 text-3 text-theme-content leading-5">
      <Icon class="text-status-warn text-3.5 -mt-1 mr-1" icon="icon-tishi1" />
      <span>{{ props.infoText }}</span>
    </div>

    <Form
      ref="sprintFormRef"
      :model="sprintData"
      class="my-5">
      <FormItem
        required
        label="任务迭代"
        name="id"
        :labelCol="{ style: { lineHeight: '28px' } }"
        class="flex-1">
        <Select
          v-model:value="sprintData.id"
          :disabled="!projectInfo.id"
          :action="`${TESTER}/task/sprint?projectId=${projectInfo.id}`"
          :fieldNames="{ value: 'id', label: 'name' }"
          placeholder="请选择迭代">
          <template #option="record">
            <div class="flex items-center truncate">
              <Icon icon="icon-jihua" class="text-4" />
              <span class="ml-1">{{ record.name }}</span>
            </div>
          </template>
        </Select>
      </FormItem>
      <FormItem
        label="测试类型"
        name="activeKey"
        :rules="{ required: true, validator: validateTestType, message: '请选择测试类型' }">
        <CheckboxGroup v-model:value="sprintData.activeKey" :options="radioList" />
      </FormItem>
    </Form>

    <div class="space-x-5 flex">
      <Toggle
        v-if="sprintData.activeKey.includes('FUNCTIONAL')"
        v-model:open="toggleOpenConfig.func"
        class="w-1/3"
        title="功能测试任务配置">
        <TestForm
          ref="fref"
          key="FUNCTIONAL"
          v-model:value="funcForm"
          :users="members"
          class="flex-1 mt-2" />
      </Toggle>

      <Toggle
        v-if="sprintData.activeKey.includes('PERFORMANCE')"
        v-model:open="toggleOpenConfig.perf"
        class="w-1/3"
        title="性能测试任务配置">
        <TestForm
          ref="pref"
          key="PERFORMANCE"
          v-model:value="perfForm"
          :type="props.type"
          :users="members"
          class="flex-1 mt-2" />
      </Toggle>

      <Toggle
        v-if="sprintData.activeKey.includes('STABILITY')"
        v-model:open="toggleOpenConfig.stability"
        class="w-1/3"
        title="稳定性测试任务配置">
        <TestForm
          ref="sref"
          key="STABILITY"
          v-model:value="stablityForm"
          :type="props.type"
          :users="members"
          class="flex-1 mt-2" />
      </Toggle>
    </div>
  </Modal>
</template>

<style>
.ant-modal-wrap .ant-modal.create-task-modal-container .ant-modal-content .ant-modal-body {
  max-height: 80vh;
  padding-top: 0;
  padding-bottom: 20px;
  overflow-y: auto;
}
</style>

<style scoped>
:deep(.toggle-title) {
  font-size: 12px;
}

:deep(.ant-radio-wrapper) {
  margin-right: 0;
}

:deep(span.ant-radio + *) {
  padding-right: 0;
}
</style>
