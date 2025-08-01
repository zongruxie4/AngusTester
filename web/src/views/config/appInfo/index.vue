<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Input, NoData } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
import { TabPane, Tabs } from 'ant-design-vue';

import { AppInfo } from './interface';

const Member = defineAsyncComponent(() => import('./memberData.vue'));
const Quota = defineAsyncComponent(() => import('./quota.vue'));
const PermitInfo = defineAsyncComponent(() => import('./permitInfo.vue'));

const appInfo = inject('appInfo', ref<AppInfo>());
const { t } = useI18n();

const defaultImg = new URL('./AngusTester.png', import.meta.url).href;

const editionType = ref<string>();
const appName = ref('');
const appUrl = ref('');
const appId = ref('');
const appIcon = ref('');
const state:{appInfo:AppInfo} = reactive({ appInfo: {} as AppInfo });

const activeTab = ref('member');

onMounted(async () => {
  editionType.value = appContext.getEditionType();

  watch(() => appInfo.value, (newValue:AppInfo) => {
    state.appInfo = newValue;
    appName.value = newValue.showName;
    appUrl.value = newValue.url;
    appId.value = newValue.id;
    appIcon.value = newValue.icon;
  }, {
    immediate: true
  });
});

</script>
<template>
  <template v-if="appId">
    <div class="px-5 flex-1">
      <div class="w-50 h-20 flex flex-col justify-center items-center rounded overflow-hidden">
        <Image
          type="image"
          :src="appIcon"
          :defaultImg="defaultImg" />
      </div>
      <div class="mt-5">
        <div class="font-medium text-3.5 leading-3.5 text-theme-title">{{ t('名称') }}</div>
        <Input
          v-model:value="appName"
          :maxlength="50"
          disabled
          class="w-100 mt-2" />
      </div>
      <div class="mt-5">
        <div class="font-medium text-3.5 leading-3.5 text-theme-title">{{ t('域名') }}</div>
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
        <TabPane key="member" tab="应用成员">
          <Member :appId="appId" />
        </TabPane>
        <TabPane key="quota" tab="应用配额">
          <Quota />
        </TabPane>
        <TabPane
          v-if="!['CLOUD_SERVICE'].includes(editionType)"
          key="permission"
          tab="许可信息">
          <PermitInfo />
        </TabPane>
      </Tabs>
    </div>
  </template>
  <template v-else>
    <NoData class="h-full -mt-10" />
  </template>
</template>
