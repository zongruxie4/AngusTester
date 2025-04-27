<script setup lang="ts">
import { computed, inject, ref } from 'vue';
import { Input, Modal, notification } from '@xcan-angus/vue-ui';

import { services } from '@/api/altester';

interface Props {
  visible:boolean,
  pid?: string
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  pid: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', val:boolean):void;
  (e:'ok'):void;
}>();

// eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars, @typescript-eslint/no-empty-function
const addTabPane = inject('addTabPane', (_data:{[key:string]:any}) => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshSidebar = inject('refreshSidebar', () => { });
const projectInfo = inject('projectInfo', ref({ id: '' }));

const inputValue = ref<string>();
const confirmLoading = ref(false);
const ok = async () => {
  if (!inputValue.value?.length) {
    return;
  }

  const params = {
    name: inputValue.value,
    projectId: projectInfo.value?.id
  };

  confirmLoading.value = true;
  const [error, { data }] = await services.addServices(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  const id = data.id;
  const name = params.name;
  addTabPane({ id, name, _id: id + 'group', value: 'group' });
  refreshSidebar();
  emit('ok');
  cancel();
  notification.success('添加成功');
};

const title = computed(() => {
  // return props.targetType === 'PROJECT' ? '添加项目' : '添加服务';
  return '添加服务';
});

const placeholder = computed(() => {
  // return props.targetType === 'PROJECT' ? '请输入项目名称' : '请输入服务名称';
  return '请输入服务名称';
});

const cancel = () => {
  inputValue.value = undefined;
  emit('update:visible', false);
};
</script>
<template>
  <Modal
    :title="title"
    :visible="props.visible"
    :confirmLoading="confirmLoading"
    @ok="ok"
    @cancel="cancel">
    <Input
      v-model:value="inputValue"
      :allowClear="false"
      :placeholder="placeholder"
      :maxLength="100"
      trim />
  </Modal>
</template>
