<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button, Upload, UploadFile } from 'ant-design-vue';
import { Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { upload, utils } from '@xcan-angus/infra';
import { task } from '@/api/tester';

import { TaskInfo } from '../../types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const { t } = useI18n();

type AttachmentItem = {
  id: string;
  name: string;
  url: string;
}

const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

const MAX_SIZE = 10;

const loading = ref(false);
const attachments = ref<AttachmentItem[]>([]);

const uploadChange = async ({ file }: { file: UploadFile }) => {
  if (file.size! > maxFileSize.value) {
    notification.warning(t('backlog.info.attachment.messages.fileSizeExceeded', { maxSize: MAX_SIZE }));
    return;
  }

  loading.value = true;
  const [error, res] = await upload(file.originFileObj!, { bizKey: 'angusTesterTaskAttachments' });
  if (error) {
    loading.value = false;
    return;
  }

  const data = res?.data?.[0];
  if (!data) {
    loading.value = false;
    return;
  }

  const attachmentList = attachments.value.filter(item => item.id !== data.id).map(item => {
    return {
      name: item.name,
      url: item.url
    };
  });

  attachmentList.push({
    name: data.name,
    url: data.url
  });

  updateAttachments(attachmentList);
};

const toDelete = async (data: AttachmentItem) => {
  const attachmentList = attachments.value.filter(item => item.id !== data.id).map(item => {
    return {
      name: item.name,
      url: item.url
    };
  });

  updateAttachments(attachmentList);
};

const updateAttachments = async (data:{name:string;url:string}[]) => {
  const params = {
    attachments: data
  };
  loading.value = true;
  const [error] = await task.editTaskAttachment(taskId.value, params);
  loading.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, attachments: params.attachments });
};

const customRequest = () => {
  return false;
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      return;
    }

    attachments.value = (newValue.attachments || []).map(item => {
      return {
        id: utils.uuid(),
        name: item.name,
        url: item.url
      };
    });
  }, { immediate: true });
});

const taskId = computed(() => props.dataSource?.id);
const isEmpty = computed(() => !attachments.value.length);

const maxFileSize = computed(() => {
  return 1024 * 1024 * MAX_SIZE;
});
</script>

<template>
  <div class="h-full text-3 leading-5 px-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('backlog.info.attachment.title') }}
    </div>
    <Spin
      :spinning="loading"
      :class="{ empty: isEmpty }"
      class="upload-container w-full px-3 py-2.5 leading-5 text-3 rounded border border-dashed">
      <template v-if="!isEmpty">
        <div
          v-for="item in attachments"
          :key="item.id"
          class="leading-4 mb-2 last:mb-0 flex items-center justify-between overflow-hidden">
          <a
            class="flex-1 flex-nowrap truncate"
            :download="item.name"
            :href="item.url">{{ item.name }}</a>
          <Icon
            icon="icon-qingchu"
            class="text-3.5 flex-shrink-0 cursor-pointer text-theme-text-hover"
            @click="toDelete(item)">
          </icon>
        </div>

        <div v-if="attachments.length < 5" class="upload-action flex justify-center h-5">
          <Upload
            :maxCount="5"
            :showUploadList="false"
            :customRequest="customRequest"
            @change="uploadChange">
            <Button
              size="small"
              type="link"
              class="flex items-center h-auto leading-4.5 p-0">
              <Icon icon="icon-shangchuan" class="text-3.5 flex-shrink-0 text-text-link" />
              <div class="flex-shrink-0 text-text-link ml-1">{{ t('backlog.info.attachment.continueUpload') }}</div>
            </Button>
          </Upload>
        </div>
      </template>

      <template v-else>
        <Upload
          :maxCount="5"
          :showUploadList="false"
          :customRequest="customRequest"
          @change="uploadChange">
          <Button
            size="small"
            type="link"
            class="flex flex-col items-center justify-center h-auto leading-5 p-0">
            <Icon icon="icon-shangchuan" class="text-5 flex-shrink-0 text-text-link" />
            <div class="flex-shrink-0 text-text-link">
              {{ t('backlog.info.attachment.selectFile') }}
            </div>
          </Button>
        </Upload>
        <div class="text-theme-sub-content mt-1">
          {{ t('backlog.info.attachment.fileSizeLimit', { maxSize: MAX_SIZE }) }}
        </div>
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.upload-container {
  border-color: var(--border-text-box);
  background-color: #fafafa;
}

.empty.upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 8px;
  padding-bottom: 8px;
}

.upload-action {
  visibility: visible;
}

.upload-container:hover .upload-action {
  visibility: visible;
}

:deep(.ant-upload.ant-upload-select) {
  width: 100%;
}
</style>
