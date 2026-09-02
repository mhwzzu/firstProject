<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const me = ref(null)
const space = ref(null)
const preference = ref(null)
const today = ref(null)
const weekend = ref(null)
const plans = ref([])
const activeTab = ref('discover')
const authMode = ref('login')
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const toast = ref('')
const inviteToken = ref('')
const setupMode = ref('create')

const authForm = reactive({ email: '', displayName: '', password: '' })
const spaceForm = reactive({ name: '我们的小宇宙', city: '杭州', token: '' })
const preferenceForm = reactive({ city: '杭州', budget: 150, travelRadiusKm: 12, foodTags: '咖啡,川菜', activityTags: '展览,散步', travelTags: '海边,慢旅行' })

const isSetup = computed(() => me.value && !space.value)
const needsPreferences = computed(() => space.value && preference.value && !preference.value.onboardingComplete)
const city = computed(() => preference.value?.city || space.value?.defaultCity || '杭州')

const api = async (url, options = {}) => {
  const response = await fetch(url, { credentials: 'include', headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, ...options })
  const body = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(body.message || '操作没有成功，请稍后重试')
  return body
}

const showToast = message => {
  toast.value = message
  window.setTimeout(() => { toast.value = '' }, 2600)
}

const loadWorkspace = async () => {
  try {
    space.value = await api('/api/v1/spaces/current')
  } catch (e) {
    if (e.message.includes('创建或加入')) { space.value = null; preference.value = null; return }
    throw e
  }
  preference.value = await api('/api/v1/preferences')
  Object.assign(preferenceForm, preference.value)
  if (preference.value.onboardingComplete) await refreshRecommendations()
  plans.value = await api('/api/v1/plans')
}

const refreshRecommendations = async () => {
  const query = `?city=${encodeURIComponent(city.value)}`
  const [todayData, weekendData] = await Promise.all([
    api(`/api/v1/recommendations/today${query}`),
    api(`/api/v1/recommendations/weekend${query}`)
  ])
  today.value = todayData
  weekend.value = weekendData
}

