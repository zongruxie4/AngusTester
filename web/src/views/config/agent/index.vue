<script setup lang="ts">
import { Switch } from 'ant-design-vue';
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import { Icon, Input, notification } from '@xcan-angus/vue-ui';
import { duration } from '@xcan-angus/tools';

import { setting } from '@/api/gm';
import apple from './assets/images/apple.png';
import windows from './assets/images/windows.png';
import linux from './assets/images/linux.png';
import sd from './assets/images/sd.png';
import wlj from './assets/images/wlj.png';
import ylj from './assets/images/ylj.png';

const { t } = useI18n();

const loading = ref(false);
const dataList = ref<{ url: string, enabled: boolean }>({ url: '', enabled: false });
const checked = ref(false);
const address = ref('');
const hostRule = ref(false);
const isEdit = ref(true);
const islinkSuccess = ref(false);
const newLint = ref(false);
let isInit = false;
let ws:null | WebSocket = null;
const init = () => {
  loadDataList();
};

const openEdit = () => {
  isEdit.value = !isEdit.value;
};

const testLint = () => {
  newLint.value = true;
  ws = new WebSocket(address.value);
  if (!ws) {
    return;
  }
  ws.onopen = () => {
    islinkSuccess.value = true;
    isEdit.value = true;
    if (!isInit) {
      const params = { enabled: checked.value, url: address.value };
      patchPorxySetting(params);
    }
  };

  islinkSuccess.value = false;
};

const loadDataList = async () => {
  loading.value = true;
  const [error, { data = {} }] = await setting.getTenantApiProxy();
  loading.value = false;
  if (error || !data) {
    return;
  }
  dataList.value = data;
  checked.value = data.enabled;
  if (data.url) {
    address.value = data.url;
    isInit = true;
    testLint();
  }
};

const patchPorxySetting = async (params) => {
  if (hostRule.value) {
    return;
  }

  if (!islinkSuccess.value) {
    return;
  }

  loading.value = true;
  const [error] = await setting.updateTenantApiProxy(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('settingAgent.title.t22'));
  loadDataList();
};

const handleChange = () => {
  const params = { enabled: checked.value, url: address.value };
  patchPorxySetting(params);
};

const handleInputChange = debounce(duration.search, () => {
  if (!address.value) {
    hostRule.value = true;
    return;
  } else {
    hostRule.value = false;
  }

  isInit = false;
  testLint();
  const params = { enabled: checked.value, url: address.value };
  patchPorxySetting(params);
});

onMounted(() => {
  init();
});

onBeforeUnmount(() => {
  ws && ws.close(1000);
});

