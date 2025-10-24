<script lang="ts" setup>
import { inject, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Select } from '@xcan-angus/vue-ui';
import { useProxyOptions, useProxyStatus } from './composables';
import { ProxyType } from './types';

const { t } = useI18n();

// Props & Injections
const readyState = inject('readyState', ref(-1));
const currentProxyUrl = inject('currentProxyUrl', ref(''));
const currentProxy = inject('currentProxy', ref(''));
const proxyOptObj = inject('proxyOptObj', ref());

// Use composables
const {
  proxyOptions: agentOptions,
  selectedProxy: agent,
  loadProxyConfiguration: loadProxy,
  handleProxyChange,
  proxyTipsMap
} = useProxyOptions(readyState, currentProxyUrl, currentProxy);

const {
  connectionStatus: status,
  statusMessages,
  statusIcons,
  statusClasses,
  isProxyConfiguredButNotConnected,
  isProxyConfiguredAndConnectionFailed
} = useProxyStatus(readyState, currentProxy, currentProxyUrl);

// Override loadProxy to handle injected proxyOptObj
const loadProxyWithInjectedData = async () => {
  if (proxyOptObj.value) {
    agentOptions.value = Object.keys(proxyOptObj.value).map(i => {
      return {
        label: proxyOptObj.value[i].name?.message,
        value: proxyOptObj.value[i].name?.value,
        enabled: proxyOptObj.value[i]?.enabled,
        key: i,
        url: proxyOptObj.value[i].url,
        disabled: true
      };
    });
    agent.value = agentOptions.value.find(item => item.enabled)?.value as string || ProxyType.NO_PROXY;
  } else {
    await loadProxy();
  }
};

onMounted(() => {
  loadProxyWithInjectedData();
});
</script>
<template>
  <div class="text-3">
    <div class="flex items-center space-x-2">
      <div class="font-semibold">{{ t('proxy.proxyConfig') }}</div>
      <div v-show="currentProxy !== ProxyType.NO_PROXY" class="flex items-center">
        <template v-if="status === 'fail'">
          <Icon :icon="statusIcons.fail" :class="statusClasses.fail" />
          {{ statusMessages.notConnected }}
        </template>
        <template v-if="status === 'success'">
          <Icon :icon="statusIcons.success" :class="statusClasses.success" />
          {{ statusMessages.connected }}
        </template>
      </div>
    </div>
    <div class="flex mt-2 items-start space-x-2">
      <Select
        :value="currentProxy"
        class="w-25"
        size="small"
        :options="agentOptions"
        @change="handleProxyChange" />
      <div class="whitespace-normal text-gray-text" v-html="proxyTipsMap[currentProxy]"></div>
    </div>
    <template v-if="agent !== ProxyType.NO_PROXY">
      <div
        v-show="isProxyConfiguredButNotConnected"
        class="mt-3"
        :class="statusClasses.warning">
        {{ statusMessages.serverProxyNotConfigured }}
      </div>
      <div
        v-show="isProxyConfiguredAndConnectionFailed"
        class="mt-3"
        :class="statusClasses.warning">
        {{ statusMessages.proxyConnectionFailed }}
      </div>
    </template>
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
