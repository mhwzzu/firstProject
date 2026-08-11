<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const memories = ref([])
const recommendations = ref([])
const summary = reactive({ totalMemories: 0, restaurantCount: 0, tripCount: 0, cityCount: 0 })
const activeView = ref('home')
const loading = ref(true)
const authChecked = ref(false)
const currentUser = ref(null)
const errorMessage = ref('')
const modalOpen = ref(false)
const saving = ref(false)
const editingId = ref(null)
const toast = ref('')
const placeResults = ref([])
const placeSearching = ref(false)
const placeMessage = ref('')
const placeKeyword = ref('')
const loginLoading = ref(false)
const loginError = ref('')

const loginForm = reactive({
  username: 'mhwzzu',
  password: ''
})

const categoryOptions = [
  { value: 'FOOD', label: '美食', emoji: '🍜' },
  { value: 'MILK_TEA', label: '奶茶', emoji: '🧋' },
  { value: 'COFFEE', label: '咖啡', emoji: '☕' },
  { value: 'DESSERT', label: '甜品', emoji: '🍰' },
  { value: 'TRIP', label: '旅行', emoji: '🧳' },
  { value: 'OTHER', label: '其他', emoji: '✨' }
]

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
  category: 'FOOD',
  specialty: '',
  placeId: '',
  province: '',
  district: '',
  latitude: null,
  longitude: null
})
const form = reactive(emptyForm())

const navItems = [
  { id: 'home', label: '漫游首页' },
  { id: 'restaurants', label: '吃喝地图' },
  { id: 'milkTea', label: '奶茶地图' },
  { id: 'trips', label: '旅行足迹' },
  { id: 'recommendations', label: '下一站' }
]

const filteredMemories = computed(() => {
  if (activeView.value === 'restaurants') return memories.value.filter(item => item.type === 'RESTAURANT')
  if (activeView.value === 'trips') return memories.value.filter(item => item.type === 'TRIP')
  return memories.value
})

const milkTeaMemories = computed(() => memories.value.filter(item => item.category === 'MILK_TEA'))

const milkTeaCities = computed(() => {
  const groups = new Map()
  milkTeaMemories.value.forEach(item => {
    const key = item.city || '未标记城市'
    if (!groups.has(key)) {
      groups.set(key, {
        city: key,
        province: item.province || '',
        count: 0,
        memories: [],
        latitude: item.latitude,
        longitude: item.longitude
      })
    }
    const group = groups.get(key)
    group.count += 1
    group.memories.push(item)
  })
  return Array.from(groups.values())
})

const pageTitle = computed(() => ({
  restaurants: '一起吃过喝过的店',
  milkTea: '她的奶茶地图',
  trips: '走过的地方',
  recommendations: '下一站，去哪里？'
}[activeView.value] || '把平常的日子，过成值得收藏的故事'))

const formatDate = date => new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: 'short', day: 'numeric'
}).format(new Date(`${date}T00:00:00`))

const categoryLabel = value => categoryOptions.find(item => item.value === value)?.label || '未分类'
const categoryEmoji = value => categoryOptions.find(item => item.value === value)?.emoji || '✨'
const hasLocation = item => Number(item?.latitude) && Number(item?.longitude)
const amapUrl = item => hasLocation(item)
  ? `https://uri.amap.com/marker?position=${item.longitude},${item.latitude}&name=${encodeURIComponent(item.title || item.address || item.city)}`
  : ''

const fetchJson = async (url, options = {}) => {
  const response = await fetch(url, {
    credentials: 'include',
    ...options,
    headers: {
      ...(options.headers || {})
    }
  })
  if (response.status === 401) {
    currentUser.value = null
  }
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

const loadAuth = async () => {
  try {
    const me = await fetchJson('/api/auth/me')
    currentUser.value = me?.username || null
    if (currentUser.value) {
      await loadData()
    } else {
      loading.value = false
    }
  } catch (error) {
    loading.value = false
  } finally {
    authChecked.value = true
  }
}

const login = async () => {
  loginLoading.value = true
  loginError.value = ''
  try {
    const user = await fetchJson('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loginForm)
    })
    currentUser.value = user.username
    toast.value = '欢迎回来，纪念册已解锁'
    setTimeout(() => { toast.value = '' }, 2200)
    await loadData()
  } catch (error) {
    loginError.value = error.message
  } finally {
    loginLoading.value = false
  }
}

