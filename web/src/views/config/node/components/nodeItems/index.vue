<script setup lang="ts">
import { Button, CheckboxGroup, Popover, Progress, TabPane, Tabs, Tag } from 'ant-design-vue';
import { computed, inject, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { Grid, Hints, Icon, Input, modal, notification, Tooltip } from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import { appContext, toClipboard } from '@xcan-angus/infra';

import { formItems, nodeStatus, nodeUseProgresses, viewItem } from './interface';
import { getDefaultNode, getStrokeColor, installConfigColumns } from '../../interface';

import { nodeCtrl, nodeInfo } from 'src/api/ctrl';
import { node } from 'src/api/tester';

type Roles = Array<{value: string, label: string, name: string, disabled?:boolean}>

interface Props {
  nodeList: Array<Record<string, any>>;
  roleOptions: Roles;
  autoRefresh: boolean;
  isAdmin: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  nodeList: () => [],
  roleOptions: () => [],
  autoRefresh: false,
  isAdmin: false
});

const nodeParams = reactive({
  name: '',
  ip: '',
  publicIp: '',
  domain: '',
  username: '',
  password: '',
  sshPort: '',
  roles: [],
  id: ''
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'cancel'): void,
  (e: 'loadList'): void
}>();

const tenantInfo = ref(appContext.getTenant());
const userInfo = ref(appContext.getUser());

const validated = ref(false);

const showTested = ref(false); // 是否点击果测试链接

const testSuccess = ref(false); // 测试链接状态

const testFailContent = ref(); // 链接测试失败消息

const installingMap = reactive({}); // 代理安装中

const enabledingMap = reactive({}); // 启用/禁用中

const restartingMap = reactive({}); // 重启

const nodeList = ref<Array<Record<string, any>>>([]);

const isPrivate = ref(false);

const disabledRestart = computed(() => {
  // if (isPrivate.value) {
  //   return !props.isAdmin
  // }
  return !props.isAdmin;
});

// 显示端口号校验提示
const showPortTip = computed(() => {
  const sshPort = Number(nodeParams.sshPort);
  return validated.value && !!nodeParams.sshPort && !(sshPort >= 1 && sshPort <= 65535);
});

// 显示测试链接 按钮
const testBtnDisable = computed(() => {
  return !nodeParams.ip && !nodeParams.publicIp;
});

const validate = () => {
  const sshPort = Number(nodeParams.sshPort);
  return nodeParams.name?.trim() && nodeParams.ip?.trim() && (!nodeParams.sshPort?.trim() || (sshPort >= 1 && sshPort <= 65535)) && nodeParams.roles.length > 0;
};

// 添加
const add = () => {
  if (nodeList.value[0]?.editable && !nodeList.value[0]?.id) {
    return;
  }
  nodeList.value.unshift(getDefaultNode());
  changeEditable(nodeList.value[0]);
  showTested.value = false;
};

// 取消 click
const cancel = (state) => {
  if (state?.id) {
    state.editable = false;
  } else {
    nodeList.value.shift();
    emits('cancel');
  }
  showTested.value = false;
  validated.value = false;
  testFailContent.value = '';
};

// 编辑几点按钮禁用提示
const getEditTip = (node) => {
  if (node.source?.value !== 'OWN_NODE') {
    return '只允许删除自有节点';
  }
  if (!props.isAdmin) {
    return '编辑节点需要系统管理员或应用管理员权限';
  }
};

// 编辑 click
const changeEditable = (state) => {
  if (nodeList.value.some(i => i.editable)) {
    nodeList.value.forEach(node => {
      if (node.editable) {
        cancel(node);
      }
    });
  }
  state.editable = !state.editable;
  nodeParams.domain = state.domain;
  nodeParams.ip = state.ip;
  nodeParams.publicIp = state.publicIp;
  nodeParams.id = state.id;
  nodeParams.name = state.name;
  nodeParams.username = state.username;
  nodeParams.password = state.password;
  nodeParams.sshPort = state.sshPort;
  nodeParams.roles = state.roles.map(i => i.value);
};

