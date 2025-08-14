<script setup lang="ts">
import { defineAsyncComponent,  onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Image, Input, NoData } from '@xcan-angus/vue-ui';
import { appContext, Context } from '@xcan-angus/infra';
import { TabPane, Tabs } from 'ant-design-vue';
const Member = defineAsyncComponent(() => import('./memberData.vue'));
const Quota = defineAsyncComponent(() => import('./quota.vue'));
const PermitInfo = defineAsyncComponent(() => import('./permitInfo.vue'));

const appInfo = ref(appContext.getAccessApp());
const { t } = useI18n();

const defaultImg = new URL('./AngusTester.png', import.meta.url).href;

const editionType = ref<string>();
const appName = ref();
const appUrl = ref();
const appId = ref();
const appIcon = ref();
const state:{appInfo:Context['accessApp']} = reactive({ appInfo: {} as Context['accessApp'] });

const activeTab = ref('member');

onMounted(async () => {
  editionType.value = appContext.getEditionType();

  watch(() => appInfo.value, (newValue) => {
    if (!newValue) {
      return;
    }
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
        <div class="font-medium text-3.5 leading-3.5 text-theme-title">{{ t('appConfig.labels.name') }}</div>
        <Input
          v-model:value="appName"
          :maxlength="50"
          disabled
          class="w-100 mt-2" />
      </div>
      <div class="mt-5">
        <div class="font-medium text-3.5 leading-3.5 text-theme-title">{{ t('appConfig.labels.domain') }}</div>
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
        <TabPane key="member" :tab="t('appConfig.tabs.member')">
          <Member :appId="appId" />
        </TabPane>
        <TabPane key="quota" :tab="t('appConfig.tabs.quota')">
          <Quota />
        </TabPane>
        <TabPane
          v-if="!['CLOUD_SERVICE'].includes(editionType)"
          key="permission"
          :tab="t('appConfig.tabs.permission')">
          <PermitInfo />
        </TabPane>
      </Tabs>
    </div>
  </template>
  <template v-else>
    <NoData class="h-full -mt-10" />
  </template>
</template>
