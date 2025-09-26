<script setup lang="ts">
import { computed, inject, ref, Ref } from 'vue';
import { Input, Modal, notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { services } from '@/api/tester';

interface Props {
  visible:boolean,
  pid?: string
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  pid: undefined
});
const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', val:boolean):void;
  (e:'ok'):void;
}>();

// eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars, @typescript-eslint/no-empty-function
const addTabPane = inject('addTabPane', (_data:{[key:string]:any}) => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshSidebar = inject('refreshSidebar', () => { });
// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const inputValue = ref<string>();
const confirmLoading = ref(false);
const ok = async () => {
  if (!inputValue.value?.length) {
    return;
  }

  const params = {
    name: inputValue.value,
    projectId: projectId.value
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
  notification.success(t('actions.tips.addSuccess'));
};

const title = computed(() => {
  return t('service.sidebar.addServiceModal.title');
});

const placeholder = computed(() => {
  return t('common.placeholders.searchKeyword');
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
