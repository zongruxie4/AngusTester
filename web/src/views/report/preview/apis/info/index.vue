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

const apis = computed(() => {
  return props.dataSource?.content?.apis;
});

const columns = computed(() => [
  [
    {
      dataIndex: 'endpoint',
      name: '接口地址'
    },
    {
      dataIndex: 'method',
      name: '请求方法',
      customRender: (text) => {
        return text?.message;
      }
    }
  ],
  [
    {
      dataIndex: 'operationId',
      name: '操作ID'
    },
    {
      dataIndex: 'ownerName',
      name: '负责人'
    }
  ],
  [
    {
      dataIndex: 'description',
      name: '描述'
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
      dataIndex: 'createdByName',
      name: '添加人'
    },
    {
      dataIndex: 'createdDate',
      name: '添加时间'
    }
  ],
  [
    {
      dataIndex: 'lastModifiedByName',
      name: '最后修改人'
    },
    {
      dataIndex: 'lastModifiedDate',
      name: '最后修改时间'
    }
  ]
]);

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1" class="text-4 text-theme-title font-medium">一、<em class="inline-block w-0.25"></em>接口信息</span>
    </h1>
    <div class="border-t border-l border-solid border-border-input">
      <div v-for="column in columns" class="flex border-b border-solid border-border-input">
        <template v-for="item in column">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ item.name }}
          </div>
          <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ item.customRender ? item.customRender(apis[item.dataIndex]) : apis[item.dataIndex] }}
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
