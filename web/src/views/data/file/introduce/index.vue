<script lang="ts" setup>
import datapng from './Image/shengchengshuju.png';
import storagepng from './Image/cunchuhebeifen.png';
import safepng from './Image/anquanheshujubaohu.png';
import spacepng from './Image/cunchukongjianguanli.png';

const tipList = [
  {
    icon: datapng,
    title: '生成数据',
    content: '生成CSV、Excel、SQL、JSON、XML等多种格式数据文件，方便测试或业务使用。'
  },
  {
    icon: storagepng,
    title: '存储和备份',
    content: '可以将工作或者其他类型的文件上传至文件网盘中，实现数据的备份和安全存储，避免丢失和共享不便问题。'
  },
  {
    icon: safepng,
    title: '安全性和数据保护',
    content: '对于上传后的文件数据，通过数据级别权限和加密保护，确保数据的安全性和隐私保护。'
  },
  {
    icon: spacepng,
    title: '存储空间管理',
    content: '提供存储空间配额管理功能，可以根据需要扩展存储容量，满足不同规模和需求的存储要求。'
  }
];

</script>
<template>
  <div class="text-3">
    <div class="mb-6 text-3">
      <div>提供统一的测试数据和业务数据文件在线生成、存储功能，以及支持多人协同管理和共享等。以下是AngusTester文件的主要作用和特点：</div>
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
