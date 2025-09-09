<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div class="space-y-2 text-3.5">
    <div class="space-y-1">
      <div class="flex items-center space-x-2">
        <span class="text-3.5 font-semibold mb-1.5">{{ t('dataVariable.introduce.title') }}</span>
      </div>
      <div class="mb-4">
        {{ t('dataVariable.introduce.description') }}
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
      <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
        <div class="flex items-start space-x-3">
          <img src="./images/1.png" class="w-12 h-12 flex-shrink-0 rounded-md" alt="File Extraction Icon">
          <div class="space-y-1">
            <h3 class="font-semibold text-gray-800 group-hover:text-blue-600 transition-colors">
              {{ t('dataVariable.introduce.types.fileExtract.title') }}
            </h3>
            <p class="text-gray-600 leading-relaxed">
              {{ t('dataVariable.introduce.types.fileExtract.description') }}
            </p>
          </div>
        </div>
      </div>

      <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
        <div class="flex items-start space-x-3">
          <img src="./images/2.png" class="w-12 h-12 flex-shrink-0 rounded-md" alt="HTTP Extraction Icon">
          <div class="space-y-1">
            <h3 class="font-semibold text-gray-800 group-hover:text-purple-600 transition-colors">
              {{ t('dataVariable.introduce.types.httpExtract.title') }}
            </h3>
            <p class="text-gray-600 leading-relaxed">
              {{ t('dataVariable.introduce.types.httpExtract.description') }}
            </p>
          </div>
        </div>
      </div>

      <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200">
        <div class="flex items-start space-x-3">
          <img src="./images/3.png" class="w-12 h-12 flex-shrink-0 rounded-md" alt="JDBC Extraction Icon">
          <div class="space-y-1">
            <h3 class="font-semibold text-gray-800 group-hover:text-indigo-600 transition-colors">
              {{ t('dataVariable.introduce.types.jdbcExtract.title') }}
            </h3>
            <p class="text-gray-600 leading-relaxed">
              {{ t('dataVariable.introduce.types.jdbcExtract.description') }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
