<script lang="ts" setup>
import { inject, onMounted, ref, watch } from 'vue';
import { Button, Radio } from 'ant-design-vue';
import { setting } from '@/api/gm';
import { Icon, Input } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { type AgentValue } from './PropsTypes';

interface Props {
  disabledEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabledEdit: false
});
const { t } = useI18n();

const readyState = inject('readyState', ref(-1));
const currentProxyUrl = inject('currentProxyUrl', ref(''));
const currentProxy = inject('currentProxy', ref(''));

const status = ref('fail');
const agentOptions = ref([{
  label: t('service.agent.noProxy.title'),
  value: 'NO_PROXY',
  enabled: false,
  key: 'noProxy',
  url: '',
  disabled: true
}, {
  label: t('service.agent.clientProxy.title'),
  value: 'CLIENT_PROXY',
  enabled: false,
  key: 'clientProxy',
  url: '',
  disabled: true
}, {
  label: t('service.agent.serverProxy.title'),
  value: 'SERVER_PROXY',
  enabled: false,
  key: 'serverProxy',
  url: '',
  disabled: true
}]);

const tipsMap = {
  NO_PROXY: t('service.agent.noProxy.tip'),
  CLIENT_PROXY: t('service.agent.clientProxy.tip'),
  SERVER_PROXY: t('service.agent.serverProxy.tip'),
  CLOUD_PROXY: t('service.agent.cloudProxy.tip')
};
const agent = ref<AgentValue>('NO_PROXY');

const loadProxy = async () => {
  const [error, res = { data: {} }] = await setting.getUserApiProxy();
  if (error) {
    return;
  }
  agentOptions.value = Object.keys(res.data).map(i => {
    return {
      label: res.data[i].name?.message,
      value: res.data[i].name?.value,
      enabled: res.data[i]?.enabled,
      key: i,
      url: res.data[i].url,
      disabled: true
    };
  });
  agent.value = agentOptions.value.find(item => item.enabled)?.value as string || 'NO_PROXY';
};

const changeProxy = async (event) => {
  const value = event.target.value;
  const [error] = await setting.enabledUserApiProxy({ name: value });
  if (error) {
    return;
  }
  const targetProxy = agentOptions.value.find(item => item.value === value);

  currentProxyUrl.value = targetProxy?.url || '';
  currentProxy.value = targetProxy?.value || '';

  loadProxy();
};

const loading = ref(false);

let initClientUrl;

// 编辑客户端 url
const handleEdit = (agent) => {
  initClientUrl = agent.url;
  agent.disabled = false;
};

// 取消编辑客户端 url
const cancelEdit = (agent) => {
  agent.url = initClientUrl;
  agent.disabled = true;
};

// 保存编辑客户端 url
const saveUrl = async (agent) => {
  if (agent.url === initClientUrl) {
    cancelEdit(agent);
    return;
  }
  loading.value = true;
  const [error] = await setting.patchUserApiProxyUrl({ url: agent.url });
  loading.value = false;
  if (error) {
    return;
  }
  if (agent.enabled) {
    currentProxyUrl.value = agent.url;
  }
  agent.disabled = true;
};

watch(() => readyState, (newValue) => {
  if (newValue.value === 1) {
    status.value = 'success';
  } else {
    status.value = 'fail';
  }
}, {
  deep: true,
  immediate: true
});

onMounted(() => {
  loadProxy();
});

</script>
<template>
  <div class="text-3">
    <div
      v-for="(ag) in agentOptions"
      :key="ag.key"
      class="pb-3 border-b border-dashed mb-2">
      <div class="flex items-center">
        <Radio
          :value="ag.value"
          :checked="ag.value === agent"
          @change="changeProxy">
          {{ ag.label }}
        </Radio>
        <div v-show="agent === ag.value && ag.value !== 'NO_PROXY'" class="flex-1 text-right">
          <template v-if="status === 'fail'">
            <Icon icon="icon-zhongzhi1" class="text-execute-yellow" />
            {{ t('service.agent.unconnect') }}
          </template>
          <template v-if="status === 'success'">
            <Icon icon="icon-duihao" class="text-execute-res4" />
            {{ t('service.agent.connectSuccess') }}
          </template>
        </div>
      </div>
      <div class="mt-2 whitespace-normal text-gray-text" v-html="tipsMap[ag.value]"></div>
      <div v-if="ag.value!=='NO_PROXY'" class="mt-3">
        <div class="flex itens-center justify-between">
          <span class="text-black-active">{{ t('service.agent.urlLabel') }}</span>
          <div v-if="ag.value==='CLIENT_PROXY' && !props.disabledEdit" class="flex text-text-link">
            <Icon
              v-if="ag.disabled"
              icon="icon-shuxie"
              @click="handleEdit(ag)" />
            <div v-else>
              <Button
                type="text"
                size="small"
                class="text-3 h-4 border-0 text-text-link hover:text-text-link"
                :disabled="loading"
                @click="cancelEdit(ag)">
                {{ t('common.cancel') }}
              </Button>
              <Button
                type="text"
                size="small"
                class="text-3 h-4 border-0 text-text-link hover:text-text-link"
                :disabled="loading"
                @click="saveUrl(ag)">
                {{ t('actions.save') }}
              </Button>
            </div>
          </div>
        </div>
        <Input
          v-model:value="ag.url"
          :disabled="ag.disabled"
          size="small"
          class="mt-2" />
      </div>
      <template v-if="agent !== 'NO_PROXY'">
        <div v-show="!currentProxyUrl && agent === ag.value && status === 'fail'" class="mt-3 text-status-orange">{{ t('service.agent.configFailTip') }}</div>
        <div v-show="currentProxyUrl && status === 'fail' && agent === ag.value" class="mt-3 text-status-orange">{{ t('service.agent.connectFailTip') }}</div>
      </template>
    </div>
  </div>
</template>
<style scoped>
:deep(.ant-radio-wrapper) {
  @apply mr-0 text-3;
}

:deep(.ant-radio-wrapper > span) {
  @apply font-medium;
}

:deep(.ant-radio-wrapper:not(.ant-radio-wrapper-checked) > span) {
  @apply text-black-active;
}
</style>
