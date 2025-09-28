<script setup lang="ts">
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Input, NoData } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
import { TabPane, Tabs } from 'ant-design-vue';

// Async component imports
const MemberTab = defineAsyncComponent(() => import('./MemberTab.vue'));
const Quota = defineAsyncComponent(() => import('./Quota.vue'));
const LicenseInfo = defineAsyncComponent(() => import('./license.vue'));

// Reactive data for application information
const appInfo = ref(appContext.getAccessApp());
const { t } = useI18n();

// Default image for application icon
const defaultImg = new URL('../../../assets/images/AngusTester.png', import.meta.url).href;

// Application properties
const appName = ref<string>();
const appUrl = ref<string>();
const appId = ref<string>();
const appIcon = ref<string | undefined>(appContext.getAccessApp()?.icon);

// Active tab state
const activeTab = ref('member');

/**
 * Watch for changes to appInfo and update related properties
 */
watch(() => appInfo.value, (newValue) => {
  if (!newValue) {
    return;
  }
  appName.value = newValue.showName;
  appUrl.value = newValue.url;
  appId.value = newValue.id;
  appIcon.value = newValue.icon;
}, {
  immediate: true
});
</script>
<template>
  <template v-if="appId">
    <div class="px-5 flex-1">
      <div class="w-50 h-20 mt-1 flex flex-col justify-center items-center rounded overflow-hidden">
        <Image
          type="image"
          :src="appIcon"
          :defaultImg="defaultImg" />
      </div>
      <div class="mt-5">
        <div class="font-medium text-3.5 leading-3.5 text-theme-title">{{ t('common.name') }}</div>
        <Input
          v-model:value="appName"
          :maxlength="50"
          disabled
          class="w-100 mt-2" />
      </div>
      <div class="mt-5 mb-3">
        <div class="font-medium text-3.5 leading-3.5 text-theme-title">{{ t('app.config.labels.domain') }}</div>
        <Input
          v-model:value="appUrl"
          :maxlength="50"
          disabled
          class="w-100 mt-2" />
      </div>
      <Tabs
        v-model:activeKey="activeTab"
        size="small"
        class="mt-5">
        <TabPane
          key="member"
          :tab="t('app.config.tabs.member')">
          <MemberTab :appId="appId" />
        </TabPane>
        <TabPane
          key="quota"
          :tab="t('app.config.tabs.quota')">
          <Quota />
        </TabPane>
        <TabPane
          key="license"
          :tab="t('app.config.tabs.license')">
          <LicenseInfo />
        </TabPane>
      </Tabs>
    </div>
  </template>
  <template v-else>
    <NoData class="h-full -mt-10" />
  </template>
</template>
