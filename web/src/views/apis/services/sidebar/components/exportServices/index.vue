<script setup lang="ts">
import { inject, ref, watch, onMounted, computed } from 'vue';
import { Colon, Icon, IconText, Input, Modal, NoData, notification, Spin, TreeSelect,  SelectApisTable } from '@xcan-angus/vue-ui';
import { Checkbox, RadioGroup, Tree } from 'ant-design-vue';
import { services } from 'src/api/tester';
import { TESTER, download, site, utils, cookie, duration } from '@xcan-angus/tools';
import { debounce } from 'throttle-debounce';
import { createPdf } from '@xcan-angus/rapipdf';

type ProjectService = {
  id: string;
  name: string;
  pid: string;
  code: string;
  targetType: {
      value: string;
      message: string;
  },
  auth: false,
  status: {
      value: string;
      message: string;
  },
  createdBy: string;
  createdDate: string;
  sequence0: string;
}

interface Props {
  visible: boolean;
  type?: 'SERVICE' | 'APIS' | 'API';
  selectedNode?: ProjectService;
  id?: ProjectService;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  type: 'SERVICE',
  selectedNode: undefined,
  id: undefined
});

const projectInfo = inject('projectInfo', ref({ id: '' }));
const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

const params = ref<{pageNo: number; pageSize: number; filters?:{key:string, op:string, value:string}[] }>({ pageNo: 1, pageSize: 30 });
const exportType = ref<'SERVICE' | 'APIS' | 'API'>(props.type);
const format = ref<'json' | 'yaml' | 'pdf'>('json');
const docOrigin = ref();
const accessToken = ref();

// 按项目服务导出 查询项目服务名称 （要求前端查询，不仅过滤出匹配的节点，还需要保留选中的节点）
const handleInputChange = debounce(duration.search, (value:string) => {
  if (value) {
    params.value.filters = [{ key: 'name', op: 'MATCH_END', value: value }];
  } else {
    delete params.value.filters;
  }
  loadService();
});

// 当前展示的项目服务列表
const projectServices = ref<ProjectService[]>([]);
const projectServiceIds = ref<string[]>([]);
const loading = ref(false);
const total = ref(0);

// 初始化项目服务
const loadService = async () => {
  loading.value = true;
  const _params = props.type === 'SERVICE' ? { ...params.value, exportFlag: true, admin: true } : { ...params.value, exportFlag: true };
  const [error, { data }] = await services.loadList({ ..._params, projectId: projectInfo.value.id });
  loading.value = false;
  if (error) { return; }

  if (!data.list?.length) {
    projectServiceIds.value = [];
    projectServices.value = [];
    return;
  }

  total.value = data.total;
  const list = data.list;
  if (params.value.pageNo > 1) {
    projectServices.value = [...projectServices.value, ...list];
  } else {
    projectServices.value = list;
  }
  projectServiceIds.value = getAllServiceIds(projectServices.value);
};

const getAllServiceIds = (projectServices: ProjectService[]): string[] => {
  return projectServices.map(service => service.id);
};

const handleTreeScroll = (event) => {
  const target = event.target;
  // 判断是否滚动到底部
  if (target.scrollTop + target.offsetHeight >= target.scrollHeight - 80) {
    if (params.value.pageNo * params.value.pageSize >= +total.value) {
      return;
    }
    params.value.pageNo++;
    loadService();
  }
};

const serviceIds = ref<string[]>([]);
const checkedKeys = ref<{checked: string[]; halfChecked: string[];}>({ checked: [], halfChecked: [] });

const indeterminate = ref<boolean>(false);
const projectCheckAll = ref<boolean>(false);

// 按项目服务导出时全选和反选
const onCheckAllChange = e => {
  if (e.target.checked) {
    checkedKeys.value.checked = projectServiceIds.value;
    indeterminate.value = false;
    projectCheckAll.value = true;
  } else {
    indeterminate.value = false;
    projectCheckAll.value = false;
    checkedKeys.value.checked = [];
  }
};

const servicesCheck = (_checkedKeys) => {
  serviceIds.value = _checkedKeys.checked;
  if (serviceIds.value.length > 1 && format.value === 'pdf') {
    format.value = 'json';
  }
  if (_checkedKeys.checked.length) {
    const isEqual = utils.deepCompare(projectServiceIds.value, _checkedKeys.checked);
    if (isEqual) {
      indeterminate.value = false;
      projectCheckAll.value = true;
      return;
    }
    indeterminate.value = true;
    projectCheckAll.value = true;
    return;
  }

  indeterminate.value = false;
  projectCheckAll.value = false;
};

