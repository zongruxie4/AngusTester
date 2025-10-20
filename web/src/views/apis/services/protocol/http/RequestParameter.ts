import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { toClipboard } from '@xcan-angus/infra';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { deconstruct } from '@/utils/swagger';
import { services } from '@/api/tester';
import { SchemaType } from '@/types/openapi-types';
import { API_EXTENSION_KEY, deepDelAttrFromObj } from '@/utils/apis';
import { ParamsInfo } from '@/views/apis/services/protocol/http/types';
import { validateType } from './utils';

/**
 * Common request parameter management utilities
 * <p>
 * Provides shared functionality for handling request parameters across different Vue components
 * </p>
 */
export class RequestParameterManager {
  private componentState: { formData: ParamsInfo[] };
  private jsonContentRefs: any[];
  private isValidationEnabled: boolean;
  private apiBaseInfo: any;
  private t: any;

  constructor (componentState: { formData: ParamsInfo[] }, jsonContentRefs: any[], apiBaseInfo: any) {
    this.componentState = componentState;
    this.jsonContentRefs = jsonContentRefs;
    this.isValidationEnabled = false;
    this.apiBaseInfo = apiBaseInfo;
    this.t = useI18n().t;
  }

  /**
   * API extension keys for value and enabled properties
   */
  get valueKey () {
    return API_EXTENSION_KEY.valueKey;
  }

  get enabledKey () {
    return API_EXTENSION_KEY.enabledKey;
  }

  /**
   * Generates a unique key for form items
   * <p>
   * Creates a symbol-based key for tracking form items
   * </p>
   * @param index - Optional index for the key
   * @returns Unique symbol key
   */
  generateUniqueKey (index?: number): symbol {
    return Symbol(index);
  }

  /**
   * Handles Enter key press in input fields
   * <p>
   * Blurs the input field when Enter key is pressed
   * </p>
   * @param event - Keyboard event
   */
  handleEnterKeyPress (event: KeyboardEvent): void {
    if (event.key !== 'Enter') {
      return;
    }
    (event.target as HTMLElement).blur();
  }

  /**
   * Handles parameter value blur event
   * <p>
   * Processes and validates parameter values when input loses focus
   * </p>
   * @param target - HTML element that lost focus
   * @param index - Index of the parameter in the form
   * @param parameterData - Parameter data object
   */
  handleParameterValueBlur (target: HTMLElement, index: number, parameterData: ParamsInfo): void {
    let processedValue = target?.innerText?.trim().replace('\n', '');
    if ([SchemaType.integer, SchemaType.number, SchemaType.boolean].includes(parameterData.schema?.type)) {
      try {
        if (Number(processedValue) <= 9007199254740992) {
          processedValue = JSON.parse(processedValue);
        }
      } catch {}
    }
    if (processedValue === parameterData[this.valueKey]) {
      return;
    }

    this.componentState.formData[index] = { ...parameterData, [this.valueKey]: processedValue } as ParamsInfo;
  }

  /**
   * Fetches model data by reference
   * <p>
   * Retrieves component model data from the API service
   * </p>
   * @param reference - Model reference ID
   * @returns Deconstructed model data
   */
  async fetchModelDataByReference (reference: string) {
    const [error, { data }] = await services.getComponentRef(this.apiBaseInfo?.serviceId, reference);
    if (error) {
      return {};
    }
    return deconstruct(data || {});
  }

  /**
   * Handles model selection
   * <p>
   * Processes selected model and updates parameter data
   * </p>
   * @param _selectedValue - Selected value
   * @param selectedOption - Selected option object
   * @param parameterIndex - Index of the parameter
   */
  async handleModelSelection (_selectedValue: any, selectedOption: any, parameterIndex: number) {
    if (selectedOption) {
      const modelData = await this.fetchModelDataByReference(selectedOption.ref);
      const parameterSchema = modelData.schema
        ? { ...modelData.schema, [this.valueKey]: modelData[this.valueKey] || modelData.schema?.[this.valueKey] }
        : {};
      const sampleValue = SwaggerUI.extension.sampleFromSchemaGeneric(parameterSchema);

      if (!parameterSchema.type) {
        let valueType: string = typeof sampleValue;
        if (valueType === SchemaType.object) {
          if (Object.prototype.toString.call(sampleValue) === '[object Array]') {
            valueType = SchemaType.array;
          }
        }
        parameterSchema.type = valueType;
      }
      if (selectedOption.readonly) {
        modelData.$ref = selectedOption.ref;
      }
      this.componentState.formData[parameterIndex] = { ...modelData, schema: parameterSchema, [this.enabledKey]: true, [this.valueKey]: sampleValue };
    }
  }

