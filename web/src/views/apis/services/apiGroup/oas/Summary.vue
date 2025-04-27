<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import { Colon } from '@xcan-angus/vue-ui';

interface Props {
    value: {
        externalDocs:{
            url:string;
        };
        info:{
            description:string;
            title:string;
            summary:string;
            version:string;
            termsOfService:string;
            license:{
                name:string;
                url:string;
            };
            contact:{
                email:string;
                name:string;
                url:string;
            };
        }
    }
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const description = ref<string>();

onMounted(() => {
  watch(() => props.value, async () => {
    description.value = await cleanHtml(props.value?.info?.description || '');
  }, { immediate: true });
});

const cleanHtml = async (htmlStr: string) => {
  const str = await marked(htmlStr || '');
  return DOMPurify.sanitize(str);
};

const openapiInfo = computed(() => {
  return props.value?.info;
});

const title = computed(() => {
  return openapiInfo.value?.title;
});

const summary = computed(() => {
  return openapiInfo.value?.summary;
});

const version = computed(() => {
  return openapiInfo.value?.version;
});

const termsOfService = computed(() => {
  return openapiInfo.value?.termsOfService;
});

const license = computed(() => {
  return openapiInfo.value?.license;
});

const contact = computed(() => {
  return openapiInfo.value?.contact;
});

const externalDocs = computed(() => {
  return props.value?.externalDocs;
});
</script>

<template>
  <div>
    <div class="text-4 text-theme-title leading-5.5 mt-1 break-all mb-3">{{ title }}</div>
    <div class="flex items-start mb-2.5">
      <div class="text-theme-title flex-shrink-0 w-17">
        <span>摘要</span>
        <Colon />
      </div>
      <div class="text-theme-sub-content">{{ summary }}</div>
    </div>
    <div class="flex items-start mb-2.5">
      <div class="text-theme-title flex-shrink-0 w-17">
        <span>版本号</span>
        <Colon />
      </div>
      <div class="text-theme-sub-content">{{ version }}</div>
    </div>
    <div class="flex items-start mb-2.5">
      <div class="text-theme-title flex-shrink-0 w-17">
        <span>服务条款</span>
        <Colon />
      </div>
      <a
        :href="termsOfService"
        target="_blank"
        style="color:#1890ff"
        class="truncate">{{ termsOfService }}</a>
    </div>
    <div class="flex items-start mb-2.5">
      <div class="text-theme-title flex-shrink-0 w-17">
        <span>许可协议</span>
        <Colon />
      </div>
      <div class="flex items-center overflow-hidden space-x-3">
        <span v-if="license?.name" class="text-theme-sub-content">{{ license?.name }}</span>
        <a
          v-if="license?.url"
          :href="license?.url"
          target="_blank"
          style="color:#1890ff"
          class="truncate">{{ license?.url }}</a>
      </div>
    </div>
    <div class="flex items-start mb-2.5">
      <div class="text-theme-title flex-shrink-0 w-17">
        <span>联系人</span>
        <Colon />
      </div>
      <div class="flex items-center overflow-hidden space-x-3">
        <span v-if="contact?.name" class="text-theme-sub-content">{{ contact?.name }}</span>
        <span v-if="contact?.email" class="text-theme-sub-content">{{ contact?.email }}</span>
        <a
          v-if="contact?.url"
          :href="contact?.url"
          target="_blank"
          style="color:#1890ff"
          class="truncate">{{ contact?.url }}</a>
      </div>
    </div>
    <div class="flex items-start mb-2.5">
      <div class="text-theme-title flex-shrink-0 w-17">
        <span>外部文档</span>
        <Colon />
      </div>
      <a
        :href="externalDocs?.url"
        target="_blank"
        style="color:#1890ff"
        class="truncate">{{ externalDocs?.url }}</a>
    </div>
    <div
      class="renderedMarkdown-wrapper text-theme-sub-content break-words whitespace-normal mb-3"
      v-html="description"></div>
  </div>
</template>

<style scoped>
.renderedMarkdown-wrapper :deep(a) {
  color: #1890ff;
}

.renderedMarkdown-wrapper :deep(em) {
  font-style: normal;
}
</style>