// 保存
const saveNode = async () => {
  validated.value = true;
  if (validate()) {
    validated.value = false;
    showTested.value = false;
    const response = await (!nodeParams.id
      ? node.addNode([{ ...nodeParams }])
      : node.updateNode([{ ...nodeParams }]));
    const [error] = response;
    if (error) {
      return;
    }
    if (nodeParams.id) {
      notification.success('更新节点成功');
    } else {
      notification.success('添加节点成功');
    }
    emits('loadList');
  }
};

// 删除按钮禁用提示
const getDelTip = (node) => {
  if (node.enabled) {
    return '节点禁用后才能删除';
  }
  if (!props.isAdmin) {
    return '删除节点需要系统管理员或应用管理员权限';
  }
  if (node.source?.value !== 'OWN_NODE') {
    return '只能删除自有节点';
  }
};

// 删除
const delNode = (state) => {
  modal.confirm({
    content: `确定删除节点【${state.name}】吗?`,
    onOk: async () => {
      const [error] = await node.deleteNode({ ids: state.id });
      if (error) {
        return;
      }
      emits('loadList');
    }
  });
};

// 安装 代理按钮禁用提示
const getOnlineInstallTip = (node) => {
  if (node.geAgentInstallationCmd) {
    return '已安装';
  }
  if (props.isAdmin) {
    return '在线安装代理需要系统管理员或应用管理员权限';
  }
};

// 安装 代理
const installAgent = async (state) => {
  if (installingMap[state.id]) {
    return;
  }
  installingMap[state.id] = true;
  const [error] = await node.installNodeAgent({ id: state.id });
  installingMap[state.id] = false;
  if (error) {
    // if (error.data.linuxOfflineInstallCmd || error.data.windowsOfflineInstallCmd) {
    //   state.linuxOfflineInstallCmd = error.data.linuxOfflineInstallCmd;
    //   state.windowsOfflineInstallCmd = error.data.windowsOfflineInstallCmd;
    //   state.showInstallAgent = true;
    // }
    if (typeof error.data !== 'object') {
      return;
    }
    if (error.data.linuxOfflineInstallSteps) {
      state.linuxOfflineInstallSteps = error.data.linuxOfflineInstallSteps;
      state.showInstallAgent = true;
    }
    if (error.data.windowsOfflineInstallSteps) {
      state.windowsOfflineInstallSteps = error.data.windowsOfflineInstallSteps;
      state.showInstallAgent = true;
    }
    return;
  }
  emits('loadList');
  notification.success('安装成功');
};

// 收起手动安装代理
const foldInstallAgent = (state) => {
  state.showInstallAgent = !state.showInstallAgent;
};

// 查询手动安装代理步骤
const showInstallStep = async (state) => {
  if (state.showInstallAgent) {
    foldInstallAgent(state);
    return;
  }
  const [error, res] = await nodeInfo.geAgentInstallationCmd({ id: state.id });
  if (error) {
    return;
  }
  state.showInstallAgent = true;
  // if (res.data.linuxOfflineInstallCmd || res.data.windowsOfflineInstallCmd) {
  //   state.linuxOfflineInstallCmd = res.data.linuxOfflineInstallCmd;
  //   state.windowsOfflineInstallCmd = res.data.windowsOfflineInstallCmd;
  // }
  if (res.data.linuxOfflineInstallSteps) {
    state.linuxOfflineInstallSteps = res.data.linuxOfflineInstallSteps;
  }
  if (res.data.windowsOfflineInstallSteps) {
    state.windowsOfflineInstallSteps = res.data.windowsOfflineInstallSteps;
  }
  state.installConfig = res.data?.installConfig || {};
};

// 重启代理
const restartProxy = async (state) => {
  restartingMap[state.id] = true;
  const [error] = await node.restartNodeAgent(state.id);
  if (error) {
    restartingMap[state.id] = false;
    return;
  }
  setTimeout(() => {
    restartingMap[state.id] = false;
    emits('loadList');
  }, 16000);
};

// 禁用
const enable = async (state) => {
  enabledingMap[state.id] = true;
  const [error] = await node.enableNode([{ enabled: !state.enabled, id: state.id }]);
  enabledingMap[state.id] = false;
  if (error) {
    return;
  }
  emits('loadList');
};

