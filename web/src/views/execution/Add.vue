<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { Button, Form, FormItem } from 'ant-design-vue';
import { IconRequired, Input, Select, Spin } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useExecutionForm } from './composables/useExecutionForm';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import ExecSettingForm from '@/components/ExecSettingForm/index.vue';

const { t } = useI18n();
const route = useRoute();

// Use execution form composable
const {
  projectId,
  formState,
  scriptInfo,
  isLoading,
  scriptParams,
  infoFormRef,
  execSettingFormRef,
  selectScript,
  validateForm,
  saveSettings,
  loadExecutionDetail,
  shouldExcludeScriptType,
  disabledScriptTypeKeys
} = useExecutionForm();

// Get query type for form configuration
const queryType = computed(() => route.query?.type === 'expr' ? 'expr' : undefined);

onMounted(async () => {
  if (route.params.id) {
    await loadExecutionDetail();
  }
});
</script>
<template>
  <Spin
    ref="execFormRef"
    :spinning="isLoading"
    class="h-full overflow-y-auto py-3.5 px-8 w-full">
    <div
      class="flex h-full text-3 w-full"
      style="max-width:1200px">
      <div class="space-y-5 mr-8 leading-7 text-text-title font-medium info-form">
        <div class="h-7 whitespace-nowrap name"><IconRequired />{{ t('execution.add.name') }}</div>
        <div class="h-7 whitespace-nowrap scriptId"><IconRequired />{{ t('execution.add.script') }}</div>
        <div class="h-7 whitespace-nowrap scriptType"><IconRequired />{{ t('common.type') }}</div>
        <div class="h-7 whitespace-nowrap"><IconRequired />{{ t('execution.add.config') }}</div>
      </div>
      <div class="flex-1">
        <Form
          ref="infoFormRef"
          :model="formState"
          :colon="false"
          @validate="validateForm">
          <FormItem
            name="name"
            class="max-w-150 pr-5"
            :rules="{ required: true,message: t('execution.add.enterExecutionNameRequired')}">
            <Input
              v-model:value="formState.name"
              :maxlength="100"
              :placeholder="t('execution.add.enterExecutionName')" />
          </FormItem>
          <FormItem
            class="max-w-150 pr-5"
            name="scriptId"
            :rules="{ required: true,message: t('execution.add.selectExecutionScript')}">
            <Select
              v-model:value="formState.scriptId"
              showSearch
              :params="scriptParams"
              :disabled="!!route.params.id"
              :action="`${TESTER}/script?projectId=${projectId}&fullTextSearch=true`"
              :fieldNames="{label: 'name', value: 'id'}"
              :placeholder="t('execution.add.selectScript')"
              @change="selectScript" />
          </FormItem>
          <FormItem
            name="scriptType"
            class="max-w-150 pr-5 h-12"
            :rules="{ required: true,message: t('execution.add.selectTestTypeRequired')}">
            <SelectEnum
              v-model:value="formState.scriptType"
              enumKey="ScriptType"
              :disabled="!formState.scriptId"
              :excludes="shouldExcludeScriptType"
              :disabledKeys="disabledScriptTypeKeys"
              :placeholder="t('execution.add.selectTestType')" />
          </FormItem>
        </Form>
        <ExecSettingForm
          ref="execSettingFormRef"
          :addType="queryType"
          :scriptType="formState.scriptType || ''"
          :scriptInfo="scriptInfo" />
        <div class="flex pl-3.5 mt-10 pb-8">
          <Button
            size="small"
            type="primary"
            class="mr-5"
            @click="saveSettings">
            {{ t('actions.save') }}
          </Button>
          <RouterLink to="/execution">
            <Button size="small">{{ t('actions.cancel') }}</Button>
          </RouterLink>
        </div>
      </div>
    </div>
  </Spin>
</template>
<style scoped>
@media (max-width: 1200px) {
  .label-calss {
    margin-bottom: 110px !important;
  }
}

.open-params {
  max-height: 1200px;
}

.stop-params {
  max-height: 0;
}
</style>
