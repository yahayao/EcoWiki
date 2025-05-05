<script setup lang="ts">

import { onMounted, onUnmounted, ref } from 'vue'

// Use refs for reactive values
const currentIndex = ref(0)
let cards: NodeListOf<HTMLElement> | null = null
let stackArea: HTMLElement | null = null

// Function to rotate cards with proper typing
function rotateCards(): void {
  if (!cards) return

  let angle = 0
  cards.forEach((card: HTMLElement) => {
    if (card.classList.contains("active")) {
      card.style.transform = `translate(-50%, -120vh) rotate(-48deg)`
    } else {
      card.style.transform = `translate(-50%, -50%) rotate(${angle}deg)`
      angle = angle - 10
    }
  })
}

// Function to handle responsive adjustments
function adjust(): void {
  const windowWidth = window.innerWidth
  const left = document.querySelector<HTMLElement>(".left")
  const stackAreaEl = document.querySelector<HTMLElement>(".stack-area")

  if (left && stackAreaEl) {
    left.remove()
    if (windowWidth < 800) {
      stackAreaEl.insertAdjacentElement("beforebegin", left)
    } else {
      stackAreaEl.insertAdjacentElement("afterbegin", left)
    }
  }
}

// Setup event handlers after component is mounted
onMounted(() => {
  cards = document.querySelectorAll<HTMLElement>(".card")
  stackArea = document.querySelector<HTMLElement>(".stack-area")

  if (stackArea) {
    stackArea.addEventListener("click", () => {
      if (!cards) return

      if (currentIndex.value < cards.length) {
        cards[currentIndex.value].classList.add("active")
        currentIndex.value++
        rotateCards()
      }
    })
  }

  // Initialize card positions
  rotateCards()
  adjust()

  // Set up event listeners
  window.addEventListener("resize", adjust)

  window.addEventListener("scroll", () => {
    if (!cards) return

    if (window.scrollY > window.innerHeight) {
      cards.forEach((card: HTMLElement) => {
        card.classList.remove("active")
      })
      currentIndex.value = 0
      rotateCards()
    }
  })
})

// Clean up event listeners when component is unmounted
onUnmounted(() => {
  window.removeEventListener("resize", adjust)
  window.removeEventListener("scroll", () => { })
})

</script>

<template>
  <div class="center">
    <div class="top">Scroll down</div>
    <div class="stack-area">
      <div class="left">
        <div class="title">Our Features</div>
        <div class="sub-title">
          <!-- website inctrudece -->
          We are a team of experts who are dedicated to providing you with
          the best possible service. We are always here to help you with any
          questions or concerns you may have.
          <br />
          <button>See More Details</button>
        </div>
      </div>
      <div class="right">
        <div class="cards">
          <div class="card">
            <div class="sub">Simplified</div>
            <div class="content">Complex tasks are now simple</div>
          </div>
          <div class="card">
            <div class="sub">Boost Productivity</div>
            <div class="content">Perform Tasks in less time</div>
          </div>
          <div class="card">
            <div class="sub">Facilitated learning</div>
            <div class="content">train anyone from anywhere</div>
          </div>
          <div class="card">
            <div class="sub">Support</div>
            <div class="content">Now its 24/7 support</div>
          </div>
        </div>
      </div>
    </div>
    <div class="bottom">Other Content...</div>
  </div>
</template>

<style scoped>
.center {
  width: 100%;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  /* background-image: url(./img/背景.jpg); */
  background-size: cover;
  background-position: center;
}

.stack-area {
  width: 100%;
  height: 100vh;
  position: relative;
  display: flex;
  justify-content: center;
}

.right,
.left {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* position: sticky; */
  top: 0;
  box-sizing: border-box;
  flex-basis: 50%;
}

.cards {
  width: 100%;
  height: 100%;
  position: relative;
}

.card {
  backdrop-filter: blur(10px);
  width: 370px;
  height: 370px;
  box-sizing: border-box;
  padding: 35px;
  border-radius: 6mm;
  display: flex;
  justify-content: space-between;
  flex-direction: column;
  position: absolute;
  top: 50%;
  left: 50%;
  transition: 0.5s ease-in-out;
}

.card:nth-child(1) {
  background: rgba(249, 102, 102, 0.8);
  z-index: 4;
}

.card:nth-child(2) {
  background: rgba(224, 203, 89, 0.8);
  z-index: 3;
}

.card:nth-child(3) {
  background: rgba(102, 224, 252, 0.8);
  z-index: 2;
}

.card:nth-child(4) {
  background: rgba(245, 85, 208, 0.8);
  z-index: 1;
}

.sub {
  font-family: poppins;
  font-size: 20px;
  font-weight: 700;
}

.content {
  font-family: poppins;
  font-size: 44px;
  font-weight: 700;
  line-height: 54px;
}

.card.active {
  transform-origin: bottom left;
}

.left {
  align-items: center;
  flex-direction: column;
}

.title {
  width: 420px;
  font-size: 84px;
  font-family: poppins;
  font-weight: 700;
  line-height: 88px;
}

.sub-title {
  width: 420px;
  font-family: poppins;
  font-size: 14px;
  margin-top: 30px;
}

.sub-title button {
  font-family: poppins;
  font-size: 14px;
  padding: 15px 30px;
  background: black;
  color: white;
  border-radius: 8mm;
  border: none;
  outline: none;
  cursor: pointer;
  margin-top: 20px;
}

.top {
  width: 100%;
  height: 100vh;
  font-family: poppins;
  font-size: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom {
  width: 100%;
  height: 100vh;
  font-family: poppins;
  font-size: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/*CSS Code for responsiveness*/
@media screen and (max-width: 800px) {
  .left {
    position: relative;
    width: 100vw;
  }
}
</style>
