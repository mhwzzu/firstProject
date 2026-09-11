<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const me = ref(null)
const space = ref(null)
const preference = ref(null)
const today = ref(null)
const weekend = ref(null)
const weather = ref(null)
const plans = ref([])
const wishes = ref([])
const memories = ref([])
const summary = ref(null)
const integrations = ref(null)
const selected = ref(null)
const selectedMemory = ref(null)
const activeTab = ref('next')
const authMode = ref('login')
const setupMode = ref('create')
const loading = ref(true)
const busy = ref(false)
const refreshing = ref(false)
const discoveryRefreshing = ref(false)
const locating = ref(false)
const weatherOpen = ref(false)
const plansOpen = ref(false)
const filtersOpen = ref(false)
const memoryOpen = ref(false)
const error = ref('')
const toast = ref('')
const inviteToken = ref('')
const variation = ref(0)
const wishFilter = ref('all')
const memoryFilter = ref('all')

const origin = reactive({ latitude: null, longitude: null, label: '按城市中心估算' })
const authForm = reactive({ email: '', displayName: '', password: '' })
const spaceForm = reactive({ name: '我们的漫游簿', city: '杭州', token: '' })
const preferenceForm = reactive({ city: '杭州', budget: 500, travelRadiusKm: 35, foodTags: '杭帮菜,咖啡,热汤', activityTags: '散步,展览,自然', travelTags: '慢旅行,小众,季节限定' })
const decisionForm = reactive({ prompt: '下周六想看秋景，不走太多路，晚上吃点热乎的', city: '杭州', tags: '秋景，散步，暖食', budget: 500, travelMinutes: 35 })
const memoryForm = reactive({ title: '', city: '杭州', address: '', visitedAt: new Date().toISOString().slice(0, 10), rating: 5, note: '', tags: '共同出发', emoji: '✦', category: 'TRIP', type: 'TRIP' })

const navItems = [
  { id: 'next', icon: '✦', label: '下一次', crumb: '下一次出发' },
  { id: 'wish', icon: '♡', label: '心愿', crumb: '共同心愿' },
  { id: 'footprint', icon: '⌖', label: '足迹', crumb: '足迹与回忆' },
  { id: 'us', icon: '◌', label: '我们', crumb: '我们与设置' }
]

const isSetup = computed(() => me.value && !space.value)
const needsPreferences = computed(() => space.value && preference.value && !preference.value.onboardingComplete)
const city = computed(() => decisionForm.city || preference.value?.city || space.value?.defaultCity || '杭州')
const candidates = computed(() => today.value?.candidates || [])
const currentCrumb = computed(() => navItems.find(item => item.id === activeTab.value)?.crumb || '下一次出发')
const planned = computed(() => plans.value.filter(item => item.status !== 'COMPLETED'))
const completed = computed(() => plans.value.filter(item => item.status === 'COMPLETED'))
const filteredWishes = computed(() => wishes.value.filter(item => wishFilter.value === 'all' || (wishFilter.value === 'shared' ? item.shared : item.mine)))
const filteredMemories = computed(() => memories.value.filter(item => memoryFilter.value === 'all' || item.type === memoryFilter.value))
const weatherHeadline = computed(() => weather.value?.available ? `${weather.value.weather} ${weather.value.temperature}°` : (today.value?.weather || '天气'))
const sourceTone = computed(() => (today.value?.discoveryStatus || 'DEGRADED').toLowerCase())
const nextSaturday = () => { const date = new Date(); const days = (6 - date.getDay() + 7) % 7 || 7; date.setDate(date.getDate() + days); return date.toISOString().slice(0, 10) }

