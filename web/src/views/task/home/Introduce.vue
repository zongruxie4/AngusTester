<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

const { t } = useI18n();
</script>

<template>
  <div class="bg-white rounded px-5 pt-0 pb-0 text-3.5">
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('taskHome.welcomeToAngusTask') }}
    </div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/introduce.png" />
      <div>{{ t('taskHome.taskDescription') }}</div>
    </div>
    <div class="mt-2 mr-5">
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 transform-gpu translate-y-0.75" />
        <span>{{ t('taskHome.workItem1') }}</span>
      </div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 transform-gpu translate-y-0.75" />
        <span>{{ t('taskHome.workItem2') }}</span>
      </div>
      <div class="flex items-start">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 transform-gpu translate-y-0.75" />
        <span>{{ t('taskHome.workItem3') }}</span>
      </div>
    </div>
  </div>
</template>
