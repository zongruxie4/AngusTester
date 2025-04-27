<script setup lang="ts">
import { computed } from 'vue';

import { ReportContent } from '../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const services = computed(() => {
  return props.dataSource?.content?.services;
});

const columns = computed(() => [
  [
    {
      dataIndex: 'name',
      name: '名称'
    },
    {
      dataIndex: 'status',
      name: '状态',
      customRender: (text) => {
        return text?.message;
      }
    }
  ],
  [
    {
      dataIndex: 'projectName',
      name: '所属项目'
    },
    {
      dataIndex: 'apisNum',
      name: '接口数'
    }
  ],
  [
    {
      dataIndex: 'apisCaseNum',
      name: '接口用例数'
    },
    {
      dataIndex: 'createdByName',
      name: '添加人'
    }
  ],
  [
    {
      dataIndex: 'createdDate',
      name: '添加时间'
    },
    {
      dataIndex: 'lastModifiedByName',
      name: '最后修改人'
    }
  ],
  [
    {
      dataIndex: 'lastModifiedDate',
      name: '最后修改时间'
    },
    {
      dataIndex: '',
      name: ''
    }
  ]
]);

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1" class="text-4 text-theme-title font-medium">一、<em class="inline-block w-0.25"></em>服务信息</span>
    </h1>
    <div class="border-t border-l border-solid border-border-input">
      <div v-for="column in columns" class="flex border-b border-solid border-border-input">
        <template v-for="item in column">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ item.name }}
          </div>
          <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ item.customRender ? item.customRender(services[item.dataIndex]) : services[item.dataIndex] }}
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
