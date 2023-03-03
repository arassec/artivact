<template>
  <div class="artivact-content q-pa-md">
    <div class="row items-center justify-between ">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ArtivactContent'
}
</script>

<style scoped>
.artivact-content {
  max-width: 65rem;
  margin-right: auto;
  margin-left: auto;
}
</style>
