<script lang="ts" setup>
import { reactive, ref, watch } from 'vue';
import { Modal, notification } from '@xcan-angus/vue-ui';
import { CheckboxGroup, Form, FormItem } from 'ant-design-vue';

import { apis, scenario, services } from '@/api/altester';

interface Props {
  visible: boolean;
  id?: string;
  type: 'API' | 'SCENARIO' | 'SERVICE'
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  visible: false,
  type: undefined
});

const emits = defineEmits<{(e: 'update:visible', value: boolean) }>();

const OK_API_MAP = {
  SERVICE: services.deleteTest,
  API: apis.deleteTest,
  SCENARIO: scenario.deleteTest
};

const validated = ref(false);
const testTypes = [
  {
    label: '功能测试',
    value: 'FUNCTIONAL'
  },
  {
    label: '性能测试',
    value: 'PERFORMANCE'
  },
  {
    label: '稳定性测试',
    value: 'STABILITY'
  }
];

const sprintFormRef = ref();
const sprintData = reactive({
  testTypes: ['FUNCTIONAL', 'PERFORMANCE', 'STABILITY']
});

const resetPlanData = () => {
  if (typeof sprintFormRef.value?.clearValidate === 'function') {
    sprintFormRef.value.clearValidate();
  }
  sprintData.testTypes = ['FUNCTIONAL', 'PERFORMANCE', 'STABILITY'];
};

const validateTestType = async () => {
  if (sprintData.testTypes.length) {
    return Promise.resolve();
  }

  // eslint-disable-next-line prefer-promise-reject-errors
  return Promise.reject();
};

const loading = ref(false);
const handleDel = async () => {
  sprintFormRef.value.validate()
    .then(async () => {
      loading.value = true;
      const [error] = await OK_API_MAP[props.type](props.id, sprintData.testTypes);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('删除测试任务成功');
      emits('update:visible', false);
    });
};

const handleClose = () => {
  emits('update:visible', false);
};

watch(() => props.visible, () => {
  validated.value = false;
  resetPlanData();
}, {
  immediate: true
});

</script>
<template>
  <Modal
    title="删除测试任务"
    :width="580"
    :confirmLoading="loading"
    :visible="props.visible"
    @cancel="handleClose"
    @ok="handleDel">
    <div class="mb-2">
      该操作会彻底删除任务，确认是否继续？
    </div>
    <Form ref="sprintFormRef" :model="sprintData">
      <FormItem
        :rules="{ validator: validateTestType, message: '请选择测试类型', required: true }"
        label="选择删除测试类型"
        class="w-full"
        name="testTypes">
        <CheckboxGroup v-model:value="sprintData.testTypes" :options="testTypes"></CheckboxGroup>
      </FormItem>
    </Form>
  </Modal>
</template>
