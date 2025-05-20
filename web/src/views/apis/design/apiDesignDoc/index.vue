<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Tag } from 'ant-design-vue';
import { apis } from '@/api/tester';
import OpenApiDesign from 'open-api-designer';

interface Props {
  designId: string
}

const props = withDefaults(defineProps<Props>(), {
  designId: ''
});

const openApiDesignRef = ref();
const designInfo = ref<{[key: string]: string}>({});
const designContent = ref({});

const getDesignContent = async () => {
  const [error, resp] = await apis.exportDesign({format: 'json', id: props.designId});
  if (error) {
    return
  }
  designContent.value = resp?.data || {};
}

const getDesignInfo = async () => {
  const [error, resp] = await apis.getDesignInfo(props.designId);
  if (error) {
    return
  }
  designInfo.value = resp?.data || {};
}

onMounted(async () => {
  await getDesignContent();
  await getDesignInfo();
  new OpenApiDesign(openApiDesignRef.value,{
    openApiDoc: designContent.value,
    language: 'zh_CN'
  });
})
</script>
<template>
<div ref="openApiDesignRef" class="h-full">
  <div slot="docTitle" class="flex justify-center items-center space-x-2">
    <Tag color="green" class="text-3.5 rounded-full">{{designInfo.openapiSpecVersion}}</Tag>
    <div class="text-5 font-medium">{{designInfo.name}}</div>
    <div>{{designInfo.lastModifiedByName}}最后修改于{{designInfo.lastModifiedDate}}</div>
  </div>
</div>
</template>
<style scoped>
</style>
