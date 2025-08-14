<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const HeadIntroduction = defineAsyncComponent(() => import('@/components/layout/header/introduce/index.vue'));

const steps = [
  {
    icon: 'icon-guanlijiedian',
    title: t('node.nodeItem.nodeTip.steps.managementNode.title'),
    content: t('node.nodeItem.nodeTip.steps.managementNode.content')
  },
  {
    icon: 'icon-yemianshitu',
    title: t('node.nodeItem.nodeTip.steps.controllerNode.title'),
    content: t('node.nodeItem.nodeTip.steps.controllerNode.content')
  },
  {
    icon: 'icon-zhihangceshi',
    title: t('node.nodeItem.nodeTip.steps.executionNode.title'),
    content: t('node.nodeItem.nodeTip.steps.executionNode.content')
  },
  {
    icon: 'icon-mockjiedian',
    title: t('node.nodeItem.nodeTip.steps.mockNode.title'),
    content: t('node.nodeItem.nodeTip.steps.mockNode.content')
  },
  {
    icon: 'icon-yingyongxinxi',
    title: t('node.nodeItem.nodeTip.steps.applicationNode.title'),
    content: t('node.nodeItem.nodeTip.steps.applicationNode.content')
  }
];
</script>
<template>
  <HeadIntroduction :title="t('node.nodeItem.nodeTip.title')" :steps="steps" />
</template>