  /**
   * Handles form field blur event
   * <p>
   * Updates parameter data when form field loses focus
   * </p>
   * @param event - Change event from input field
   * @param index - Index of the parameter
   * @param parameterData - Parameter data object
   * @param fieldKey - Field key to update
   */
  handleFormFieldBlur (event: any, index: number, parameterData: ParamsInfo, fieldKey: string): void {
    const fieldValue = (event.target as HTMLInputElement).value?.trim() || '';
    this.componentState.formData[index] = { ...parameterData, [fieldKey]: fieldValue } as ParamsInfo;
  }

  /**
   * Handles checkbox change event
   * <p>
   * Enables or disables parameter based on checkbox state
   * </p>
   * @param event - Change event from checkbox
   * @param index - Index of the parameter
   * @param parameterData - Parameter data object
   */
  handleCheckboxChange (event: any, index: number, parameterData: ParamsInfo) {
    const isChecked = event?.target?.checked;
    this.componentState.formData[index] = { ...parameterData, [this.enabledKey]: isChecked } as ParamsInfo;
    if (!isChecked && this.isValidationEnabled) {
      this.jsonContentRefs[index] && this.jsonContentRefs[index].validate(false);
    }
  }

  /**
   * Copies parameter value to clipboard
   * <p>
   * Copies the parameter value to the system clipboard
   * </p>
   * @param parameterData - Parameter data object
   */
  async copyParameterValue (parameterData: ParamsInfo) {
    let textToCopy = parameterData[this.valueKey];
    if (typeof textToCopy !== 'string') {
      textToCopy = JSON.stringify(textToCopy);
    }

    toClipboard(textToCopy).then(() => {
      notification.success(this.t('actions.tips.copySuccess'));
    });
  }

  /**
   * Changes parameter data type
   * <p>
   * Updates parameter schema and value based on selected data type
   * </p>
   * @param dataType - New data type
   * @param index - Index of the parameter
   * @param parameterItem - Parameter item object
   */
  changeParameterDataType (dataType: string, index: number, parameterItem: ParamsInfo) {
    const currentSchema = parameterItem.schema || {};
    const updatedParameter = { ...parameterItem, schema: { ...currentSchema, type: dataType } } as any;
    if (dataType === SchemaType.object) {
      updatedParameter.deepObject = true;
      updatedParameter[this.valueKey] = { '': '' };
    } else {
      updatedParameter[this.valueKey] = undefined;
      if (dataType === SchemaType.array) {
        updatedParameter[this.valueKey] = [''];
        updatedParameter.schema.item = {
          type: SchemaType.string
        };
      }
      delete updatedParameter.deepObject;
      delete updatedParameter.explode;
    }
    this.componentState.formData[index] = updatedParameter;
  }

  /**
   * Handles parameter deletion
   * <p>
   * Removes parameter from the form data array
   * </p>
   * @param index - Index of the parameter to delete
   * @param parameterData - Parameter data object
   * @param emitChangeToParent - Function to emit change to parent
   */
  handleParameterDeletion (index: number, parameterData: ParamsInfo, emitChangeToParent: () => void): void {
    const emptyParameters = this.componentState.formData.filter(item => !item.name);
    // Keep at least one empty parameter
    if (!parameterData.name && emptyParameters.length <= 1) {
      return;
    }
    this.componentState.formData.splice(index, 1);
    emitChangeToParent();
  }

  /**
   * Adds child item to JSON content
   * <p>
   * Adds a new child item to the JSON content component
   * </p>
   * @param parentItem - Parent parameter item
   * @param index - Index of the parameter
   */
  addChildParameter (parentItem: ParamsInfo, index: number) {
    this.jsonContentRefs[index].addItem({
      type: parentItem.schema.type,
      id: -1,
      idLine: [-1],
      level: 0
    });
  }

  /**
   * Updates parameter data and emits change
   * <p>
   * Updates parameter data at specified index and emits change event
   * </p>
   * @param index - Index of the parameter
   * @param parameterData - Updated parameter data
   */
  updateParameterData (index: number, parameterData: ParamsInfo): void {
    this.componentState.formData[index] = parameterData;
  }

