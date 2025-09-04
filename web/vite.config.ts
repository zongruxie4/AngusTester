import vue from '@vitejs/plugin-vue';
import { defineConfig } from 'vite';
import { resolve } from 'path';
import viteCompression from 'vite-plugin-compression';
import vueJsx from '@vitejs/plugin-vue-jsx';
import { nodePolyfills } from 'vite-plugin-node-polyfills';

import server from './server/dev-server';

export default defineConfig({
  server,
  plugins: [
    vue(),
    vueJsx(),
    viteCompression({
      algorithm: 'gzip',
      ext: '.gz',
      threshold: 2048,
      deleteOriginFile: false,
      verbose: true
    }),
    nodePolyfills()
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      src: resolve(__dirname, 'src')
    },
    extensions: ['.ts', '.tsx', '.js', '.jsx', '.json', '.vue']
  },
  envDir: 'conf',
  clearScreen: false,
  json: {
    namedExports: true,
    stringify: false
  },
  build: {
    outDir: 'dist',
    target: 'es2020',
    minify: false,
    manifest: false,
    assetsDir: 'assets',
    cssCodeSplit: true,
    assetsInlineLimit: 4096,
    chunkSizeWarningLimit: 500,
    reportCompressedSize: false,
    rollupOptions: {
      external: ['canvg']
    }
  }
});
