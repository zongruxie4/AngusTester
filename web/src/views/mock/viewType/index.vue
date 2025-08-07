<script setup lang='ts'>
import { Hints, Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { textList } from '../data';
const { t } = useI18n();
const tips = t('mock.introduce.description');
</script>

<template>
  <div class=" bg-gray-2 flex items-center text-3 pl-7.5 rounded h-45">
    <img
      src="@/assets/home/mock.svg"
      class="mr-7.75 w-45 h-45" />
    <div class="p-8 flex-1 h-full flex flex-col justify-between">
      <Hints :text="tips" />
      <ul class="w-full flex justify-between space-x-20">
        <li
          v-for="(item,index) in textList"
          :key="index"
          class="flex-1/3 flex">
          <Icon :icon="item.icon" class="text-10 mr-3.5 flex-none" />
          <div class="h-full flex flex-col py-0.5 space-y-2">
            <p class="text-3.5 leading-3.5 text-theme-title font-semibold">{{ item.name }}</p>
            <p class="text-3 leading-3.5 text-theme-sub-content whitespace-break-spaces break-all">{{ item.description }}</p>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>
