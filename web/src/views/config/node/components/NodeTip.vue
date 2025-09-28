<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const HeadIntroduction = defineAsyncComponent(() => import('@/components/layout/header/introduce/index.vue'));

const steps = [
  {
    icon: 'icon-guanlijiedian',
    title: t('node.introduce.steps.managementNode.title'),
    content: t('node.introduce.steps.managementNode.content')
  },
  {
    icon: 'icon-yemianshitu',
    title: t('node.introduce.steps.controllerNode.title'),
    content: t('node.introduce.steps.controllerNode.content')
  },
  {
    icon: 'icon-zhihangceshi',
    title: t('node.introduce.steps.executionNode.title'),
    content: t('node.introduce.steps.executionNode.content')
  },
  {
    icon: 'icon-mockjiedian',
    title: t('node.introduce.steps.mockNode.title'),
    content: t('node.introduce.steps.mockNode.content')
  },
  {
    icon: 'icon-yingyongxinxi',
    title: t('node.introduce.steps.applicationNode.title'),
    content: t('node.introduce.steps.applicationNode.content')
  }
];
</script>
<template>
  <HeadIntroduction :title="t('node.introduce.description')" :steps="steps" />
</template>
