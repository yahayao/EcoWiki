<template>
  <svg-icon type="mdi" :path="path"></svg-icon>
</template>

<script>
import SvgIcon from '@jamescoyle/vue-icon';
import { mdiTableOfContents } from '@mdi/js';

export default {
  name: "IconArticleTOC",
  components: {
    SvgIcon
  },
  data() {
    return {
      path: mdiTableOfContents,
    }
  }
}
</script>