// 测试链接
const test = async () => {
  const { ip, password, sshPort, username, publicIp } = nodeParams;
  const [error] = await node.testNodeConnection({ ip: publicIp || ip, password, sshPort: Number(sshPort), username });
  showTested.value = true;
  if (error) {
    testSuccess.value = false;
    testFailContent.value = error.message;
    return;
  }
  testSuccess.value = true;
};

// 编辑节点名称
const editNameInputref = ref();
const editNameValue = ref();
const editNameId = ref();
const editName = (name, id) => {
  editNameId.value = id;
  editNameValue.value = name;
  setTimeout(() => {
    editNameInputref.value?.[0].focus();
  });
};

// 节点名称失焦
const nodeNameBlur = async (name, id) => {
  if (name === editNameValue.value || !editNameValue.value.trim()) {
    editNameId.value = undefined;
    return;
  }

  const [error] = await node.updateNode([{ id, name: editNameValue.value }]);
  if (error) {
    return;
  }
  editNameId.value = undefined;
  nodeList.value.forEach(node => {
    if (node.id === id) {
      node.name = editNameValue.value;
    }
  });
  notification.success('修改节点名称成功');
};

// 网络接口
const loadNodeNetWork = (id) => {
  return nodeCtrl.getNetworkInfoMetrics(id);
};

// 资源接口
const loadNodeMetrics = (id) => {
  return nodeCtrl.getLatestMetrics(id);
};

// 请求多个节点网络数据
const loadNodeMetricsNetwork = () => {
  return Promise.all(nodeList.value.filter(node => node.id).map(node => loadNodeNetWork(node.id)))
    .then(resp => {
      nodeList.value.forEach(node => {
        if (node.id) {
          const [error, res] = resp[0];
          if (error) {
            return;
          }
          // rxBytes,rxBytesRate,rxErrors,txBytes,txBytesRate
          const { cvsValue = '' } = res.data?.[0]?.networkUsage || {};
          const cvsValues = cvsValue.split(',');
          if (cvsValues.length > 1) {
            const rxBytesRate = (+cvsValues[1] || 0); // 下载
            const txBytesRate = (+cvsValues[4] || 0); // 上传
            // node.spec = {
            //   ...node.spec,
            //   rxBytesRate,
            //   txBytesRate
            // };
            node.monitorData = {
              ...node.monitorData,
              rxBytesRate,
              txBytesRate
            };
          } else {
            // node.spec = {
            //   ...node.spec,
            //   rxBytesRate: 0,
            //   txBytesRate: 0
            // };
            node.monitorData = {
              ...node.monitorData,
              rxBytesRate: 0,
              txBytesRate: 0
            };
          }

          if (res.data?.[0]?.networkUsage) {
            const timestamp = res.data?.[0]?.networkUsage.timestamp;
            const datetime = res.datetime;
            if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
              // node.spec = {
              //   ...node.spec,
              //   rxBytesRate: 0,
              //   txBytesRate: 0
              // };
              node.monitorData = {
                ...node.monitorData,
                rxBytesRate: 0,
                txBytesRate: 0
              };
            }
          }
          resp.shift();
        }
      });
    });
};

