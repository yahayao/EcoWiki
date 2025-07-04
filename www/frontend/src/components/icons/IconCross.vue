<template>
  <svg-icon type="mdi" :path="path"></svg-icon>
</template>

<script>
import SvgIcon from '@jamescoyle/vue-icon';
import { mdiCloseThick } from '@mdi/js';

export default {
  name: "IconCross",
  components: {
    SvgIcon
  },
  data() {
    return {
      path: mdiCloseThick,
    }
  }
}
</script>