<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { Tag, Button } from 'ant-design-vue';
import { apis } from '@/api/tester';
import OpenApiDesign from 'open-api-designer';
import { notification, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

interface Props {
  designId: string
}

const props = withDefaults(defineProps<Props>(), {
  designId: ''
});

const { t } = useI18n();
const designInfo = ref<{[key: string]: string}>({});
const designContent = ref();
const openApiDesignerRef = ref();
const isLoading = ref(false);

function blobToJson(blob) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsText(blob, 'utf-8');
        reader.onload = function(e) {
            try {
                const jsonData = JSON.parse(reader.result as string);
                resolve(jsonData);
            } catch (error) {
              resolve({});
            }
        };
        reader.onerror = function(e) {
            reject(new Error('read blob failed'));
        };
    });
}
/**
 * Fetch the openapi content for current design and cache as string.
 */
const fetchDesignContent = async () => {
  const [error, resp] = await apis.exportDesign({ format: 'json', id: props.designId }, {responseType: 'blob'});
  if (error) {
    return;
  }
  const text = await blobToJson(resp.data);
  designContent.value = JSON.stringify(text);
};

/**
 * Fetch meta information for current design such as name and version.
 */
const fetchDesignInfo = async () => {
  const [error, resp] = await apis.getDesignDetail(props.designId);
  if (error) {
    return;
  }
  designInfo.value = resp?.data || {};
};

/**
 * Persist the latest openapi content from designer to backend service.
 */
const saveContent = async () => {
  if (typeof openApiDesignerRef.value?.updateData === 'function') {
    openApiDesignerRef.value.updateData();
  }

  const content = (openApiDesignerRef.value && typeof openApiDesignerRef.value.getDocApi === 'function')
    ? openApiDesignerRef.value.getDocApi()
    : designContent.value;
  const [error] = await apis.putDesignContent({ id: props.designId, openapi: JSON.stringify(content) });
  if (error) {
    return;
  }
  notification.success(t('actions.tips.saveSuccess'));
};

/**
 * Publish current design. After success, user will see a publish success message.
 */
const publishDesign = async () => {
  const [error] = await apis.releaseDesign(props.designId);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.publishSuccess'));
};

onMounted(async () => {
  isLoading.value = true;
  await fetchDesignContent();
  await fetchDesignInfo();
  openApiDesignerRef.value = new OpenApiDesign({
    defaultFontSize: 12
  });
  isLoading.value = false;
});
</script>
<template>
  <div class="h-full text-3">
    <Spin class="h-full" :spinning="isLoading">
      <component
        :is="openApiDesignerRef.compName"
        v-if="openApiDesignerRef"
        :openApiDoc="designContent">
        <div
          slot="docTitle"
          class="flex justify-center items-center space-x-2 mb-3">
          <Tag color="green" class="text-3.5 rounded-full">
            {{ designInfo.openapiSpecVersion }}
          </Tag>

          <div class="text-5 font-medium">{{ designInfo.name }}</div>

          <div>{{ designInfo.modifier }}{{ t('status.modifiedAt')}} {{ designInfo.modifiedDate }}</div>

          <div class="relative left-20 space-x-2">
            <Button
              type="primary"
              size="small"
              @click="saveContent">
              {{ t('apiDesign.actions.saveDraft') }}
            </Button>
            <Button
              size="small"
              @click="publishDesign">
              {{ t('apiDesign.actions.publish') }}
            </Button>
          </div>
        </div>
      </component>
    </Spin>
  </div>
</template>