// 请求多个节点资源使用数据
const loadNodeListMetrics = () => {
  return Promise.all(nodeList.value.filter(node => node.id).map(node => loadNodeMetrics(node.id)))
    .then(resp => {
      nodeList.value.forEach((node) => {
        if (node.id) {
          const [error, res] = resp[0];
          if (error) {
            return;
          }
          const { cvsCpu = '', cvsFilesystem = '', cvsMemory = '' } = res.data || {};
          // cvsCpu：idle,sys,user,wait,other,total 取 total
          const cpu = (+cvsCpu.split(',')[5] || 0).toFixed(2);
          // cvsMemory：free,used,freePercent,usedPercent,actualFree,actualUsed,actualFreePercent,actualUsedPercent,swapFree,swapUsed 取 usedPercent,改为实际内存actualUsedPercent 再次改为usedPercent, 再次改为actualUsedPercent
          const memory = (+cvsMemory.split(',')[7] || 0).toFixed(2);
          // cvsFilesystem：free,used,avail,usePercent 取 usePercent
          const disk = (+cvsFilesystem.split(',')[3] || 0).toFixed(2);
          // cvsMemory：free,used,freePercent,usedPercent,actualFree,actualUsed,actualFreePercent,actualUsedPercent,swapFree,swapUsed 取 swapUsed
          const cvsMemories = cvsMemory.split(',');
          const swapTotal = (+cvsMemories[9] || 0) + (+cvsMemories[8] || 0);
          const swap = ((+cvsMemories[9] || 0) / (+swapTotal || 1) * 100).toFixed(2);
          // node.spec = {
          //   ...node.spec,
          //   cpu,
          //   memory,
          //   disk,
          //   swap
          // };
          node.monitorData = {
            ...node.monitorData,
            cpu,
            memory,
            disk,
            swap
          };
          if (res.data) {
            const timestamp = res.data?.timestamp;
            const datetime = res.datetime;
            // node.online = true;
            if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
              // node.spec = {
              //   ...node.spec,
              //   cpu: 0,
              //   memory: 0,
              //   disk: 0,
              //   swap: 0
              // };
              node.monitorData = {
                ...node.monitorData,
                cpu: 0,
                memory: 0,
                disk: 0,
                swap: 0
              };
              // node.online = false;
            }
          } else {
            // node.online = false;
          }
          resp.shift();
        }
      });
    });
};

let timer;

// 定时获取监控数据
const startInterval = () => {
  loadNodeListMetrics();
  loadNodeMetricsNetwork();
  timer && clearInterval(timer);
  if (props.autoRefresh) {
    timer = setInterval(() => {
      loadNodeListMetrics();
      loadNodeMetricsNetwork();
    }, 5000);
  }
};

watch(() => props.nodeList, newValue => {
  if (newValue) {
    nodeList.value = JSON.parse(JSON.stringify(newValue));
  } else {
    nodeList.value = [];
  }
  startInterval();
}, {
  deep: true,
  immediate: true
});

// 自动刷新
watch(() => props.autoRefresh, newValue => {
  if (newValue) {
    startInterval();
  } else {
    timer && clearInterval(timer);
  }
});

onMounted(async () => {
  isPrivate.value = appContext.isPrivateEdition();
});

const showInstallCtrlAccessTokenMap = ref<{[key: string]: boolean}>({});
const toggleShowCtrlAccessToken = (id) => {
  showInstallCtrlAccessTokenMap.value[id] = !showInstallCtrlAccessTokenMap.value[id];
};

const copyContent = (text) => {
  toClipboard(text).then(() => {
    notification.success('已复制到剪贴板');
  });
};

onBeforeUnmount(() => {
  timer && clearInterval(timer);
});
defineExpose({ add, startInterval });

