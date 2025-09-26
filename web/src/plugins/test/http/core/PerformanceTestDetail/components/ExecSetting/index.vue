<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Button } from 'ant-design-vue';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { exec } from '@/api/tester';

import ExecSettingForm from '../../../ExecSettingForm/index.vue';
import Spin from '../../../Spin/index.vue';

const router = useRouter();

interface Props {
  execId: string;
  scriptInfo: Record<string, any>;
  loading:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  execId: '',
  scriptInfo: undefined,
  loading: false
});

const { t } = useI18n();

const emit = defineEmits<{(e: 'update:loading', value: boolean): void
}>();

const execSettingFormRef = ref();

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

const saveSetting = async () => {
  let hasErr = true;
  const data = await execSettingFormRef.value.isValid();
  hasErr = data.valid;
  const errors = data.errors;
  const formNames = ['execut-form', 'global-form', 'http-form', 'websocket-form', 'jdbc-form'];
  for (const formName of formNames) {
    if (errors[formName]?.errorFields?.length) {
      if (formName === 'execut-form') {
        execSettingFormRef.value.openExecutParames();
      }

      if (formName === 'global-form') {
        execSettingFormRef.value.openGlobalParames(true);
      }

      if (formName === 'http-form' || formName === 'websocket-form' || formName === 'jdbc-form') {
        execSettingFormRef.value.openPulginParames(true);
      }

      scrollToErrorElement(formName, errors);
      break;
    }
  }

  if (!hasErr) {
    return;
  }

  const params = {
    ...execSettingFormRef.value.getData()
  };

  emit('update:loading', true);
  const [error] = await exec.putExecScriptConfig(props.execId, params.value);
  emit('update:loading', false);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.modifySuccess'));
  router.push('/execution');
};
</script>
<template>
  <Spin
    ref="exexFormRef"
    :spinning="loading"
    class="h-full overflow-y-auto py-3.5 px-8 w-full">
    <div style="max-width: 1440px;">
      <ExecSettingForm
        ref="execSettingFormRef"
        :scriptId="props.scriptInfo.id"
        :scriptInfo="props.scriptInfo" />
      <div class="flex pl-3.5 mt-10 pb-8">
        <Button
          size="small"
          type="primary"
          class="mr-5"
          @click="saveSetting">
          {{ t('actions.save') }}
        </Button>
        <RouterLink to="/execution">
          <Button size="small">{{ t('common.cancel') }}</Button>
        </RouterLink>
      </div>
    </div>
  </Spin>
</template>
