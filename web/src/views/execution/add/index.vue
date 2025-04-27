<script setup lang="ts">
import { computed, inject, onMounted, ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { Button, Form, FormItem } from 'ant-design-vue';
import { IconRequired, Input, notification, Select, SelectEnum, Spin } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';
import { angusScript } from '@/api/altester';
import ExecSettingForm from '@angus/exec';
import '@angus/exec/style.css';

import { exec } from '@/api/alctrl';

const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const { t } = useI18n();
const router = useRouter();
const route = useRoute();
const type = route.query?.type;
const formState = ref({
  name: '',
  scriptId: undefined,
  scriptType: undefined
});

const scriptInfo = ref();
const pluginType = ref();
const selectScript = async (value:string) => {
  const [error, { data }] = await angusScript.loadDetail(value);
  if (error) {
    return;
  }

  scriptInfo.value = {
    ...data.content,
    type: data.content.type.value
  };
  formState.value.scriptId = data.id;
  formState.value.scriptType = data.type.value;
  infoFormRef.value.clearValidate();
  pluginType.value = data.type;
  if (!formState.value.name) {
    formState.value.name = data.name;
  }
};

const infoFormRef = ref();

const infoRules = ref<Record<string, boolean | undefined>>({
  name: undefined,
  scriptId: undefined
});

const infoFormValidate = (name, status) => {
  infoRules.value[name] = status;
};

const execFormRef = ref();
const execSettingFormRef = ref();

const loading = ref(false);
const saveSetting = async () => {
  let hasErr = true;
  await infoFormRef.value.validate().then().catch((errors) => {
    if (errors?.errorFields?.length) {
      scrollToErrorElement('info-form', { 'info-form': errors });
      hasErr = false;
    }
  });

  const data = await execSettingFormRef.value.isValid();

  if (hasErr) {
    hasErr = data.valid;
  }

  const errors = data.errors;
  const formNames = ['execut-form', 'global-form', 'http-form', 'websocket-form', 'jdbc-form'];
  for (const formName of formNames) {
    if (errors[formName]?.errorFields?.length) {
      if (formName === 'execut-form') {
        execSettingFormRef.value.openExecutParames();
      }

      if (formName === 'global-form') {
        execSettingFormRef.value.openGlobalParames();
      }

      if (formName === 'http-form' || formName === 'websocket-form' || formName === 'jdbc-form') {
        execSettingFormRef.value.openPulginParames();
      }

      scrollToErrorElement(formName, errors);
      break;
    }
  }

  if (!hasErr) {
    return;
  }

  let params:any = {
    name: formState.value.name,
    scriptType: formState.value.scriptType,
    ...execSettingFormRef.value.getData()
  };

  if (!route.params.id) {
    params = {
      ...params,
      scriptId: formState.value.scriptId
    };
  }

  if (type === 'expr') {
    params = {
      ...params,
      trial: true
    };
  }

  loading.value = true;
  const [error] = route.params.id ? await exec.editExecutetByScript(route.params.id as string, params) : await exec.addExecutetByScript(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(route.params.id ? '修改成功' : '添加成功');
  router.push('/execution');
};

const scrollToErrorElement = (formName, errors) => {
  const formElement = document.querySelector(`.${formName}`);
  if (!formElement || !errors[formName]?.errorFields?.length) {
    return;
  }
  const errEleClass = errors[formName].errorFields[0].name.join('-');
  const errorElement = formElement.querySelector(`.${errEleClass}`);
  if (!errorElement) {
    return;
  }
  errorElement.scrollIntoView({
    block: 'center',
    inline: 'start'
  });
};

const scriptParams = { filters: [{ key: 'type', op: 'IN', value: ['TEST_FUNCTIONALITY', 'TEST_PERFORMANCE', 'TEST_STABILITY', 'TEST_CUSTOMIZATION', 'MOCK_DATA'] }] };

const detail = ref();

const getDetail = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error, { data }] = await exec.getDetail(route.params.id as string);
  loading.value = false;
  if (error) {
    return;
  }
  detail.value = data;
  if (detail.value?.scriptType.value === 'MOCK_DATA') {
    detail.value.batchRows = detail.value.task?.mockData?.settings.batchRows || '1';
  }
  const { configuration, plugin, task, scriptId, scriptType } = data;
  scriptInfo.value = {
    type: scriptType.value,
    plugin,
    configuration,
    task
  };
  formState.value.name = data.name;
  formState.value.scriptId = scriptId;
  formState.value.scriptType = scriptType.value;
};

onMounted(async () => {
  if (!route.params.id) {
    return;
  }

  getDetail();
});

const disabledKeys = computed(() => {
  if (['MOCK_DATA'].includes(scriptInfo.value?.type)) {
    return ['TEST_FUNCTIONALITY', 'TEST_PERFORMANCE', 'TEST_STABILITY', 'TEST_CUSTOMIZATION'];
  }
  return ['MOCK_DATA'];
});

const scriptTypeExcludes = (option) => {
  return option.value === 'MOCK_APIS';
};
</script>
<template>
  <Spin
    ref="execFormRef"
    :spinning="loading"
    class="h-full overflow-y-auto py-3.5 px-8 w-full">
    <div
      class="flex h-full text-3 w-full"
      style="max-width:1200px">
      <div class="space-y-5 mr-8 leading-7 text-text-title font-medium info-form">
        <div class="h-7 whitespace-nowrap name"><IconRequired />名称</div>
        <div class="h-7 whitespace-nowrap scriptId"><IconRequired />脚本</div>
        <div class="h-7 whitespace-nowrap scriptType"><IconRequired />类型</div>
        <div class="h-7 whitespace-nowrap"><IconRequired />配置</div>
      </div>
      <div class="flex-1">
        <Form
          ref="infoFormRef"
          :model="formState"
          :colon="false"
          @validate="infoFormValidate">
          <FormItem
            name="name"
            class="max-w-150 pr-5"
            :rules="{ required: true,message:'请输入执行名称'}">
            <Input
              v-model:value="formState.name"
              :maxlength="100"
              :placeholder="t('请输入执行名称，最多100字符')" />
          </FormItem>
          <FormItem
            class="max-w-150 pr-5"
            name="scriptId"
            :rules="{ required: true,message:'请选择执行脚本'}">
            <Select
              v-model:value="formState.scriptId"
              showSearch
              :params="scriptParams"
              :disabled="!!route.params.id"
              :action="`${TESTER}/script/search?projectId=${projectId}`"
              :fieldNames="{label: 'name', value: 'id'}"
              :placeholder="t('请选择脚本')"
              @change="selectScript" />
          </FormItem>
          <FormItem
            name="scriptType"
            class="max-w-150 pr-5 h-12"
            :rules="{ required: true,message:'请选择测试类型'}">
            <SelectEnum
              v-model:value="formState.scriptType"
              enumKey="ScriptType"
              :disabled="!formState.scriptId"
              :excludes="scriptTypeExcludes"
              :disabledKeys="disabledKeys"
              :placeholder="t('请选择测试类型')" />
          </FormItem>
        </Form>
        <ExecSettingForm
          ref="execSettingFormRef"
          :addType="type"
          :scriptType="formState.scriptType"
          :scriptInfo="scriptInfo" />
        <div class="flex pl-3.5 mt-10 pb-8">
          <Button
            size="small"
            type="primary"
            class="mr-5"
            @click="saveSetting">
            保存
          </Button>
          <RouterLink to="/execution">
            <Button size="small">取消</Button>
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