const logout = async () => {
  await fetchJson('/api/auth/logout', { method: 'POST' }).catch(() => null)
  currentUser.value = null
  memories.value = []
  activeView.value = 'home'
}

const resetPlaceSearch = () => {
  placeResults.value = []
  placeMessage.value = ''
  placeKeyword.value = ''
}

const openCreate = (type = 'RESTAURANT') => {
  editingId.value = null
  Object.assign(form, emptyForm())
  setType(type)
  if (activeView.value === 'milkTea') {
    form.category = 'MILK_TEA'
    form.emoji = '🧋'
  }
  resetPlaceSearch()
  modalOpen.value = true
}

const openEdit = memory => {
  editingId.value = memory.id
  Object.assign(form, emptyForm(), memory)
  resetPlaceSearch()
  modalOpen.value = true
}

const setType = type => {
  form.type = type
  if (type === 'TRIP') {
    form.category = 'TRIP'
    form.emoji = '🧳'
  } else if (form.category === 'TRIP') {
    form.category = 'FOOD'
    form.emoji = '🍜'
  } else {
    form.emoji = categoryEmoji(form.category)
  }
}

const setCategory = category => {
  form.category = category
  form.emoji = categoryEmoji(category)
  if (category === 'TRIP') {
    form.type = 'TRIP'
  } else {
    form.type = 'RESTAURANT'
  }
}

const searchPlaces = async () => {
  if (!placeKeyword.value.trim()) {
    placeMessage.value = '先输入店名、景点或地址'
    return
  }
  placeSearching.value = true
  placeMessage.value = ''
  try {
    const params = new URLSearchParams({
      keywords: placeKeyword.value.trim(),
      city: form.city || ''
    })
    const results = await fetchJson(`/api/places/search?${params.toString()}`)
    placeResults.value = results
    if (results.length === 0) {
      placeMessage.value = '没有搜到结果；如果还没配置 AMAP_KEY，可以先手动填写地址和坐标。'
    }
  } catch (error) {
    placeMessage.value = error.message
  } finally {
    placeSearching.value = false
  }
}

