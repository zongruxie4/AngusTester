<script setup lang="ts">
import { inject, ref, watch, onMounted, computed, Ref } from 'vue';
import {
  Colon, Icon, IconText, Input, Modal, NoData, notification, Spin, TreeSelect, SelectApisTable
} from '@xcan-angus/vue-ui';
import { Checkbox, RadioGroup, Tree } from 'ant-design-vue';
import { services } from '@/api/tester';
import {
  TESTER, download, utils, cookieUtils, duration, DomainManager, ApiUrlBuilder, routerUtils, ApiType, SearchCriteria
} from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import { ServicesInfo, ExportType } from '@/views/apis/services/services/types';
import { ServicesPermission } from '@/enums/enums';

interface Props {
  visible: boolean;
  type?: ExportType;
  selectedNode?: ServicesInfo;
  id?: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  type: ExportType.SERVICE,
  selectedNode: undefined,
  id: undefined
});

const { t } = useI18n();

// PDF creation function (dynamically imported)
let createPdf: ((url: string) => void) | undefined;
const importCreatePdf = () => {
  import('@xcan-angus/rapipdf').then((res) => {
    createPdf = res.createPdf;
  });
};

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

// Refs for pagination parameters
const paginationParams = ref<{pageNo: number; pageSize: number; filters?:SearchCriteria[] }>({
  pageNo: 1,
  pageSize: 30
});

// Ref for the export type (SERVICE, APIS, or API)
const exportType = ref<ExportType>(props.type);

// Ref for the export format (json, yaml, or pdf)
const exportFormat = ref<'json' | 'yaml' | 'pdf'>('json');

// Refs for document origin and access token
const documentOrigin = ref();
const accessToken = ref();

/**
 * Handle input change for service search
 * Debounced to prevent excessive API calls
 * @param searchValue - The search input value
 */
const handleServiceSearch = debounce(duration.search, (searchValue: string) => {
  if (searchValue) {
    paginationParams.value.filters = [{ key: 'name', op: SearchCriteria.OpEnum.Match, value: searchValue }];
  } else {
    delete paginationParams.value.filters;
  }
  loadServices();
});

// Refs for project services data
const projectServices = ref<ServicesInfo[]>([]);
const projectServiceIds = ref<string[]>([]);
const isLoadingServices = ref(false);
const totalServices = ref(0);

/**
 * Load services for export selection
 */
const loadServices = async () => {
  isLoadingServices.value = true;
  const requestParams = props.type === ExportType.SERVICE
    ? {
        ...paginationParams.value,
        exportFlag: true,
        admin: true
      }
    : {
        ...paginationParams.value,
        exportFlag: true
      };

  const [error, { data }] = await services.getList({
    ...requestParams,
    projectId: projectId.value
  });

  isLoadingServices.value = false;
  if (error) {
    return;
  }

  if (!data.list?.length) {
    projectServiceIds.value = [];
    projectServices.value = [];
    return;
  }

  totalServices.value = data.total;
  const list = data.list;
  if (paginationParams.value.pageNo > 1) {
    projectServices.value = [...projectServices.value, ...list];
  } else {
    projectServices.value = list;
  }
  projectServiceIds.value = extractServiceIds(projectServices.value);
};

/**
 * Extract service IDs from a list of services
 * @param services - Array of service objects
 * @returns Array of service IDs
 */
const extractServiceIds = (services: ServicesInfo[]): string[] => {
  return services.map(service => service.id);
};

/**
 * Handle scroll event for infinite scrolling in service list
 * @param event - Scroll event object
 */
const handleServiceListScroll = (event: Event) => {
  const target = event.target as HTMLElement;
  // Check if scrolled to bottom
  if (target.scrollTop + target.offsetHeight >= target.scrollHeight - 80) {
    if (paginationParams.value.pageNo * paginationParams.value.pageSize >= +totalServices.value) {
      return;
    }
    paginationParams.value.pageNo++;
    loadServices();
  }
};

// Refs for service selection
const selectedServiceIds = ref<string[]>([]);
const checkedKeys = ref<{checked: string[]; halfChecked: string[]}>({
  checked: [],
  halfChecked: []
});

// Refs for checkbox states
const isIndeterminate = ref<boolean>(false);
const isProjectCheckAll = ref<boolean>(false);

/**
 * Handle "select all" checkbox change
 * @param event - Checkbox change event
 */
const handleSelectAllChange = (event: { target: { checked: boolean } }) => {
  if (event.target.checked) {
    checkedKeys.value.checked = projectServiceIds.value;
    isIndeterminate.value = false;
    isProjectCheckAll.value = true;
  } else {
    isIndeterminate.value = false;
    isProjectCheckAll.value = false;
    checkedKeys.value.checked = [];
  }
};

