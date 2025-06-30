<template>
  <div>
    <h2>忘记密码</h2>
    <form @submit.prevent="submitFormSubmit">
      <div>
        <label for="email">电子邮件:</label>
          <input type="email" v-model="email" required />
      </div>
      <div v-if="securityQuestion">
        <label :for="securityQuestion">安全问题:</label>
          <input type="text" v-model="answer" required />
      </div>
      <button type="submit">重置密码</button>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      email: '',
      answer: '',
      securityQuestion: ''
    }
  },
  computed: {
    securityQuestion() {
      return this.$store.state.securityQuestion;
    }
  },
  methods: {
    async submit() {
      try {
        const response = await this.$http.post('/auth/forgot-password', {
           email: this.email,
           answer: this.answer
        });
        if (response.data.success) {
          alert('密码已重置成功！');
          this.$router.push('/login');
        } else {
          alert(response.data.message || '发生错误！');
        }
     } catch (error) {
        console.error(error);
      }
    }
  }
}
</script>