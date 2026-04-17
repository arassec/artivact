<template>
  <div ref="viewerContainerRef" class="model-viewer-container">
    <model-viewer
      v-if="$q.platform.is.mobile && isModelViewerFormat && modelUrl"
      id="av-model-viewer"
      :src="modelUrl"
      camera-controls
      class="fit"
      :reveal="reveal"
    >
      <div
        id="button-load"
        slot="poster"
        class="download-div row justify-center items-center"
      >
        <q-btn icon="download" class="column" @click="reveal = 'auto'">{{
            $t('ItemModelViewer.downloadModelButtonLabel')
          }}
        </q-btn>
      </div>
    </model-viewer>

    <model-viewer
      v-else-if="!$q.platform.is.mobile && isModelViewerFormat && modelUrl"
      id="av-model-viewer"
      :src="modelUrl"
      camera-controls
      class="fit"
    />

    <div v-else-if="isObjModel && modelUrl" ref="objViewerRef" class="fit"/>

    <q-btn
      v-if="fullscreenAllowed"
      round
      dense
      flat
      class="absolute-top-right fullscreen-button"
      color="white"
      :icon="isFullscreen ? 'fullscreen_exit' : 'fullscreen'"
      @click="toggleFullscreen"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import {AmbientLight, Box3, DirectionalLight, PerspectiveCamera, Scene, Vector3, WebGLRenderer} from 'three';
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls.js';
import {OBJLoader} from 'three/examples/jsm/loaders/OBJLoader.js';

const MIN_MODEL_DIMENSION = 1;

const props = defineProps({
  modelUrl: {
    required: true,
    type: String,
  },
  fullscreenAllowed: {
    required: false,
    type: Boolean,
    default: false,
  },
});

const reveal = ref('manual');
const isFullscreen = ref(false);
const viewerContainerRef = ref<HTMLDivElement | null>(null);
const objViewerRef = ref<HTMLDivElement | null>(null);

const modelExtension = computed(() => {
  const sanitizedModelUrl = props.modelUrl.split('?')[0].split('#')[0];
  const extension = sanitizedModelUrl.split('.').pop();
  return extension ? extension.toLowerCase() : '';
});

const isModelViewerFormat = computed(() => ['glb', 'gltf'].includes(modelExtension.value));
const isObjModel = computed(() => modelExtension.value === 'obj');

let scene: Scene | null = null;
let camera: PerspectiveCamera | null = null;
let renderer: WebGLRenderer | null = null;
let controls: OrbitControls | null = null;
let resizeObserver: ResizeObserver | null = null;
let animationFrameId: number | null = null;

function fitCameraToObject() {
  if (!scene || !camera || !controls) {
    return;
  }

  const box = new Box3().setFromObject(scene);
  const size = box.getSize(new Vector3());
  const center = box.getCenter(new Vector3());
  const largestDimension = Math.max(size.x, size.y, size.z, MIN_MODEL_DIMENSION);
  const distance = largestDimension * 2;

  controls.target.copy(center);
  camera.position.set(center.x + distance, center.y + distance * 0.6, center.z + distance);
  camera.near = 0.1;
  camera.far = distance * 100;
  camera.updateProjectionMatrix();
  controls.update();
}

function renderScene() {
  if (!renderer || !scene || !camera || !controls) {
    return;
  }

  controls.update();
  renderer.render(scene, camera);
  animationFrameId = window.requestAnimationFrame(renderScene);
}

function resizeObjViewer() {
  if (!renderer || !camera || !objViewerRef.value) {
    return;
  }

  const width = objViewerRef.value.clientWidth;
  const height = objViewerRef.value.clientHeight || 1;

  renderer.setSize(width, height);
  camera.aspect = width / height;
  camera.updateProjectionMatrix();
}

function cleanupObjViewer() {
  if (animationFrameId !== null) {
    window.cancelAnimationFrame(animationFrameId);
    animationFrameId = null;
  }

  resizeObserver?.disconnect();
  resizeObserver = null;
  controls?.dispose();
  controls = null;
  renderer?.dispose();
  renderer = null;
  scene = null;
  camera = null;

  if (objViewerRef.value) {
    objViewerRef.value.replaceChildren();
  }
}

async function initializeObjViewer() {
  cleanupObjViewer();

  if (!isObjModel.value) {
    return;
  }

  await nextTick();

  if (!objViewerRef.value) {
    return;
  }

  scene = new Scene();

  const width = objViewerRef.value.clientWidth || 1;
  const height = objViewerRef.value.clientHeight || 1;

  camera = new PerspectiveCamera(45, width / height, 0.1, 1000);

  renderer = new WebGLRenderer({antialias: true, alpha: true});
  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.setSize(width, height);
  objViewerRef.value.replaceChildren(renderer.domElement);

  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;

  scene.add(new AmbientLight(0xffffff, 1.4));

  const mainLight = new DirectionalLight(0xffffff, 1.2);
  mainLight.position.set(8, 10, 6);
  scene.add(mainLight);

  const fillLight = new DirectionalLight(0xffffff, 0.7);
  fillLight.position.set(-6, 4, -8);
  scene.add(fillLight);

  resizeObserver = new ResizeObserver(() => resizeObjViewer());
  resizeObserver.observe(objViewerRef.value);

  const loader = new OBJLoader();
  loader.load(props.modelUrl, (object) => {
    scene?.add(object);
    fitCameraToObject();
    renderScene();
  });
}

async function toggleFullscreen() {
  if (!viewerContainerRef.value) {
    return;
  }

  if (document.fullscreenElement === viewerContainerRef.value) {
    await document.exitFullscreen();
    return;
  }

  await viewerContainerRef.value.requestFullscreen();
}

function updateFullscreenState() {
  isFullscreen.value = document.fullscreenElement === viewerContainerRef.value;
}

watch(() => props.modelUrl, () => {
  void initializeObjViewer();
});

onMounted(() => {
  document.addEventListener('fullscreenchange', updateFullscreenState);
  void initializeObjViewer();
});

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', updateFullscreenState);
  cleanupObjViewer();
});
</script>

<style scoped>
.model-viewer-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 300px;
  background: #1d1d1d;
}

.download-div {
  height: 100%;
}

.fullscreen-button {
  z-index: 2;
  margin: 15px 15px 0 0;
}
</style>
