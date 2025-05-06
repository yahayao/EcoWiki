<template>
    <svg ref="containerRef" class="container"></svg>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import gsap from 'gsap'

interface Ball extends SVGCircleElement {
    line: SVGLineElement;
    ori_x: number;
    ori_y: number;
    move_x?: number;
    move_y?: number;
    animater?: gsap.core.Timeline;
}

interface Magnetic {
    container: SVGSVGElement | null;
    width: number;
    height: number;
    left: number;
    top: number;
    lines: number;
    rows: number;
    balls: Ball[];
    mouse_radius: number;
    init(): void;
    resize(): void;
    create_yoyo(radius: number): void;
    move_balls(x: number, y: number): void;
}

const containerRef = ref<SVGSVGElement | null>(null)

const magnetic: Magnetic = {
    container: null,
    width: 0,
    height: 0,
    left: 0,
    top: 0,
    lines: 10,
    rows: 10,
    balls: [],
    mouse_radius: 180,

    init() {
        if (!this.container) return;
        this.resize();
        this.create_yoyo(25);
        document.addEventListener("mousemove", this.handleMouseMove);
        window.addEventListener("resize", this.handleResize);
    },

    handleMouseMove(e: MouseEvent) {
        magnetic.move_balls(e.x, e.y);
    },

    handleResize() {
        magnetic.resize();
    },

    resize() {
        if (!this.container) return;
        const rect = this.container.getBoundingClientRect();
        this.width = rect.width;
        this.height = rect.height;
        this.left = rect.left;
        this.top = rect.top;
    },

    create_yoyo(radius: number) {
        if (!this.container) return;
        
        for (let r = 0; r <= this.rows; r++) {
            for (let l = 0; l <= this.lines; l++) {
                const x = this.width / this.lines * l;
                const y = this.height / this.rows * r;

                const ball = document.createElementNS("http://www.w3.org/2000/svg", "circle") as Ball;
                ball.setAttribute("fill", "#17f700");
                ball.setAttribute("cx", x.toString());
                ball.setAttribute("cy", y.toString());
                ball.setAttribute("r", radius.toString());

                const point = document.createElementNS("http://www.w3.org/2000/svg", "circle");
                point.setAttribute("fill", "#f7f7f7");
                point.setAttribute("cx", x.toString());
                point.setAttribute("cy", y.toString());
                point.setAttribute("r", (radius / 5).toString());

                const line = document.createElementNS("http://www.w3.org/2000/svg", "line");
                line.setAttribute("x1", x.toString());
                line.setAttribute("y1", y.toString());
                line.setAttribute("x2", x.toString());
                line.setAttribute("y2", y.toString());

                this.container.appendChild(line);
                this.container.appendChild(point);
                this.container.appendChild(ball);

                ball.line = line;
                ball.ori_x = x;
                ball.ori_y = y;
                this.balls.push(ball);
            }
        }
    },

    move_balls(x: number, y: number) {
        this.balls.forEach(ball => {
            const mouse_x = x - this.left;
            const mouse_y = y - this.top;
            const dx = ball.ori_x - mouse_x;
            const dy = ball.ori_y - mouse_y;
            const distance = Math.sqrt(dx ** 2 + dy ** 2);

            if (distance <= this.mouse_radius) {
                ball.move_x = mouse_x + dx / distance * this.mouse_radius;
                ball.move_y = mouse_y + dy / distance * this.mouse_radius;

                if (ball.animater) ball.animater.kill();
                ball.animater = gsap.timeline()
                    .to(ball, {
                        attr: {
                            cx: ball.move_x,
                            cy: ball.move_y,
                        },
                        duration: 0.5,
                        ease: "power3.out",
                    })
                    .to(ball.line, {
                        attr: {
                            x2: ball.move_x,
                            y2: ball.move_y,
                        },
                        duration: 0.5,
                        ease: "power3.out",
                    }, "<")
                    .to(ball, {
                        attr: {
                            cx: ball.ori_x,
                            cy: ball.ori_y,
                        },
                        duration: 1,
                        ease: "power3.out",
                    }, "<0.1")
                    .to(ball.line, {
                        attr: {
                            x2: ball.ori_x,
                            y2: ball.ori_y,
                        },
                        duration: 1,
                        ease: "power3.out",
                    }, "<");
            }
        });
    }
};

onMounted(() => {
    magnetic.container = containerRef.value;
    magnetic.init();
});

onUnmounted(() => {
    document.removeEventListener("mousemove", magnetic.handleMouseMove);
    window.removeEventListener("resize", magnetic.handleResize);
});
</script>

<style>
.container {
    position: absolute;
    width: 50rem;
    height: 50rem;
    overflow: visible;
}

.container line {
    fill: none;
    stroke: #f7f7f7;
    stroke-width: 2;
}
</style>