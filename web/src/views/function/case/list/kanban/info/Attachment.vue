<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button, Upload, UploadFile } from 'ant-design-vue';
import { Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { utils, upload } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { funcCase } from '@/api/tester';

import { CaseDetail } from '@/views/function/types';
import { CaseInfoEditProps } from '@/views/function/case/list/types';
import { AttachmentInfo } from '@/types/types';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseDetail): void;
  (event: 'update:dataSource', value: CaseDetail): void;
}>();

const MAX_SIZE = 10;

const loading = ref(false);
const attachments = ref<AttachmentInfo[]>([]);

const uploadChange = async ({ file }: { file: UploadFile }) => {
  if (file.size! > maxFileSize.value) {
    notification.warning(t('functionCase.kanbanView.infoAttachment.fileSizeLimit', { size: MAX_SIZE }));
    return;
  }

  loading.value = true;
  const [error, res] = await upload(file.originFileObj!, { bizKey: 'angusTesterCaseAttachments' });
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

  await updateAttachments(attachmentList);
};

const toDelete = async (data: AttachmentInfo) => {
  const attachmentList = attachments.value.filter(item => item.id !== data.id).map(item => {
    return {
      name: item.name,
      url: item.url
    };
  });

  await updateAttachments(attachmentList);
};

const updateAttachments = async (data:{name:string;url:string}[]) => {
  const params = {
    attachments: data
  };
  loading.value = true;
  const [error] = await funcCase.putAttachment(caseId.value, params);
  loading.value = false;
  if (error) {
    return;
  }

  await change();
};

const customRequest = () => {
  return false;
};

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

const change = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  loadingChange(true);
  const [error, res] = await funcCase.getCaseDetail(id);
  loadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  emit('change', data);
  emit('update:dataSource', data);
};

const caseId = computed(() => props.dataSource?.id);
const isEmpty = computed(() => !attachments.value.length);

const maxFileSize = computed(() => {
  return 1024 * 1024 * MAX_SIZE;
});

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
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('functionCase.kanbanView.infoAttachment.title') }}
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
            v-if="props.canEdit"
            icon="icon-qingchu"
            class="text-3.5 flex-shrink-0 cursor-pointer text-theme-text-hover"
            @click="toDelete(item)">
          </icon>
        </div>

        <div v-if="props.canEdit&&attachments.length < 5" class="upload-action flex justify-center h-5">
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
              <div class="flex-shrink-0 text-text-link ml-1">{{ t('functionCase.kanbanView.infoAttachment.continueUpload') }}</div>
            </Button>
          </Upload>
        </div>
      </template>

      <template v-else-if="props.canEdit">
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
            <div class="flex-shrink-0 text-text-link">{{ t('functionCase.kanbanView.infoAttachment.selectFile') }}</div>
          </Button>
        </Upload>
        <div class="text-theme-sub-content mt-1">{{ t('functionCase.kanbanView.infoAttachment.fileSizeTip', { size: MAX_SIZE }) }}</div>
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
