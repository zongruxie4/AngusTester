<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Colon, IconTask, Modal, notification, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';
import { task } from '@/api/altester';

import { TaskInfo } from '../../../../../../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  visible: boolean;
  taskInfo: TaskInfo;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  visible: false,
  taskInfo: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

const loading = ref(false);
const refTaskIds = ref<string[]>([]);

const cancel = () => {
  emit('update:visible', false);
  reset();
};

const ok = async () => {
  const params = {
    subTaskIds: refTaskIds.value
  };
  loading.value = true;
  const [error] = await task.setSubTask(props.taskInfo?.id, params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('关联子任务成功');
  emit('update:visible', false);
  emit('ok');
  reset();
};

const reset = () => {
  refTaskIds.value = [];
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    refTaskIds.value = props.taskInfo?.subTaskInfos?.map(item => item.id) || [];
  }, { immediate: true });
});

const taskIdExcludes = (data: { id: string }) => {
  return props.taskInfo?.id === data.id;
};
</script>
<template>
  <Modal
    title="关联子任务"
    :centered="true"
    :visible="props.visible"
    :width="680"
    @cancel="cancel"
    @ok="ok">
    <div class="flex items-start">
      <div class="flex items-center flex-shrink-0 mr-1.5 leading-7"><span>关联子任务</span><Colon /></div>
      <Select
        v-model:value="refTaskIds"
        showSearch
        internal
        mode="tags"
        placeholder="请选择子任务"
        class="flex-1"
        :excludes="taskIdExcludes"
        :fieldNames="{ label: 'name', value: 'id' }"
        :action="`${TESTER}/task/search?projectId=${props.projectId}`">
        <template #option="record">
          <div class="flex items-center">
            <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
            <span class="ml-1.5">{{ record.name }}</span>
          </div>
        </template>
      </Select>
    </div>
  </Modal>
</template>
