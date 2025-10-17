<script setup lang="ts">
import { inject, onMounted, ref, Ref } from 'vue';
import { Button, Popover, Radio, RadioGroup, UploadDragger } from 'ant-design-vue';
import { Icon, Input, notification, Spin, Validate } from '@xcan-angus/vue-ui';
import postmanToOpenApi from '@xcan-angus/postman-to-openapi';
import { useI18n } from 'vue-i18n';
import { services } from '@/api/tester';
import { formatBytes } from '@/utils/common';
import { StrategyWhenDuplicated, ApiImportSource } from '@/enums/enums';
import { UploadRequestOption } from 'ant-design-vue/lib/vc-upload/interface';

import SelectEnum from '@/components/enum/SelectEnum.vue';

interface Props {
  serviceId?: string;
  serviceName?: string;
  source?:'introduce' | 'global' | 'services';
}

interface UploadOption extends UploadRequestOption {
  file: File;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  serviceId: undefined,
  serviceName: undefined,
  source: 'introduce'
});
const { t } = useI18n();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void,
  (e: 'ok', value: {id: string, key: string}): void,
  (e: 'close'): void
}>();

const isLoading = ref(false);
const strategyWhenDuplicated = ref<StrategyWhenDuplicated>(StrategyWhenDuplicated.COVER);
const deleteWhenNotExisted = ref(false);
const projectServiceName = ref<string>();
const nameErrorMsg = ref<string>();
const importSource = ref<ApiImportSource>(ApiImportSource.OPENAPI);
const uploadErrorMsg = ref<string>();
const progressing = ref(false);
const file = ref<File>();
const originFile = ref<File>();

const importSourceChange = (value:ApiImportSource.POSTMAN|ApiImportSource.OPENAPI) => {
  if (value === ApiImportSource.POSTMAN && originFile.value && originFile.value.name === file.value?.name) {
    toOpenapi(originFile.value);
  } else {
    file.value = originFile.value;
    uploadErrorMsg.value = undefined;
  }
};

const saveModalData = async () => {
  if (isLoading.value) {
    return;
  }

  if (!importSource.value) {
    notification.warning(t('service.importForm.sourceRule'));
    return;
  }

  if (!file.value) {
    uploadErrorMsg.value = t('service.importForm.fileRule_required');
    return;
  }

  if (!props.serviceId && !projectServiceName.value) {
    nameErrorMsg.value = t('service.importForm.nameRule');
    return;
  }

  const obj = new FormData();

  obj.append('file', file.value);
  obj.append('strategyWhenDuplicated', strategyWhenDuplicated.value);
  obj.append('deleteWhenNotExisted', deleteWhenNotExisted.value.toString());
  obj.append('importSource', importSource.value);
  obj.append('projectId', projectId.value);
  if (props.serviceId) {
    obj.append('serviceId', props.serviceId);
  } else {
    obj.append('serviceName', projectServiceName.value || '');
  }
  isLoading.value = true;
  const [error, resp] = await services.localImport(obj);
  isLoading.value = false;
  if (error) {
    return;
  }

  resetModalData();
  emits('ok', resp.data || {});
  notification.success(t('actions.tips.importSuccess'));
};

const customRequest = (info: UploadOption) => {
  uploadErrorMsg.value = undefined;
  const _file = info.file;
  if (info.file.type === 'application/json' && importSource.value === ApiImportSource.POSTMAN) {
    toOpenapi(_file);
    return;
  }

  file.value = _file;
  originFile.value = _file;
  progressing.value = false;
};

const toOpenapi = (_file:File) => {
  progressing.value = true;
  const reader = new FileReader();
  reader.onload = () => {
    try {
      const fileContent = reader.result as string;
      const openapiDoc = postmanToOpenApi(fileContent);
      const yamlFileName = _file.name.replace(/\.json$/, '') + '.yml';
      const yamlFile = new File([openapiDoc], yamlFileName, { type: 'text/yaml' });
      file.value = yamlFile;
      originFile.value = _file;
      progressing.value = false;
    } catch (error) {
      progressing.value = false;
      file.value = _file;
      originFile.value = _file;
      uploadErrorMsg.value = t('service.importForm.fileRule');
    }
  };
  reader.readAsText(_file);
};

const deleteFile = () => {
  file.value = undefined;
  originFile.value = undefined;
  uploadErrorMsg.value = undefined;
};

const resetModalData = () => {
  strategyWhenDuplicated.value = StrategyWhenDuplicated.COVER;
  deleteWhenNotExisted.value = false;
  projectServiceName.value = undefined;
  nameErrorMsg.value = undefined;
  file.value = undefined;
  originFile.value = undefined;
  uploadErrorMsg.value = undefined;
  importSource.value = ApiImportSource.OPENAPI;
};

