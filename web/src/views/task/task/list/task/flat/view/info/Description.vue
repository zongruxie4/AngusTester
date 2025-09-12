<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, NoData, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { task } from '@/api/tester';

import { TaskInfo } from '../../../../../../types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const props = withDefaults(defineProps<TaskInfoProps>(), {
  taskId: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  loading: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const richEditorRef = ref();

const openFlag = ref(true);
const editFlag = ref(false);
const content = ref<string>('');

const toEdit = () => {
  openFlag.value = true;
  editFlag.value = true;
  content.value = props?.dataSource?.description || '';
};

const editorChange = (value: string) => {
  content.value = value;
};

const cancel = () => {
  editFlag.value = false;
  content.value = props.dataSource?.description || '';
};

const validateErr = ref(false);
const ok = async () => {
  if (isError()) {
    validateErr.value = true;
    return;
  }
  validateErr.value = false;

  const params = { description: content.value };
  emit('loadingChange', true);
  const [error] = await task.editTaskDescription(props.taskId, params);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  editFlag.value = false;
  emit('change', { id: props.taskId, description: content.value });
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    content.value = newValue?.description || '';
  }, { immediate: true });
});

const isError = () => {
  if (!content.value) {
    return false;
  }
  const length = richEditorRef.value.getLength();
  return length > 6000;
};

</script>

<template>
  <Toggle v-model:open="openFlag">
    <template #title>
      <div class="flex items-center text-3">
        <span>{{ t('task.detailInfo.description.title') }}</span>
        <template v-if="editFlag">
          <Button
            size="small"
            type="link"
            @click="cancel">
            {{ t('task.detailInfo.description.actions.cancel') }}
          </Button>
          <Button
            size="small"
            type="link"
            @click="ok">
            {{ t('task.detailInfo.description.actions.confirm') }}
          </Button>
        </template>
        <Button
          v-else
          type="link"
          class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
          @click="toEdit">
          <Icon icon="icon-shuxie" class="text-3.5" />
        </Button>
      </div>
    </template>

    <template #default>
      <template v-if="editFlag">
        <div class="mb-2.5 mt-2.5 ml-5.5">
          <RichEditor
            ref="richEditorRef"
            :value="content"
            :options="{placeholder: t('task.detailInfo.description.placeholder')}"
            :placeholder="t('task.detailInfo.description.validation.maxLength')"
            @change="editorChange" />
          <div v-show="validateErr" class="text-status-error text-3">
            {{ t('task.detailInfo.description.validation.maxLength') }}
          </div>
        </div>
      </template>

      <div v-if="!editFlag" class="browser-container">
        <RichEditor :value="props.dataSource?.description" mode="view" />
      </div>

      <NoData
        v-if="!editFlag&&!content?.length"
        size="small"
        style="min-height: 160px;" />
    </template>
  </Toggle>
</template>

<style scoped>
.border-none {
  border: none;
}

.browser-container  {
  padding-left: 21px;
  transform: translateY(1px);
}
</style>
