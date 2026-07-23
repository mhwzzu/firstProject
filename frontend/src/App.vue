<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const memories = ref([])
const recommendations = ref([])
const summary = reactive({ totalMemories: 0, restaurantCount: 0, tripCount: 0, cityCount: 0 })
const activeView = ref('home')
const loading = ref(true)
const errorMessage = ref('')
const modalOpen = ref(false)
const saving = ref(false)
const editingId = ref(null)
const toast = ref('')

const emptyForm = () => ({
  title: '',
  type: 'RESTAURANT',
  city: '',
  address: '',
  visitedAt: new Date().toISOString().slice(0, 10),
  rating: 5,
  note: '',
  tags: '',
  emoji: '🍜',
  latitude: null,
  longitude: null
})
const form = reactive(emptyForm())

const navItems = [
  { id: 'home', label: '漫游首页' },
  { id: 'restaurants', label: '吃过的店' },
  { id: 'trips', label: '旅行足迹' },
  { id: 'recommendations', label: '下一站' }
]

const filteredMemories = computed(() => {
  if (activeView.value === 'restaurants') return memories.value.filter(item => item.type === 'RESTAURANT')
  if (activeView.value === 'trips') return memories.value.filter(item => item.type === 'TRIP')
  return memories.value
})

const pageTitle = computed(() => ({
  restaurants: '一起吃过的店',
  trips: '走过的地方',
  recommendations: '下一站，去哪里？'
}[activeView.value] || '把平常的日子，过成值得收藏的故事'))

const formatDate = date => new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: 'short', day: 'numeric'
}).format(new Date(`${date}T00:00:00`))

const fetchJson = async (url, options) => {
  const response = await fetch(url, options)
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw new Error(body.message || '请求没有成功，请稍后再试')
  }
  return response.status === 204 ? null : response.json()
}

const loadData = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const [memoryData, summaryData, destinationData] = await Promise.all([
      fetchJson('/api/memories'),
      fetchJson('/api/summary'),
      fetchJson('/api/recommendations')
    ])
    memories.value = memoryData
    Object.assign(summary, summaryData)
    recommendations.value = destinationData
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

const openCreate = (type = 'RESTAURANT') => {
  editingId.value = null
  Object.assign(form, emptyForm(), {
    type,
    emoji: type === 'RESTAURANT' ? '🍜' : '🧳'
  })
  modalOpen.value = true
}

const openEdit = memory => {
  editingId.value = memory.id
  Object.assign(form, memory)
  modalOpen.value = true
}

const saveMemory = async () => {
  saving.value = true
  try {
    const url = editingId.value ? `/api/memories/${editingId.value}` : '/api/memories'
    await fetchJson(url, {
      method: editingId.value ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...form, rating: Number(form.rating) })
    })
    modalOpen.value = false
    toast.value = editingId.value ? '这段回忆已经更新' : '新的回忆已经收进漫游簿'
    setTimeout(() => { toast.value = '' }, 2600)
    await loadData()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

const deleteMemory = async memory => {
  if (!window.confirm(`确定删除“${memory.title}”吗？`)) return
  try {
    await fetchJson(`/api/memories/${memory.id}`, { method: 'DELETE' })
    toast.value = '这条记录已删除'
    setTimeout(() => { toast.value = '' }, 2200)
    await loadData()
  } catch (error) {
    errorMessage.value = error.message
  }
}

const setType = type => {
  form.type = type
  form.emoji = type === 'RESTAURANT' ? '🍜' : '🧳'
}

