<template>
  <svg-icon type="mdi" :path="path"></svg-icon>
</template>

<script>
import SvgIcon from '@jamescoyle/vue-icon';
import { mdiTag } from '@mdi/js';

export default {
  name: "IconTag",
  components: {
    SvgIcon
  },
  data() {
    return {
      path: mdiTag,
    }
  }
}
</script>