const bootstrap = async () => {
  loading.value = true
  error.value = ''
  try {
    const user = await api('/api/v1/auth/me')
    me.value = user.authenticated ? user : null
    if (me.value) await loadWorkspace()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

const submitAuth = async () => {
  submitting.value = true; error.value = ''
  try {
    const url = authMode.value === 'register' ? '/api/v1/auth/register' : '/api/v1/auth/login'
    const payload = authMode.value === 'register' ? authForm : { email: authForm.email, password: authForm.password }
    me.value = await api(url, { method: 'POST', body: JSON.stringify(payload) })
    await loadWorkspace()
    showToast(authMode.value === 'register' ? '账户已创建，先建立你们的空间吧' : '欢迎回来')
  } catch (e) { error.value = e.message } finally { submitting.value = false }
}

const createOrJoinSpace = async () => {
  submitting.value = true; error.value = ''
  try {
    if (setupMode.value === 'create') {
      space.value = await api('/api/v1/spaces', { method: 'POST', body: JSON.stringify({ name: spaceForm.name, city: spaceForm.city }) })
      showToast('空间已建立，现在设置你的偏好')
    } else {
      space.value = await api('/api/v1/spaces/join', { method: 'POST', body: JSON.stringify({ token: spaceForm.token }) })
      showToast('已加入你们的空间')
    }
    preference.value = await api('/api/v1/preferences')
    Object.assign(preferenceForm, preference.value)
  } catch (e) { error.value = e.message } finally { submitting.value = false }
}

const savePreferences = async () => {
  submitting.value = true; error.value = ''
  try {
    const payload = {
      city: preferenceForm.city,
      budget: preferenceForm.budget,
      travelRadiusKm: preferenceForm.travelRadiusKm,
      foodTags: preferenceForm.foodTags,
      activityTags: preferenceForm.activityTags,
      travelTags: preferenceForm.travelTags
    }
    preference.value = await api('/api/v1/preferences', { method: 'PUT', body: JSON.stringify(payload) })
    await refreshRecommendations()
    showToast('偏好已保存，推荐已为你们更新')
  } catch (e) { error.value = e.message } finally { submitting.value = false }
}

const createInvite = async () => {
  try {
    const invite = await api('/api/v1/spaces/current/invites', { method: 'POST' })
    inviteToken.value = invite.token
    if (navigator.clipboard) await navigator.clipboard.writeText(invite.token).catch(() => null)
    showToast('邀请口令已复制，发给另一位成员即可')
  } catch (e) { error.value = e.message }
}

const feedback = async (candidate, action) => {
  try {
    await api(`/api/v1/candidates/${candidate.id}/feedback`, { method: 'POST', body: JSON.stringify({ action }) })
    showToast(action === 'WANT' ? '已记下：你想去这里' : action === 'SKIP' ? '已降低类似推荐的频率' : '已投票，等待对方的选择')
  } catch (e) { error.value = e.message }
}

const addPlan = async candidate => {
  try {
    await api('/api/v1/plans', { method: 'POST', body: JSON.stringify({ candidateId: candidate.id, budget: preference.value?.budget, note: '' }) })
    plans.value = await api('/api/v1/plans')
    activeTab.value = 'plans'
    showToast('已加入共同计划')
  } catch (e) { error.value = e.message }
}

const completePlan = async plan => {
  try {
    await api(`/api/v1/plans/${plan.id}/complete`, { method: 'POST' })
    plans.value = await api('/api/v1/plans')
    showToast('计划已沉淀成一段共同回忆')
  } catch (e) { error.value = e.message }
}

const logout = async () => {
  await api('/api/v1/auth/logout', { method: 'POST' }).catch(() => null)
  me.value = null; space.value = null; preference.value = null; today.value = null; weekend.value = null; plans.value = []; activeTab.value = 'discover'
}

onMounted(bootstrap)
</script>

<template>
  <main class="page-shell">
    <section v-if="loading" class="centered-card"><span class="pulse-dot"></span><p>正在整理你们下一次出发的灵感…</p></section>

    <section v-else-if="!me" class="auth-layout">
      <div class="brand-intro"><span class="brand-orbit">⌁</span><p class="eyebrow">TOGETHER, IN MOTION</p><h1>把“去哪”<br>变成共同期待。</h1><p>不必先收藏，也能为你们推荐此刻和下一个周末最值得出发的地方。</p><div class="feature-row"><span>☔ 看天气</span><span>♡ 懂偏好</span><span>✦ 一起决定</span></div></div>
      <form class="auth-card" @submit.prevent="submitAuth">
        <p class="eyebrow">PRIVATE SPACE</p><h2>{{ authMode === 'login' ? '欢迎回来' : '建立你的账户' }}</h2><p class="muted">{{ authMode === 'login' ? '登录后继续你们的下一次出发。' : '使用邮箱创建私密的双人决策空间。' }}</p>
        <label v-if="authMode === 'register'">怎么称呼你<input v-model.trim="authForm.displayName" maxlength="80" required placeholder="例如：小明"></label>
        <label>邮箱<input v-model.trim="authForm.email" type="email" required autocomplete="email" placeholder="you@example.com"></label>
        <label>密码<input v-model="authForm.password" type="password" minlength="8" required autocomplete="current-password" placeholder="至少 8 位"></label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="primary wide" :disabled="submitting">{{ submitting ? '处理中…' : authMode === 'login' ? '进入双人空间' : '创建账户' }}</button>
        <button type="button" class="text-button" @click="authMode = authMode === 'login' ? 'register' : 'login'; error = ''">{{ authMode === 'login' ? '还没有账户？创建一个' : '已有账户？直接登录' }}</button>
      </form>
    </section>

    <section v-else-if="isSetup" class="setup-card">
      <span class="brand-orbit">⌁</span><p class="eyebrow">FIRST STEP</p><h1>先建立你们的私密空间</h1><p class="muted">每位成员各自有账户，在同一个空间中保留偏好、投票和计划。</p>
      <div class="segmented"><button :class="{ active: setupMode === 'create' }" @click="setupMode = 'create'">我来创建</button><button :class="{ active: setupMode === 'join' }" @click="setupMode = 'join'">加入对方</button></div>
      <form @submit.prevent="createOrJoinSpace">
        <template v-if="setupMode === 'create'"><label>空间名字<input v-model.trim="spaceForm.name" required maxlength="100"></label><label>常驻城市<input v-model.trim="spaceForm.city" required maxlength="80" placeholder="杭州"></label></template>
        <label v-else>邀请口令<input v-model.trim="spaceForm.token" required placeholder="粘贴对方发来的口令"></label>
        <p v-if="error" class="error">{{ error }}</p><button class="primary wide" :disabled="submitting">{{ setupMode === 'create' ? '建立并继续' : '加入空间' }}</button>
      </form>
    </section>

    <section v-else-if="needsPreferences" class="setup-card preference-setup">
      <p class="eyebrow">MAKE IT YOURS</p><h1>先认识一下你</h1><p class="muted">这些偏好只服务于你们的空间，并可随时调整。</p>
      <form @submit.prevent="savePreferences"><div class="two-columns"><label>常驻城市<input v-model.trim="preferenceForm.city" required></label><label>单次预算（元）<input v-model.number="preferenceForm.budget" type="number" min="0" max="10000" required></label></div><label>喜欢吃什么？<input v-model="preferenceForm.foodTags" placeholder="例如：川菜, 咖啡, 面馆"></label><label>喜欢做什么？<input v-model="preferenceForm.activityTags" placeholder="例如：展览, 散步, 看电影"></label><label>旅行偏好<input v-model="preferenceForm.travelTags" placeholder="例如：海边, 慢旅行, 古城"></label><label>可接受的本地出行半径（km）<input v-model.number="preferenceForm.travelRadiusKm" type="number" min="1" max="200" required></label><p v-if="error" class="error">{{ error }}</p><button class="primary wide" :disabled="submitting">生成第一批推荐</button></form>
    </section>

    <template v-else>
      <header class="app-header"><button class="wordmark" @click="activeTab = 'discover'"><span>⌁</span><b>{{ space?.name }}</b></button><button class="avatar" @click="activeTab = 'settings'">{{ me.displayName.slice(0, 1) }}</button></header>
      <section v-if="error" class="banner error">{{ error }} <button @click="error = ''">×</button></section>
      <section v-if="activeTab === 'discover'" class="content"><div class="greeting"><p class="eyebrow">{{ city }} · 为两个人准备</p><h1>今天，想一起<br><em>去哪里？</em></h1><button class="refresh" @click="refreshRecommendations">↻ 更新建议</button></div>
        <section class="recommendation-section"><div class="section-heading"><div><p>此刻可去</p><h2>今晚，去哪里？</h2></div><span class="weather">{{ today?.weather || '正在读取天气' }}</span></div><p v-if="today" class="source-status">{{ today.sourceStatus }}</p><div class="recommendation-stack"><article v-for="card in today?.candidates || []" :key="card.id" class="recommendation-card today-card"><div class="card-score">{{ card.score }}<small>匹配</small></div><div class="card-content"><p class="category">{{ card.category }}</p><h3>{{ card.title }}</h3><p>{{ card.reason }}</p><small>依据：{{ card.scoreBreakdown }}</small><div class="card-actions"><button @click="feedback(card, 'WANT')">♡ 想去</button><button @click="feedback(card, 'SKIP')">不太想去</button><button class="solid-action" @click="addPlan(card)">加入计划 →</button></div></div></article></div></section>
        <section class="recommendation-section weekend-section"><div class="section-heading"><div><p>提前期待</p><h2>下个周末，去哪？</h2></div><span class="spark">✦</span></div><div class="recommendation-stack"><article v-for="card in weekend?.candidates || []" :key="card.id" class="recommendation-card weekend-card"><div class="card-score">{{ card.score }}<small>匹配</small></div><div class="card-content"><p class="category">{{ card.bestSeason }}</p><h3>{{ card.title }}</h3><p>{{ card.reason }}</p><small>{{ card.estimate }} · {{ card.sourceLabel }}</small><div class="card-actions"><button @click="feedback(card, 'VOTE')">✦ 投给它</button><button class="solid-action" @click="addPlan(card)">加入候选 →</button></div></div></article></div></section>
      </section>
      <section v-else-if="activeTab === 'plans'" class="content"><div class="greeting"><p class="eyebrow">OUR NEXT STORIES</p><h1>共同计划</h1><p class="muted">把一个候选放进计划，再把完成的行程沉淀成回忆。</p></div><div v-if="plans.length" class="plan-list"><article v-for="plan in plans" :key="plan.id" class="plan-card"><span class="plan-status" :class="plan.status.toLowerCase()">{{ plan.status === 'COMPLETED' ? '已完成' : '进行中' }}</span><h3>{{ plan.title }}</h3><p>{{ plan.city }} · {{ plan.budget ? `预算 ¥${plan.budget}` : '预算待定' }}</p><p v-if="plan.note" class="muted">{{ plan.note }}</p><button v-if="plan.status !== 'COMPLETED'" class="primary small" @click="completePlan(plan)">完成并写入回忆</button></article></div><div v-else class="empty-state"><span>⌁</span><h2>还没有计划</h2><p>从“发现”页把一个喜欢的建议加入这里。</p><button class="primary" @click="activeTab = 'discover'">去看推荐</button></div></section>
      <section v-else class="content settings"><div class="greeting"><p class="eyebrow">YOUR SIGNALS</p><h1>偏好与空间</h1></div><form class="settings-form" @submit.prevent="savePreferences"><div class="two-columns"><label>常驻城市<input v-model.trim="preferenceForm.city" required></label><label>单次预算<input v-model.number="preferenceForm.budget" type="number" min="0" max="10000" required></label></div><label>喜欢吃什么？<input v-model="preferenceForm.foodTags"></label><label>喜欢做什么？<input v-model="preferenceForm.activityTags"></label><label>旅行偏好<input v-model="preferenceForm.travelTags"></label><label>出行半径（km）<input v-model.number="preferenceForm.travelRadiusKm" type="number" min="1" max="200" required></label><button class="primary wide" :disabled="submitting">保存并更新推荐</button></form><section class="invite-box"><p class="eyebrow">INVITE PARTNER</p><h2>邀请另一位成员</h2><p class="muted">邀请码 72 小时有效，仅能使用一次。</p><button class="secondary wide" @click="createInvite">生成邀请口令</button><code v-if="inviteToken">{{ inviteToken }}</code></section><button class="logout" @click="logout">退出登录</button></section>
      <nav class="bottom-nav"><button :class="{ active: activeTab === 'discover' }" @click="activeTab = 'discover'"><span>⌁</span>发现</button><button :class="{ active: activeTab === 'plans' }" @click="activeTab = 'plans'"><span>□</span>计划</button><button :class="{ active: activeTab === 'settings' }" @click="activeTab = 'settings'"><span>◌</span>我的</button></nav>
    </template>
    <div v-if="toast" class="toast">{{ toast }}</div>
  </main>
</template>
