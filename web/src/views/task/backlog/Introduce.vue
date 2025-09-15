<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div class="text-3.5">
    <div class="font-semibold mb-2.5">
      {{ t('backlog.aboutBacklog') }}
    </div>
    <div class="mb-6">
      <div>{{ t('backlog.aboutBacklogDesc') }}</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/2">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="font-semibold">
              {{ t('backlog.productBacklog') }}
            </div>
            <div>{{ t('backlog.productBacklogDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/2">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="font-semibold">
              {{ t('backlog.sprintBacklog') }}
            </div>
            <div>{{ t('backlog.sprintBacklogDesc') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
