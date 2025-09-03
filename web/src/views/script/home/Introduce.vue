<script lang="ts" setup>
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>
<template>
  <div class="flex content items-center flex-1">
    <img src="./images/logo.svg" class="w-45 h-45" />
    <div class="ml-3.25 text-content flex-1">
      <div>{{ t('scriptHome.introduce.description') }}</div>
      <div class="mt-2">{{ t('scriptHome.introduce.workDescription') }}</div>

      <div class="mt-1">
        <Icon icon="icon-duihaolv" class="mr-2.5" />
        <span>{{ t('scriptHome.introduce.workItems.testScript') }}</span>
      </div>

      <div class="mt-1">
        <Icon icon="icon-duihaolv" class="mr-2.5" />
        <span>{{ t('scriptHome.introduce.workItems.mockScript') }}</span>
      </div>
    </div>
  </div>
</template>
