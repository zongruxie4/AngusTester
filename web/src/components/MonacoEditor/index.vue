<script setup lang='ts'>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, toRaw, watch } from 'vue';

import * as monaco from 'monaco-editor';
import EditorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import JsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker';
import HtmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker';
import TsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';

/**
 * Ref: Editor container DOM element
 */
const editorRef = ref<HTMLElement>();

/**
 * Supported editor languages
 */
type EditorLanguage = 'json' | 'html' | 'typescript' | 'text' | 'yaml';

/**
 * Editor theme options
 */
type EditorTheme = 'vs' | 'vs-dark';

/**
 * Component props interface
 */
interface Props {
  value: string | undefined;         // Editor content value
  theme?: EditorTheme;               // Editor theme (light/dark)
  language?: EditorLanguage;         // Programming language for syntax highlighting
  notifyClear?: number;              // Trigger to clear/reset editor (increment to trigger)
  isFormat?: boolean;                // Whether to execute formatting
  isClear?: boolean;                 // Whether to execute clear
  readOnly?: boolean;                // Read-only mode
  searchText?: string;               // Text to search for
  isExcuteSearch?: boolean;          // Whether to toggle to next search match
  loading?: boolean;                 // Whether editor resources are loaded
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  theme: 'vs',
  language: 'text',
  isFormat: false,
  isClear: false,
  readOnly: false,
  searchText: '',
  isExcuteSearch: false,
  notifyClear: 0,
  loading: false
});

/**
 * Emitted events interface
 */
const emit = defineEmits<{
  (e: 'update:value', val: string): void;   // Emitted when editor value changes
  (e: 'change', val: string): void;         // Emitted when editor value changes
  (e: 'update:loading', val: boolean): void; // Emitted when loading state changes
}>();

/**
 * Reactive state for editor instance and search
 */
const state = reactive<{
  editorInstace: monaco.editor.IStandaloneCodeEditor | null;  // Monaco editor instance
  allMatches: Array<monaco.Range>;                            // All search matches
  matchIndex: number;                                          // Current match index
}>({
  editorInstace: null,
  allMatches: [],
  matchIndex: 0
});

/**
 * Configure Monaco Environment for Web Workers
 * Sets up language-specific workers for better performance
 * - JSON Worker: JSON parsing and validation
 * - HTML Worker: HTML/CSS parsing
 * - TypeScript Worker: TypeScript/JavaScript IntelliSense
 * - Editor Worker: Default fallback worker
 */
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
self.MonacoEnvironment = {
  getWorker(_: any, label: string) {
    if (label === 'json') {
      return new JsonWorker();
    }
    if (label === 'html') {
      return new HtmlWorker();
    }
    if (label === 'javascript' || label === 'typescript') {
      return new TsWorker();
    }
    return new EditorWorker();
  }
};

/**
 * Create Monaco Editor instance
 * Initializes the editor with configuration options and sets up event listeners
 * 
 * Configuration includes:
 * - Language-specific syntax highlighting
 * - Theme (light/dark mode)
 * - Read-only mode toggle
 * - Minimap disabled for cleaner interface
 * - Custom scrollbar styling
 * - Code folding enabled
 * - Automatic layout adjustment
 * - Smooth cursor animation
 * - Word wrapping enabled
 * - Custom font size and scrolling behavior
 */
