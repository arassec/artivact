<template>
  <ArtivactContent>
    <div class="col">
      <h1 class="av-text-h1">404</h1>

      <div class="text-h2" style="opacity:.4">
        Page not found...
      </div>
    </div>
  </ArtivactContent>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import ArtivactContent from 'components/ArtivactContent.vue';

export default defineComponent({
  name: 'ErrorNotFound',
  components: {ArtivactContent}
});
</script>
