<script setup lang="ts">
import { defineAsyncComponent, inject, ref } from 'vue';
import { AsyncComponent, Icon } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { utils } from '@xcan-angus/tools';

const LocalImport = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/localImport/index.vue'));

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

const importModalVisible = ref(false);

const toDebug = () => {
  addTabPane({ name: '添加API', value: 'API', _id: utils.uuid() + 'API' });
};

const toCreateService = () => {
  addTabPane({ name: '引导添加', value: 'api_conduct_created_service', _id: utils.uuid() + 'api_conduct_created_service', type: 'SERVICE', noCache: true });
};

const toImport = () => {
  importModalVisible.value = true;
};
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">快速入口</div>
    <div class="flex space-x-3.75 flex-1 justify-between">
      <div class="flex flex-col justify-between px-5 pt-4 pb-3.5 border border-theme-text-box rounded w-1/3">
        <div class="text-theme-content mb-3.5">
          <div class="flex items-center space-x-2 mb-2">
            <Icon icon="icon-jiekoutiaoshi" class="text-5" />
            <span class="title-normal">调试接口</span>
          </div>
          <div>在线快速编辑、调试接口。</div>
        </div>

        <div class="flex items-start">
          <!-- <ButtonAuth
            code="ApisDebug"
            type="link"
            icon="icon-tiaoshi"
            iconStyle="font-size:14px;"
            @click="toDebug" /> -->
          <Button
            type="link"
            size="small"
            class="flex space-x-1"
            @click="toDebug">
            <Icon icon="icon-tiaoshi" />
            调试接口
          </Button>
        </div>
      </div>

      <div class="flex flex-col justify-between px-5 pt-4 pb-3.5 border border-theme-text-box rounded w-1/3">
        <div class="text-theme-content mb-3.5">
          <div class="flex items-center space-x-2 mb-2">
            <Icon icon="icon-chuangjianxiangfuwu" class="text-5" />
            <span class="title-normal">服务</span>
          </div>
          <div>通过服务来组织接口，建议和您的系统微服务对应。</div>
        </div>

        <div class="flex items-start">
          <Button
            type="link"
            size="small"
            class="flex space-x-1"
            @click="toCreateService">
            <Icon icon="icon-chuangjianfuwu" />
            引导添加
          </Button>
        </div>
      </div>

      <div class="flex flex-col justify-between px-5 pt-4 pb-3.5 border border-theme-text-box rounded w-1/3">
        <div class="text-theme-content mb-3.5">
          <div class="flex items-center space-x-2 mb-2">
            <Icon icon="icon-daoruxiangmufuwu" class="text-5" />
            <span class="title-normal">根据文件导入服务</span>
          </div>
          <div>导入OpenAPI或Postman接口YAML文件或JSON文件。</div>
        </div>

        <div class="flex items-start">
          <Button
            type="link"
            size="small"
            class="flex space-x-1"
            @click="toImport">
            <Icon icon="icon-daoru" />
            导入
          </Button>
        </div>
      </div>
    </div>

    <AsyncComponent :visible="importModalVisible">
      <LocalImport v-model:visible="importModalVisible" source="global" />
    </AsyncComponent>
  </div>
</template>