/**
 * Handle service selection changes in the tree
 * @param checkedKeysObj - Object containing checked and half-checked keys
 */
const handleServiceSelection = (checkedKeysObj: {
  checked: string[],
  halfChecked: string[]
}) => {
  selectedServiceIds.value = checkedKeysObj.checked;
  // PDF format only supports single service export
  if (selectedServiceIds.value.length > 1 && exportFormat.value === 'pdf') {
    exportFormat.value = 'json';
  }

  if (checkedKeysObj.checked.length) {
    const isEqual = utils.deepCompare(projectServiceIds.value, checkedKeysObj.checked);
    if (isEqual) {
      isIndeterminate.value = false;
      isProjectCheckAll.value = true;
      return;
    }
    isIndeterminate.value = true;
    isProjectCheckAll.value = true;
    return;
  }

  isIndeterminate.value = false;
  isProjectCheckAll.value = false;
};

// Ref for API selection indeterminate state
const isApiIndeterminate = ref<boolean>(false);

// Ref for the selected service ID when exporting APIs
const selectedServiceId = ref<string>();

// Ref for tracking export loading state
const isExportLoading = ref(false);

/**
 * Handle the export action
 * Supports exporting services, APIs, or individual API in JSON, YAML, or PDF formats
 */
const handleExport = async () => {
  // Prevent multiple simultaneous exports
  if (isExportLoading.value) {
    return;
  }

  // Validate service selection for SERVICE export type
  if (exportType.value === ExportType.SERVICE && !selectedServiceIds.value.length) {
    notification.warning(t('service.service.exportServiceModal.serviceTip'));
    return;
  }

  // Validate service and API selection for APIS export type
  if (exportType.value === ExportType.APIS) {
    if (!selectedServiceId.value || !selectedServiceId.value.length) {
      notification.warning(t('service.service.exportServiceModal.serviceTip'));
      return;
    }
    if (!selectedApiIds.value.length) {
      notification.warning(t('service.service.exportServiceModal.apiTip'));
      return;
    }
  }

  // Prepare export parameters
  let exportParams: {
    serviceIds: string[];
    exportScope: ExportType;
    format: 'json' | 'yaml' | 'pdf';
    onlyApisComponents: boolean;
    apiIds?: string[];
  } = {
    serviceIds: selectedServiceIds.value,
    exportScope: exportType.value,
    format: exportFormat.value,
    onlyApisComponents: shouldExportOnlyApisComponents.value
  };

  // Adjust parameters for APIS export type
  if (exportType.value === ExportType.APIS) {
    exportParams = {
      ...exportParams,
      serviceIds: [selectedServiceId.value as string],
      apiIds: selectedApiIds.value
    };
  }

  // Handle PDF export specially
  if (exportFormat.value === 'pdf') {
    let apiUrl = '';
    if (props.type === ExportType.API) {
      apiUrl = `${documentOrigin.value}${TESTER}/apis/${props.id}/openapi/export?format=yaml&access_token=${accessToken.value}`;
    } else {
      apiUrl = `${documentOrigin.value}${TESTER}/services/${props.id || selectedServiceIds.value[0]}/openapi?${new URLSearchParams({
        access_token: accessToken.value,
        ...(exportParams.apiIds && { apiIds: exportParams.apiIds.join(',') })
      })}`;
    }

    if (createPdf) {
      createPdf(apiUrl);
    }
    handleCloseModal();
    return;
  }

  // Handle JSON/YAML exports
  isExportLoading.value = true;
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);

  // Export a single API
  if (props.type === ExportType.API) {
    const apiUrl = ApiUrlBuilder.buildApiUrl(routeConfig, `/apis/${props.id}/openapi/export?format=${exportFormat.value}`);
    const [error] = await download(apiUrl);
    isExportLoading.value = false;
    if (error) {
      return;
    }
  } else {
    // Export services or APIs
    const [error, res] = await services.exportServices({
      serviceIds: exportParams.serviceIds,
      exportScope: exportParams.exportScope,
      format: exportParams.format,
      onlyApisComponents: exportParams.onlyApisComponents,
      ...(exportParams.apiIds ? { apiIds: exportParams.apiIds } : {})
    }, { responseType: 'blob', timeout: 600000 });

    isExportLoading.value = false;
    if (error) {
      return;
    }

    // Extract filename from response headers
    const disposition = res.headers['content-disposition'] || res.headers['Content-Disposition'];
    let filename = 'exported-file';
    if (disposition) {
      filename = disposition.replace(/"/gi, '').replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1').replace(/(\S\s+);\S*/, '$1').replace(/\s$/, '');
      filename = decodeURIComponent(filename);
    }

    // Create object URL and trigger download
    const url = URL.createObjectURL(res.data);
    triggerFileDownload(url, filename);
    window.URL.revokeObjectURL(url);
  }

  handleCloseModal();
};

