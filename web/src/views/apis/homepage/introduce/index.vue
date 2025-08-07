<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('apis.welcome.title') }}</div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/1.png" />
      <div>{{ t('apis.welcome.description') }}</div>
    </div>
    <div class="mt-2 mr-5">
      <div class="font-semibold mb-1.5">{{ t('apis.welcome.featuresTitle') }}</div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('apis.welcome.features[0]') }}</span>
      </div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('apis.welcome.features[1]') }}</span>
      </div>
      <div class="flex items-start">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('apis.welcome.features[2]') }}</span>
      </div>
    </div>
  </div>
</template>
