import { ref } from 'vue';
import type { ProjectMembers, ProjectMember, UseProjectMembersReturn, DefaultOptions, MemberType } from '../types';

/**
 * Project members management composable
 * Handles member selection, type switching, and data preparation for form submission
 */
export function useMembers (): UseProjectMembersReturn {
  // ============================================================================
  // State Management
  // ============================================================================

  /** Current selected member type for UI tabs */
  const memberType = ref<MemberType>('user');

  /** Selected members organized by type */
  const members = ref<{
    USER: string[];
    DEPT: string[];
    GROUP: string[];
  }>({
    USER: [],
    DEPT: [],
    GROUP: []
  });

  /** Default options cache for user selector to avoid re-fetching */
  const defaultOptionsUser = ref<DefaultOptions>({});

  /** Default options cache for department selector */
  const defaultOptionsDept = ref<DefaultOptions>({});

  /** Default options cache for group selector */
  const defaultOptionsGroup = ref<DefaultOptions>({});

  // ============================================================================
  // Data Processing Methods
  // ============================================================================

  /**
   * Initialize members data from project details
   * Processes server data and prepares it for form usage
   *
   * @param projectMembers - Members data from project detail API
   */
  const initializeMembers = (projectMembers?: ProjectMembers): void => {
    if (!projectMembers) return;

    // Process USER members
    if (projectMembers.USER) {
      members.value.USER = projectMembers.USER.map((member: ProjectMember) => {
        // Cache member data for select component default options
        defaultOptionsUser.value[member.id] = {
          ...member,
          fullName: member.name || member.fullName || ''
        };
        return member.id;
      });
    }

    // Process DEPT members
    if (projectMembers.DEPT) {
      members.value.DEPT = projectMembers.DEPT.map((member: ProjectMember) => {
        // Cache member data for select component default options
        defaultOptionsDept.value[member.id] = {
          ...member,
          fullName: member.name || ''
        };
        return member.id;
      });
    }

    // Process GROUP members
    if (projectMembers.GROUP) {
      members.value.GROUP = projectMembers.GROUP.map((member: ProjectMember) => {
        // Cache member data for select component default options
        defaultOptionsGroup.value[member.id] = {
          ...member,
          fullName: member.name || ''
        };
        return member.id;
      });
    }
  };

  /**
   * Get members data formatted for API submission
   * Filters out empty arrays to avoid sending unnecessary data
   *
   * @returns Formatted members object for API submission
   */
  const getMembersForSubmission = () => {
    const { USER, DEPT, GROUP } = members.value;

    return {
      USER: USER.length > 0 ? USER : undefined,
      DEPT: DEPT.length > 0 ? DEPT : undefined,
      GROUP: GROUP.length > 0 ? GROUP : undefined
    };
  };

  /**
   * Reset all member selections to empty state
   */
  const resetMembers = (): void => {
    members.value = {
      USER: [],
      DEPT: [],
      GROUP: []
    };

    // Clear cached options
    defaultOptionsUser.value = {};
    defaultOptionsDept.value = {};
    defaultOptionsGroup.value = {};
  };

  /**
   * Add a member to specific type
   * @param type - Member type to add to
   * @param memberId - Member ID to add
   * @param memberData - Optional member data for caching
   */
  const addMember = (
    type: keyof typeof members.value,
    memberId: string,
    memberData?: ProjectMember
  ): void => {
    if (!members.value[type].includes(memberId)) {
      members.value[type].push(memberId);

      // Cache member data if provided
      if (memberData) {
        const defaultOptions = type === 'USER'
          ? defaultOptionsUser.value
          : type === 'DEPT'
            ? defaultOptionsDept.value
            : defaultOptionsGroup.value;

        defaultOptions[memberId] = {
          ...memberData,
          fullName: memberData.name || memberData.fullName || ''
        };
      }
    }
  };

  /**
   * Remove a member from specific type
   * @param type - Member type to remove from
   * @param memberId - Member ID to remove
   */
  const removeMember = (
    type: keyof typeof members.value,
    memberId: string
  ): void => {
    const index = members.value[type].indexOf(memberId);
    if (index > -1) {
      members.value[type].splice(index, 1);
    }
  };

  /**
   * Get total member count across all types
   * @returns Total number of selected members
   */
  const getTotalMemberCount = (): number => {
    return members.value.USER.length +
           members.value.DEPT.length +
           members.value.GROUP.length;
  };

  /**
   * Check if any members are selected
   * @returns True if at least one member is selected
   */
  const hasMembers = (): boolean => {
    return getTotalMemberCount() > 0;
  };

  /**
   * Switch member type tab
   * @param type - Member type to switch to
   */
  const switchMemberType = (type: MemberType): void => {
    memberType.value = type;
  };

  // ============================================================================
  // Return Interface
  // ============================================================================

  return {
    // State
    memberType,
    members,
    defaultOptionsUser,
    defaultOptionsDept,
    defaultOptionsGroup,

    // Methods
    initializeMembers,
    getMembersForSubmission,
    resetMembers,
    addMember,
    removeMember,
    getTotalMemberCount,
    hasMembers,
    switchMemberType
  };
}
