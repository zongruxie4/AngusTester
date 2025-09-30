<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import datapng from '@/views/data/file/images/shengchengshuju.png';
import storagepng from '@/views/data/file/images/cunchuhebeifen.png';
import safepng from '@/views/data/file/images/anquanheshujubaohu.png';
import spacepng from '@/views/data/file/images/cunchukongjianguanli.png';

const { t } = useI18n();

const tipList = [
  {
    icon: datapng,
    title: t('file.introduce.features.generateData.title'),
    content: t('file.introduce.features.generateData.content')
  },
  {
    icon: storagepng,
    title: t('file.introduce.features.storageAndBackup.title'),
    content: t('file.introduce.features.storageAndBackup.content')
  },
  {
    icon: safepng,
    title: t('file.introduce.features.securityAndProtection.title'),
    content: t('file.introduce.features.securityAndProtection.content')
  },
  {
    icon: spacepng,
    title: t('file.introduce.features.spaceManagement.title'),
    content: t('file.introduce.features.spaceManagement.content')
  }
];

</script>
<template>
  <div class="text-3.5">
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('file.introduce.title') }}
    </div>
    <div class="mb-6">
      {{ t('file.introduce.description') }}
    </div>
    <div class="flex items-start justify-between">
      <div
        v-for="item in tipList"
        :key="item.title"
        class="flex items-start space-x-3 w-1/3">
        <img :src="item.icon" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="font-semibold">{{ item.title }}</div>
          <div>{{ item.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