  /**
   * Validates form contents
   * <p>
   * Validates all enabled parameters in the form
   * </p>
   * @param enableValidation - Whether to enable validation
   */
  async validateFormContents (enableValidation = true) {
    this.isValidationEnabled = enableValidation;
    for (const index in this.jsonContentRefs) {
      if (this.componentState.formData[index][this.enabledKey]) {
        this.jsonContentRefs[index].validate(enableValidation);
      }
    }
  }

  /**
   * Gets error state for parameter
   * <p>
   * Determines if parameter has validation errors
   * </p>
   * @param parameterItem - Parameter item to check
   * @returns True if parameter has errors
   */
  getParameterErrorState (parameterItem: ParamsInfo) {
    if (!this.isValidationEnabled || !parameterItem.name || !parameterItem[this.enabledKey]) {
      return false;
    }
    const validationErrors = validateType(parameterItem[this.valueKey], deepDelAttrFromObj(parameterItem.schema, []));
    return !!(validationErrors?.length);
  }

  /**
   * Updates parameter schema
   * <p>
   * Updates the schema for a parameter item
   * </p>
   * @param newSchema - New schema object
   * @param parameterItem - Parameter item to update
   * @param index - Index of the parameter
   */
  updateParameterSchema (newSchema: any, parameterItem: ParamsInfo, index: number) {
    if (parameterItem.$ref) {
      return;
    }
    const updatedParameter = { ...parameterItem, schema: newSchema };
    this.updateParameterData(index, updatedParameter);
  }

  /**
   * Updates component data
   * <p>
   * Updates component data in the API service
   * </p>
   */
  async updateComponentData () {
    if (!this.apiBaseInfo?.serviceId) {
      return;
    }
    for (let i = 0; i < this.componentState.formData.length; i++) {
      if (this.componentState.formData[i].$ref) {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { $ref, ...content } = this.componentState.formData[i];
        await services.addComponent(this.apiBaseInfo?.serviceId, 'parameters', this.componentState.formData[i].name,
          { ...content, schema: { ...(content.schema || {}), [this.valueKey]: content[this.valueKey] } });
      }
      if (this.jsonContentRefs[i]) {
        await this.jsonContentRefs[i].updateComp();
      }
    }
  }

  /**
   * Gets resolved model data
   * <p>
   * Returns resolved model data for all parameters with references
   * </p>
   * @returns Resolved model data object
   */
  getResolvedModelData () {
    const resolvedModels = {};
    this.componentState.formData.forEach((parameterItem, index) => {
      if (parameterItem.$ref) {
        resolvedModels[parameterItem.$ref] = JSON.parse(JSON.stringify(parameterItem));
        delete resolvedModels[parameterItem.$ref].schema.$ref;
        this.jsonContentRefs[index] && this.jsonContentRefs[index].getModelResolve(resolvedModels);
      }
    });
    return resolvedModels;
  }
}

/**
 * Common interfaces for request components
 */
export interface ChangeEvent {
  target: {
    value: string;
  };
}

/**
 * Common props interface for parameter components
 */
export interface ParameterComponentProps {
  value: ParamsInfo[];
}

/**
 * Common emits interface for parameter components
 */
export interface ParameterComponentEmits {
  (e: 'change', data: ParamsInfo[]): void;
  (e: 'del', index: number): void;
}

/**
 * Utility functions for parameter management
 */
export const ParameterUtils = {
  /**
   * Filters model options based on parameter type
   * <p>
   * Filters out models that don't match the specified parameter type
   * </p>
   * @param option - Model option to filter
   * @param allowedTypes - Array of allowed parameter types
   * @returns True if option should be excluded
   */
  filterModelOptions (option: any, allowedTypes: string[]) {
    const model = option.model && JSON.parse(option.model);
    return !model || !allowedTypes.includes(model.in);
  },

  /**
   * Creates a parameter manager instance
   * <p>
   * Factory function to create a new RequestParameterManager instance
   * </p>
   * @param componentState - Component state object
   * @param jsonContentRefs - JSON content component references
   * @param apiBaseInfo - API base information
   * @returns New RequestParameterManager instance
   */
  createManager (componentState: { formData: ParamsInfo[] }, jsonContentRefs: any[], apiBaseInfo: any) {
    return new RequestParameterManager(componentState, jsonContentRefs, apiBaseInfo);
  }
};
