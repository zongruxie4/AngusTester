<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Spin, Hints, Input, AsyncComponent, Select } from '@xcan-angus/vue-ui';
import { RadioButton, RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';

const { t } = useI18n();

const activeType = ref<'upload' | 'generated'>('upload');
const activeSource = ref('file');

const SelectFileModal = defineAsyncComponent(() => import('./SelectFileModal.vue'));

const sourceOpt = [
  { value: 'file', label: t('jmsPlugin.parameterize.options.file') }, { value: 'datasource', label: t('jmsPlugin.parameterize.options.dataSource') }
];

const selectFileVisible = ref(false);
const selectFile = () => {
  selectFileVisible.value = true;
};
const confirmFile = () => {
};

</script>

<template>
  <Spin class="flex flex-col">
    <Hints :text="t('jmsPlugin.parameterize.hints')" />
    <RadioGroup
      v-model:value="activeType"
      size="small"
      class="mt-3"
      buttonStyle="solid">
      <RadioButton value="upload">{{ t('actions.upload') }}</RadioButton>
      <RadioButton value="generated">{{ t('jmsPlugin.parameterize.generated') }}</RadioButton>
    </RadioGroup>
    <template v-if="activeType === 'upload'"></template>
    <template v-if="activeType === 'generated'">
      <div class="mt-4">
        <div>{{ t('common.source') }}</div>
        <RadioGroup
          v-model:value="activeSource"
          :options="sourceOpt" />
      </div>
      <div v-if="activeSource === 'file'" class="mt-3">
        <div>{{ t('jmsPlugin.parameterize.selectFile') }}</div>
        <Input readonly>
          <template #addonAfter>
            <span class="text-3 cursor-pointer text-theme-special" @click="selectFile">{{ t('jmsPlugin.parameterize.select') }}</span>
          </template>
        </Input>
      </div>
      <div v-if="activeSource === 'datasource'" class="mt-3">
        <div>{{ t('jmsPlugin.parameterize.dataSource') }}</div>
        <Select
          :action="`${TESTER}/mock/datasource`"
          :fieldName="{value: 'id', label: 'database'}" />
      </div>
    </template>
    <AsyncComponent :visible="selectFileVisible">
      <SelectFileModal
        v-model:visible="selectFileVisible"
        @ok="confirmFile" />
    </AsyncComponent>
  </Spin>
</template>