const create = (): void => {
  if (!editorRef.value) {
    console.warn('Editor container element not found');
    return;
  }

  state.editorInstace = monaco.editor.create(editorRef.value, {
    value: props.value,                    // Initial editor content
    language: props.language,              // Programming language for syntax highlighting
    theme: props.theme,                    // Editor theme (light/dark)
    readOnly: props.readOnly,              // Read-only mode toggle
    minimap: {                             // Minimap configuration
      enabled: false                       // Disable minimap for cleaner interface
    },
    scrollbar: {                           // Custom scrollbar styling
      verticalSliderSize: 7,               // Vertical slider width
      horizontalSliderSize: 7,             // Horizontal slider width
      verticalScrollbarSize: 7,            // Vertical scrollbar width
      horizontalScrollbarSize: 7,          // Horizontal scrollbar width
      arrowSize: 11,                       // Scrollbar arrow size
      useShadows: true,                    // Enable scrollbar shadows
      scrollByPage: false,                 // Scroll by line instead of page
      vertical: 'auto',                    // Vertical scrollbar behavior
      horizontal: 'auto',                  // Horizontal scrollbar behavior
      verticalHasArrows: false,            // Disable vertical arrows
      horizontalHasArrows: false           // Disable horizontal arrows
    },
    folding: true,                         // Enable code folding
    automaticLayout: true,                 // Auto-resize based on container
    contextmenu: true,                     // Enable right-click context menu
    renderLineHighlight: 'none',           // Disable current line highlighting
    cursorSmoothCaretAnimation: 'on',      // Enable smooth cursor animation
    trimAutoWhitespace: true,              // Trim trailing whitespace
    fontSize: 12,                          // Font size in pixels
    wordBreak: 'normal',                   // Word breaking behavior
    wordWrap: 'on',                        // Enable word wrapping
    smoothScrolling: false,                // Disable smooth scrolling
    mouseWheelScrollSensitivity: 1.5,      // Mouse wheel scroll sensitivity
    scrollBeyondLastLine: true             // Allow scrolling beyond last line
  });

  // Set up content change listener
  state.editorInstace.onDidChangeModelContent(() => {
    // Use toRaw to prevent Vue reactivity issues that can cause performance problems
    const value = toRaw(state.editorInstace)?.getValue();
    emit('update:value', value || '');
    emit('change', value || '');
  });

  // Auto-format document after creation
  formatDocument();
};

/**
 * Clear editor value to initial state
 * Resets the editor content to the original props.value
 * Used when notifyClear or isClear props change
 */
const clearValue = (): void => {
  if (state.editorInstace && props.value) {
    toRaw(state.editorInstace).setValue(props.value);
  }
};

/**
 * Set editor value from props
 * Updates the editor content with the current props.value
 * Used when the value prop changes externally
 */
const setValue = (): void => {
  if (state.editorInstace) {
    toRaw(state.editorInstace).setValue(props.value || '');
  }
};

/**
 * Change editor language
 * Updates the syntax highlighting and language features
 * Also clears the value to prevent language mismatch issues
 */
const changeLanguage = (): void => {
  if (state.editorInstace) {
    const model = toRaw(state.editorInstace).getModel();
    if (model) {
      monaco.editor.setModelLanguage(model, props.language);
    }
  }
};

/**
 * Change editor theme
 * Switches between light (vs) and dark (vs-dark) themes
 * Applied globally to all Monaco editor instances
 */
const changeTheme = (): void => {
  monaco.editor.setTheme(props.theme);
};

/**
 * Toggle read-only mode
 * Enables or disables editing based on props.readOnly
 * Updates editor options without recreating the instance
 */
const changeReadOnly = (): void => {
  toRaw(state.editorInstace)?.updateOptions({ readOnly: props.readOnly });
};

/**
 * Format document content
 * Executes the built-in format document action
 * Only runs when props.isFormat is true
 */
const formatDocument = (): void => {
  if (props.isFormat && state.editorInstace) {
    toRaw(state.editorInstace)?.getAction('editor.action.formatDocument')?.run();
  }
};
/**
 * Find all text matches in the editor
 * Searches for the searchText prop and highlights all matches
 * Automatically selects and centers the first match
 * 
 * Search options:
 * - Case sensitive: false
 * - Whole word: false  
 * - Regex: false
 * - Word separators: null
 * - Capture matches: false
 */
const findAllMatches = (): void => {
  state.allMatches = [];
  state.matchIndex = 0;
  
  if (props.searchText && state.editorInstace) {
    const data = toRaw(state.editorInstace)?.getModel()?.findMatches(
      props.searchText,    // Search text
      false,               // Case sensitive
      false,               // Whole word
      false,               // Regex
      null,                // Word separators
      false                // Capture matches
    );
    
    (data || []).forEach((item, index) => {
      const { startLineNumber, endLineNumber, startColumn, endColumn } = item.range;
      const range = new monaco.Range(startLineNumber, startColumn, endLineNumber, endColumn);
      state.allMatches.push(range);
      
      // Select and center the first match
      if (index === 0) {
        toRaw(state.editorInstace)?.setSelection(range);
        toRaw(state.editorInstace)?.revealRangeInCenter(range);
      }
    });
  }
};

