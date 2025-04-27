<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';

const HeadIntroduction = defineAsyncComponent(() => import('@/components/layout/header/introduce/index.vue'));

const steps = [
  {
    icon: 'icon-guanlijiedian',
    title: '管理节点',
    content: '用于部署和运行Web UI应用'
  },
  {
    icon: 'icon-yemianshitu',
    title: '控制器节点',
    content: '用于调度、管理、监控执行和代理服务'
  },
  {
    icon: 'icon-zhihangceshi',
    title: '执行节点',
    content: '用于执行功能测试、性能测试、生成数据等'
  },
  {
    icon: 'icon-mockjiedian',
    title: 'Mock节点',
    content: '运行Mock服务和Mock接口'
  },
  {
    icon: 'icon-yingyongxinxi',
    title: '应用节点',
    content: '测试应用所在节点，用于压测时观察被压测应用节点系统负载，有助于定位分析性能问题'
  }
];
</script>
<template>
  <HeadIntroduction title="提示：1. 节点系统：支持Linux、Mac、Windows 操作系统，推荐使用Linux。2. 添加和安装代理：在执行任务之前，您需要先添加节点并为节点安装代理，如果因为网络或服务器账户等问题无法自动安装代理，请按照手动安装的提示进行操作，请确保在执行任务之前，代理处于连接状态（如果代理未启动，则需要先启动代理）。3.节点角色配置：一个节点可以承担多个角色，但为了提高性能测试结果的准确性，请为压测节点配置单一的“执行”节点角色。4. 网络环境选择：在压力测试时，如果您想尽可能还原用户真实的访问环境（如网络延迟），请使用公网节点进行测试。如果您想排除网络延迟对测试结果的干扰，以测试应用的极限性能，请添加和测试应用在相同内网环境的节点进行测试。5. 默认只允许系统管理员和应用管理员对节点进行管理操作。" :steps="steps" />
</template>