</script>
<template>
  <div
    v-for="state in nodeList"
    :key="state.id"
    class="border rounded mb-4"
    :class="{'border-blue-text-active': state.editable}">
    <div class="py-2 px-4 font-semibold bg-gray-2 flex justify-between border-b text-3">
      <div class="flex items-center">
        <span v-if="!state.id">添加节点</span>
        <template v-else>
          <template v-if="editNameId !== state.id">
            <RouterLink :to="`/node/detail/${state.id}`"><span class="text-3.5">{{ state.name }}</span></RouterLink>
            <Icon
              v-if="!state.free || tenantInfo.id === '1'"
              icon="icon-shuxie"
              class="ml-1 hover:text-blue-1 cursor-pointer"
              @click="editName(state.name, state.id)" />
          </template>
          <template v-else>
            <Input
              ref="editNameInputref"
              v-model:value="editNameValue"
              class="输入限制200字符以内'"
              trim
              :maxlength="200"
              @blur="nodeNameBlur(state.name, state.id)" />
          </template>
        </template>
        <Tooltip
          color="#fff"
          overlayClassName="proxy-uninstall"
          placement="right">
          <template #title><div class="text-3">检测到该节点还未安装代理，请先安装代理后在进行使用</div></template>
          <p v-show="!state.installAgent && !!state.id" class="text-http-put ml-6 cursor-pointer"><Icon icon="icon-tishi1" class="text-3.5" /></p>
        </Tooltip>
      </div>
      <div v-show="!state.editable" class="flex">
        <Tag
          v-if="state.free"
          color="success"
          class="h-5 leading-5 mr-4">
          免费节点
        </Tag>
        <div
          v-for="item in nodeStatus"
          :key="item.label"
          class="w-40">
          <label class="text-gray-text">{{ item.label }}</label>
          <span class="text-text-content node-status-icon pl-1" :class="item.status?.['' + !!state?.[item.valueKey]]">{{ item.valueName?.['' + !!state?.[item.valueKey]] }}</span>
        </div>
      </div>
    </div>
    <div class="px-4">
      <!-- 编辑状态 -->
      <div v-if="state.editable" class="text-3">
        <div class="flex flex-wrap justify-between text-text-content text-3 leading-3">
          <div
            v-for="item in formItems"
            :key="item.valueKey"
            class="mt-4 pb-2 relative"
            :style="{width: item.width }">
            <div class="mb-2.5 text-text-content font-medium">{{ item.label }} <span v-if="item.required" class="text-status-error">*</span></div>
            <Input
              v-model:value="nodeParams[item.valueKey]"
              class="input-sm rounded"
              :type="item.type || 'input'"
              :dataType="item.inputType"
              :placeholder="item.placeholder"
              :maxlength="item.maxlength" />
            <span
              v-if="item.required"
              class="text-status-error absolute left-0 -bottom-1.5"
              :class="[validated && !(nodeParams[item.valueKey]?.trim()) ? 'inline' : 'hidden']">必填项</span>
            <span
              v-if="item.valueKey === 'sshPort'"
              class="text-status-error absolute left-0 -bottom-1.5"
              :class="[showPortTip ? 'inline' : 'hidden']">请填写端口号范围在1 ~ 65535之间</span>
          </div>
          <div class="mt-4 w-1/3">
            角色 <span class="text-status-error">*</span>
            <Popover
              title="可根据您的实际需求，勾选节点对应的角色"
              placement="bottomLeft"
              class="text-3">
              <template #content>
                <div class="max-w-200 text-3">
                  <p>提示：节点必须是 Linux（推荐）、Mac、Windows 系统；执行任务前您需要添加节点到节点列表中并安装代理，如果因为网络或服务器root账户问题无法自动“安装代理”，您需要按照“手动安装”方式安装代理，确保执行任务前代理处于连接状态（未启动时需要先启动代理）；一个节点可以同时承担多个角色，为了保证测试结果更加准确请为压测节点使用单一“执行节点”角色；压测中您想尽量还原用户真实访问环境及体验请使用公网节点，如果想排除网络等外界因素的干扰来测试应用服务的极限，需要添加您的自有内网机器到节点中。</p>
                  <p class="pl-3"><span>-管理节点: </span>部署和运行Web UI应用</p>
                  <p class="pl-3"><span>-控制器节点: </span>用于调度、管理、监控执行和代理服务。</p>
                  <p class="pl-3"><span>-执行节点: </span>用于执行功能测试、性能测试、生成数据等。</p>
                  <p class="pl-3"><span>-Mock节点: </span>运行Mock服务和Mock接口。</p>
                  <p class="pl-3"><span>-应用节点: </span>测试应用所在节点，用于压测时观察被压测应用节点系统负载，有助于定位分析性能问题。</p>
                </div>
              </template>
              <Icon icon="icon-shuoming" class="text-blue-light text-3.5 leading-3 ml-1.5" />
            </Popover>
            <div>
              <CheckboxGroup
                v-model:value="nodeParams.roles"
                class="mt-3 check-box-group"
                :options="roleOptions">
              </CheckboxGroup>
              <span
                class="text-status-error"
                :class="[validated && nodeParams.roles.length < 1 ? 'block' : 'hidden']">请选择角色</span>
            </div>
          </div>
        </div>

        <div class="pt-5 pb-4 flex items-center">
          <Button
            type="primary"
            class="node-normal-btn"
            @click="saveNode">
            保存
          </Button>
          <Button class="node-normal-btn" @click="cancel(state)">取消</Button>
          <Button
            :disabled="testBtnDisable"
            class="node-normal-btn"
            @click="test">
            测试链接
          </Button>
          <p v-show="showTested && testSuccess" class="text-text-content font-medium">
            <Icon icon="icon-duihao" class="text-status-success mr-1.5 icon-text-3 leading-3" />
            连接成功
          </p>
          <p v-show="showTested && !testSuccess" class="text-text-content font-medium">
            <Icon icon="icon-chahao" class="text-status-error mr-1.5 icon-text-3 leading-3" />
            连接失败，请检查并重新填写节点信息后再次测试连接, {{ testFailContent }}
          </p>
        </div>
      </div>
      <!-- 视图状态 -->
      <div v-else class="text-3">
        <div class="flex items-center min-h-28.5 justify-between">
          <div class="flex-1">
            <div class="py-2 flex-wrap leading-8 justify-between">
              <div
                v-for="item in viewItem"
                :key="item.valueKey"
                class="inline-flex items-center"
                :class="{'mr-7.25': !!state[item.valueKey]}">
                <label v-if="!!state[item.valueKey]" class="text-black-active mr-1">{{ item.label }}</label>
                <span v-if="item.type !== 'tag'" class="text-text-content">{{ state[item.valueKey] }}</span>
                <span
                  v-for="tag in state.roles"
                  v-else-if="item.valueKey === 'roles'"
                  :key="tag.value"
                  class="px-1.5 py-1 border text-3 leading-3 rounded-full border-gray-light-b mr-2 text-text-content">{{ tag.message }}
                </span>
                <div v-else-if="!!state.os" class="80">
                  <span
                    v-for="tag in state.os"
                    :key="tag"
                    class="px-1.5 py-1 border text-3 leading-3 rounded-full border-gray-light-b mr-2 text-text-content">{{ tag }}
                  </span>
                </div>
              </div>
            </div>
            <div class="pb-2 flex justify-between max-w-180">
              <!-- 权限控制：（1、只允许管理员修改 || 2、自己添加的节点允许修改）&& 3、不允许修改租户1共享的节点。 -->
              <Tooltip v-if="state.tenantId !== tenantInfo.id || !(props.isAdmin || state.createdBy === userInfo.id)" :title="getEditTip(state)">
                <Button
                  type="text"
                  :disabled="true"
                  size="small"
                  class="flex space-x-1">
                  <Icon icon="icon-shuxie" />
                  编辑
                </Button>
              </Tooltip>
              <Button
                v-else
                type="text"
                size="small"
                class="flex space-x-1"
                @click="changeEditable(state)">
                <Icon icon="icon-shuxie" />
                编辑
              </Button>
              <Button
                type="text"
                :disabled="state.tenantId !== tenantInfo.id || !(props.isAdmin || state.createdBy === userInfo.id)"
                :showTextIndex="state.enabled?0:1"
                size="small"
                class="flex space-x-1"
                @click="enable(state)">
                <Icon :icon="state.enabled?'icon-jinyong':'icon-qiyong'" />
                {{ state.enabled?'禁用':'启用' }}
              </Button>
              <Tooltip v-if="state.tenantId !== tenantInfo.id || !(props.isAdmin || state.createdBy === userInfo.id) || state.enabled" :title="getDelTip(state)">
                <Button
                  type="text"
                  :disabled="true"
                  size="small"
                  class="flex space-x-1"
                  @click="enable(state)">
                  <Icon icon="icon-qingchu" />
                  删除
                </Button>
              </Tooltip>
              <!-- <ButtonAuth
                v-else
                code="NodeDelete"
                type="text"
                icon="icon-qingchu"
                @click="delNode(state)" /> -->
              <Button
                v-else
                type="text"
                size="small"
                class="flex space-x-1"
                @click="delNode(state)">
                <Icon icon="icon-qingchu" />
                删除
              </Button>
              <Tooltip v-if="state.free" :title="getOnlineInstallTip(state)">
                <!-- <ButtonAuth
                  code="InstallAgentOnline"
                  type="text"
                  :disabled="true"
                  icon="icon-anzhuangdaili" /> -->
                <Button
                  type="text"
                  :disabled="true"
                  size="small"
                  class="flex space-x-1">
                  <Icon icon="icon-anzhuangdaili" />
                  在线安装
                </Button>
              </Tooltip>
              <Button
                v-else
                :disabled="state.installAgent"
                class="node-action-btn"
                :loading="installingMap[state.id]"
                @click="installAgent(state)">
                <Icon icon="icon-anzhuangdaili" class="mr-1" />
                <span> 在线安装 </span>
                <Hints
                  v-if="installingMap[state.id]"
                  class="ml-1"
                  text="预计需要一分钟" />
              </Button>
              <Tooltip v-if="state.free" :title="!props.isAdmin ? `手动安装代理需要系统管理员或应用管理员权限` : ''">
                <!-- <ButtonAuth
                  code="InstallAgentManually"
                  type="text"
                  :disabled="true"
                  icon="icon-anzhuangdaili" /> -->
                <Button
                  type="text"
                  :disabled="true"
                  size="small"
                  class="flex space-x-1">
                  <Icon icon="icon-anzhuangdaili" />
                  手动安装
                </Button>
              </Tooltip>
              <!-- <ButtonAuth
                v-else
                code="InstallAgentManually"
                type="text"
                icon="icon-anzhuangdaili"
                @click="showInstallStep(state)" /> -->
              <Button
                v-else
                type="text"
                size="small"
                class="flex space-x-1"
                @click="showInstallStep(state)">
                <Icon icon="icon-anzhuangdaili" />
                手动安装
              </Button>
              <Button
                type="text"
                size="small"
                class="flex space-x-1"
                :loading="restartingMap[state.id]"
                :disabled="state.tenantId !== tenantInfo.id || !(props.isAdmin || state.createdBy === userInfo.id)"
                @click="restartProxy(state)">
                <Icon icon="icon-zhongxinkaishi" />
                重启代理
              </Button>
            </div>
          </div>
          <div class="flex items-center justify-between text-right min-w-2/5">
            <div
              v-for="pro in nodeUseProgresses"
              :key="pro.valueKey"
              class="ml-5">
              <Progress
                type="circle"
                :percent="+state.monitorData[pro.valueKey] || 0"
                :width="72"
                :strokeColor="getStrokeColor(state.monitorData[pro.valueKey] || 0)">
                <template #format>
                  <div v-if="pro.valueKey === 'network-up'" class="text-3">
                    <div class="whitespace-nowrap">{{ state.monitorData.txBytesRate || 0 }}MB/s</div>
                  </div>
                  <div v-else-if="pro.valueKey === 'network-down'" class="text-3">
                    <div class="mt-1 whitespace-nowrap">{{ state.monitorData.rxBytesRate || 0 }}MB/s</div>
                  </div>
                  <div v-else class="text-black-active text-3">{{ state.monitorData[pro.valueKey] || 0 }}%</div>
                </template>
              </Progress>
              <div class="text-center mt-1">{{ pro.label }}</div>
            </div>
          </div>
        </div>
        <div v-show="state.showInstallAgent" class="border-t pt-4 pb-4 relative">
          <Tabs size="small">
            <TabPane key="linux" tab="Linux或者Mac系统自动安装步骤">
              <div class="text-3">必须以root用户执行脚本, 安装目录为脚本所在目录</div>
              <div class="text-3">
                安装方式1：<Icon
                  icon="icon-fuzhi"
                  class="cursor-pointer text-3.5 text-blue-1"
                  @click="copyContent(state.linuxOfflineInstallSteps?.onlineInstallCmd)" />
                <p class="install-step whitespace-pre-line">
                  {{ state.linuxOfflineInstallSteps?.onlineInstallCmd }}
                </p>
                安装方式2：
                <p class="install-step whitespace-pre-line">
                  1).下载自动安装脚本：<a class="cursor-pointer" :href="state.linuxOfflineInstallSteps?.installScriptUrl">{{ state.linuxOfflineInstallSteps?.installScriptName }}</a> <br />
                  2).将{{ state.linuxOfflineInstallSteps?.installScriptName }}复制到自定义的安装目录，运行脚本安装：<br />
                  {{ state.linuxOfflineInstallSteps?.runInstallCmd }}
                </p>
              </div>
            </TabPane>
            <!--            <TabPane key="windows" tab="Windows系统自动安装步骤">-->
            <!--              <div class="text-3">-->
            <!--                安装方式1：-->
            <!--                <p class="install-step whitespace-pre-line">-->
            <!--                  {{ state.windowsOfflineInstallSteps?.onlineInstallCmd }}-->
            <!--                </p>-->
            <!--                安装方式2：-->
            <!--                <p class="install-step whitespace-pre-line">-->
            <!--                  1).下载自动安装脚本：<a class="cursor-pointer" :href="state.windowsOfflineInstallSteps?.installScriptUrl">{{ state.windowsOfflineInstallSteps?.installScriptName }}</a> <br />-->
            <!--                  2).将{{ state.windowsOfflineInstallSteps?.installScriptName }}复制到自定义的安装目录，运行脚本安装：<br />-->
            <!--                  {{ state.windowsOfflineInstallSteps?.runInstallCmd }}-->

            <!--                </p>-->
            <!--              </div>-->
            <!--            </TabPane>-->
            <TabPane key="config" tab="安装配置信息">
              <Grid
                :dataSource="state.installConfig"
                :columns="installConfigColumns">
                <template #tenantId="{text}">
                  <div class="flex items-center">
                    <span>{{ text }}</span> <Button
                      type="link"
                      size="small"
                      @click="copyContent(text)">
                      <Icon icon="icon-fuzhi" class="ml-0.5" />
                    </Button>
                  </div>
                </template>
                <template #deviceId="{text}">
                  <div class="flex items-center">
                    <span>{{ text }}</span> <Button
                      type="link"
                      size="small"
                      @click="copyContent(text)">
                      <Icon icon="icon-fuzhi" class="ml-0.5" />
                    </Button>
                  </div>
                </template>
                <template #serverCtrlUrlPrefix="{text}">
                  <div class="flex items-center">
                    <span>{{ text }}</span> <Button
                      type="link"
                      size="small"
                      @click="copyContent(text)">
                      <Icon icon="icon-fuzhi" class="ml-0.5" />
                    </Button>
                  </div>
                </template>
                <template #ctrlAccessToken="{text}">
                  <div class="flex items-center">
                    <span>{{ showInstallCtrlAccessTokenMap[state.id] ? text : '******' }}</span>
                    <Button
                      size="small"
                      type="link"
                      class="leading-5 h-5"
                      @click="toggleShowCtrlAccessToken(state.id)">
                      <Icon class="text-4" :icon="showInstallCtrlAccessTokenMap[state.id] ? 'icon-biyan' : 'icon-zhengyan'" />
                    </Button>
                  </div>
                </template>
              </Grid>
            </TabPane>
          </Tabs>
          <Button
            class="absolute right-0 bottom-0 text-3"
            type="link"
            size="small"
            @click="foldInstallAgent(state)">
            收起
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.node-action-btn {
  @apply rounded mr-2 text-text-content text-3 border-0  px-2 shadow-none flex items-center;
}