onMounted(loadData)
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <button class="brand" aria-label="返回漫游首页" @click="activeView = 'home'">
        <span class="brand-mark">∞</span>
        <span>
          <strong>两个人的漫游簿</strong>
          <small>Together, everywhere.</small>
        </span>
      </button>
      <nav aria-label="主要导航">
        <button
          v-for="item in navItems"
          :key="item.id"
          :class="{ active: activeView === item.id }"
          @click="activeView = item.id"
        >
          {{ item.label }}
        </button>
      </nav>
      <button class="add-button" @click="openCreate(activeView === 'trips' ? 'TRIP' : 'RESTAURANT')">
        <span>＋</span> 记一笔
      </button>
    </header>

    <main>
      <section v-if="activeView === 'home'" class="hero">
        <div class="hero-copy">
          <p class="eyebrow">OUR LITTLE JOURNEY · 从第一次出发开始</p>
          <h1>把平常的日子，<br><em>过成值得收藏的故事。</em></h1>
          <p class="hero-description">
            记下每一家舍不得忘记的小店，每一条牵手走过的街，
            也一起期待地图上还没点亮的下一站。
          </p>
          <div class="hero-actions">
            <button class="primary" @click="openCreate('RESTAURANT')">记录今天 <span>↗</span></button>
            <button class="text-button" @click="activeView = 'trips'">翻翻我们的足迹 <span>→</span></button>
          </div>
        </div>
        <div class="memory-collage" aria-label="共同旅行回忆拼贴">
          <div class="postcard postcard-main">
            <div class="scene scene-lake"><span>☀</span><i></i></div>
            <div class="postcard-caption">
              <strong>杭州 · 西湖</strong>
              <span>2025.04.19</span>
            </div>
            <p>“那天的晚风，刚好也喜欢我们。”</p>
          </div>
          <div class="ticket">
            <span>ADMIT TWO</span>
            <strong>一起去更远的地方</strong>
            <small>NO. 0520</small>
          </div>
          <div class="round-stamp">LOVE<br>TRIP</div>
          <span class="tape tape-one"></span>
          <span class="tape tape-two"></span>
        </div>
      </section>

      <section v-if="activeView === 'home'" class="stats" aria-label="共同回忆统计">
        <article>
          <span class="stat-icon peach">♡</span>
          <div><strong>{{ summary.totalMemories }}</strong><small>段共同回忆</small></div>
        </article>
        <article>
          <span class="stat-icon yellow">⌂</span>
          <div><strong>{{ summary.restaurantCount }}</strong><small>家好吃的店</small></div>
        </article>
        <article>
          <span class="stat-icon green">⌖</span>
          <div><strong>{{ summary.cityCount }}</strong><small>座点亮的城市</small></div>
        </article>
        <article class="next-stat" @click="activeView = 'recommendations'">
          <div><small>下一次出发</small><strong>等我们决定</strong></div><span>→</span>
        </article>
      </section>

      <section v-if="activeView !== 'recommendations'" class="content-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">{{ activeView === 'home' ? 'RECENT STORIES' : 'OUR COLLECTION' }}</p>
            <h2>{{ activeView === 'home' ? '最近收藏的故事' : pageTitle }}</h2>
          </div>
          <button v-if="activeView === 'home'" class="text-button" @click="activeView = 'trips'">查看全部 <span>→</span></button>
        </div>

        <div v-if="loading" class="state-card">正在翻开漫游簿…</div>
        <div v-else-if="errorMessage" class="state-card error">
          {{ errorMessage }} <button @click="loadData">重新加载</button>
        </div>
        <div v-else-if="filteredMemories.length === 0" class="state-card">
          这里还没有记录。下一段故事，就从今天开始吧。
        </div>
        <div v-else class="memory-grid">
          <article
            v-for="memory in (activeView === 'home' ? filteredMemories.slice(0, 3) : filteredMemories)"
            :key="memory.id"
            class="memory-card"
          >
            <div class="memory-visual" :class="memory.type.toLowerCase()">
              <span class="memory-emoji">{{ memory.emoji }}</span>
              <span class="type-chip">{{ memory.type === 'RESTAURANT' ? '一起吃过' : '一起走过' }}</span>
              <div class="visual-landscape"><i></i><b></b></div>
            </div>
            <div class="memory-body">
              <div class="memory-meta">
                <span>{{ memory.city }} · {{ memory.address }}</span>
                <span>{{ '★'.repeat(memory.rating || 0) }}</span>
              </div>
              <h3>{{ memory.title }}</h3>
              <p>{{ memory.note }}</p>
              <div class="tag-row">
                <span v-for="tag in (memory.tags || '').split(',').filter(Boolean)" :key="tag"># {{ tag }}</span>
              </div>
              <footer>
                <time>{{ formatDate(memory.visitedAt) }}</time>
                <div>
                  <button aria-label="编辑记录" @click="openEdit(memory)">编辑</button>
                  <button aria-label="删除记录" @click="deleteMemory(memory)">删除</button>
                </div>
              </footer>
            </div>
          </article>
        </div>
      </section>

      <section v-else class="recommendation-section">
        <div class="recommendation-intro">
          <p class="eyebrow">THE NEXT CHAPTER</p>
          <h1>{{ pageTitle }}</h1>
          <p>根据你们喜欢的慢旅行、美食和海边体验，漫游簿挑出了三个还没点亮的目的地。</p>
        </div>
        <div class="destination-grid">
          <article v-for="(destination, index) in recommendations" :key="destination.city">
            <div class="destination-number">0{{ index + 1 }}</div>
            <span class="destination-emoji">{{ destination.emoji }}</span>
            <p>{{ destination.province }}</p>
            <h2>{{ destination.city }}</h2>
            <div class="match"><span :style="{ width: destination.matchScore + '%' }"></span></div>
            <small>{{ destination.matchScore }}% 契合你们的偏好</small>
            <p class="reason">{{ destination.reason }}</p>
            <footer><span>适合 {{ destination.bestSeason }}</span><button @click="openCreate('TRIP')">加入期待清单 ＋</button></footer>
          </article>
        </div>
        <div class="recommendation-note">
          <span>✦</span>
          <p><strong>推荐会越来越懂你们</strong>记录更多去过的城市和喜欢的体验，下一站建议也会跟着变化。</p>
        </div>
      </section>
    </main>

    <footer class="page-footer">
      <span>∞</span>
      <p>愿以后翻开这里，每一页都有当时的风。</p>
      <small>Made for two · {{ new Date().getFullYear() }}</small>
    </footer>

    <div v-if="modalOpen" class="modal-backdrop" @click.self="modalOpen = false">
      <form class="modal" @submit.prevent="saveMemory">
        <button type="button" class="modal-close" aria-label="关闭" @click="modalOpen = false">×</button>
        <p class="eyebrow">{{ editingId ? 'EDIT A MEMORY' : 'A NEW MEMORY' }}</p>
        <h2>{{ editingId ? '重新写好这段故事' : '今天，发生了什么好事？' }}</h2>
        <div class="type-switch">
          <button type="button" :class="{ active: form.type === 'RESTAURANT' }" @click="setType('RESTAURANT')">🍜 吃过的店</button>
          <button type="button" :class="{ active: form.type === 'TRIP' }" @click="setType('TRIP')">🧳 旅行足迹</button>
        </div>
        <label>这段回忆的名字<input v-model.trim="form.title" required maxlength="100" placeholder="例如：海边看过的那场日落"></label>
        <div class="form-row">
          <label>城市<input v-model.trim="form.city" required placeholder="厦门"></label>
          <label>地点<input v-model.trim="form.address" placeholder="鼓浪屿"></label>
        </div>
        <div class="form-row">
          <label>日期<input v-model="form.visitedAt" type="date" required></label>
          <label>心动指数
            <select v-model="form.rating">
              <option :value="5">★★★★★ 一定再去</option>
              <option :value="4">★★★★☆ 很喜欢</option>
              <option :value="3">★★★☆☆ 还不错</option>
              <option :value="2">★★☆☆☆ 一般般</option>
              <option :value="1">★☆☆☆☆ 特别体验</option>
            </select>
          </label>
        </div>
        <label>想留下的话<textarea v-model.trim="form.note" maxlength="1000" rows="4" placeholder="写下味道、天气、说过的话，或一个只有你们懂的细节…"></textarea></label>
        <label>标签<input v-model.trim="form.tags" placeholder="海边, 日落, 周末（用逗号分隔）"></label>
        <button class="primary submit-button" :disabled="saving">{{ saving ? '正在收藏…' : '收进漫游簿' }}</button>
      </form>
    </div>

    <div v-if="toast" class="toast">{{ toast }}</div>
  </div>
</template>

