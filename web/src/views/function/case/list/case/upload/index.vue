<script lang="ts" setup>
import { computed, inject, ref, watch } from 'vue';
import { Icon, Modal, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, RadioGroup, UploadDragger } from 'ant-design-vue';
import { TESTER, enumLoader } from '@xcan-angus/tools';
import { formatBytes } from '@/utils/common';
import { funcCase } from '@/api/tester';

export interface Props{
  visible: boolean;
  downloadTemplate: () => void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void;}>();
const projectInfo = inject('projectInfo', ref({ id: '' }));

const projectId = computed(() => {
  return projectInfo.value?.id;
});

const loading = ref(false);
const strategyWhenDuplicatedOpt = ref<{value: string; label: string}[]>([]);

const formRef = ref();

const formData = ref<{
  file: File|undefined;
  strategyWhenDuplicated: 'COVER'|'IGNORE';
  planId: string|undefined;
}>({
  file: undefined,
  strategyWhenDuplicated: 'COVER',
  planId: undefined
});

const loadEnums = async () => {
  const [error, data] = await enumLoader.load('StrategyWhenDuplicated');
  if (error) {
    return;
  }
  strategyWhenDuplicatedOpt.value = (data || []).map(i => ({ value: i.value, label: i.message }));
};

const handleFile = (fileInfo) => {
  formData.value.file = fileInfo.file;
  formRef.value.validateFields(['file']);
};

const deleteFile = () => {
  formData.value.file = undefined;
};

const validateFile = async () => {
  if (formData.value.file) {
    return Promise.resolve();
  }
  return Promise.reject();
};

const handleDownloadTemplate = () => {
  if (typeof props.downloadTemplate === 'function') {
    props.downloadTemplate();
  }
};

const cancel = () => {
  emits('update:visible', false);
};
const ok = () => {
  formRef.value.validate()
    .then(async () => {
      const formParams = new FormData();
      formParams.append('planId', formData.value.planId);
      formParams.append('strategyWhenDuplicated', formData.value.strategyWhenDuplicated);
      formParams.append('file', formData.value.file);
      loading.value = true;
      const [error] = await funcCase.importCase(formParams);
      loading.value = false;
      if (error) {
        return;
      }
      emits('ok');
      cancel();
    });
};

watch(() => props.visible, newValue => {
  if (!newValue) {
    formData.value.file = undefined;
    formData.value.strategyWhenDuplicated = 'COVER';
  }
  if (!strategyWhenDuplicatedOpt.value.length) {
    loadEnums();
  }
}, {
  immediate: true
});

</script>
<template>
  <Modal
    title="导入用例"
    :visible="props.visible"
    @cancel="cancel"
    @ok="ok">
    <Form
      ref="formRef"
      :model="formData"
      size="small">
      <FormItem
        label="计划"
        name="planId"
        required>
        <Select
          v-model:value="formData.planId"
          :disabled="!projectId"
          :action="`${TESTER}/func/plan?projectId=${projectId}&fullTextSearch=true`"
          :defaultActiveFirstOption="true"
          :lazy="false"
          placeholder="选择计划"
          :fieldNames="{value: 'id', label: 'name'}" />
      </FormItem>
      <FormItem :rules="{message: '请上传文件', validator: validateFile}" name="file">
        <Spin :spinning="loading">
          <UploadDragger
            v-show="!formData.file"
            accept=".xls,.xlsx"
            :multiple="false"
            :showUploadList="false"
            :customRequest="handleFile"
            class="text-3 leading-5">
            <div class="flex flex-col items-center justify-center text-3 leading-5">
              <Icon icon="icon-shangchuan" class="text-5 text-text-link" />
              <div class="mt-1 mb-1.5 text-text-link">选择文件</div>
              <div class="text-theme-sub-content">可直接将文件拖入此区域直接上传，文件大小不超过20M，支持.xls .xlsx类型文件。</div>
            </div>
          </UploadDragger>
        </Spin>
        <div
          v-show="!!formData.file"
          class="px-3.5 border rounded">
          <div class="flex py-2 text-3 leading-3">
            <div :title="formData.file?.name" class="flex-2 truncate">{{ formData.file?.name }}</div>
            <div class="flex-1 text-right">{{ formatBytes(Number(formData.file?.size)) }}</div>
            <div class="flex-1 text-right text-gray-500">
              <Icon
                icon="icon-qingchu"
                class="cursor-pointer ml-2 text-3.5 text-theme-text-hover"
                @click="deleteFile" />
            </div>
          </div>
        </div>
        <div class="text-right">
          <Button
            type="link"
            size="small"
            @click="handleDownloadTemplate">
            <Icon icon="icon-daochu1" class="text-4 cursor-pointer mr-1" />
            用例导入模板
          </Button>
        </div>
      </FormItem>
      <FormItem
        label="遇到重复时的处理策略"
        name="strategyWhenDuplicated"
        required>
        <RadioGroup
          v-model:value="formData.strategyWhenDuplicated"
          :options="strategyWhenDuplicatedOpt">
        </RadioGroup>
      </FormItem>
    </Form>
  </Modal>
</template>
