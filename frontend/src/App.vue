<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const me = ref(null), space = ref(null), preference = ref(null), today = ref(null), weekend = ref(null), plans = ref([])
const activeTab = ref('discover'), authMode = ref('login'), loading = ref(true), submitting = ref(false), refreshing = ref(false)
const error = ref(''), toast = ref(''), inviteToken = ref(''), setupMode = ref('create'), selected = ref(null), locating = ref(false)
const origin = reactive({ latitude: null, longitude: null, label: '尚未使用当前位置' })
const authForm = reactive({ email: '', displayName: '', password: '' })
const spaceForm = reactive({ name: '我们的小宇宙', city: '杭州', token: '' })
const preferenceForm = reactive({ city: '杭州', budget: 150, travelRadiusKm: 12, foodTags: '咖啡,川菜', activityTags: '展览,散步', travelTags: '海边,慢旅行' })
const decisionForm = reactive({ prompt: '', city: '杭州', tags: '咖啡', budget: 150, travelMinutes: 35 })

const isSetup = computed(() => me.value && !space.value)
const needsPreferences = computed(() => space.value && preference.value && !preference.value.onboardingComplete)
const city = computed(() => decisionForm.city || preference.value?.city || space.value?.defaultCity || '杭州')
const queryTags = computed(() => decisionForm.tags.split(/[,，、/|]/).map(item => item.trim()).filter(Boolean))
const mapUrl = computed(() => selected.value?.sourceUrl || `https://ditu.amap.com/search?query=${encodeURIComponent(city.value)}`)
const selectedCandidates = computed(() => today.value?.candidates || [])

const api = async (url, options = {}) => {
  const response = await fetch(url, { credentials: 'include', headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }, ...options })
  const body = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(body.message || '操作没有成功，请稍后重试')
  return body
}
const showToast = message => { toast.value = message; window.setTimeout(() => { toast.value = '' }, 2800) }
const requestParams = () => {
  const params = new URLSearchParams({ city: city.value, prompt: decisionForm.prompt, tags: decisionForm.tags, budget: String(decisionForm.budget || ''), travelMinutes: String(decisionForm.travelMinutes || '') })
  if (origin.latitude != null && origin.longitude != null) { params.set('latitude', origin.latitude); params.set('longitude', origin.longitude) }
  return `?${params.toString()}`
}
const refreshRecommendations = async (message = true) => {
  refreshing.value = true; error.value = ''
  try {
    const query = requestParams()
    const [todayData, weekendData] = await Promise.all([api(`/api/v1/recommendations/today${query}`), api(`/api/v1/recommendations/weekend${query}`)])
    today.value = todayData; weekend.value = weekendData
    if (!selected.value || !todayData.candidates.some(item => item.id === selected.value.id)) selected.value = todayData.candidates[0] || null
    if (message) showToast('已按新的条件更新共同决策地图')
  } catch (e) { error.value = e.message } finally { refreshing.value = false }
}
const useCurrentLocation = () => {
  if (!navigator.geolocation) { showToast('当前浏览器不支持定位，将继续按城市中心估算'); return }
  locating.value = true
  navigator.geolocation.getCurrentPosition(async position => {
    origin.latitude = Number(position.coords.latitude.toFixed(6)); origin.longitude = Number(position.coords.longitude.toFixed(6)); origin.label = '已使用当前位置'
    locating.value = false; await refreshRecommendations()
  }, () => { locating.value = false; origin.label = '定位未授权，已使用城市中心'; showToast(origin.label) }, { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 })
}
const removeTag = tag => { decisionForm.tags = queryTags.value.filter(item => item !== tag).join('，') }
const chooseCandidate = candidate => { selected.value = candidate; document.querySelector('.map-workbench')?.scrollIntoView({ behavior: 'smooth', block: 'start' }) }