const selectPlace = place => {
  form.placeId = place.id
  form.title = form.title || place.name
  form.province = place.province
  form.city = place.city || form.city
  form.district = place.district
  form.address = [place.district, place.address].filter(Boolean).join(' ')
  form.latitude = place.latitude || null
  form.longitude = place.longitude || null
  placeMessage.value = `已选中：${place.name}`
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

onMounted(loadAuth)
</script>

<template>
  <div v-if="!authChecked" class="boot-screen">正在打开你们的纪念册...</div>

  <div v-else-if="!currentUser" class="login-screen">
    <form class="login-card" @submit.prevent="login">
      <span class="brand-mark">∴</span>
      <p class="eyebrow">PRIVATE JOURNEY</p>
      <h1>两个人的漫游簿</h1>
      <p>输入账号密码后，才能查看你们吃喝玩乐和旅行足迹。</p>
      <label>账号<input v-model.trim="loginForm.username" autocomplete="username" required></label>
      <label>密码<input v-model="loginForm.password" type="password" autocomplete="current-password" required placeholder="默认 change-me-now"></label>
      <div v-if="loginError" class="inline-error">{{ loginError }}</div>
      <button class="primary submit-button" :disabled="loginLoading">{{ loginLoading ? '正在解锁...' : '进入纪念册' }}</button>
    </form>
  </div>

  <div v-else class="app-shell">
    <header class="topbar">
      <button class="brand" aria-label="返回漫游首页" @click="activeView = 'home'">
        <span class="brand-mark">∴</span>
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
      <div class="top-actions">
        <button class="add-button" @click="openCreate(activeView === 'trips' ? 'TRIP' : 'RESTAURANT')">
          <span>＋</span> 记一笔
        </button>
        <button class="logout-button" @click="logout">退出</button>
      </div>
    </header>

    <main>
      <section v-if="activeView === 'home'" class="hero">
        <div class="hero-copy">
          <p class="eyebrow">OUR LITTLE JOURNEY · 从第一次出发开始</p>
          <h1>把平常的日子，<br><em>过成值得收藏的故事。</em></h1>
          <p class="hero-description">
            记下每一家舍不得忘记的小店、每一条牵手走过的街，也把她喜欢的奶茶单独点亮成地图。
          </p>
          <div class="hero-actions">
            <button class="primary" @click="openCreate('RESTAURANT')">记录今天 <span>→</span></button>
            <button class="text-button" @click="activeView = 'milkTea'">翻她的奶茶地图 <span>→</span></button>
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
          <span class="stat-icon yellow">⌁</span>
          <div><strong>{{ summary.restaurantCount }}</strong><small>家好吃好喝</small></div>
        </article>
        <article>
          <span class="stat-icon green">⌖</span>
          <div><strong>{{ summary.cityCount }}</strong><small>座点亮城市</small></div>
        </article>
        <article class="next-stat" @click="activeView = 'recommendations'">
          <div><small>下一次出发</small><strong>等我们决定</strong></div><span>→</span>
        </article>
      </section>

      <section v-if="activeView === 'milkTea'" class="milk-tea-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">MILK TEA ATLAS</p>
            <h2>{{ pageTitle }}</h2>
          </div>
          <button class="primary compact" @click="openCreate('RESTAURANT')">新增奶茶</button>
        </div>
        <div class="tea-map">
          <div class="tea-map-panel">
            <span>🧋</span>
            <strong>{{ milkTeaCities.length }}</strong>
            <small>座城市已经喝过奶茶</small>
          </div>
          <article v-for="city in milkTeaCities" :key="city.city" class="tea-city">
            <div>
              <p>{{ city.province || '已点亮' }}</p>
              <h3>{{ city.city }}</h3>
              <small>{{ city.count }} 杯 / 店</small>
            </div>
            <a v-if="hasLocation(city)" :href="amapUrl(city)" target="_blank" rel="noreferrer">看地图</a>
          </article>
        </div>
        <div v-if="milkTeaMemories.length === 0" class="state-card">还没有奶茶记录，下一杯就从今天开始。</div>
        <div v-else class="memory-grid tea-grid">
          <article v-for="memory in milkTeaMemories" :key="memory.id" class="memory-card">
            <div class="memory-visual milk">
              <span class="memory-emoji">🧋</span>
              <span class="type-chip">{{ memory.city }} · {{ memory.specialty || '奶茶' }}</span>
              <div class="visual-landscape"><i></i><b></b></div>
            </div>
            <div class="memory-body">
              <div class="memory-meta">
                <span>{{ memory.province }} {{ memory.district }}</span>
                <span>{{ '★'.repeat(memory.rating || 0) }}</span>
              </div>
              <h3>{{ memory.title }}</h3>
              <p>{{ memory.note }}</p>
              <div class="tag-row">
                <span v-for="tag in (memory.tags || '').split(',').filter(Boolean)" :key="tag"># {{ tag.trim() }}</span>
              </div>
              <footer>
                <time>{{ formatDate(memory.visitedAt) }}</time>
                <div>
                  <a v-if="hasLocation(memory)" :href="amapUrl(memory)" target="_blank" rel="noreferrer">地图</a>
                  <button @click="openEdit(memory)">编辑</button>
                  <button @click="deleteMemory(memory)">删除</button>
                </div>
              </footer>
            </div>
          </article>
        </div>
      </section>

      <section v-else-if="activeView !== 'recommendations'" class="content-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">{{ activeView === 'home' ? 'RECENT STORIES' : 'OUR COLLECTION' }}</p>
            <h2>{{ activeView === 'home' ? '最近收藏的故事' : pageTitle }}</h2>
          </div>
          <button v-if="activeView === 'home'" class="text-button" @click="activeView = 'trips'">查看全部 <span>→</span></button>
        </div>

        <div v-if="loading" class="state-card">正在翻开漫游簿...</div>
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
              <span class="type-chip">{{ memory.type === 'RESTAURANT' ? categoryLabel(memory.category) : '一起走过' }}</span>
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
                <span v-for="tag in (memory.tags || '').split(',').filter(Boolean)" :key="tag"># {{ tag.trim() }}</span>
              </div>
              <footer>
                <time>{{ formatDate(memory.visitedAt) }}</time>
                <div>
                  <a v-if="hasLocation(memory)" :href="amapUrl(memory)" target="_blank" rel="noreferrer">地图</a>
                  <button @click="openEdit(memory)">编辑</button>
                  <button @click="deleteMemory(memory)">删除</button>
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
          <p>根据你们喜欢的慢旅行、美食和奶茶城市，漫游簿挑出了三个还没点亮的目的地。</p>
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
      <span>∴</span>
      <p>愿以后翻开这里，每一页都有当时的风。</p>
      <small>Made for two · {{ new Date().getFullYear() }}</small>
    </footer>

    <div v-if="modalOpen" class="modal-backdrop" @click.self="modalOpen = false">
      <form class="modal" @submit.prevent="saveMemory">
        <button type="button" class="modal-close" aria-label="关闭" @click="modalOpen = false">×</button>
        <p class="eyebrow">{{ editingId ? 'EDIT A MEMORY' : 'A NEW MEMORY' }}</p>
        <h2>{{ editingId ? '重新写好这段故事' : '今天，发生了什么好事？' }}</h2>
        <div class="type-switch">
          <button type="button" :class="{ active: form.type === 'RESTAURANT' }" @click="setType('RESTAURANT')">🍜 吃喝记录</button>
          <button type="button" :class="{ active: form.type === 'TRIP' }" @click="setType('TRIP')">🧳 旅行足迹</button>
        </div>
        <div class="category-switch" v-if="form.type === 'RESTAURANT'">
          <button
            v-for="category in categoryOptions.filter(item => item.value !== 'TRIP')"
            :key="category.value"
            type="button"
            :class="{ active: form.category === category.value }"
            @click="setCategory(category.value)"
          >
            <span>{{ category.emoji }}</span>{{ category.label }}
          </button>
        </div>
        <label>这段回忆的名字<input v-model.trim="form.title" required maxlength="100" placeholder="例如：海边看过的那场日落"></label>
        <div class="place-search">
          <div class="place-search-row">
            <label>搜索店铺 / 地址<input v-model.trim="placeKeyword" placeholder="例如：茶颜悦色 五一广场"></label>
            <button type="button" class="secondary" :disabled="placeSearching" @click="searchPlaces">{{ placeSearching ? '搜索中' : '搜索' }}</button>
          </div>
          <div v-if="placeMessage" class="place-message">{{ placeMessage }}</div>
          <div v-if="placeResults.length" class="place-results">
            <button v-for="place in placeResults" :key="place.id" type="button" @click="selectPlace(place)">
              <strong>{{ place.name }}</strong>
              <small>{{ place.city }} {{ place.district }} {{ place.address }}</small>
            </button>
          </div>
        </div>
        <div class="form-row">
          <label>城市<input v-model.trim="form.city" required placeholder="厦门"></label>
          <label>地点<input v-model.trim="form.address" placeholder="鼓浪屿"></label>
        </div>
        <div class="form-row">
          <label>省份<input v-model.trim="form.province" placeholder="福建"></label>
          <label>区县<input v-model.trim="form.district" placeholder="思明区"></label>
        </div>
        <div class="form-row">
          <label>纬度<input v-model.number="form.latitude" type="number" step="0.000001" placeholder="24.448"></label>
          <label>经度<input v-model.number="form.longitude" type="number" step="0.000001" placeholder="118.063"></label>
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
        <label v-if="form.category === 'MILK_TEA'">这座城市的特色奶茶<input v-model.trim="form.specialty" placeholder="例如：幽兰拿铁、桂花乌龙奶茶"></label>
        <label>想留下的话<textarea v-model.trim="form.note" maxlength="1000" rows="4" placeholder="写下味道、天气、说过的话，或一个只有你们懂的细节"></textarea></label>
        <label>标签<input v-model.trim="form.tags" placeholder="海边, 日落, 周末（用逗号分隔）"></label>
        <button class="primary submit-button" :disabled="saving">{{ saving ? '正在收藏...' : '收进漫游簿' }}</button>
      </form>
    </div>

    <div v-if="toast" class="toast">{{ toast }}</div>
  </div>
</template>
