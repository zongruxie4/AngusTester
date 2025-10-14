<script setup lang="ts">
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('apiServer.introduce.title') }}
    </div>
    <div class="flex items-center space-x-10 text-3.5 mb-6 font-serif">
      <div>
        <div class="mb-6">
          <div>{{ t('apiServer.introduce.description') }}</div>
        </div>

        <div class="flex items-start justify-between">
          <div class="flex items-start space-x-3 w-1/3">
            <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
            <div class="space-y-1 pr-10">
              <div class="text-3.5 font-semibold">
                {{ t('apiServer.introduce.integration.title') }}
              </div>
              <div>{{ t('apiServer.introduce.integration.description') }}</div>
            </div>
          </div>

          <div class="flex items-start space-x-3 w-1/3">
            <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
            <div class="space-y-1 pr-10">
              <div class="text-3.5 font-semibold">
                {{ t('apiServer.introduce.env.title') }}
              </div>
              <div>{{ t('apiServer.introduce.env.description') }}</div>
            </div>
          </div>

          <div class="flex items-start space-x-3 w-1/3">
            <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
            <div class="space-y-1 pr-10">
              <div class="text-3.5 font-semibold">
                {{ t('apiServer.introduce.version.title') }}
              </div>
              <div>{{ t('apiServer.introduce.version.description') }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex-shrink-0 ">
        <div class="font-semibold text-center">
          {{ t('apiServer.introduce.url.description') }}
        </div>
        <div class="mb-1 mt-2">
          https://api.example.com/v1/users?role=admin&status=active
        </div>
        <div class="flex items-center mb-8">
          <div class="diagonal-line line"></div>
          <div style="width:112px;" class="horizontal-line line"></div>
          <div style="transform: rotate(-65deg);" class="diagonal-line line"></div>
          <div class="diagonal-line line"></div>
          <div style="width:32px;" class="horizontal-line line"></div>
          <div style="transform: rotate(-65deg);" class="diagonal-line line"></div>
          <div class="diagonal-line line"></div>
          <div style="width:125px;" class="horizontal-line line"></div>
          <div style="transform: rotate(-65deg);" class="diagonal-line line"></div>
        </div>
        <div class="text-3 font-semibold text-center">
          {{ t('apiServer.introduce.url.title') }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.line {
  height: 1px ;
  background-color: rgb(82, 90, 101);
}

.diagonal-line {
  width: 10px;
  transform: rotate(65deg);
}

.horizontal-line {
  position: relative;
  transform: translateY(5px);
}

.horizontal-line::after {
  content:"server URL";
  display: block;
  position:absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
}

.horizontal-line:nth-of-type(5)::after {
  content:"endpoint";
}

.horizontal-line:nth-of-type(8)::after {
  content:"query parameters";
}
</style>
