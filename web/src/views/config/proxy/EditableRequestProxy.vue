<script lang="ts" setup>
import { inject, onMounted, ref } from 'vue';
import { Button, Radio, RadioChangeEvent } from 'ant-design-vue';
import { Icon, Input } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { ProxyType } from '@/views/config/proxy/types';
import { useProxyOptions, useProxyStatus } from './composables';

interface Props {
  disabledEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabledEdit: false
});
const { t } = useI18n();

// Injected Dependencies
const readyState = inject('readyState', ref(-1));
const currentProxyUrl = inject('currentProxyUrl', ref(''));
const currentProxy = inject('currentProxy', ref(''));

// Use composables
const {
  isLoading,
  proxyOptions,
  selectedProxy,
  connectionStatus,
  proxyTipsMap,
  loadProxyConfiguration,
  handleProxyChange: changeProxy,
  enableUrlEditing,
  cancelUrlEditing,
  saveProxyUrl: saveUrl
} = useProxyOptions(readyState, currentProxyUrl, currentProxy);

const {
  statusMessages,
  statusIcons,
  statusClasses,
  isProxyConfiguredButNotConnected,
  isProxyConfiguredAndConnectionFailed
} = useProxyStatus(readyState, currentProxy, currentProxyUrl);

// Local state for URL editing
let originalClientUrl: string;

/**
 * Handle proxy change with radio button event
 * @param event - Radio button change event
 */
const handleProxyChange = async (event: RadioChangeEvent) => {
  await changeProxy(event.target.value);
};

/**
 * Enable editing mode for client proxy URL
 * @param proxyOption - The proxy option to edit
 */
const enableUrlEditingMode = (proxyOption: any) => {
  originalClientUrl = proxyOption.url;
  enableUrlEditing(proxyOption);
};

/**
 * Cancel URL editing and restore original value
 * @param proxyOption - The proxy option being edited
 */
const cancelUrlEditingMode = (proxyOption: any) => {
  cancelUrlEditing(proxyOption, originalClientUrl);
};

/**
 * Save the edited URL to server
 * @param proxyOption - The proxy option with updated URL
 */
const saveProxyUrlMode = async (proxyOption: any) => {
  await saveUrl(proxyOption, originalClientUrl);
};

// Lifecycle
onMounted(() => {
  loadProxyConfiguration();
});
</script>
<template>
  <div class="text-3">
    <div
      v-for="(proxyOption) in proxyOptions"
      :key="proxyOption.key"
      class="pb-3 border-b border-dashed mb-2">
      <div class="flex items-center">
        <Radio
          :value="proxyOption.value"
          :checked="proxyOption.value === selectedProxy"
          @change="handleProxyChange">
          {{ proxyOption.label }}
        </Radio>
        <div v-show="selectedProxy === proxyOption.value && proxyOption.value !== ProxyType.NO_PROXY" class="flex-1 text-right">
          <template v-if="connectionStatus === 'fail'">
            <Icon :icon="statusIcons.fail" :class="statusClasses.fail" />
            {{ statusMessages.notConnected }}
          </template>
          <template v-if="connectionStatus === 'success'">
            <Icon :icon="statusIcons.success" :class="statusClasses.success" />
            {{ statusMessages.connected }}
          </template>
        </div>
      </div>
      <div class="mt-2 whitespace-normal text-gray-text" v-html="proxyTipsMap[proxyOption.value]"></div>
      <div v-if="proxyOption.value !== ProxyType.NO_PROXY" class="mt-3">
        <div class="flex items-center justify-between">
          <span class="text-black-active">{{ t('service.agent.urlLabel') }}</span>
          <div v-if="proxyOption.value === ProxyType.CLIENT_PROXY && !props.disabledEdit" class="flex text-text-link">
            <Icon
              v-if="proxyOption.disabled"
              icon="icon-shuxie"
              @click="enableUrlEditingMode(proxyOption)" />
            <div v-else>
              <Button
                type="text"
                size="small"
                class="text-3 h-4 border-0 text-text-link hover:text-text-link"
                :disabled="isLoading"
                @click="cancelUrlEditingMode(proxyOption)">
                {{ t('actions.cancel') }}
              </Button>
              <Button
                type="text"
                size="small"
                class="text-3 h-4 border-0 text-text-link hover:text-text-link"
                :disabled="isLoading"
                @click="saveProxyUrlMode(proxyOption)">
                {{ t('actions.save') }}
              </Button>
            </div>
          </div>
        </div>
        <Input
          v-model:value="proxyOption.url"
          :disabled="proxyOption.disabled"
          size="small"
          class="mt-2" />
      </div>
      <template v-if="selectedProxy !== ProxyType.NO_PROXY">
        <div
          v-show="isProxyConfiguredButNotConnected && selectedProxy === proxyOption.value"
          class="mt-3"
          :class="statusClasses.warning">
          {{ statusMessages.serverProxyNotConfigured }}
        </div>
        <div
          v-show="isProxyConfiguredAndConnectionFailed && selectedProxy === proxyOption.value"
          class="mt-3"
          :class="statusClasses.warning">
          {{ statusMessages.proxyConnectionFailed }}
        </div>
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
