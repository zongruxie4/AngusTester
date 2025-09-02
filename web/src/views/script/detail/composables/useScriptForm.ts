import { reactive, ref, watch, onMounted } from 'vue';

/**
 * Script form management composable
 * Handles form state and validation
 */
export function useScriptForm(props: any) {
  const formRef = ref();

  const formData = reactive({
    name: undefined,
    type: undefined,
    typeName: undefined,
    description: undefined
  });

  /**
   * Exclude certain script types
   */
  const excludes = (data: { value: string; message: string; }) => {
    if (data.value === 'MOCK_APIS') {
      return true;
    }
    return false;
  };

  /**
   * Handle type change
   */
  const handleTypeChange = (_value: any, option: any) => {
    formData.typeName = option.message;
  };

  /**
   * Validate form
   */
  const validate = () => {
    return formRef.value?.validate();
  };

  /**
   * Get form data
   */
  const getFormData = () => {
    const { name, type, description, typeName } = formData;
    return { name, type, description, typeName };
  };

  // Watch for data source changes
  onMounted(() => {
    watch(() => props.dataSource, (newValue) => {
      if (!newValue) {
        return;
      }

      const { name, description, type } = newValue;
      formData.name = name;
      formData.type = type?.value;
      formData.typeName = type?.message;
      formData.description = description;
    }, { immediate: true });
  });

  return {
    // Form refs
    formRef,
    formData,
    
    // Form methods
    excludes,
    handleTypeChange,
    validate,
    getFormData
  };
}