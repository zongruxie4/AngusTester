// ----------------------------------------------------
// ECharts configuration and setup
// ----------------------------------------------------

// Import ECharts core module - provides essential interfaces for ECharts usage
import * as echarts from 'echarts/core';

// Import chart types - all chart types have 'Chart' suffix
import { BarChart, GaugeChart, LineChart, PieChart } from 'echarts/charts';

// Import components - all components have 'Component' suffix
// Includes tooltip, title, grid, dataset, and data transformer components
import {
  DatasetComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  TransformComponent
} from 'echarts/components';

// Import features - label auto layout, global transition animations, etc.
import { LabelLayout, UniversalTransition } from 'echarts/features';

// Import Canvas renderer - CanvasRenderer or SVGRenderer is required
import { CanvasRenderer } from 'echarts/renderers';

// Register all required components and features
// This enables the specific chart types and functionality we need
const registeredECharts = echarts.use([
  // Core components
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,

  // Chart types
  BarChart,
  LineChart,
  PieChart,
  GaugeChart,

  // Features
  LabelLayout,
  UniversalTransition,

  // Renderer
  CanvasRenderer,

  // Additional components
  LegendComponent,
  ToolboxComponent
]);

// Export the configured ECharts instance
export default registeredECharts;