const api = async (url, options = {}) => {
  const response = await fetch(url, { credentials: 'include', headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, ...options })
  const body = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(body.message || '操作没有成功，请稍后重试')
  return body
}
const showToast = message => { toast.value = message; window.setTimeout(() => { toast.value = '' }, 2600) }
const formatDate = value => value ? new Intl.DateTimeFormat('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value)) : '近期'
const shortDate = value => value ? new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric' }).format(new Date(value)) : '待安排'
const initials = name => (name || '?').slice(0, 1).toUpperCase()
const weatherIcon = value => /雨/.test(value || '') ? '☂' : /雪/.test(value || '') ? '❄' : /云|阴/.test(value || '') ? '☁' : '☀'

const requestParams = (forceDiscovery = false) => {
  const params = new URLSearchParams({ city: city.value, prompt: decisionForm.prompt, tags: decisionForm.tags, budget: String(decisionForm.budget || ''), travelMinutes: String(decisionForm.travelMinutes || ''), variation: String(variation.value) })
  if (origin.latitude != null && origin.longitude != null) { params.set('latitude', origin.latitude); params.set('longitude', origin.longitude) }
  if (forceDiscovery) params.set('refresh', 'true')
  return `?${params.toString()}`
}

const loadSecondary = async () => {
  const results = await Promise.allSettled([
    api('/api/v1/plans'), api('/api/v1/wishes'), api('/api/memories'), api('/api/summary'),
    api('/api/v1/integrations/status'), api(`/api/v1/weather?city=${encodeURIComponent(city.value)}`)
  ])
  if (results[0].status === 'fulfilled') plans.value = results[0].value
  if (results[1].status === 'fulfilled') wishes.value = results[1].value
  if (results[2].status === 'fulfilled') memories.value = results[2].value
  if (results[3].status === 'fulfilled') summary.value = results[3].value
  if (results[4].status === 'fulfilled') integrations.value = results[4].value
  if (results[5].status === 'fulfilled') weather.value = results[5].value
}
const refreshRecommendations = async (notify = true, forceDiscovery = false) => {
  refreshing.value = true; error.value = ''
  try {
    const query = requestParams(forceDiscovery)
    const todayData = await api(`/api/v1/recommendations/today${query}`)
    today.value = todayData
    if (!selected.value || !todayData.candidates.some(item => item.id === selected.value.id)) selected.value = todayData.candidates[0] || null
    api(`/api/v1/recommendations/weekend${query}`).then(data => { weekend.value = data }).catch(() => null)
    if (notify) showToast(forceDiscovery ? '近期公开内容与推荐已经刷新' : '已按当前条件生成新的建议')
  } catch (e) { error.value = e.message } finally { refreshing.value = false }
}
const loadWorkspace = async () => {
  try { space.value = await api('/api/v1/spaces/current') } catch (e) { if (e.message.includes('创建或加入')) { space.value = null; preference.value = null; return }; throw e }
  preference.value = await api('/api/v1/preferences')
  Object.assign(preferenceForm, preference.value)
  Object.assign(decisionForm, { city: preference.value.city || space.value.defaultCity || '杭州', budget: Math.max(preference.value.budget || 150, 300) })
  if (preference.value.onboardingComplete) { await refreshRecommendations(false); await loadSecondary() }
}
const bootstrap = async () => {
  loading.value = true; error.value = ''
  try { const user = await api('/api/v1/auth/me'); me.value = user.authenticated ? user : null; if (me.value) await loadWorkspace() }
  catch (e) { error.value = e.message } finally { loading.value = false }
}

const submitAuth = async () => {
  busy.value = true; error.value = ''
  try {
    const url = authMode.value === 'register' ? '/api/v1/auth/register' : '/api/v1/auth/login'
    const payload = authMode.value === 'register' ? authForm : { email: authForm.email, password: authForm.password }
    me.value = await api(url, { method: 'POST', body: JSON.stringify(payload) }); await loadWorkspace(); showToast('欢迎回来')
  } catch (e) { error.value = e.message } finally { busy.value = false }
}
const enterDemo = async () => {
  busy.value = true; error.value = ''
  try {
    const stamp = Date.now(); me.value = await api('/api/v1/auth/register', { method: 'POST', body: JSON.stringify({ email: `atlas-${stamp}@local.test`, displayName: '我', password: `local-demo-${stamp}` }) })
    space.value = await api('/api/v1/spaces', { method: 'POST', body: JSON.stringify({ name: '两个人的漫游簿', city: '杭州' }) })
    preference.value = await api('/api/v1/preferences', { method: 'PUT', body: JSON.stringify(preferenceForm) })
    await refreshRecommendations(false); await loadSecondary(); showToast('体验空间已准备好')
  } catch (e) { error.value = e.message } finally { busy.value = false }
}
const createOrJoinSpace = async () => {
  busy.value = true; error.value = ''
  try {
    space.value = setupMode.value === 'create'
      ? await api('/api/v1/spaces', { method: 'POST', body: JSON.stringify({ name: spaceForm.name, city: spaceForm.city }) })
      : await api('/api/v1/spaces/join', { method: 'POST', body: JSON.stringify({ token: spaceForm.token }) })
    preference.value = await api('/api/v1/preferences'); Object.assign(preferenceForm, preference.value)
  } catch (e) { error.value = e.message } finally { busy.value = false }
}
const savePreferences = async () => {
  busy.value = true; error.value = ''
  try {
    preference.value = await api('/api/v1/preferences', { method: 'PUT', body: JSON.stringify(preferenceForm) })
    Object.assign(decisionForm, { city: preferenceForm.city, budget: preferenceForm.budget })
    await refreshRecommendations(false); await loadSecondary(); showToast('偏好已保存，推荐已经更新')
  } catch (e) { error.value = e.message } finally { busy.value = false }
}
const updateRecentGuides = async () => { discoveryRefreshing.value = true; try { await refreshRecommendations(true, true) } finally { discoveryRefreshing.value = false } }
const showAnotherBatch = async () => { variation.value += 1; await refreshRecommendations(false); showToast('已经换了一组不同的候选') }
const useCurrentLocation = () => {
  if (!navigator.geolocation) { showToast('当前浏览器不支持定位，将按城市中心估算'); return }
  locating.value = true
  navigator.geolocation.getCurrentPosition(async position => {
    origin.latitude = Number(position.coords.latitude.toFixed(6)); origin.longitude = Number(position.coords.longitude.toFixed(6)); origin.label = '正在使用当前位置'
    locating.value = false; await refreshRecommendations()
  }, () => { locating.value = false; origin.label = '定位未授权，按城市中心估算'; showToast(origin.label) }, { timeout: 10000, maximumAge: 300000 })
}
const localAdjust = async kind => {
  if (kind === 'near') decisionForm.travelMinutes = Math.max(15, decisionForm.travelMinutes - 10)
  if (kind === 'budget') decisionForm.budget = Math.max(100, decisionForm.budget - 100)
  if (kind === 'indoor') decisionForm.tags = `${decisionForm.tags}，室内，雨天友好`
  if (kind === 'food') decisionForm.tags = '暖食，本地菜，安静晚餐'
  await refreshRecommendations()
}
const chooseCandidate = candidate => { selected.value = candidate }
const pinStyle = index => {
  const positions = [{ left: '59%', top: '43%' }, { left: '34%', top: '63%' }, { left: '75%', top: '59%' }, { left: '48%', top: '72%' }, { left: '68%', top: '28%' }, { left: '22%', top: '46%' }]
  return positions[index % positions.length]
}
const feedback = async (candidate, action) => {
  try { await api(`/api/v1/candidates/${candidate.id}/feedback`, { method: 'POST', body: JSON.stringify({ action }) }); wishes.value = await api('/api/v1/wishes'); showToast(action === 'WANT' ? '已加入你的心愿' : '已记录本次反馈') }
  catch (e) { error.value = e.message }
}
const addPlan = async candidate => {
  try {
    await api('/api/v1/plans', { method: 'POST', body: JSON.stringify({ candidateId: candidate.id, startDate: nextSaturday(), endDate: nextSaturday(), budget: decisionForm.budget, note: decisionForm.prompt }) })
    plans.value = await api('/api/v1/plans'); plansOpen.value = true; showToast('已经加入下周六计划')
  } catch (e) { error.value = e.message }
}
const completePlan = async plan => {
  try { await api(`/api/v1/plans/${plan.id}/complete`, { method: 'POST' }); await loadSecondary(); plansOpen.value = false; activeTab.value = 'footprint'; showToast('计划已完成，回忆已经落入足迹') }
  catch (e) { error.value = e.message }
}
const openWeather = async () => { weatherOpen.value = true; if (!weather.value) weather.value = await api(`/api/v1/weather?city=${encodeURIComponent(city.value)}`).catch(() => null) }
const replanForWeather = async () => { weatherOpen.value = false; await localAdjust('indoor'); showToast('已加入天气条件并重新规划') }
const createInvite = async () => {
  try { const invite = await api('/api/v1/spaces/current/invites', { method: 'POST' }); inviteToken.value = invite.token; if (navigator.clipboard) await navigator.clipboard.writeText(invite.token).catch(() => null); showToast('邀请口令已复制') }
  catch (e) { error.value = e.message }
}
const openMemory = item => { selectedMemory.value = item }
const createMemory = async () => {
  busy.value = true
  try { await api('/api/memories', { method: 'POST', body: JSON.stringify(memoryForm) }); memoryOpen.value = false; await loadSecondary(); activeTab.value = 'footprint'; showToast('这段回忆已经保存到足迹') }
  catch (e) { error.value = e.message } finally { busy.value = false }
}
const logout = async () => { await api('/api/v1/auth/logout', { method: 'POST' }).catch(() => null); me.value = null; space.value = null; activeTab.value = 'next' }

onMounted(bootstrap)
</script>

<template>
  <main class="page-shell">
    <section v-if="loading" class="loading-stage"><span class="brand-mark">⌁</span><div class="loading-line"><i></i></div><p>正在整理你们下一次出发的灵感</p></section>

    <section v-else-if="!me" class="auth-page">
      <div class="auth-visual"><span class="brand-mark">⌁</span><p class="eyebrow">TOGETHER, IN MOTION</p><h1>把“去哪”<br>变成共同期待。</h1><p class="lead">读懂此刻的心情、真实天气和最近攻略，为两个人安排真正可以出发的一天。</p><div class="auth-preview"><div class="preview-route"></div><span class="preview-pin one">01</span><span class="preview-pin two">02</span><div><small>NEXT SATURDAY</small><b>九溪烟树与满觉陇晚餐</b><span>31 min · 两人约 ¥320</span></div></div></div>
      <form class="auth-card" @submit.prevent="submitAuth"><p class="eyebrow">PRIVATE ATLAS</p><h2>{{ authMode === 'login' ? '欢迎回来' : '建立你的账户' }}</h2><p class="muted">属于两个人的私密决策与回忆空间</p><label v-if="authMode === 'register'">怎么称呼你<input v-model.trim="authForm.displayName" maxlength="80" required placeholder="例如：小明"></label><label>邮箱<input v-model.trim="authForm.email" type="email" required autocomplete="email" placeholder="you@example.com"></label><label>密码<input v-model="authForm.password" type="password" minlength="8" required autocomplete="current-password" placeholder="至少 8 位"></label><p v-if="error" class="form-error">{{ error }}</p><button class="button primary wide" :disabled="busy">{{ busy ? '正在准备…' : authMode === 'login' ? '进入双人空间' : '创建账户' }}</button><button type="button" class="button ghost wide" :disabled="busy" @click="enterDemo">无需填写，一键体验完整产品</button><button type="button" class="text-button" @click="authMode = authMode === 'login' ? 'register' : 'login'; error = ''">{{ authMode === 'login' ? '还没有账户？创建一个' : '已有账户？直接登录' }}</button></form>
    </section>

    <section v-else-if="isSetup" class="setup-page"><div class="setup-card"><span class="brand-mark">⌁</span><p class="eyebrow">FIRST STEP</p><h1>先建立你们的私密空间</h1><p class="muted">每个人保留独立偏好，系统只在后台寻找共同区间。</p><div class="segmented"><button :class="{ active: setupMode === 'create' }" @click="setupMode = 'create'">我来创建</button><button :class="{ active: setupMode === 'join' }" @click="setupMode = 'join'">加入对方</button></div><form @submit.prevent="createOrJoinSpace"><template v-if="setupMode === 'create'"><label>空间名字<input v-model.trim="spaceForm.name" required></label><label>常驻城市<input v-model.trim="spaceForm.city" required></label></template><label v-else>邀请口令<input v-model.trim="spaceForm.token" required placeholder="粘贴对方发来的口令"></label><p v-if="error" class="form-error">{{ error }}</p><button class="button primary wide" :disabled="busy">{{ busy ? '处理中…' : '继续' }}</button></form></div></section>

    <section v-else-if="needsPreferences" class="setup-page"><div class="setup-card preference-card"><p class="eyebrow">MAKE IT YOURS</p><h1>让推荐先认识你</h1><p class="muted">以后每一次心动、跳过与到访都会让它更准确。</p><form @submit.prevent="savePreferences"><div class="form-grid"><label>常驻城市<input v-model.trim="preferenceForm.city" required></label><label>两人预算（元）<input v-model.number="preferenceForm.budget" type="number" min="0" max="10000"></label></div><label>喜欢吃什么<input v-model="preferenceForm.foodTags"></label><label>喜欢做什么<input v-model="preferenceForm.activityTags"></label><label>旅行偏好<input v-model="preferenceForm.travelTags"></label><label>可接受出行半径（km）<input v-model.number="preferenceForm.travelRadiusKm" type="number" min="1" max="200"></label><button class="button primary wide" :disabled="busy">生成第一张共同地图</button></form></div></section>

    <section v-else class="atlas-app">
      <aside class="side-rail"><button class="logo-button" @click="activeTab = 'next'">⌁</button><nav><button v-for="item in navItems" :key="item.id" :class="{ active: activeTab === item.id }" @click="activeTab = item.id"><i>{{ item.icon }}</i><span>{{ item.label }}</span></button></nav><button class="rail-avatar" @click="activeTab = 'us'">{{ initials(me.displayName) }}</button></aside>
      <header class="topbar"><div class="breadcrumb"><b>{{ space.name }}</b><span>/</span>{{ currentCrumb }}</div><div class="top-actions"><button class="source-pill" :class="sourceTone" @click="activeTab = 'us'"><i></i>{{ today?.discoveryStatus === 'LIVE' ? '实时灵感已连接' : today?.discoveryStatus === 'PARTIAL' ? '部分灵感已连接' : '查看数据状态' }}</button><button class="weather-summary" @click="openWeather"><strong>{{ weatherIcon(weatherHeadline) }} {{ weatherHeadline }}</strong><span>{{ city }} · 点击看详情</span></button><button class="plan-count" @click="plansOpen = true">计划 <b>{{ planned.length }}</b></button><div class="member-stack"><span v-for="member in space.members" :key="member.id">{{ initials(member.displayName) }}</span></div></div></header>
      <div v-if="error" class="error-banner"><span>{{ error }}</span><button @click="error = ''">×</button></div>

      <section v-if="activeTab === 'next'" class="view next-view">
        <div class="decision-map">
          <form class="prompt-card" @submit.prevent="refreshRecommendations()"><p class="eyebrow">✦ TELL US THE MOOD</p><div class="prompt-row"><input v-model.trim="decisionForm.prompt" maxlength="180" placeholder="说说你们这次想要怎样的一天"><button class="button primary" :disabled="refreshing">{{ refreshing ? '正在决定…' : '帮我们决定 →' }}</button></div><div class="condition-row"><button type="button" class="condition active" @click="useCurrentLocation">◎ {{ locating ? '正在定位…' : origin.label }}</button><button type="button" class="condition" @click="filtersOpen = !filtersOpen">{{ decisionForm.travelMinutes }} 分钟内</button><button type="button" class="condition" @click="filtersOpen = !filtersOpen">两人 ¥{{ decisionForm.budget }}</button><button type="button" class="condition" @click="localAdjust('near')">更近一些</button><button type="button" class="condition" @click="localAdjust('food')">只换晚餐</button><button type="button" class="condition" @click="filtersOpen = !filtersOpen">＋ 调整条件</button></div><div v-if="filtersOpen" class="filter-drawer"><label>城市<input v-model.trim="decisionForm.city"></label><label>预算<input v-model.number="decisionForm.budget" type="number"></label><label>车程<input v-model.number="decisionForm.travelMinutes" type="number"></label><label>关键词<input v-model="decisionForm.tags"></label></div></form>
          <div class="map-surface"><div class="map-grid"></div><div class="lake"></div><svg class="route-line" viewBox="0 0 900 680" preserveAspectRatio="none"><path d="M135,570 C220,510 260,465 340,475 S475,520 540,425 S620,270 765,225"/><path class="road" d="M15,430 C180,370 300,290 430,335 S610,425 890,365"/></svg><span class="district d1">西湖风景区</span><span class="district d2">上城区</span><span class="district d3">滨江区</span><button v-for="(candidate, index) in candidates" :key="candidate.id" class="map-pin" :class="{ selected: selected?.id === candidate.id }" :style="pinStyle(index)" @click="chooseCandidate(candidate)">{{ String(index + 1).padStart(2, '0') }}</button><div class="map-tools"><button @click="useCurrentLocation">◎</button><button @click="filtersOpen = !filtersOpen">⌗</button></div><div class="source-float"><i :class="sourceTone"></i><b>{{ today?.discoveryStatus === 'LIVE' ? '近期攻略已刷新' : '推荐仍可使用' }}</b><span>{{ today?.connectedSources?.join(' · ') || '等待公开内容来源' }}</span><small>{{ formatDate(today?.refreshedAt) }}</small></div></div>
        </div>

        <aside class="recommendation-panel" v-if="selected"><div class="panel-scroll"><div class="recommendation-head"><p class="eyebrow">RECOMMENDATION {{ String(candidates.indexOf(selected) + 1).padStart(2, '0') }} / {{ String(candidates.length).padStart(2, '0') }}</p><div class="score-ring">{{ selected.score }}</div></div><p class="category">{{ selected.category }} · {{ selected.bestSeason }}</p><h1>{{ selected.title }}</h1><p class="address">{{ selected.city }} · {{ selected.district || '位置已核验' }}{{ selected.address ? ' · ' + selected.address : '' }}</p><div class="metric-grid"><div><b>{{ selected.distanceKm != null ? selected.distanceKm + ' km' : '待定位' }}</b><span>距离出发点</span></div><div><b>{{ selected.travelMinutes ? selected.travelMinutes + ' min' : '待计算' }}</b><span>驾车时间</span></div><div><b>¥{{ decisionForm.budget }}</b><span>两人预算内</span></div></div><div class="why-card"><b>为什么是它</b><p>{{ selected.reason }}</p></div><div class="evidence-title"><b>近期公开内容依据</b><span>{{ selected.evidence?.length || 0 }} 条</span></div><div v-if="selected.evidence?.length" class="evidence-list"><a v-for="item in selected.evidence" :key="item.sourceUrl" :href="item.sourceUrl" target="_blank" rel="noreferrer"><span class="platform">{{ item.platform }}</span><div><b>{{ item.title }}</b><p>{{ item.summary }}</p><small>{{ item.relationLabel }} · {{ formatDate(item.publishedAt || item.discoveredAt) }}</small></div><em>↗</em></a></div><div v-else class="empty-evidence"><b>地图候选已核验</b><span>暂无近期平台内容；可以手动刷新内容源。</span></div></div><div class="panel-actions"><button @click="feedback(selected, 'WANT')">♡ 心动</button><button @click="feedback(selected, 'SKIP')">暂时跳过</button><a :href="selected.navigationUrl" target="_blank" rel="noreferrer">查看路线 ↗</a><button class="button primary" @click="addPlan(selected)">加入下周六计划 →</button></div></aside>

        <div class="floating-actions"><button @click="showAnotherBatch">换一批</button><button class="accent" :disabled="discoveryRefreshing" @click="updateRecentGuides">{{ discoveryRefreshing ? '正在寻找…' : '更新近期灵感' }}</button></div>
      </section>

      <section v-else-if="activeTab === 'wish'" class="view content-view"><header class="section-header"><div><p class="eyebrow">SHARED DESIRES</p><h1>值得一起去的地方</h1><p>系统理解各自倾向，但不把喜欢变成输赢。</p></div><div class="segmented"><button :class="{ active: wishFilter === 'all' }" @click="wishFilter = 'all'">全部 {{ wishes.length }}</button><button :class="{ active: wishFilter === 'shared' }" @click="wishFilter = 'shared'">共同心动</button><button :class="{ active: wishFilter === 'mine' }" @click="wishFilter = 'mine'">我的心愿</button></div></header><div v-if="filteredWishes.length" class="wish-layout"><div class="wish-grid"><article v-for="(item, index) in filteredWishes" :key="item.candidateId" class="wish-card" @click="activeTab = 'next'"><div class="wish-art" :class="'tone-' + (index % 4)"><span>{{ item.shared ? '两个人都心动' : '我的心愿' }}</span><b>{{ String(index + 1).padStart(2, '0') }}</b></div><div class="wish-copy"><p>{{ item.category || '新的灵感' }}</p><h3>{{ item.title }}</h3><span>{{ item.city }} · {{ item.district || '位置已核验' }}</span><footer><small>{{ formatDate(item.savedAt) }}</small><b>{{ item.shared ? '共同心动 ♡' : '已收藏 ♡' }}</b></footer></div></article></div><aside class="wish-map"><p class="eyebrow">WISH MAP</p><h3>{{ wishes.length }} 个想一起去的地方</h3><div class="mini-map"><i v-for="(item, index) in filteredWishes" :key="item.candidateId" :style="pinStyle(index)">{{ index + 1 }}</i></div><div class="smart-note"><b>季节提醒</b><p>系统会结合天气与近期内容，在最合适的时候把心愿重新带回首页。</p></div></aside></div><div v-else class="grand-empty"><span>♡</span><h2>还没有留下心动</h2><p>在“下一次”里标记一个地点，它会立即出现在这里。</p><button class="button primary" @click="activeTab = 'next'">去发现第一个心愿</button></div></section>

      <section v-else-if="activeTab === 'footprint'" class="view content-view"><header class="section-header"><div><p class="eyebrow">OUR ATLAS</p><h1>走过的地方，正在变成故事</h1><p>每一次完成的计划都会自动落到地图与时间线上。</p></div><div class="header-buttons"><div class="segmented"><button :class="{ active: memoryFilter === 'all' }" @click="memoryFilter = 'all'">全部</button><button :class="{ active: memoryFilter === 'TRIP' }" @click="memoryFilter = 'TRIP'">旅行</button><button :class="{ active: memoryFilter === 'RESTAURANT' }" @click="memoryFilter = 'RESTAURANT'">美食</button></div><button class="button primary" @click="memoryOpen = true">＋ 记一段回忆</button></div></header><div class="footprint-stats"><div><b>{{ summary?.totalMemories || memories.length }}</b><span>共同回忆</span></div><div><b>{{ summary?.cityCount || 0 }}</b><span>一起走过的城市</span></div><div><b>{{ summary?.restaurantCount || 0 }}</b><span>认真吃过的店</span></div><div><b>{{ completed.length }}</b><span>完成的共同计划</span></div></div><div class="footprint-layout"><div class="footprint-map"><p class="eyebrow">MEMORY MAP · {{ city.toUpperCase() }}</p><div class="map-grid"></div><i v-for="(item, index) in filteredMemories" :key="item.id" :style="pinStyle(index)" @click="openMemory(item)">{{ item.emoji || '✦' }}</i><div v-if="!memories.length" class="map-empty">完成第一个计划后，足迹会出现在这里</div></div><aside class="timeline"><div class="timeline-head"><b>回忆时间线</b><span>{{ new Date().getFullYear() }}</span></div><button v-for="item in filteredMemories" :key="item.id" @click="openMemory(item)"><time>{{ shortDate(item.visitedAt) }}</time><div><b>{{ item.title }}</b><span>{{ item.city }} · {{ item.rating || 5 }} 星 · {{ item.note || '这一天值得被记住' }}</span></div><em>↗</em></button><div v-if="!filteredMemories.length" class="timeline-empty">这里会按一次约会或旅行自动归档，而不是把“完成”藏进数据库。</div></aside></div></section>

      <section v-else class="view content-view"><header class="section-header"><div><p class="eyebrow">TWO PEOPLE, ONE RHYTHM</p><h1>我们喜欢怎样度过时间</h1><p>偏好来自明确设置和真实行为，并且始终可以修改。</p></div><button class="button primary" @click="savePreferences" :disabled="busy">{{ busy ? '保存中…' : '保存偏好' }}</button></header><div class="profile-grid"><article class="profile-card"><div class="profile-head"><span>{{ initials(me.displayName) }}</span><div><h3>{{ me.displayName }}的节奏</h3><p>每次心动、跳过和到访都会继续学习</p></div></div><div class="preference-editor"><label>常驻城市<input v-model="preferenceForm.city"></label><label>常用预算<input v-model.number="preferenceForm.budget" type="number"></label><label>喜欢吃什么<input v-model="preferenceForm.foodTags"></label><label>喜欢做什么<input v-model="preferenceForm.activityTags"></label><label>旅行偏好<input v-model="preferenceForm.travelTags"></label><label>出行半径<input v-model.number="preferenceForm.travelRadiusKm" type="number"></label></div></article><article class="profile-card partner"><div class="profile-head"><span>{{ initials(space.members.find(item => item.id !== me.id)?.displayName || 'TA') }}</span><div><h3>{{ space.members.find(item => item.id !== me.id)?.displayName || '等待另一位成员' }}的节奏</h3><p>由对方自己控制，不展示胜负式比较</p></div></div><div class="insight-list"><div><span>共同倾向</span><b>慢节奏 · 好吃的 · 少排队</b></div><div><span>最近变化</span><b>更偏爱有季节感的路线</b></div><div><span>平衡方式</span><b>共同区间 + 偶尔探索</b></div></div><button class="button ghost wide" @click="createInvite">{{ inviteToken ? '口令：' + inviteToken : '邀请另一位成员' }}</button></article></div><div class="integration-grid"><article><div><i :class="{ ok: integrations?.discovery?.configured }"></i><b>{{ integrations?.discovery?.name || '内容发现' }}</b></div><p>{{ integrations?.discovery?.message || '正在检查连接' }}</p><span>密钥仅保存在本机后端</span></article><article><div><i :class="{ ok: integrations?.amap?.configured }"></i><b>高德地图与天气</b></div><p>{{ integrations?.amap?.message || '正在检查连接' }}</p><span>地点、路线与天气事实底座</span></article><article><div><i class="ok"></i><b>本地数据与隐私</b></div><p>数据已持久化到本机，密钥不会进入前端或 Git。</p><span>双人空间数据隔离</span></article></div><div class="account-bar"><div><b>{{ me.email }}</b><span>当前账号 · {{ space.name }}</span></div><button class="button ghost" @click="logout">安全退出</button></div></section>

      <div v-if="weatherOpen" class="overlay" @click.self="weatherOpen = false"><section class="weather-drawer"><header><div><p class="eyebrow">{{ city.toUpperCase() }} · WEATHER INTELLIGENCE</p><h2>{{ weatherIcon(weather?.weather) }} {{ weather?.temperature || '--' }}°</h2><span>{{ weather?.weather || '天气暂不可用' }} · 湿度 {{ weather?.humidity || '--' }}% · {{ weather?.windDirection || '--' }}风 {{ weather?.windPower || '--' }}级</span></div><button @click="weatherOpen = false">×</button></header><div class="weather-advice"><b>{{ weather?.available ? '适合当前计划' : '当前使用降级信息' }}</b><p>{{ weather?.advice }}</p></div><div class="forecast"><article v-for="(day, index) in weather?.days" :key="day.date" :class="{ featured: index === 1 }"><span>{{ index === 0 ? '今天' : '周' + day.week }}</span><i>{{ weatherIcon(day.dayWeather) }}</i><b>{{ day.high }}° / {{ day.low }}°</b><small>{{ day.dayWeather }} · {{ day.suitability }}</small></article><div v-if="!weather?.days?.length" class="weather-empty">尚未获取到未来天气，请确认高德 Key 是“Web 服务”类型。</div></div><div class="weather-actions"><button class="button ghost" @click="openWeather">重新获取</button><button class="button primary" @click="replanForWeather">按天气重排当前推荐</button></div><footer>数据来源：{{ weather?.source || '高德天气' }} · {{ weather?.reportTime || formatDate(weather?.fetchedAt) }}</footer></section></div>

      <div v-if="plansOpen" class="overlay" @click.self="plansOpen = false"><section class="drawer plan-drawer"><header><div><p class="eyebrow">UPCOMING JOURNEYS</p><h2>接下来的共同计划</h2></div><button @click="plansOpen = false">×</button></header><div v-if="planned.length" class="plan-list"><article v-for="plan in planned" :key="plan.id"><time>{{ shortDate(plan.startDate) }}</time><div><span>{{ plan.city }}</span><h3>{{ plan.title }}</h3><p>{{ plan.note || '由推荐生成的共同计划' }}</p><small>预算 ¥{{ plan.budget || '待定' }} · 路线将在出发前重新核验</small></div><div class="plan-actions"><a :href="`https://ditu.amap.com/search?query=${encodeURIComponent(plan.title)}`" target="_blank">打开地图</a><button @click="completePlan(plan)">完成并写入足迹 →</button></div></article></div><div v-else class="grand-empty compact"><span>✦</span><h2>还没有待出发计划</h2><p>从推荐或心愿中挑一个地方开始。</p><button class="button primary" @click="plansOpen = false; activeTab = 'next'">去看看推荐</button></div></section></div>

      <div v-if="memoryOpen" class="overlay" @click.self="memoryOpen = false"><form class="drawer memory-form" @submit.prevent="createMemory"><header><div><p class="eyebrow">ADD A MEMORY</p><h2>记住这一天</h2></div><button type="button" @click="memoryOpen = false">×</button></header><div class="form-grid"><label>标题<input v-model="memoryForm.title" required placeholder="例如：九溪的秋日下午"></label><label>日期<input v-model="memoryForm.visitedAt" type="date" required></label><label>城市<input v-model="memoryForm.city" required></label><label>地点<input v-model="memoryForm.address" placeholder="可选"></label><label>类型<select v-model="memoryForm.type"><option value="TRIP">旅行 / 约会</option><option value="RESTAURANT">美食</option></select></label><label>评分<select v-model.number="memoryForm.rating"><option v-for="n in 5" :key="n" :value="n">{{ n }} 星</option></select></label></div><label>想记住的话<textarea v-model="memoryForm.note" rows="4" placeholder="一句话也很好"></textarea></label><button class="button primary wide" :disabled="busy">保存到足迹</button></form></div>

      <div v-if="selectedMemory" class="overlay" @click.self="selectedMemory = null"><section class="drawer memory-detail"><header><div><p class="eyebrow">{{ shortDate(selectedMemory.visitedAt) }} · {{ selectedMemory.city }}</p><h2>{{ selectedMemory.emoji }} {{ selectedMemory.title }}</h2></div><button @click="selectedMemory = null">×</button></header><div class="memory-hero"><span>{{ selectedMemory.emoji || '✦' }}</span></div><div class="memory-copy"><b>{{ selectedMemory.rating || 5 }} / 5</b><p>{{ selectedMemory.note || '这一天已经被记在你们的共同地图里。' }}</p><small>{{ selectedMemory.address || selectedMemory.city }} · {{ selectedMemory.tags }}</small></div></section></div>

      <div v-if="toast" class="toast">{{ toast }}</div>
    </section>
  </main>
</template>