.node-action-btn :deep(span) {
  @apply ml-1;
}

.install-step {
  @apply px-3 py-1.5 my-2 leading-6;

  background-color: #f6f6f6;
}

.node-action-btn[disabled],
.node-action-btn:not([disabled]),
.node-action-btn:focus {
  @apply bg-transparent !important;
}

.node-action-btn[disabled] {
  @apply opacity-50;
}

.node-action-btn:not([disabled]):hover {
  @apply text-text-link;
}

.node-action-btn::after {
  @apply hidden;
}

.check-box-group :deep(.ant-checkbox-group-item span) {
  @apply text-gray-text text-3 leading-3 !important;
}

.check-box-group :deep(.ant-checkbox-inner) {
  @apply w-3 h-3;
}

.node-normal-btn {
  @apply border-0 rounded mr-3 text-3 leading-3 h-7;
}

.node-normal-btn:not(.ant-btn-primary) {
  @apply bg-gray-bg;
}

.node-status-icon::before {
  @apply w-1.5 h-1.5 rounded-full mr-1;

  content: "";
  display: inline-block;
}

.success.node-status-icon::before {
  @apply bg-status-success;
}

.fail.node-status-icon::before {
  @apply bg-status-error;
}

</style>
<style>
.ant-modal.ant-modal-confirm-confirm .ant-modal-confirm-body > .anticon > svg {
  font-size: 18px;
}

.proxy-uninstall {
  max-width: inherit;
}
</style>
