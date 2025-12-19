/**
 * <p>Composable for rendering user avatars with names in table cells.</p>
 * <p>Provides consistent avatar display format across the application.</p>
 */
export function useUserAvatar () {
  /**
   * <p>Renders a user avatar with name in a table cell.</p>
   * <p>Includes avatar image and truncated name with proper overflow handling.</p>
   *
   * @param avatarSrc - URL of the user's avatar image
   * @param userName - Display name of the user
   * @param title - Tooltip text for the full name
   * @returns Object containing template slot content
   */
  const renderUserAvatar = (avatarSrc: string, userName: string, title: string) => ({
    template: `
      <div class="flex items-center overflow-hidden" :title="'${title}'">
        <div class="flex items-center flex-shrink-0 w-5 h-5 rounded-xl overflow-hidden mr-2">
          <Image
            src="${avatarSrc}"
            type="avatar"
            class="w-full" />
        </div>
        <div class="flex-1 truncate">${userName}</div>
      </div>
    `
  });

  /**
   * <p>Renders creator avatar cell with consistent styling.</p>
   *
   * @param record - Table record containing user information
   * @returns Object containing template slot content
   */
  const renderCreatorAvatar = (record: any) =>
    renderUserAvatar(record.creatorAvatar, record.creator, record.creator);

  /**
   * <p>Renders deleter avatar cell with consistent styling.</p>
   *
   * @param record - Table record containing user information
   * @returns Object containing template slot content
   */
  const renderDeleterAvatar = (record: any) =>
    renderUserAvatar(record.deletedByAvatar, record.deletedByName, record.deletedByName);

  return {
    renderUserAvatar,
    renderCreatorAvatar,
    renderDeleterAvatar
  };
}