const loadWorkspace = async () => {
  try { space.value = await api('/api/v1/spaces/current') } catch (e) { if (e.message.includes('创建或加入')) { space.value = null; preference.value = null; return }; throw e }
  preference.value = await api('/api/v1/preferences'); Object.assign(preferenceForm, preference.value)
  Object.assign(decisionForm, { city: preference.value.city || space.value.defaultCity || '杭州', budget: preference.value.budget || 150, tags: preference.value.foodTags || '咖啡' })
  if (preference.value.onboardingComplete) await refreshRecommendations(false)
  plans.value = await api('/api/v1/plans')
}
const bootstrap = async () => { loading.value = true; error.value = ''; try { const user = await api('/api/v1/auth/me'); me.value = user.authenticated ? user : null; if (me.value) await loadWorkspace() } catch (e) { error.value = e.message } finally { loading.value = false } }
const submitAuth = async () => { submitting.value = true; error.value = ''; try { const url = authMode.value === 'register' ? '/api/v1/auth/register' : '/api/v1/auth/login'; const payload = authMode.value === 'register' ? authForm : { email: authForm.email, password: authForm.password }; me.value = await api(url, { method: 'POST', body: JSON.stringify(payload) }); await loadWorkspace(); showToast(authMode.value === 'register' ? '账户已创建，先建立你们的空间吧' : '欢迎回来') } catch (e) { error.value = e.message } finally { submitting.value = false } }
const createOrJoinSpace = async () => { submitting.value = true; error.value = ''; try { space.value = setupMode.value === 'create' ? await api('/api/v1/spaces', { method: 'POST', body: JSON.stringify({ name: spaceForm.name, city: spaceForm.city }) }) : await api('/api/v1/spaces/join', { method: 'POST', body: JSON.stringify({ token: spaceForm.token }) }); preference.value = await api('/api/v1/preferences'); Object.assign(preferenceForm, preference.value); showToast(setupMode.value === 'create' ? '空间已建立，现在设置你的偏好' : '已加入你们的空间') } catch (e) { error.value = e.message } finally { submitting.value = false } }
const savePreferences = async () => { submitting.value = true; error.value = ''; try { preference.value = await api('/api/v1/preferences', { method: 'PUT', body: JSON.stringify(preferenceForm) }); Object.assign(decisionForm, { city: preferenceForm.city, budget: preferenceForm.budget, tags: preferenceForm.foodTags || '咖啡' }); await refreshRecommendations(false); showToast('偏好已保存，地图推荐已更新') } catch (e) { error.value = e.message } finally { submitting.value = false } }
const feedback = async (candidate, action) => { try { await api(`/api/v1/candidates/${candidate.id}/feedback`, { method: 'POST', body: JSON.stringify({ action }) }); showToast(action === 'WANT' ? '已标记为你的心动地点，系统会记住它' : '已降低类似地点的推荐频率') } catch (e) { error.value = e.message } }
const addPlan = async candidate => { try { await api('/api/v1/plans', { method: 'POST', body: JSON.stringify({ candidateId: candidate.id, budget: decisionForm.budget, note: decisionForm.prompt }) }); plans.value = await api('/api/v1/plans'); activeTab.value = 'plans'; showToast('已加入共同计划') } catch (e) { error.value = e.message } }
const completePlan = async plan => { try { await api(`/api/v1/plans/${plan.id}/complete`, { method: 'POST' }); plans.value = await api('/api/v1/plans'); showToast('已完成；足迹地图与相册将在下一阶段呈现') } catch (e) { error.value = e.message } }
const createInvite = async () => { try { const invite = await api('/api/v1/spaces/current/invites', { method: 'POST' }); inviteToken.value = invite.token; if (navigator.clipboard) await navigator.clipboard.writeText(invite.token).catch(() => null); showToast('邀请口令已复制，发给另一位成员即可') } catch (e) { error.value = e.message } }
const logout = async () => { await api('/api/v1/auth/logout', { method: 'POST' }).catch(() => null); me.value = null; space.value = null; preference.value = null; today.value = null; weekend.value = null; plans.value = []; selected.value = null; activeTab.value = 'discover' }
onMounted(bootstrap)
</script>

