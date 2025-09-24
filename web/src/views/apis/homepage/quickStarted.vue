<script setup lang="ts">
import { defineAsyncComponent, inject, ref } from 'vue';
import { AsyncComponent, Icon } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const LocalImport = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/localImport/index.vue'));

const { t } = useI18n();
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

const importModalVisible = ref(false);

const toDebug = () => {
  addTabPane({ name: t('apis.quickAccess.addApiTabName'), value: 'API', _id: utils.uuid() + 'API' });
};

const toImport = () => {
  importModalVisible.value = true;
};
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">{{ t('apis.quickAccess.title') }}</div>
    <div class="flex space-x-3.75 flex-1 ">
      <div class="flex flex-col justify-between px-5 pt-4 pb-3.5 border border-theme-text-box rounded w-1/3">
        <div class="text-theme-content mb-3.5">
          <div class="flex items-center space-x-2 mb-2">
            <Icon icon="icon-jiekoutiaoshi" class="text-5" />
            <span class="title-normal">{{ t('apis.quickAccess.debug.title') }}</span>
          </div>
          <div>{{ t('apis.quickAccess.debug.description') }}</div>
        </div>

        <div class="flex items-start">
          <!-- <ButtonAuth
            code="ApisDebug"
            type="link"
            icon="icon-tiaoshi"
            iconStyle="font-size:14px;"
            @click="toDebug" /> -->
          <Button
            type="link"
            size="small"
            class="flex space-x-1"
            @click="toDebug">
            <Icon icon="icon-tiaoshi" />
            {{ t('common.actions') }}
          </Button>
        </div>
      </div>

      <div class="flex flex-col justify-between px-5 pt-4 pb-3.5 border border-theme-text-box rounded w-1/3">
        <div class="text-theme-content mb-3.5">
          <div class="flex items-center space-x-2 mb-2">
            <Icon icon="icon-daoruxiangmufuwu" class="text-5" />
            <span class="title-normal">{{ t('apis.quickAccess.import.title') }}</span>
          </div>
          <div>{{ t('apis.quickAccess.import.description') }}</div>
        </div>

        <div class="flex items-start">
          <Button
            type="link"
            size="small"
            class="flex space-x-1"
            @click="toImport">
            <Icon icon="icon-daoru" />
            {{ t('common.actions') }}
          </Button>
        </div>
      </div>
    </div>

    <AsyncComponent :visible="importModalVisible">
      <LocalImport v-model:visible="importModalVisible" source="global" />
    </AsyncComponent>
  </div>
</template>
