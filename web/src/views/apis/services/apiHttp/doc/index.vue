<script setup lang="ts">
import { defineAsyncComponent, ref, onMounted } from 'vue';
import { site, cookie, TESTER } from '@xcan-angus/tools';
import { Button } from 'ant-design-vue';

import '@xcan-angus/rapidoc';
// import { createPdf } from 'rapipdf';

interface Props {
  id: string;
}
const props = withDefaults(defineProps<Props>(), {
  id: undefined
});
const emit = defineEmits<{(e: 'update:data', value: any):void}>();

const ExportApi = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/exportServices/index.vue'));

// visible: boolean;
//   type?: 'SERVICE' | 'APIS' | 'API';
//   selectedNode?: ProjectService;
//   id?: ProjectService;
const docOrigin = ref();
const accessToken = ref();
const isPrivate = ref();


const exportVisible = ref(false);
const handleExportdoc = () => {
  exportVisible.value = true;
};

onMounted(async () => {
  accessToken.value = cookie.get('access_token');
  isPrivate.value = await site.isPrivate();
  docOrigin.value = await site.getUrl('apis');
});

</script>
<template>
  <div class="">
    <rapi-doc
      v-if="docOrigin"
      :spec-url="`${docOrigin}${isPrivate ? '/api/v1' : TESTER}/apis/${props.id}/openapi/export?format=yaml&access_token=${accessToken}`"
      allowSpecFileDownload="false"
      allowSpecFileLoad="false"
      allowSpecUrlLoad="false"
      allowTry="false"
      allowAdvancedSearch="false"
      bgColor="#fff"
      headerColor="#fff"
      renderStyle="view"
      schemaExpandLevel="20"
      schemaStyle="table"
      showHeader="true"
      showInfo="true"
      theme="light"
      updateRoute="false">
      <!-- Delete the default logo at the top -->
      <Button
        slot="exportDoc"
        class="text-3 ml-2"
        @click="handleExportdoc">
        导出
      </Button>
    </rapi-doc>

    <ExportApi
      :id="props.id"
      v-model:visible="exportVisible"
      type="API" />
  </div>
</template>
