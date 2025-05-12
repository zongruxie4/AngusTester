<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Modal, notification, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';
import { task } from '@/api/tester';

interface Props {
  projectId: string;
  visible: boolean;
  taskIds: string;
  sprintId?: string;
  taskName?: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: false,
  taskIds: undefined,
  sprintId: undefined,
  taskName: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: string, ids:string[]): void;
}>();

const confirmLoading = ref(false);
const selectedId = ref<string>();
const defaultOptions = {
  name: '产品Backlog',
  id: ''
};

const cancel = () => {
  emit('update:visible', false);
};

const confirm = async () => {
  const currentSprintId = selectedId.value;

  const prevSprintId = props.sprintId;
  if (currentSprintId === prevSprintId) {
    cancel();
    return;
  }

  confirmLoading.value = true;
  const taskIds = Array.isArray(props.taskIds) ? props.taskIds : [props.taskIds];
  const params = {
    taskIds,
    targetSprintId: currentSprintId || undefined
  };
  const [error] = await task.moveTask(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  emit('ok', currentSprintId, params.taskIds);
  cancel();

  if (props.taskName) {
    notification.success('任务移动成功');
  } else {
    notification.success(`选中的 ${props.taskIds?.length} 条任务全部移动成功`);
  }
};

onMounted(() => {
  watch(() => props.sprintId, (newValue) => {
    selectedId.value = newValue;
  }, { immediate: true });
});

const format = (data) => {
  return { ...data, disabled: data.id === props.sprintId };
};

// const okButtonProps = computed(() => {
//   return {
//     disabled: !selectedId.value
//   };
// });
</script>
<template>
  <Modal
    title="移动任务"
    :visible="props.visible"
    :width="500"
    :confirmLoading="confirmLoading"
    @cancel="cancel"
    @ok="confirm">
    <div class="flex items-center">
      <div class="mr-2">选择迭代</div>
      <Select
        v-model:value="selectedId"
        :action="`${TESTER}/task/sprint/search?projectId=${props.projectId}`"
        :fieldNames="{ value: 'id', label: 'name' }"
        :format="format"
        :additionalOption="defaultOptions"
        showSearch
        placeholder="选择或查询迭代"
        class="flex-1" />
    </div>
  </Modal>
</template>