const closeModal = () => {
  resetModalData();
  emits('close');
};

const serviceNameChange = () => {
  nameErrorMsg.value = undefined;
};

const sourceExcludes = ({ value }) => {
  return [ApiImportSource.ANGUS].includes(value);
};

onMounted(() => {
  projectServiceName.value = props.serviceName;
});
</script>
<template>
  <div class="text-text-content text-3 flex flex-col w-full">
    <Spin :spinning="isLoading">
      <div v-if="!props.serviceId && props.source !== 'introduce'" class="mb-5 text-3">
        <div class="mb-2 text-black-active leading-3">{{ t('service.importForm.nameLabel') }}</div>
        <Validate :text="nameErrorMsg">
          <Input
            v-model:value="projectServiceName"
            allowClear
            :error="!!nameErrorMsg"
            :maxlength="100"
            :placeholder="t('common.placeholders.searchKeyword')"
            @change="serviceNameChange" />
        </Validate>
      </div>
      <div class="leading-3">{{ t('service.importForm.sourceLabel') }}</div>
      <SelectEnum
        v-model:value="importSource"
        class="w-full mt-2 mb-5"
        size="small"
        :excludes="sourceExcludes"
        enumKey="ApiImportSource"
        defaultActiveFirstOption
        @change="importSourceChange" />
      <div :class="{'mb-5':props.source == 'services'}">
        <Spin :spinning="progressing">
          <UploadDragger
            v-show="!file"
            :class="{'error-border':!!uploadErrorMsg}"
            :multiple="false"
            :showUploadList="false"
            :customRequest="customRequest"
            class="text-3 leading-5"
            accept=".zip,.rar,.7z,.gz,.tar,.bz2,.xz,.lzma,.json,.yaml,.yml">
            <div class="flex flex-col items-center justify-center text-3 leading-5">
              <Icon icon="icon-shangchuan" class="text-5 text-text-link" />
              <div class="mt-1 mb-1.5 text-text-link">{{ t('service.importForm.fileLabel') }}</div>
              <div class="text-theme-sub-content">{{ t('service.importForm.fileTip') }}</div>
            </div>
          </UploadDragger>
        </Spin>
        <Validate :text="uploadErrorMsg">
          <div
            v-show="!!originFile"
            :class="{'border-status-error':!!uploadErrorMsg}"
            class="px-3.5 border rounded">
            <div class="flex py-2 text-3 leading-3">
              <Popover placement="top">
                <template #content>
                  {{ originFile?.name }}
                </template>
                <div class="flex-2 truncate">{{ originFile?.name }}</div>
              </Popover>
              <div class="flex-1 text-right">{{ formatBytes(Number(originFile?.size)) }}</div>
              <div class="flex-1 text-right text-gray-500">
                <Icon
                  icon="icon-qingchu"
                  class="cursor-pointer ml-2 text-3.5 text-theme-text-hover"
                  @click="deleteFile" />
              </div>
            </div>
          </div>
        </Validate>
      </div>

      <template v-if="props.source == 'services'">
        <div class="space-y-0.5 mb-5 leading-5">
          <div>{{ t('service.importForm.strategyWhenDuplicated') }}</div>
          <RadioGroup v-model:value="strategyWhenDuplicated">
            <Radio value="COVER">{{ t('service.importForm.strategy_cover') }}</Radio>
            <Radio value="IGNORE">{{ t('service.importForm.strategy_ignore') }}</Radio>
          </RadioGroup>
        </div>

        <div class="space-y-0.5 leading-5">
          <div>{{ t('service.importForm.deleteWhenNotExisted') }}</div>
          <RadioGroup v-model:value="deleteWhenNotExisted">
            <Radio :value="true">{{ t('service.importForm.delete_y') }}</Radio>
            <Radio :value="false">{{ t('service.importForm.delete_n') }}</Radio>
          </RadioGroup>
        </div>
      </template>
    </Spin>
    <div class="flex mt-5" :class="{'justify-end':props.source !== 'introduce'}">
      <template v-if="['global','services'].includes(props.source)">
        <Button
          size="small"
          class="mr-5"
          @click="closeModal">
          {{ t('actions.cancel') }}
        </Button>
      </template>
      <Button
        type="primary"
        size="small"
        :loading="isLoading || progressing"
        @click="saveModalData">
        {{ t('actions.confirm') }}
      </Button>
    </div>
  </div>
</template>
<style scoped>
:deep(.ant-upload.ant-upload-drag.error-border) {
  @apply border-border-error;
}
</style>
