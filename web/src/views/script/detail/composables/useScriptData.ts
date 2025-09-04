import { ref, computed, Ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { notification } from '@xcan-angus/vue-ui';
import { script } from '@/api/tester';
import { exec } from 'src/api/ctrl';
import YAML from 'yaml';
import { PermissionKey, ScriptInfo } from '../types';

/**
 * Script data management composable
 * Handles script loading, saving, and permission management
 */
export function useScriptData (projectInfo: Ref<{ id: string; avatar: string; name: string; }>, isAdmin: Ref<boolean>) {
  const route = useRoute();
  const router = useRouter();

  // Script data refs
  const scriptInfo = ref<ScriptInfo>();
  const permissionList = ref<PermissionKey[]>([]);
  const loading = ref(false);

  // Script content refs
  const oldScriptValue = ref<string>('');
  const scriptValue = ref<string>('');
  const contentType = ref<'json' | 'yaml'>('yaml');

  // Route and ID refs
  const routeQuery = route.query;
  const routeParams = route.params;
  const scriptId = ref<string>(routeParams.id as string);
  const pageNo = routeQuery?.pageNo ? +routeQuery?.pageNo : 1;
  const pageSize = routeQuery?.pageSize ? +routeQuery?.pageSize : 10;
  const viewMode = ref<'view' | 'edit'>(routeQuery.type as 'view' | 'edit');

  // Computed properties
  const projectId = computed(() => {
    return projectInfo.value?.id;
  });

  const pageType = computed(() => {
    if (viewMode.value === 'view') {
      return 'detail';
    }

    if (viewMode.value === 'edit' && scriptId.value) {
      return 'edit';
    }

    return 'create';
  });

  /**
   * Load script details from API
   */
  const loadScript = async () => {
    if (!scriptId.value) return;

    loading.value = true;
    const [error, res] = await script.getScriptDetail(scriptId.value);
    if (error) {
      loading.value = false;
      return;
    }

    const data = res?.data;
    if (data) {
      contentType.value = 'yaml';
      scriptValue.value = data.content;
      oldScriptValue.value = data.content;
      scriptInfo.value = data;

      await loadScriptListAuth(scriptId.value);
    }
    loading.value = false;
  };

  /**
   * Load script permissions
   */
  const loadScriptListAuth = async (id: string) => {
    permissionList.value = [];

    loading.value = true;
    const [error, res] = await script.getScriptCurrentAuth([id]);
    loading.value = false;
    if (error) {
      return;
    }

    const item = res?.data?.[id];
    if (item) {
      const { scriptAuth, permissions } = item;
      let list: PermissionKey[] = [];
      const values = permissions.map(item => item.value);
      if (isAdmin.value || scriptAuth === false) {
        list = ['TEST', 'VIEW', 'MODIFY', 'DELETE', 'EXPORT', 'COLON'];
        if (values.includes('GRANT')) {
          list.push('GRANT');
        }
      } else {
        list = [...values];
        if (values.includes('VIEW') || !values.includes('COLON')) {
          list.push('COLON');
        }
      }

      permissionList.value = list;
    }
  };

  /**
   * Add new script
   */
  const addScript = async (formData: any) => {
    const scriptYaml = contentType.value === 'json' ? YAML.stringify(YAML.parse(scriptValue.value)) : scriptValue.value;
    loading.value = true;
    const [error, res] = await script.addScript({ ...formData, content: scriptYaml, projectId: projectId.value });
    loading.value = false;
    if (error) {
      return;
    }

    scriptId.value = res?.data?.id;
    notification.success('添加脚本成功');
    router.push('/script');
  };

  /**
   * Update existing script
   */
  const updateScript = async (formData: any) => {
    const scriptYaml = contentType.value === 'json' ? YAML.stringify(YAML.parse(scriptValue.value)) : scriptValue.value;
    loading.value = true;
    const [error] = await script.updateScript({ ...formData, content: scriptYaml, id: scriptId.value, projectId: projectId.value });
    loading.value = false;
    if (error) {
      return;
    }

    notification.success('保存成功');
    if (viewMode.value === 'view') {
      if (scriptInfo.value) {
        // Update script info
        scriptInfo.value.name = formData.name;
        scriptInfo.value.description = formData.description;
        scriptInfo.value.type = {
          value: formData.type,
          message: formData.typeName
        };

        scriptInfo.value.content = scriptYaml;
      }
      return;
    }

    router.push(`/script?pageNo=${pageNo}&pageSize=${pageSize}`);
  };

  /**
   * Delete script
   */
  const deleteScript = async () => {
    loading.value = true;
    const [error] = await script.deleteScript([scriptId.value]);
    loading.value = false;
    if (error) {
      return;
    }

    notification.success('删除成功');
    router.push('/script');
  };

  /**
   * Clone script
   */
  const cloneScript = async () => {
    loading.value = true;
    const [error] = await script.cloneScript(scriptId.value);
    loading.value = false;
    if (error) {
      return;
    }
    notification.success('克隆成功');
  };

  /**
   * Execute script
   */
  const executeScript = async () => {
    if (!scriptId.value) {
      return;
    }

    loading.value = true;
    const [error] = await exec.addByScript({ scriptId: scriptId.value });
    loading.value = false;
    if (error) {
      return;
    }

    notification.success('添加执行成功');
  };

  return {
    // Data refs
    scriptInfo,
    permissionList,
    loading,
    oldScriptValue,
    scriptValue,
    contentType,
    scriptId,
    pageNo,
    pageSize,
    viewMode,
    pageType,
    projectId,

    // Methods
    loadScript,
    addScript,
    updateScript,
    deleteScript,
    cloneScript,
    executeScript
  };
}
