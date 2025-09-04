<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div class="text-3.5">
    <!-- Section Title -->
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('dataVariable.introduce.title') }}
    </div>

    <!-- Description -->
    <div class="mb-6">
      <div>{{ t('dataVariable.introduce.description') }}</div>
    </div>

    <!-- Variable Types Overview -->
    <div class="flex items-start justify-between">
      <!-- File Extraction Type -->
      <div class="flex items-start space-x-3 w-1/3">
        <img
          src="./images/1.png"
          class="w-10 flex-shrink-0 transform-gpu translate-y-0.5"
          alt="File Extraction Icon">
        <div class="space-y-1 pr-10">
          <div class="font-semibold">
            {{ t('dataVariable.introduce.types.fileExtract.title') }}
          </div>
          <div>{{ t('dataVariable.introduce.types.fileExtract.description') }}</div>
        </div>
      </div>

      <!-- HTTP Extraction Type -->
      <div class="flex items-start space-x-3 w-1/3">
        <img
          src="./images/2.png"
          class="w-10 flex-shrink-0 transform-gpu translate-y-0.5"
          alt="HTTP Extraction Icon">
        <div class="space-y-1 pr-10">
          <div class="tfont-semibold">
            {{ t('dataVariable.introduce.types.httpExtract.title') }}
          </div>
          <div>{{ t('dataVariable.introduce.types.httpExtract.description') }}</div>
        </div>
      </div>

      <!-- JDBC Extraction Type -->
      <div class="flex items-start space-x-3 w-1/3">
        <img
          src="./images/3.png"
          class="w-10 flex-shrink-0 transform-gpu translate-y-0.5"
          alt="JDBC Extraction Icon">
        <div class="space-y-1 pr-10">
          <div class="font-semibold">
            {{ t('dataVariable.introduce.types.jdbcExtract.title') }}
          </div>
          <div>{{ t('dataVariable.introduce.types.jdbcExtract.description') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
