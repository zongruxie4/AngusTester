<script setup lang="ts">
import { defineAsyncComponent, ref, onMounted } from 'vue';
import { TESTER, VERSION, API, cookieUtils, appContext, routerUtils, ApiType, ApiUrlBuilder } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';
import '@xcan-angus/rapidoc';
import { useI18n } from 'vue-i18n';

const ExportApiModal = defineAsyncComponent(() => import('@/views/apis/services/components/ExportOptionalModal.vue'));

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

defineEmits<{(e: 'update:data', value: any): void}>();

const { t } = useI18n();

/**
 * API documentation origin URL
 */
const apiDocumentationOrigin = ref();

/**
 * Access token for API authentication
 */
const userAccessToken = ref();

/**
 * Whether the application is in private edition
 */
const isPrivateEdition = ref();

/**
 * Export modal visibility state
 */
const isExportModalVisible = ref(false);

/**
 * Handles API documentation export
 * <p>
 * Shows the export modal when user clicks export button
 * </p>
 */
const handleApiDocumentationExport = () => {
  isExportModalVisible.value = true;
};

/**
 * Component mounted lifecycle hook
 * <p>
 * Initializes component data including access token, edition type, and API URL
 * </p>
 */
onMounted(async () => {
  userAccessToken.value = cookieUtils.getTokenInfo().access_token;
  isPrivateEdition.value = appContext.isPrivateEdition();
  apiDocumentationOrigin.value = ApiUrlBuilder.buildApiUrl(
    routerUtils.getTesterApiRouteConfig(ApiType.API),
    ''
  );
});
</script>
<template>
  <div class="">
    <rapi-doc
      v-if="apiDocumentationOrigin"
      :spec-url="`${apiDocumentationOrigin}${isPrivateEdition
        ? `/${API}/${VERSION}`
        : ''}/apis/${props.id}/openapi/export?format=yaml&access_token=${userAccessToken}`"
      allow-spec-file-download="false"
      allow-spec-file-load="false"
      allow-spec-url-load="false"
      allow-try="false"
      allow-advanced-search="false"
      bg-color="#fff"
      header-color="#fff"
      render-style="view"
      schema-expand-level="20"
      schema-style="table"
      show-header="true"
      show-info="true"
      theme="light"
      update-route="false">
      <!-- Export button for API documentation -->
      <Button
        slot="exportDoc"
        class="text-3 ml-2"
        @click="handleApiDocumentationExport">
        {{ t('actions.export') }}
      </Button>
    </rapi-doc>

    <ExportApiModal
      :id="props.id"
      v-model:visible="isExportModalVisible"
      type="API" />
  </div>
</template>