/**
 * Navigate to next search match
 * Cycles through all found matches when isExcuteSearch prop changes
 * Wraps around to the first match after reaching the last one
 */
const gotoMatch = (): void => {
  if (props.isExcuteSearch && state.allMatches.length > 0) {
    // Cycle through matches
    if (state.matchIndex >= state.allMatches.length - 1) {
      state.matchIndex = 0;  // Wrap to first match
    } else {
      state.matchIndex++;    // Go to next match
    }
    
    const range = state.allMatches[state.matchIndex];
    toRaw(state.editorInstace)?.setSelection(range);
    toRaw(state.editorInstace)?.revealRangeInCenter(range);
  }
};

/**
 * Destroy editor instance
 * Properly disposes of the Monaco editor to prevent memory leaks
 * Must use toRaw to avoid Vue reactivity issues that can cause performance problems
 */
const destroy = (): void => {
  if (state.editorInstace) {
    // Use toRaw to prevent Vue reactivity issues that can cause performance problems
    toRaw(state.editorInstace).dispose();
    state.editorInstace = null;
  }
};

/**
 * Watch: notifyClear prop changes
 * Triggers editor value reset when notifyClear increments
 */
watch(() => props.notifyClear, () => {
  clearValue();
});

/**
 * Watch: isClear prop changes
 * Triggers editor value reset when isClear becomes true
 */
watch(() => props.isClear, () => {
  clearValue();
});

/**
 * Watch: language prop changes
 * Updates editor language and resets value to prevent language mismatch
 * Language switching can change data format, so we reset the content
 */
watch(() => props.language, () => {
  changeLanguage();
  clearValue(); // Reset data when language changes to prevent format issues
});

/**
 * Watch: theme prop changes
 * Updates editor theme (light/dark mode)
 */
watch(() => props.theme, () => {
  changeTheme();
});

/**
 * Watch: isFormat prop changes
 * Triggers document formatting when isFormat becomes true
 */
watch(() => props.isFormat, () => {
  formatDocument();
});

/**
 * Watch: readOnly prop changes
 * Toggles read-only mode when readOnly prop changes
 */
watch(() => props.readOnly, () => {
  changeReadOnly();
});

/**
 * Watch: searchText prop changes
 * Performs new search when search text changes
 */
watch(() => props.searchText, () => {
  findAllMatches();
});

/**
 * Watch: isExcuteSearch prop changes
 * Navigates to next search match when search execution is triggered
 */
watch(() => props.isExcuteSearch, () => {
  gotoMatch();
});

/**
 * Watch: value prop changes
 * Updates editor content when value prop changes externally
 * Includes whitespace normalization to prevent unnecessary updates
 */
watch(() => props.value, (newValue) => {
  const refValue = toRaw(state.editorInstace)?.getValue();
  
  // Normalize whitespace for comparison to prevent unnecessary updates
  if (refValue && newValue && (newValue.replace(' ', '') === refValue.replace(' ', ''))) {
    return;
  }
  
  // Update editor if values are different
  if (newValue !== refValue) {
    setValue();
  }
});

/**
 * Component mounted lifecycle hook
 * Creates the Monaco editor instance and emits loading completion
 */
onMounted(() => {
  create();
  nextTick(() => {
    emit('update:loading', false);
  });
});

/**
 * Component before unmount lifecycle hook
 * Properly disposes of the Monaco editor to prevent memory leaks
 */
onBeforeUnmount(() => {
  destroy();
});

/**
 * Expose public methods to parent components
 * Allows external formatting control
 */
defineExpose({
  /**
   * Format the current document
   * Executes the built-in format document action
   */
  format: (): void => {
    if (state.editorInstace) {
      toRaw(state.editorInstace).getAction('editor.action.formatDocument')?.run();
    }
  }
});
</script>

<template>
  <div>
    <div ref="editorRef" class="h-full w-full"></div>
  </div>
</template>
