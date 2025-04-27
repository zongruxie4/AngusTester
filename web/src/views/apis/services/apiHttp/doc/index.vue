<script setup lang="ts">
import { defineAsyncComponent, ref, onMounted } from 'vue';
import { site, cookie, TESTER } from '@xcan-angus/tools';
import { Button } from 'ant-design-vue';

import '@xcan/rapidoc';
import { createPdf } from '@xcan/rapipdf';
// import 'rapidoc';
// import 'rapipdf';
// import 'rapipdf';

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

const exportPdf = () => {
  createPdf(`${docOrigin.value}${TESTER}/apis/${props.id}/openapi/export?format=yaml&access_token=${accessToken.value}`);
};

const exportVisible = ref(false);
const handleExportdoc = () => {
  exportVisible.value = true;
};

onMounted(async () => {
  accessToken.value = cookie.get('access_token');
  docOrigin.value = await site.getUrl('apis');
});

</script>
<template>
  <div class="">
    <!-- <Button @click="exportPdf">PDF</Button>
    <rapi-pdf></rapi-pdf> -->
    <rapi-doc
      v-if="docOrigin"
      :specUrl="`${docOrigin}${TESTER}/apis/${props.id}/openapi/export?format=yaml&access_token=${accessToken}`"
      renderStyle="view"
      theme="light"
      headerColor="#fff"
      :showInfo="true"
      allowSpecUrlLoad="false"
      :allowSpecFileLoad="false"
      :allowSpecFileDownload="false"
      :allowTry="false"
      bgColor="#fff"
      schemaStyle="table"
      :showHeader="true"
      updateRoute="false"
      schemaExpandLevel="20">
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
