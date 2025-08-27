<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const openFlag = ref(true);
const editFlag = ref(false);
const content = ref<string>('');

const toEdit = () => {
  openFlag.value = true;
  editFlag.value = true;
};

const editorChange = (value: string) => {
  content.value = value;
};

const cancel = () => {
  editFlag.value = false;
};

const ok = async () => {
  if (content.value.length > 8000) {
    return;
  }

  const params = { description: content.value };
  emit('loadingChange', true);
  const [error] = await task.editTaskDescription(taskId.value, params);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  editFlag.value = false;
  emit('change', { id: taskId.value, description: content.value });
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    content.value = newValue?.description || '';
  }, { immediate: true });
});

const taskId = computed(() => {
  return props.dataSource?.id;
});

const error = computed(() => {
  if (!content.value) {
    return false;
  }

  return content.value.length > 8000;
});

const OPTIONS = {
  menubar: false,
  height: 300
};

const UPLOAD_OPTIONS = {
  bizKey: 'angusTesterTaskAttachments'
};

const LINK_TYPES = ['@', '#', '>'];
</script>

<template>
  <div class="mt-4">
    <div class="flex items-center text-theme-title mb-1.75">
      <span class="font-semibold">{{ t('task.detailInfo.description.title') }}</span>
      <Button
        v-show="!editFlag"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="toEdit">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <AsyncComponent :visible="editFlag">
      <div v-show="editFlag">
        <div>
          <RichEditor
            :value="content"
            :height="300"
            @change="editorChange" />
          <div v-show="error" class="text-status-error">{{ t('task.detailInfo.description.validation.maxLength') }}</div>
        </div>

        <div class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
          <Button size="small" @click="cancel">{{ t('actions.cancel') }}</Button>
          <Button
            size="small"
            type="primary"
            @click="ok">
            {{ t('actions.confirm') }}
          </Button>
        </div>
      </div>
    </AsyncComponent>

    <AsyncComponent :visible="!editFlag">
      <div v-show="!editFlag">
        <RichEditor :value="content" mode="view" />
      </div>

      <NoData
        v-show="!editFlag&&!content?.length"
        size="small"
        class="my-10" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.border-none {
  border: none;
}
</style>