/**
 * Trigger file download using a dynamically created anchor element
 * @param url - The URL of the file to download
 * @param filename - The name to save the file as
 */
function triggerFileDownload (url: string, filename: string) {
  const downloadLink = document.createElement('a');
  downloadLink.style.display = 'none';
  downloadLink.href = url;
  downloadLink.download = filename;
  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);
}

/**
 * Handle service selection change in the TreeSelect component
 * @param id - Selected service ID
 */
const handleServiceSelectionChange = (id: string) => {
  selectedServiceId.value = id;
};

// Ref for selected API IDs
const selectedApiIds = ref<string[]>([]);

/**
 * Handle API selection changes
 * @param ids - Array of selected API IDs
 */
const handleApiSelection = (ids: string[]) => {
  selectedApiIds.value = ids;
};

/**
 * Handle export type change
 * @param event - Radio group change event
 */
const handleExportTypeChange = (event: { target: { value: ExportType } }) => {
  exportType.value = event.target.value;
  selectedServiceId.value = undefined;
};

// Ref for "only APIs components" option
const shouldExportOnlyApisComponents = ref(false);

/**
 * Handle closing the modal
 */
const handleCloseModal = () => {
  emit('update:visible', false);
};

// TreeSelect component properties
let treeSelectProps = {
  params: {
    hasPermission: ServicesPermission.EXPORT,
    admin: true
  },
  defaultValue: {
    name: props.selectedNode?.name,
    id: props.selectedNode?.id
  }
};

/**
 * Watch for visibility changes to reset component state
 */
watch(() => props.visible, (isVisible) => {
  if (!isVisible) {
    return;
  }

  // Reset all state when modal becomes visible
  paginationParams.value = { pageNo: 1, pageSize: 30 };
  exportType.value = props.type;
  exportFormat.value = 'json';
  projectServices.value = [];
  projectServiceIds.value = [];
  totalServices.value = 0;
  selectedServiceIds.value = [];
  checkedKeys.value = { checked: [], halfChecked: [] };
  isIndeterminate.value = false;
  isProjectCheckAll.value = false;
  isApiIndeterminate.value = false;
  selectedServiceId.value = undefined;
  selectedApiIds.value = [];
  shouldExportOnlyApisComponents.value = false;

  // Load services if export type is SERVICE
  if (props.type === ExportType.SERVICE) {
    loadServices();
  }

  // Set selected service ID if export type is APIS
  if (props.type === ExportType.APIS) {
    selectedServiceId.value = props.selectedNode?.id;
  }

  // Update TreeSelect properties
  treeSelectProps = {
    params: {
      hasPermission: ServicesPermission.EXPORT,
      admin: true
    },
    defaultValue: {
      name: props.selectedNode?.name,
      id: props.selectedNode?.id
    }
  };
}, {
  immediate: true
});

// Export type options for the radio group
const exportTypeOptions = [{
  label: t('service.service.exportServiceModal.exportType_service'),
  value: 'SERVICE'
}, {
  label: t('service.service.exportServiceModal.exportType_api'),
  value: 'APIS'
}];

// Component type options for the radio group
const componentTypeOptions = [{
  label: t('service.service.exportServiceModal.compType_all'),
  value: false
}, {
  label: t('service.service.exportServiceModal.compType_api'),
  value: true
}];

// Computed property for format options (PDF only available for API type)
const formatOptions = computed(() => [{
  label: 'JSON',
  value: 'json'
},
{
  label: 'YAML',
  value: 'yaml'
},
(props.type === ExportType.API) && {
  label: 'PDF',
  value: 'pdf'
}].filter(Boolean));

/**
 * Initialize component on mount
 * Retrieve access token and document origin
 */
onMounted(async () => {
  accessToken.value = cookieUtils.getTokenInfo().access_token;
  documentOrigin.value = DomainManager.getInstance().getApiDomain('tester');
  if (props.type === ExportType.API) {
    importCreatePdf();
  }
});
</script>

