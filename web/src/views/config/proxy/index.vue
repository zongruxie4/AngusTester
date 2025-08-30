<script setup lang="ts">
import { Switch } from 'ant-design-vue';
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Input } from '@xcan-angus/vue-ui';

// Import images for UI display
import apple from './images/apple.png';
import windows from './images/windows.png';
import linux from './images/linux.png';
import sd from './images/sd.png';
import wlj from './images/wlj.png';
import ylj from './images/ylj.png';

// Import composables for proxy data management
import { useProxyData } from './composables';

// Initialize i18n for internationalization
const { t } = useI18n();

// Initialize proxy data composable
const {
  proxyConfig,
  connectionStatus,
  isEditMode,
  addressValidationError,
  toggleEditMode,
  handleProxyToggle,
  validateProxyAddress
} = useProxyData();

// Reference to input element for focus management
const addressInputRef = ref<HTMLInputElement | null>(null);
</script>

<template>
  <div class="px-5 py-3">
    <!-- Proxy information panel with descriptions of different proxy types -->
    <div class="border rounded p-5" style="border-color: #bcdcff;background-color: #f9fcff;">
      <div class="text-3 leading-3 flex">
        <Icon class="text-tips text-3.5 mt-0.5" icon="icon-tishi1" />
        <p class="text-theme-title font-medium text-3 ml-1.5 leading-5 whitespace-pre-wrap">
          {{ t('proxy.title') }}
        </p>
      </div>

      <!-- Proxy type descriptions -->
      <div class="text-3 leading-3 flex space-x-10 mx-5 mt-5">
        <!-- No proxy description -->
        <div class="flex-1">
          <div class="text-theme-title font-medium">
            <Icon class="mr-1.5 -mt-0.5" icon="icon-wudaili" />
            {{ t('proxy.noProxy') }}
          </div>
          <div class="text-theme-sub-content mt-2 leading-5">
            {{ t('proxy.noProxyDescription') }}
          </div>
        </div>

        <!-- Client proxy description -->
        <div class="flex-1">
          <div class="text-theme-title font-medium">
            <Icon class="mr-1.5 -mt-0.5" icon="icon-jiekoudaili" />
            {{ t('proxy.clientProxy') }}
          </div>
          <div class="text-theme-sub-content mt-2 leading-5">
            {{ t('proxy.clientProxyDescription') }}
          </div>
        </div>

        <!-- Server proxy description -->
        <div class="flex-1">
          <div class="text-theme-title font-medium">
            <Icon class="mr-1.5 -mt-0.5" icon="icon-host" />
            {{ t('proxy.serverProxy') }}
          </div>
          <div class="text-theme-sub-content mt-2 leading-5">
            {{ t('proxy.serverProxyDescription') }}
          </div>
        </div>

        <!-- Cloud proxy description -->
        <div class="flex-1">
          <div class="text-theme-title font-medium">
            <Icon class="mr-1.5 -mt-0.5" icon="icon-host" />
            {{ t('proxy.cloudProxy') }}
          </div>
          <div class="text-theme-sub-content mt-2 leading-5">
            {{ t('proxy.cloudProxyDescription') }}
          </div>
        </div>
      </div>
    </div>

    <!-- Download proxy section (currently hidden) -->
    <template v-if="false">
      <div class="font-medium text-3.5 leading-3.5 text-theme-title pb-2 mx-5 mt-10">
        {{ t('proxy.downloadProxy') }}
      </div>
      <div class="flex 2xl:space-x-2 m-2 5xl:space-x-7.5 text-3 leading-3">
        <div class="px-7.5 py-3.75 w-1/4 flex items-center text-theme-text-hover cursor-pointer">
          <img :src="windows" class="w-7.5 mr-5" />
          <span>{{ t('proxy.windowsDesktopProxy') }}</span>
        </div>
        <div class="px-7.5 py-3.75 w-1/4 flex items-center text-theme-text-hover cursor-pointer">
          <img :src="apple" class="w-7.5 mr-5" />
          <span>{{ t('proxy.macDesktopProxy') }}</span>
        </div>
        <div class="px-7.5 py-3.75 w-1/4 flex items-center text-theme-text-hover cursor-pointer">
          <img :src="linux" class="w-7.5 mr-5" />
          <span>{{ t('proxy.linuxProxy') }}</span>
        </div>
        <div class="px-7.5 py-3.75 w-1/4 flex items-center text-theme-text-hover cursor-pointer">
          <img :src="sd" class="w-7.5 mr-5" />
          <span>{{ t('proxy.manualInstallPackage') }}</span>
        </div>
      </div>
    </template>

    <!-- Server request proxy configuration -->
    <div class="font-medium text-3.5 leading-3.5 text-theme-title pb-2 mt-10">
      {{ t('proxy.serverRequestProxyConfig') }}
    </div>

    <div class="flex space-x-5 text-theme-sub-content text-3 leading-3">
      <!-- Labels column -->
      <div>
        <div class="h-12" style="line-height: 48px;">
          {{ t('proxy.enable') }}
        </div>
        <div class="h-12" style="line-height: 48px;">
          {{ t('proxy.proxyAddress') }}
        </div>
      </div>

      <!-- Configuration inputs column -->
      <div class="w-150 text-theme-content">
        <!-- Enable switch -->
        <div class="h-12" style="line-height: 48px;">
          <Switch
            v-model:checked="proxyConfig.enabled"
            size="small"
            class="w-8"
            @change="handleProxyToggle" />
        </div>

        <!-- Proxy address input -->
        <div class="flex h-12 whitespace-nowrap text-theme-sub-content" style="line-height: 48px;">
          <div class="h-12 relative">
            <Input
              ref="addressInputRef"
              v-model:value="proxyConfig.url"
              :disabled="isEditMode"
              class="w-100 h-8"
              :placeholder="t('proxy.inputProxyAddress')"
              @change="validateProxyAddress">
              <template #suffix>
                <template v-if="proxyConfig.enabled">
                  <Icon
                    icon="icon-shuxie"
                    class="text-theme-special text-theme-text-hove cursor-pointer"
                    @click="toggleEditMode" />
                </template>
                <template v-else>
                  <Icon
                    icon="icon-shuxie"
                    class="text-text-sub-content cursor-not-allowed" />
                </template>
              </template>
            </Input>

            <!-- Address validation error message -->
            <div
              v-if="addressValidationError"
              class="absolute top-12 text-3 leading-3 text-status-error">
              {{ t('proxy.addressTip') }}
            </div>
          </div>

          <!-- Connection status indicator -->
          <template v-if="proxyConfig.url">
            <template v-if="connectionStatus.isConnected">
              <div class="flex items-center ml-3">
                <img :src="ylj" class="w-4 mr-2" />
                <span>
                  {{ connectionStatus.isNewConnection ?
                    t('proxy.connectionSuccess') :
                    t('proxy.connected') }}
                </span>
              </div>
            </template>
            <template v-else>
              <div class="flex items-center ml-3">
                <img :src="wlj" class="w-4 mr-2" />
                <span>
                  {{ connectionStatus.isNewConnection ?
                    t('proxy.connectionFailed') :
                    t('proxy.notConnected') }}
                </span>
              </div>
            </template>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Animation styles for information panels */
.open-info {
  width: 320px;
}

.sotp-info {
  width: 20px;
}

.sotp-info .script-info {
  opacity: 0;
}
</style>
