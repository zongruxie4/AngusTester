<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div class="bg-white rounded px-5 pt-0 pb-0 text-3.5 font-serif">
    <div class="font-semibold mb-2.5">
      {{ t('apisHome.introduce.title') }}
    </div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/introduce.png" />
      <div>{{ t('apisHome.introduce.description') }}</div>
    </div>
    <div class="mr-5">
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('apisHome.introduce.features[0]') }}</span>
      </div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('apisHome.introduce.features[1]') }}</span>
      </div>
      <div class="flex items-start">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('apisHome.introduce.features[2]') }}</span>
      </div>
    </div>
  </div>
</template>