<template>
  <main class="page-shell">
    <section v-if="loading" class="centered-card"><span class="pulse-dot"></span><p>正在整理你们下一次出发的灵感…</p></section>
    <section v-else-if="!me" class="auth-layout"><div class="brand-intro"><span class="brand-orbit">⌁</span><p class="eyebrow">TOGETHER, IN MOTION</p><h1>把“去哪”<br>变成共同期待。</h1><p>不必先收藏，也能为你们推荐此刻和下一个周末最值得出发的地方。</p></div><form class="auth-card" @submit.prevent="submitAuth"><p class="eyebrow">PRIVATE SPACE</p><h2>{{ authMode === 'login' ? '欢迎回来' : '建立你的账户' }}</h2><label v-if="authMode === 'register'">怎么称呼你<input v-model.trim="authForm.displayName" maxlength="80" required placeholder="例如：小明"></label><label>邮箱<input v-model.trim="authForm.email" type="email" required autocomplete="email" placeholder="you@example.com"></label><label>密码<input v-model="authForm.password" type="password" minlength="8" required autocomplete="current-password" placeholder="至少 8 位"></label><p v-if="error" class="error">{{ error }}</p><button class="primary wide" :disabled="submitting">{{ submitting ? '处理中…' : authMode === 'login' ? '进入双人空间' : '创建账户' }}</button><button type="button" class="text-button" @click="authMode = authMode === 'login' ? 'register' : 'login'; error = ''">{{ authMode === 'login' ? '还没有账户？创建一个' : '已有账户？直接登录' }}</button></form></section>
    <section v-else-if="isSetup" class="setup-card"><span class="brand-orbit">⌁</span><p class="eyebrow">FIRST STEP</p><h1>先建立你们的私密空间</h1><p class="muted">每位成员各自有账户，在同一个空间中保留偏好、心动地点和计划。</p><div class="segmented"><button :class="{ active: setupMode === 'create' }" @click="setupMode = 'create'">我来创建</button><button :class="{ active: setupMode === 'join' }" @click="setupMode = 'join'">加入对方</button></div><form @submit.prevent="createOrJoinSpace"><template v-if="setupMode === 'create'"><label>空间名字<input v-model.trim="spaceForm.name" required maxlength="100"></label><label>常驻城市<input v-model.trim="spaceForm.city" required maxlength="80" placeholder="杭州"></label></template><label v-else>邀请口令<input v-model.trim="spaceForm.token" required placeholder="粘贴对方发来的口令"></label><p v-if="error" class="error">{{ error }}</p><button class="primary wide" :disabled="submitting">{{ setupMode === 'create' ? '建立并继续' : '加入空间' }}</button></form></section>
    <section v-else-if="needsPreferences" class="setup-card preference-setup"><p class="eyebrow">MAKE IT YOURS</p><h1>先认识一下你</h1><p class="muted">这些偏好只服务于你们的空间，并可随时调整。</p><form @submit.prevent="savePreferences"><div class="two-columns"><label>常驻城市<input v-model.trim="preferenceForm.city" required></label><label>单次预算（元）<input v-model.number="preferenceForm.budget" type="number" min="0" max="10000" required></label></div><label>喜欢吃什么？<input v-model="preferenceForm.foodTags" placeholder="例如：川菜, 咖啡, 面馆"></label><label>喜欢做什么？<input v-model="preferenceForm.activityTags" placeholder="例如：展览, 散步, 看电影"></label><label>旅行偏好<input v-model="preferenceForm.travelTags" placeholder="例如：海边, 慢旅行, 古城"></label><label>可接受的本地出行半径（km）<input v-model.number="preferenceForm.travelRadiusKm" type="number" min="1" max="200" required></label><p v-if="error" class="error">{{ error }}</p><button class="primary wide" :disabled="submitting">生成第一张共同地图</button></form></section>
    <template v-else>
      <header class="app-header"><button class="wordmark" @click="activeTab = 'discover'"><span>⌁</span><b>{{ space?.name }}</b></button><button class="avatar" @click="activeTab = 'settings'">{{ me.displayName.slice(0, 1) }}</button></header>
      <section v-if="error" class="banner error">{{ error }} <button @click="error = ''">×</button></section>
      <section v-if="activeTab === 'discover'" class="content decision-page">
        <div class="greeting"><p class="eyebrow">共同决策地图 · {{ city }}</p><h1>今天，想一起<br><em>去哪里？</em></h1><p class="muted">说出此刻的想法，系统会把它转成可调整的共同条件。</p></div>
        <form class="decision-console" @submit.prevent="refreshRecommendations()"><label class="prompt-input"><span>✦</span><input v-model.trim="decisionForm.prompt" maxlength="180" placeholder="例如：下周末想找一家安静、能聊天的咖啡馆"></label><div class="filter-grid"><label>城市<input v-model.trim="decisionForm.city" required></label><label>预算上限（元）<input v-model.number="decisionForm.budget" type="number" min="0" max="10000"></label><label>可接受车程（分钟）<input v-model.number="decisionForm.travelMinutes" type="number" min="5" max="240"></label><label>偏好关键词<input v-model="decisionForm.tags" placeholder="咖啡，展览，散步"></label></div><div class="chips"><span v-for="tag in queryTags" :key="tag" class="chip">{{ tag }} <button type="button" @click="removeTag(tag)" aria-label="删除条件">×</button></span><span class="location-chip" :class="{ ready: origin.latitude !== null }">◎ {{ origin.label }}</span></div><div class="decision-actions"><button type="button" class="secondary" :disabled="locating" @click="useCurrentLocation">{{ locating ? '正在定位…' : '使用我的位置' }}</button><button class="primary" :disabled="refreshing">{{ refreshing ? '正在重算…' : '更新共同推荐' }}</button></div></form>
        <section class="map-workbench"><div class="map-heading"><div><p class="eyebrow">LIVE DECISION MAP</p><h2>候选都落在地图上</h2></div><span class="weather">{{ today?.weather || '正在读取天气' }}</span></div><p v-if="today" class="source-status">{{ today.sourceStatus }}</p><div class="map-canvas"><iframe :key="mapUrl" title="候选地点地图" :src="mapUrl" loading="lazy" referrerpolicy="no-referrer"></iframe><div class="map-shade"></div><button v-for="(candidate, index) in selectedCandidates" :key="candidate.id" class="map-pin" :class="{ selected: selected?.id === candidate.id }" :style="{ left: `${18 + (index % 3) * 30}%`, top: `${28 + Math.floor(index / 3) * 31}%` }" @click="chooseCandidate(candidate)">{{ index + 1 }}</button><div class="map-fallback-label">{{ selected?.sourceLabel || '地图服务加载中' }}</div></div>
        <article v-if="selected" class="place-sheet"><div class="place-number">{{ selectedCandidates.findIndex(item => item.id === selected.id) + 1 }}</div><div class="place-main"><p class="category">{{ selected.category }}</p><h2>{{ selected.title }}</h2><p class="place-address">{{ selected.district || selected.city }}{{ selected.address ? ' · ' + selected.address : '' }}</p><div class="place-metrics"><span>⌖ {{ selected.distanceKm != null ? `${selected.distanceKm} km` : '距离待确认' }}</span><span>▹ {{ selected.travelMinutes ? `约 ${selected.travelMinutes} 分钟车程` : '路线待确认' }}</span><span>¥ {{ selected.estimate }}</span></div><p class="route-status">{{ selected.travelDataStatus }}</p><p class="reason">{{ selected.reason }}</p><small>数据依据：{{ selected.sourceLabel }} · {{ selected.scoreBreakdown }}</small><div class="card-actions"><button @click="feedback(selected, 'WANT')">♡ 标记心动</button><button @click="feedback(selected, 'SKIP')">先不考虑</button><a v-if="selected.navigationUrl" :href="selected.navigationUrl" target="_blank" rel="noreferrer">打开路线 ↗</a><button class="solid-action" @click="addPlan(selected)">加入计划 →</button></div></div></article></section>
        <section class="candidate-list"><div class="section-heading"><div><p>共同候选</p><h2>换个角度再看看</h2></div><span>{{ today?.querySummary }}</span></div><button v-for="(candidate, index) in selectedCandidates" :key="candidate.id" class="candidate-row" :class="{ active: selected?.id === candidate.id }" @click="chooseCandidate(candidate)"><b>{{ index + 1 }}</b><span><strong>{{ candidate.title }}</strong><small>{{ candidate.distanceKm != null ? `${candidate.distanceKm} km · ` : '' }}{{ candidate.travelMinutes ? `约 ${candidate.travelMinutes} 分钟` : candidate.sourceLabel }}</small></span><em>{{ candidate.score }}</em></button></section>
        <section class="weekend-panel"><div class="section-heading"><div><p>提前期待</p><h2>下个周末，也可以这样选</h2></div><span class="spark">✦</span></div><article v-for="candidate in (weekend?.candidates || []).slice(0, 3)" :key="candidate.id" class="weekend-card"><div><p class="category">{{ candidate.bestSeason }}</p><h3>{{ candidate.title }}</h3><p>{{ candidate.reason }}</p><small>{{ candidate.distanceKm != null ? `${candidate.distanceKm} km · ` : '' }}{{ candidate.estimate }}</small></div><button @click="addPlan(candidate)">加入计划</button></article></section>
      </section>
      <section v-else-if="activeTab === 'plans'" class="content"><div class="greeting"><p class="eyebrow">OUR NEXT STORIES</p><h1>共同计划</h1><p class="muted">第一阶段先把想去的地方收进计划；下一阶段会将完成的行程展示在足迹地图与相册中。</p></div><div v-if="plans.length" class="plan-list"><article v-for="plan in plans" :key="plan.id" class="plan-card"><span class="plan-status" :class="plan.status.toLowerCase()">{{ plan.status === 'COMPLETED' ? '已完成' : '进行中' }}</span><h3>{{ plan.title }}</h3><p>{{ plan.city }} · {{ plan.budget ? `预算 ¥${plan.budget}` : '预算待定' }}</p><p v-if="plan.note" class="muted">{{ plan.note }}</p><button v-if="plan.status !== 'COMPLETED'" class="primary small" @click="completePlan(plan)">完成这次行程</button></article></div><div v-else class="empty-state"><span>⌁</span><h2>还没有计划</h2><p>从共同地图中把一个心动地点加入这里。</p><button class="primary" @click="activeTab = 'discover'">去看地图</button></div></section>
      <section v-else class="content settings"><div class="greeting"><p class="eyebrow">YOUR SIGNALS</p><h1>偏好与空间</h1></div><form class="settings-form" @submit.prevent="savePreferences"><div class="two-columns"><label>常驻城市<input v-model.trim="preferenceForm.city" required></label><label>单次预算<input v-model.number="preferenceForm.budget" type="number" min="0" max="10000" required></label></div><label>喜欢吃什么？<input v-model="preferenceForm.foodTags"></label><label>喜欢做什么？<input v-model="preferenceForm.activityTags"></label><label>旅行偏好<input v-model="preferenceForm.travelTags"></label><label>出行半径（km）<input v-model.number="preferenceForm.travelRadiusKm" type="number" min="1" max="200" required></label><button class="primary wide" :disabled="submitting">保存并更新地图</button></form><section class="invite-box"><p class="eyebrow">INVITE PARTNER</p><h2>邀请另一位成员</h2><p class="muted">邀请码 72 小时有效，仅能使用一次。</p><button class="secondary wide" @click="createInvite">生成邀请口令</button><code v-if="inviteToken">{{ inviteToken }}</code></section><button class="logout" @click="logout">退出登录</button></section>
      <nav class="bottom-nav"><button :class="{ active: activeTab === 'discover' }" @click="activeTab = 'discover'"><span>⌖</span>地图</button><button :class="{ active: activeTab === 'plans' }" @click="activeTab = 'plans'"><span>□</span>计划</button><button :class="{ active: activeTab === 'settings' }" @click="activeTab = 'settings'"><span>◌</span>我的</button></nav>
    </template>
    <div v-if="toast" class="toast">{{ toast }}</div>
  </main>
</template>