<template>
  <Modal
    :visible="props.visible"
    :width="800"
    :title="t('service.service.exportServiceModal.title')"
    @cancel="handleCloseModal"
    @ok="handleExport">
    <Spin :spinning="isExportLoading">
      <div class="flex flex-wrap">
        <template v-if="props.type !== ExportType.APIS && props.type !== ExportType.API">
          <div class="flex-1/2">
            <span>{{ t('service.service.exportServiceModal.exportTypeLabel') }}<Colon class="ml-1 mr-3.5" /></span>
            <RadioGroup
              :value="exportType"
              :options="exportTypeOptions"
              @change="handleExportTypeChange">
            </RadioGroup>
          </div>
        </template>

        <template v-if="props.type !== ExportType.API">
          <div class="mt-1.5 flex-1/2">
            <span>{{ t('service.service.exportServiceModal.onlyApisComponentsLabel') }}<Colon class="ml-1 mr-3.5" /></span>
            <RadioGroup
              v-model:value="shouldExportOnlyApisComponents"
              :options="componentTypeOptions">
            </RadioGroup>
          </div>
        </template>

        <div class="mt-1.5 flex-1/2">
          <span>{{ t('service.service.exportServiceModal.formatLabel') }}<Colon class="ml-1 mr-3.5" /></span>
          <RadioGroup
            v-model:value="exportFormat"
            :options="formatOptions">
          </RadioGroup>
        </div>

        <template v-if="exportType === ExportType.SERVICE">
          <Input
            :placeholder="t('service.service.exportServiceModal.serviceNamePlaceholder')"
            size="small"
            class="my-2 flex-1/2"
            allowClear
            @change="handleServiceSearch($event.target.value)">
            <template #suffix>
              <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
            </template>
          </Input>
        </template>

        <template v-if="exportType === ExportType.APIS">
          <div class="mt-2 inline-flex flex-1/2">
            <div class="inline-flex items-center">
              <span>{{ t('service.service.exportServiceModal.serviceLabel') }}<Colon class="ml-1 mr-3.5" /></span>
            </div>
            <TreeSelect
              ref="treeRef"
              :allowClear="true"
              :action="`${TESTER}/services?projectId=${projectId}&fullTextSearch=true`"
              :fieldNames="{ label: 'name', value: 'id', children: 'children' }"
              :placeholder="t('service.service.exportServiceModal.servicePlaceholder')"
              showSearch
              class="flex-1"
              v-bind="treeSelectProps"
              @change="handleServiceSelectionChange">
              <template #title="{ name }">
                <div class="flex items-center leading-6.5 h-6.5 space-x-1.5">
                  <IconText
                    style="width: 16px;height: 16px;background-color: rgb(162, 222, 236);"
                    text="S"
                    class="flex-shrink-0" />
                  <div :title="name" class="flex-1 truncate">{{ name }}</div>
                </div>
              </template>
            </TreeSelect>
          </div>
        </template>
      </div>

      <template v-if="exportType === ExportType.SERVICE">
        <div class="border border-border-divider p-2 rounded" style="height: 406px;">
          <div class="flex py-1 px-4 bg-bg-table-head text-text-title text-3 font-normal">
            <Checkbox
              class="checkbox-border-black"
              :checked="isProjectCheckAll"
              :indeterminate="isIndeterminate"
              @change="handleSelectAllChange">
              {{ t('service.service.exportServiceModal.serviceNameLabel') }}
            </Checkbox>
          </div>
          <Spin
            :spinning="isLoadingServices"
            class="leading-4 text-3 overflow-hidden hover:overflow-y-auto flex-1 -mx-2"
            style="scrollbar-gutter: stable;height: 360px;"
            @scroll="handleServiceListScroll">
            <template v-if="projectServices.length">
              <Tree
                v-model:checkedKeys="checkedKeys"
                checkable
                checkStrictly
                :allowClear="false"
                :treeData="projectServices"
                :fieldNames="{ children: 'children', title: 'name', key: 'id' }"
                @check="handleServiceSelection">
                <template #title="node">
                  <div class="flex items-center space-x-2 text-3">
                    <div
                      :title="node.name"
                      style="width: 386px;"
                      class="truncate">
                      {{ node.name }}
                    </div>
                  </div>
                </template>
              </Tree>
            </template>
            <template v-else>
              <NoData class="h-full" />
            </template>
          </Spin>
        </div>
      </template>

      <template v-if="exportType === ExportType.APIS">
        <SelectApisTable
          v-if="props.visible"
          class="mt-2"
          :projectId="projectId"
          :serviceId="selectedServiceId"
          @change="handleApiSelection" />
      </template>
    </Spin>
  </Modal>
</template>

<style scoped>
:deep(.checkbox-border-black .ant-checkbox-inner) {
  border-color: #d3dce6;
}
</style>
