<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import datapng from './Image/shengchengshuju.png';
import storagepng from './Image/cunchuhebeifen.png';
import safepng from './Image/anquanheshujubaohu.png';
import spacepng from './Image/cunchukongjianguanli.png';

const { t } = useI18n();

const tipList = [
  {
    icon: datapng,
    title: t('fileSpace.introduce.features.generateData.title'),
    content: t('fileSpace.introduce.features.generateData.content')
  },
  {
    icon: storagepng,
    title: t('fileSpace.introduce.features.storageAndBackup.title'),
    content: t('fileSpace.introduce.features.storageAndBackup.content')
  },
  {
    icon: safepng,
    title: t('fileSpace.introduce.features.securityAndProtection.title'),
    content: t('fileSpace.introduce.features.securityAndProtection.content')
  },
  {
    icon: spacepng,
    title: t('fileSpace.introduce.features.spaceManagement.title'),
    content: t('fileSpace.introduce.features.spaceManagement.content')
  }
];

</script>
<template>
  <div class="text-3">
    <div class="mb-6 text-3">
      <div>{{ t('fileSpace.introduce.description') }}</div>
    </div>
    <div class="flex items-start justify-between">
      <div
        v-for="item in tipList"
        :key="item.title"
        class="flex items-start space-x-3 w-1/3">
        <img :src="item.icon" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">{{ item.title }}</div>
          <div>{{ item.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
