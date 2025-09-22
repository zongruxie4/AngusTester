<script setup lang='ts'>
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useMockUI } from './composables/useMockUI';

const { t } = useI18n();

const { textList } = useMockUI();
</script>

<template>
  <div class="bg-gray-2 flex items-center text-3 pl-7.5 rounded h-45">
    <img
      src="../../../assets/images/home/mock.svg"
      class="mr-7.75 w-45 h-45" />
    <div class="p-8 flex-1 h-full flex flex-col justify-between font-serif">
      <div class="text-3.5 font-semibold">{{ t('mock.introduce.description') }}</div>
      <div class="mt-5">
        <ul class="w-full flex justify-between">
          <li
            v-for="(item,index) in textList"
            :key="index"
            class="flex-1/3 flex pr-8">
            <Icon :icon="item.icon" class="text-10 mr-3.5 flex-none" />
            <div class="h-full flex flex-col py-0.5">
              <p class="text-3.5 leading-3 text-theme-title font-semibold">{{ item.name }}</p>
              <p class="text-3.5 text-theme-sub-content whitespace-break-spaces break-all">{{ item.description }}</p>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
