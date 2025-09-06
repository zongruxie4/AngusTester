<script setup lang="ts">
// ===== IMPORTS =====
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

// ===== COMPOSABLES =====
const { t } = useI18n();
</script>

<template>
  <!-- Product introduction section -->
  <div>
    <!-- Welcome title -->
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('scenarioHome.introduce.welcome') }}
    </div>
    
    <!-- Introduction content with image and description -->
    <div class="flex items-center justify-between space-x-2.5">
      <img 
        class="w-1/3" 
        src="./images/introduce.png" 
        alt="Product introduction illustration" />
      <div>{{ t('scenarioHome.introduce.description') }}</div>
    </div>
    
    <!-- Product capabilities list -->
    <div class="mt-2 mr-5">
      <div class="font-semibold mb-1.5">
        {{ t('scenarioHome.introduce.capabilities') }}
      </div>
      
      <!-- Capability 1 -->
      <div class="flex items-start mb-1">
        <Icon 
          icon="icon-duihaolv" 
          class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('scenarioHome.introduce.capability1') }}</span>
      </div>
      
      <!-- Capability 2 -->
      <div class="flex items-start mb-1">
        <Icon 
          icon="icon-duihaolv" 
          class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('scenarioHome.introduce.capability2') }}</span>
      </div>
      
      <!-- Capability 3 -->
      <div class="flex items-start">
        <Icon 
          icon="icon-duihaolv" 
          class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>{{ t('scenarioHome.introduce.capability3') }}</span>
      </div>
    </div>
  </div>
</template>
