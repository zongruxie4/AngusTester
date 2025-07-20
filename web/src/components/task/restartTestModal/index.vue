<script lang="ts" setup>
import { ref } from 'vue';
import { Modal, notification } from '@xcan-angus/vue-ui';
import { apis, scenario, services } from 'src/api/tester';

interface Props {
  visible: boolean;
  content: string;
  type: 'API' | 'SERVICE' | 'SCENARIO';
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  content: undefined,
  type: 'SERVICE',
  id: undefined
});

const OK_API_MAP = {
  API: apis.restartTestTask,
  SERVICE: services.resetTest,
  SCENARIO: scenario.reStart
};

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

// const sprintFormRef = ref();

const loading = ref(false);

// const sprintData = reactive({
//   id: undefined
// });

// const resetSprintData = () => {
//   sprintData.id = undefined;
//   if (typeof sprintFormRef.value?.clearValidate === 'function') {
//     sprintFormRef.value.clearValidate();
//   }
// };

const closeModal = () => {
  emits('update:visible', false);
};

const confirm = async () => {
  loading.value = true;
  const [error] = await OK_API_MAP[props.type](props.id);
  loading.value = false;
  if (error) {
    return;
  }

  emits('ok');
  emits('update:visible', false);
  notification.success('重新开始测试设置成功');
};

// watch(() => props.visible, () => {
//   resetSprintData();
// });
</script>
<template>
  <Modal
    title="重新开始测试"
    :width="580"
    :visible="props.visible"
    :confirmLoading="loading"
    @ok="confirm"
    @cancel="closeModal">
    <div class="mb-5">{{ props.content }}</div>
    <!-- <Form ref="sprintFormRef" :model="sprintData">
      <FormItem
        required
        label="任务迭代"
        name="id"
        :labelCol="{ style: { lineHeight: '28px' } }"
        class="w-full">
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
    </Form> -->
  </Modal>
</template>
