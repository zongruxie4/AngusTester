<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div class="text-3.5">
    <div class="font-semibold mb-2.5">
      {{ t('backlog.introduce.title') }}
    </div>
    <div class="mb-6 font-serif">
      {{ t('backlog.introduce.description') }}
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between font-serif">
        <div class="flex items-start space-x-3 w-1/2">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="font-semibold">
              {{ t('backlog.productBacklogTitle') }}
            </div>
            <div>{{ t('backlog.introduce.productBacklogDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/2">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="font-semibold">
              {{ t('backlog.sprintBacklogTitle') }}
            </div>
            <div>{{ t('backlog.introduce.sprintBacklogDesc') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