const apiIndeterminate = ref<boolean>(false);
// 按接口导出绑定的项目服务的id
const serviceId = ref<string>();
const exportLoading = ref(false);

// 导出
const handleOk = async () => {
  if (exportLoading.value) {
    return;
  }

  if (exportType.value === 'SERVICE' && !serviceIds.value.length) {
    notification.warning('请先选择服务');
    return;
  }

  if (exportType.value === 'APIS') {
    if (!serviceId.value || !serviceId.value.length) {
      notification.warning('请先选择服务');
      return;
    }
    if (!apiIds.value.length) {
      notification.warning('请先选择接口');
      return;
    }
  }

  let params: {
    serviceIds: string[];
    exportScope: 'SERVICE' | 'APIS' | 'API';
    format: 'json' | 'yaml' | 'pdf';
    onlyApisComponents: boolean;
    apiIds?: string[];
  } = {
    serviceIds: serviceIds.value,
    exportScope: exportType.value,
    format: format.value,
    onlyApisComponents: onlyApisComponents.value
  };
  if (exportType.value === 'APIS') {
    params = {
      ...params,
      serviceIds: [serviceId.value as string],
      apiIds: apiIds.value
    };
  }
  if (format.value === 'pdf') {
    let apiUrl = '';
    if (props.type === 'API') {
      apiUrl = `${docOrigin.value}${TESTER}/apis/${props.id}/openapi/export?format=yaml&access_token=${accessToken.value}`;
    } else {
      apiUrl = `${docOrigin.value}${TESTER}/services/${props.id || serviceIds.value[0]}/openapi?${new URLSearchParams({
        access_token: accessToken.value,
        ...(params.apiIds && { apiIds: params.apiIds.join(',') })
      })}`;
    }
    createPdf(apiUrl);
    handleCancel();
    return;
  }
  exportLoading.value = true;
  const host = await site.getUrl('apis');

  // 单接口导出
  if (props.type === 'API') {
    const apiUrl = `${host}${TESTER}/apis/${props.id}/openapi/export?format=${format.value}`;
    const [error] = await download(apiUrl);
    exportLoading.value = false;
    if (error) {
      return;
    }
  } else {
    const [error, res] = await services.exportServices({
      serviceIds: params.serviceIds,
      exportScope: params.exportScope,
      format: params.format,
      onlyApisComponents: params.onlyApisComponents,
      ...(params.apiIds ? { apiIds: params.apiIds } : {})
    }, { responseType: 'blob', timeout: 600000 });
    exportLoading.value = false;
    if (error) {
      return;
    }
    const disposition = res.headers['content-disposition'] || res.headers['Content-Disposition'];
    let filename = 'file';
    if (disposition) {
      filename = disposition.replace(/"/gi, '').replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1').replace(/(\S\s+);\S*/, '$1').replace(/\s$/, '');
      filename = decodeURIComponent(filename);
    }
    const url = URL.createObjectURL(res.data);
    downloadFile(url, filename);
    window.URL.revokeObjectURL(url);
  }

  handleCancel();
};

function downloadFile (url: string, filename: string) {
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}

const handleServiceChange = (id: string) => {
  serviceId.value = id;
};

// 当前选中的接口的ids
const apiIds = ref<string[]>([]);
const selectApis = (ids: string[]) => {
  apiIds.value = ids;
};

// 切换导出范围
const exportTypeChange = async e => {
  exportType.value = e.target.value;
  serviceId.value = undefined;
};

const onlyApisComponents = ref(false);

const handleCancel = () => {
  emit('update:visible', false);
};

let treeProps = {
  params: {
    hasPermission: 'EXPORT',
    admin: true
  },
  defaultValue: {
    name: props.selectedNode?.name,
    id: props.selectedNode?.id
  }
};

watch(() => props.visible, (newValue) => {
  if (!newValue) {
    return;
  }

  params.value = { pageNo: 1, pageSize: 30 };
  exportType.value = props.type;
  format.value = 'json';
  projectServices.value = [];
  projectServiceIds.value = [];
  total.value = 0;
  serviceIds.value = [];
  checkedKeys.value = { checked: [], halfChecked: [] };
  indeterminate.value = false;
  projectCheckAll.value = false;
  apiIndeterminate.value = false;
  serviceId.value = undefined;
  apiIds.value = [];
  onlyApisComponents.value = false;

  if (props.type === 'SERVICE') {
    loadService();
  }

  if (props.type === 'APIS') {
    serviceId.value = props.selectedNode?.id;
  }

  treeProps = {
    params: {
      hasPermission: 'EXPORT',
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

const exportTypes = [{
  label: '按服务',
  value: 'SERVICE'
}, {
  label: '按接口',
  value: 'APIS'
}];

const compTypeOption = [{
  label: '服务所有组件',
  value: false
}, {
  label: '仅接口关联组件',
  value: true
}];

const formatTypes = computed(() => [{
  label: 'JSON',
  value: 'json'
},
{
  label: 'YAML',
  value: 'yaml'
},
(props.type === 'API') && {
  label: 'PDF',
  value: 'pdf'
}].filter(Boolean));

onMounted(async () => {
  accessToken.value = cookie.get('access_token');
  docOrigin.value = await site.getUrl('apis');
});

</script>
<template>
  <Modal
    :visible="props.visible"
    :width="800"
    title="导出"
    @cancel="handleCancel"
    @ok="handleOk">
    <Spin :spinning="exportLoading">
      <div class="flex flex-wrap">
        <template v-if="props.type !=='APIS' && props.type !=='API'">
          <div class="flex-1/2">
            <span>范围<Colon class="ml-1 mr-3.5" /></span>
            <RadioGroup
              :value="exportType"
              :options="exportTypes"
              @change="exportTypeChange">
            </RadioGroup>
          </div>
        </template>

        <template v-if="props.type !=='API'">
          <div class="mt-1.5 flex-1/2">
            <span>组件<Colon class="ml-1 mr-3.5" /></span>
            <RadioGroup
              v-model:value="onlyApisComponents"
              :options="compTypeOption">
            </RadioGroup>
          </div>
        </template>

        <div class="mt-1.5 flex-1/2">
          <span>格式<Colon class="ml-1 mr-3.5" /></span>
          <RadioGroup
            v-model:value="format"
            :options="formatTypes">
          </RadioGroup>
        </div>

        <template v-if="exportType === 'SERVICE'">
          <Input
            placeholder="查询服务名称"
            size="small"
            class="my-2 flex-1/2"
            allowClear
            @change="handleInputChange($event.target.value)">
            <template #suffix>
              <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
            </template>
          </Input>
        </template>

        <template v-if="exportType === 'APIS'">
          <div class="mt-2 inline-flex flex-1/2">
            <div class="inline-flex items-center">
              <span>服务<Colon class="ml-1 mr-3.5" /></span>
            </div>
            <TreeSelect
              ref="treeRef"
              :allowClear="true"
              :action="`${TESTER}/services/search?projectId=${projectInfo.id}`"
              :fieldNames="{ label: 'name', value: 'id', children: 'children' }"
              placeholder="请选择服务"
              showSearch
              class="flex-1"
              v-bind="treeProps"
              @change="handleServiceChange">
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

      <template v-if="exportType === 'SERVICE'">
        <div class="border border-border-divider p-2 rounded" style="height: 406px;">
          <div class="flex py-1 px-4 bg-bg-table-head text-text-title text-3 font-normal">
            <Checkbox
              class="checkbox-border-black"
              :checked="projectCheckAll"
              :indeterminate="indeterminate"
              @change="onCheckAllChange">
              服务名称
            </Checkbox>
          </div>
          <Spin
            :spinning="loading"
            class="leading-4 text-3 overflow-hidden hover:overflow-y-auto flex-1 -mx-2"
            style="scrollbar-gutter: stable;height: 360px;"
            @scroll="handleTreeScroll">
            <template v-if="projectServices.length">
              <Tree
                v-model:checkedKeys="checkedKeys"
                checkable
                checkStrictly
                :allowClear="false"
                :treeData="projectServices"
                :fieldNames="{ children: 'children', title: 'name', key: 'id' }"
                @check="servicesCheck">
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

      <template v-if="exportType === 'APIS'">
        <SelectApisTable
          v-if="props.visible"
          class="mt-2"
          :projectId="projectInfo?.id"
          :serviceId="serviceId"
          @change="selectApis" />
      </template>
    </Spin>
  </Modal>
</template>
<style scoped>
:deep(.checkbox-border-black .ant-checkbox-inner) {
  border-color: #d3dce6;
}
</style>
