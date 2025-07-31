<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button, Upload, UploadFile } from 'ant-design-vue';
import { Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { utils, upload } from '@xcan-angus/infra';
import { funcCase } from '@/api/tester';

import { CaseInfo } from '../../PropsType';

type AttachmentItem = {
  id: string;
  name: string;
  url: string;
}

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseInfo): void;
  (event: 'update:dataSource', value: CaseInfo): void;
}>();

const MAX_SIZE = 10;

const loading = ref(false);
const attachments = ref<AttachmentItem[]>([]);

const uploadChange = async ({ file }: { file: UploadFile }) => {
  if (file.size! > maxFileSize.value) {
    notification.warning(`文件大小不能超过 ${MAX_SIZE} M`);
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
  const [error] = await funcCase.putAttachment(caseId.value, params);
  loading.value = false;
  if (error) {
    return;
  }

  change();
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

const caseId = computed(() => props.dataSource?.id);
const isEmpty = computed(() => !attachments.value.length);

const maxFileSize = computed(() => {
  return 1024 * 1024 * MAX_SIZE;
});
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">附件</div>
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
              <div class="flex-shrink-0 text-text-link ml-1">继续上传</div>
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
            <div class="flex-shrink-0 text-text-link">选择文件</div>
          </Button>
        </Upload>
        <div class="text-theme-sub-content mt-1">单个文件大小不超过{{ MAX_SIZE }}M，最多上传5个</div>
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
