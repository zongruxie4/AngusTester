import { ref } from 'vue';
import type { CreatorObjectType, CreatorSelectionPayload, CreatorSelectOption } from '../types';

/**
 * <p>Encapsulates selection state and handlers for choosing a creator entity.</p>
 * <p>Exposes reactive state and strongly typed callbacks for the modal.</p>
 */
export function useCreatorSelection () {
  const creatorObjectType = ref<CreatorObjectType>('USER');
  const creatorObjectId = ref<string | undefined>();
  const creatorObjectName = ref<string | undefined>();
  const avatar = ref<string | undefined>();

  /**
   * <p>Reset selection when the entity type changes to avoid stale IDs.</p>
   */
  const handleChangeType = (): void => {
    creatorObjectId.value = undefined;
    creatorObjectName.value = undefined;
    avatar.value = undefined;
  };

  /**
   * <p>Set creator fields from selected option coming from the Select component.</p>
   */
  const setCreator = (value: string, option?: CreatorSelectOption): void => {
    creatorObjectId.value = value;
    creatorObjectName.value = option?.fullName || option?.name;
    avatar.value = option?.avatar || undefined;
  };

  /**
   * <p>Builds an emit-ready payload if selection is valid; otherwise returns undefined.</p>
   */
  const buildPayload = (): CreatorSelectionPayload | undefined => {
    if (!creatorObjectId.value) return undefined;
    return {
      creatorObjectType: creatorObjectType.value,
      creatorObjectId: creatorObjectId.value,
      creatorObjectName: creatorObjectName.value,
      avatar: avatar.value
    };
  };

  return {
    creatorObjectType,
    creatorObjectId,
    creatorObjectName,
    avatar,
    handleChangeType,
    setCreator,
    buildPayload
  };
}
