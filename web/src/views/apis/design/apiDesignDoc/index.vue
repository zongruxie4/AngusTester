<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Tag, Button } from 'ant-design-vue';
import { apis } from '@/api/tester';
import OpenApiDesign from 'open-api-designer';
import { notification, Spin } from "@xcan-angus/vue-ui";

interface Props {
  designId: string
}

const props = withDefaults(defineProps<Props>(), {
  designId: ''
});

const designInfo = ref<{[key: string]: string}>({});
const designContent = ref();
const openAPIDesignInstance = ref();
const loadingData = ref(false);

const getDesignContent = async () => {
  const [error, resp] = await apis.exportDesign({format: 'json', id: props.designId});
  if (error) {
    return
  }

  designContent.value = JSON.stringify(resp?.data || {});
}

const getDesignInfo = async () => {
  const [error, resp] = await apis.getDesignDetail(props.designId);
  if (error) {
    return
  }
  designInfo.value = resp?.data || {};
};

const updateContent = async () => {
  if (typeof openAPIDesignInstance.value?.updateData === 'function') {
    openAPIDesignInstance.value.updateData();
  }

  const content = (openAPIDesignInstance && typeof openAPIDesignInstance.value.getDocApi === 'function')
    ? openAPIDesignInstance.value.getDocApi()
    : designContent.value;
  const [error] = await apis.putDesignContent({id: props.designId, openapi: JSON.stringify(content)});
  if (error) {
    return;
  }
  notification.success('保存成功');
};

const releaseDesign = async ()  => {
  console.log(updateContent)
  const [error] = await apis.releaseDesign(props.designId);
  if (error) {
    return;
  }
  notification.success('发布成功');
}

onMounted(async () => {
  loadingData.value = true;
  await getDesignContent();
  await getDesignInfo();
  openAPIDesignInstance.value = new OpenApiDesign({
    defaultFontSize: 12
  });
  loadingData.value = false;
})
</script>
<template>
<div class="h-full text-3">
  <Spin class="h-full" :spinning="loadingData">
    <component v-if="openAPIDesignInstance" :is="openAPIDesignInstance.compName" :open-api-doc="designContent">
      <div slot="docTitle" class="flex justify-center items-center space-x-2 mb-3">
        <Tag color="green" class="text-3.5 rounded-full">{{designInfo.openapiSpecVersion}}</Tag>
        <div class="text-5 font-medium">{{designInfo.name}}</div>
        <div>{{designInfo.lastModifiedByName}}最后修改于{{designInfo.lastModifiedDate}}</div>
        <div class="relative left-20 space-x-2">
          <Button
            type="primary"
            size="small"
            @click="updateContent">
            保存草稿
          </Button>
          <Button
            size="small"
            @click="releaseDesign">
            发布设计
          </Button>
        </div>
      </div>
    </component>
  </Spin>
</div>
</template>
<style scoped>
@import 'open-api-designer/style.css';
</style>