</script>
<template>
  <div class="px-5 py-3">
    <div class="border rounded p-5" style="border-color: #bcdcff;background-color: #f9fcff;">
      <div class="text-3 leading-3 flex">
        <Icon class="text-tips text-3.5 mt-0.5" icon="icon-tishi1" />
        <p class="text-theme-title font-medium text-3 ml-1.5 leading-5 whitespace-pre-wrap">{{ t('settingAgent.title.t1') }}</p>
      </div>
      <div class="text-3 leading-3 flex space-x-10 mx-5 mt-5">
        <div class="flex-1">
          <div class="text-theme-title font-medium">
            <Icon class="mr-1.5 -mt-0.5" icon="icon-wudaili" />{{ t('settingAgent.title.t2')
            }}
          </div>
          <div class="text-theme-sub-content mt-2 leading-5">{{ t('settingAgent.title.t3') }}</div>
        </div>
        <div class="flex-1">
          <div class="text-theme-title font-medium">
            <Icon class="mr-1.5 -mt-0.5" icon="icon-jiekoudaili" />{{ t('settingAgent.title.t4')
            }}
          </div>
          <div class="text-theme-sub-content mt-2 leading-5">{{ t('settingAgent.title.t5') }}</div>
        </div>
        <div class="flex-1">
          <div class="text-theme-title font-medium">
            <Icon class="mr-1.5 -mt-0.5" icon="icon-host" />{{ t('settingAgent.title.t6') }}
          </div>
          <div class="text-theme-sub-content mt-2 leading-5">{{ t('settingAgent.title.t7') }}</div>
        </div>
        <div class="flex-1">
          <div class="text-theme-title font-medium">
            <Icon class="mr-1.5 -mt-0.5" icon="icon-host" />云代理
          </div>
          <div class="text-theme-sub-content mt-2 leading-5">
            通过AngusTester云服务器节点请求接口，注意：不能访问到客户内网服务地址。
          </div>
        </div>
      </div>
    </div>
    <template v-if="false">
      <div class="font-medium text-3.5 leading-3.5 text-theme-title pb-2 mx-5 mt-10">{{ t('settingAgent.title.t8') }}</div>
      <div class="flex 2xl:space-x-2 m-2 5xl:space-x-7.5  text-3 leading-3">
        <div class="px-7.5 py-3.75 w-1/4  flex items-center text-theme-text-hover cursor-pointer">
          <img :src="windows" class="w-7.5 mr-5" /><span>{{ t('settingAgent.title.t9') }}</span>
        </div>
        <div class="px-7.5 py-3.75 w-1/4  flex items-center text-theme-text-hover cursor-pointer">
          <img :src="apple" class="w-7.5 mr-5" /><span>{{ t('settingAgent.title.t10') }}</span>
        </div>
        <div class="px-7.5 py-3.75 w-1/4  flex items-center text-theme-text-hover cursor-pointer">
          <img :src="linux" class="w-7.5 mr-5" /><span>{{ t('settingAgent.title.t11') }}</span>
        </div>
        <div class="px-7.5 py-3.75 w-1/4 flex items-center text-theme-text-hover cursor-pointer">
          <img :src="sd" class="w-7.5 mr-5" /><span>{{ t('settingAgent.title.t12') }}</span>
        </div>
      </div>
    </template>
    <div class="font-medium text-3.5 leading-3.5 text-theme-title pb-2 mt-10">{{ t('settingAgent.title.t13') }}</div>
    <div class="flex space-x-5 text-theme-sub-content  text-3 leading-3">
      <div>
        <div class="h-12" style="line-height: 48px;">{{ t('settingAgent.title.t14') }}</div>
        <div class="h-12" style="line-height: 48px;">{{ t('settingAgent.title.t15') }}</div>
      </div>
      <div class="w-150 text-theme-content">
        <div class="h-12" style="line-height: 48px;">
          <Switch
            v-model:checked="checked"
            size="small"
            class="w-8"
            @change="handleChange" />
        </div>
        <div class="flex h-12 whitespace-nowrap text-theme-sub-content" style="line-height: 48px;">
          <div class="h-12 relative">
            <Input
              v-model:value="address"
              :disabled="isEdit"
              class="w-100 h-8"
              placeholder="请输入地址"
              @change="handleInputChange">
              <template #suffix>
                <template v-if="checked">
                  <Icon
                    icon="icon-shuxie"
                    class="text-theme-special text-theme-text-hove cursor-pointer"
                    @click="openEdit" />
                </template>
                <template v-else>
                  <Icon
                    icon="icon-shuxie"
                    class="text-text-sub-content cursor-not-allowed" />
                </template>
              </template>
            </Input>
            <div v-if="hostRule" class="absolute top-12 text-3 leading-3 text-status-error">请输入正确的代理地址</div>
          </div>
          <template v-if="address">
            <template v-if="islinkSuccess">
              <div class="flex items-center ml-3">
                <img :src="ylj" class="w-4 mr-2" /><span>
                  {{ newLint ? t('settingAgent.title.t23') : t('settingAgent.title.t18') }}</span>
              </div>
            </template>
            <template v-else>
              <div class="flex items-center ml-3">
                <img :src="wlj" class="w-4 mr-2" /><span>{{
                  newLint ? t('settingAgent.title.t24') : t('settingAgent.title.t19')
                }}</span>
              </div>
            </template>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.open-info {
  width: 320px;
}

.sotp-info {
  width: 20px;
}

.sotp-info .script-info {
  opacity: 0;
}
</style>
