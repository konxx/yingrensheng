const routes = [
  { path: "/web", label: "总览", icon: "⌘" },
  { path: "/web/create", label: "开始创作", icon: "✦" },
  { path: "/web/projects", label: "项目", icon: "▦" },
  { path: "/web/works", label: "作品", icon: "◈" },
  { path: "/web/orders", label: "订单", icon: "¥" },
  { path: "/web/member", label: "会员权益", icon: "◆" },
  { path: "/web/ai", label: "生成服务", icon: "◇" },
  { path: "/web/settings", label: "设置", icon: "⚙" },
];

const state = {
  token: localStorage.getItem("yrs_web_access_token") || "",
  refreshToken: localStorage.getItem("yrs_web_refresh_token") || "",
  user: JSON.parse(localStorage.getItem("yrs_web_user") || "null"),
  scenes: [],
  projects: [],
  works: [],
  orders: [],
  plans: [],
  member: null,
  aiStatus: null,
  selectedProjectId: "",
  draft: null,
  storyboard: [],
  preview: null,
  activeFilter: "ALL",
  search: "",
  toast: "",
  modal: null,
};

const app = document.getElementById("app");

function apiBase() {
  return "/api/v1";
}

function currentPath() {
  const path = window.location.pathname.replace(/\/$/, "");
  return path === "" ? "/web" : path;
}

function navigate(path) {
  window.history.pushState({}, "", path);
  render();
}

window.addEventListener("popstate", render);

async function request(path, options = {}) {
  const headers = {
    Accept: "application/json",
    ...(options.body instanceof FormData ? {} : { "Content-Type": "application/json" }),
    ...(state.token ? { Authorization: `Bearer ${state.token}` } : {}),
    ...(options.headers || {}),
  };
  const response = await fetch(`${apiBase()}${path}`, {
    ...options,
    headers,
    body: options.body && !(options.body instanceof FormData) ? JSON.stringify(options.body) : options.body,
  });
  const text = await response.text();
  let payload = null;
  try {
    payload = text ? JSON.parse(text) : null;
  } catch {
    payload = { error: { code: "INVALID_JSON", message: text } };
  }
  if (!response.ok) {
    const detail = payload?.detail || payload?.error?.message || response.statusText;
    throw new Error(`${response.status} ${detail}`);
  }
  if (payload?.error) {
    throw new Error(`${payload.error.code}: ${payload.error.message}`);
  }
  return payload?.data;
}

function setToast(message) {
  state.toast = message;
  render();
  window.clearTimeout(setToast.timer);
  setToast.timer = window.setTimeout(() => {
    state.toast = "";
    render();
  }, 3600);
}

function sleep(ms) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

function taskStageLabel(stage) {
  return {
    STORYBOARD_QUEUED: "章节生成已提交",
    STORYBOARD_DRAFT_READY: "正在整理创作草稿",
    STORYBOARD_AI_WRITING: "千问正在创作章节正文",
    STORYBOARD_ASSEMBLING: "正在整理章节内容",
    STORYBOARD_SUCCEEDED: "章节正文已生成",
    STORYBOARD_FAILED: "章节生成失败",
    DASHSCOPE_VIDEO_RUNNING: "正在生成视频预览",
    DASHSCOPE_VIDEO_SUCCEEDED: "视频预览已生成",
  }[stage] || stage || "生成中";
}

function taskErrorMessage(task) {
  return task?.error?.message || task?.error?.code || "生成失败";
}

async function waitTask(taskId, maxAttempts = 80) {
  let latest = null;
  for (let index = 0; index < maxAttempts; index += 1) {
    latest = await request(`/tasks/${taskId}`);
    setToast(`${taskStageLabel(latest.currentStage)} · ${latest.progress || 0}%`);
    if (latest.status === "SUCCEEDED" || latest.status === "FAILED") {
      return latest;
    }
    await sleep(3000);
  }
  return latest || { status: "FAILED", error: { message: "生成任务查询超时" } };
}

async function boot() {
  render();
  if (state.token) {
    await refreshAll().catch((error) => setToast(`数据同步失败：${error.message}`));
  }
  render();
}

async function refreshAll() {
  const [scenes, projects, works, orders, plans, member, aiStatus] = await Promise.all([
    request("/scenes").catch(() => []),
    request("/projects?page=1&pageSize=50").then((page) => page.items || []).catch(() => []),
    request("/works").catch(() => []),
    request("/orders").catch(() => []),
    request("/member/plans").catch(() => []),
    request("/member/me").catch(() => null),
    request("/ai/status").catch(() => null),
  ]);
  Object.assign(state, { scenes, projects, works, orders, plans, member, aiStatus });
  state.selectedProjectId = state.selectedProjectId || projects[0]?.projectId || "";
}

async function login(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const username = String(form.get("username") || "").trim();
  const password = String(form.get("password") || "");
  if (!username || !password) {
    setToast("请输入账号和密码");
    return;
  }
  try {
    const data = await request("/auth/login", {
      method: "POST",
      body: { username, password, smsCode: "123456", deviceId: "web_console" },
    });
    state.token = data.accessToken;
    state.refreshToken = data.refreshToken;
    state.user = data.user;
    localStorage.setItem("yrs_web_access_token", state.token);
    localStorage.setItem("yrs_web_refresh_token", state.refreshToken);
    localStorage.setItem("yrs_web_user", JSON.stringify(state.user));
    await refreshAll();
    setToast("已登录映人生");
    navigate("/web");
  } catch (error) {
    setToast(`登录失败：${error.message}`);
  }
}

function logout() {
  state.token = "";
  state.refreshToken = "";
  state.user = null;
  localStorage.removeItem("yrs_web_access_token");
  localStorage.removeItem("yrs_web_refresh_token");
  localStorage.removeItem("yrs_web_user");
  render();
}

function pageMeta(path) {
  const map = {
    "/web": ["映人生创作中心", "在浏览器中完成场景选择、项目管理、草稿、分镜、预览、导出和作品管理。"],
    "/web/create": ["开始创作", "选择创作方向，填写主题或大纲，生成故事草稿与后续成品。"],
    "/web/projects": ["项目管理", "查看项目状态、草稿、章节正文、分镜和预览资源。"],
    "/web/works": ["作品库", "浏览导出作品，直接查看文本、封面与视频资源。"],
    "/web/orders": ["订单与导出", "追踪导出任务、会员权益豁免和订单记录。"],
    "/web/member": ["会员权益", "查看 Lite、Pro、Max 与尊享权益，了解当前账号可用能力。"],
    "/web/ai": ["生成服务", "查看文本、图片、视频和语音生成能力的可用状态。"],
    "/web/settings": ["设置", "管理账号状态、访问入口和常用操作。"],
  };
  return map[path] || map["/web"];
}

function render() {
  if (!state.token) {
    app.innerHTML = renderLogin();
    bindLogin();
    return;
  }
  const path = currentPath();
  app.innerHTML = `
    <div class="app-shell">
      ${renderSidebar(path)}
      <main class="main">
        ${renderTopbar(path)}
        ${renderPage(path)}
      </main>
    </div>
    ${state.toast ? `<div class="toast">${escapeHtml(state.toast)}</div>` : ""}
    ${state.modal ? renderModal() : ""}
  `;
  bindGlobal();
  bindPage(path);
}

function renderLogin() {
  return `
    <div class="login-layout">
      <section class="login-visual">
        <div>
          <div class="brand">
            <div class="brand-mark">映</div>
            <div>
              <h1 class="brand-title">映人生</h1>
              <p class="brand-subtitle" style="color:rgba(255,255,255,.72)">AI Story Studio</p>
            </div>
          </div>
          <h2 class="page-title">AI 故事创作中心</h2>
          <p class="page-desc">在网页端继续完成故事创作、作品预览、导出和会员权益管理。</p>
        </div>
        <div class="grid three">
          ${["智能生成", "作品预览", "尊享权益"].map((item) => `<div class="card" style="background:rgba(255,255,255,.14);color:#fff;border-color:rgba(255,255,255,.24)">${item}</div>`).join("")}
        </div>
      </section>
      <section class="login-panel">
        <form class="login-card form" id="login-form">
          <div>
            <p class="eyebrow" style="color:#4f46e5">WELCOME BACK</p>
            <h2 class="card-title" style="font-size:28px">登录映人生</h2>
            <p class="small-note">请输入账号密码登录。</p>
          </div>
          <div class="field">
            <label>账号</label>
            <input name="username" autocomplete="username" />
          </div>
          <div class="field">
            <label>密码</label>
            <input name="password" type="password" autocomplete="current-password" />
          </div>
          <button class="btn primary" type="submit">登录映人生</button>
          <p class="small-note">退出登录会清除本机登录状态。</p>
        </form>
      </section>
    </div>
    ${state.toast ? `<div class="toast">${escapeHtml(state.toast)}</div>` : ""}
  `;
}

function renderSidebar(path) {
  return `
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">映</div>
        <div>
          <h1 class="brand-title">映人生</h1>
          <p class="brand-subtitle">AI Story Console</p>
        </div>
      </div>
      <div class="nav-group-label">创作</div>
      ${routes.map((route) => `
        <button class="nav-link ${path === route.path ? "active" : ""}" data-nav="${route.path}">
          <span><span class="nav-icon">${route.icon}</span>${route.label}</span>
          <span class="nav-count">${navCount(route.path)}</span>
        </button>
      `).join("")}
      <div class="nav-group-label">账户</div>
      <button class="nav-link" id="logout-button"><span><span class="nav-icon">↩</span>退出登录</span></button>
    </aside>
  `;
}

function renderTopbar(path) {
  return `
    <div class="topbar">
      <select class="mobile-nav" id="mobile-nav">
        ${routes.map((route) => `<option value="${route.path}" ${path === route.path ? "selected" : ""}>${route.label}</option>`).join("")}
      </select>
      <div class="search-box">
        <span>⌕</span>
        <input id="global-search" value="${escapeAttr(state.search)}" placeholder="搜索项目、作品、场景或订单" />
      </div>
      <div class="user-chip">
        <div class="avatar">${escapeHtml((state.user?.nickname || state.user?.username || "映").slice(0, 1))}</div>
        <div>
          <div style="font-weight:800">${escapeHtml(state.user?.nickname || state.user?.username || "映人生用户")}</div>
          <div class="small-note">${escapeHtml(state.user?.role === "admin" ? "尊享账号" : "普通用户")}</div>
        </div>
      </div>
    </div>
  `;
}

function renderPage(path) {
  if (path === "/web/create") return renderCreate();
  if (path === "/web/projects") return renderProjects();
  if (path === "/web/works") return renderWorks();
  if (path === "/web/orders") return renderOrders();
  if (path === "/web/member") return renderMember();
  if (path === "/web/ai") return renderAi();
  if (path === "/web/settings") return renderSettings();
  return renderDashboard();
}

function renderHeader(path, actions = "") {
  const [title, desc] = pageMeta(path);
  return `
    <section class="page-header">
      <p class="eyebrow">YING REN SHENG</p>
      <h2 class="page-title">${title}</h2>
      <p class="page-desc">${desc}</p>
      ${actions ? `<div class="header-actions">${actions}</div>` : ""}
    </section>
  `;
}

function renderDashboard() {
  const actions = `
    <button class="btn ghost" data-action="refresh">同步数据</button>
    <button class="btn" data-nav="/web/create">开始创作</button>
  `;
  return `
    ${renderHeader("/web", actions)}
    <section class="grid stats">
      ${statCard("项目", state.projects.length, "草稿与生成任务", "↗")}
      ${statCard("作品", state.works.length, "已导出成果", "✓")}
      ${statCard("订单", state.orders.length, "导出与权益", "¥")}
      ${statCard("生成服务", state.aiStatus?.credentials?.dashscopeApiKeyConfigured ? "可用" : "待开通", "文本、图片与视频能力", "◇")}
    </section>
    <section class="grid two" style="margin-top:16px">
      <div class="card">
        <h3 class="card-title">创作流水线</h3>
        <p class="card-subtitle">这里汇总你的创作进度、作品状态和权益使用情况。</p>
        <div class="stack" style="margin-top:18px">
          ${["选择场景", "创建项目", "生成草稿", "生成分镜/章节", "预览与导出"].map((step, index) => `
            <div class="timeline-item">
              <div class="timeline-dot">${index + 1}</div>
              <div>
                <strong>${step}</strong>
                <p class="card-subtitle">${pipelineCopy(index)}</p>
              </div>
            </div>
          `).join("")}
        </div>
      </div>
      <div class="card">
        <h3 class="card-title">最近项目</h3>
        <p class="card-subtitle">点击项目可进入项目页查看细节。</p>
        <div class="stack" style="margin-top:16px">
          ${filtered(state.projects).slice(0, 5).map((project) => projectMini(project)).join("") || empty("暂无项目，先创建一个项目。")}
        </div>
      </div>
    </section>
  `;
}

function renderCreate() {
  const sceneCards = filtered(state.scenes).map((scene) => `
    <label class="card scene-card">
      <div class="scene-head">
        <div>
          <h3 class="card-title">${escapeHtml(scene.title)}</h3>
          <p class="card-subtitle">${escapeHtml(scene.subtitle)}</p>
        </div>
        <div class="scene-icon">${sceneIcon(scene.sceneId)}</div>
      </div>
      <span class="pill">${escapeHtml(scene.recommendedDurationLabel || "创作包")}</span>
      <input type="radio" name="sceneId" value="${escapeAttr(scene.sceneId)}" ${state.scenes[0]?.sceneId === scene.sceneId ? "checked" : ""} />
    </label>
  `).join("");
  return `
    ${renderHeader("/web/create", `<button class="btn ghost" data-action="refresh">刷新场景</button>`)}
    <section class="grid two">
      <form class="card form" id="create-form">
        <h3 class="card-title">新建创作项目</h3>
        <p class="card-subtitle">创建后将为你生成草稿，并继续整理章节、分镜或预览。</p>
        <div class="field">
          <label>项目标题</label>
          <input name="title" value="新的故事创作" />
        </div>
        <div class="field">
          <label>创作方式</label>
          <select name="mode">
            <option value="CHARACTER_TIME_TRAVEL">角色穿越</option>
            <option value="OUTLINE_STORY">大纲成文</option>
            <option value="NOVEL_TO_MEDIA">小说改编</option>
          </select>
        </div>
        <div class="field">
          <label>主题 / 大纲 / 小说片段</label>
          <textarea name="themeLine">一个现代创作者进入古典小说世界，试图用自己的选择改写人物命运。</textarea>
        </div>
        <div class="field">
          <label>叙事风格</label>
          <select name="styleId">
            <option value="style_classic">古典章回</option>
            <option value="style_webnovel">网文爽感</option>
            <option value="style_comic">连环漫画</option>
            <option value="style_cinematic">影视短剧</option>
          </select>
        </div>
        <button class="btn primary" type="submit">创建并生成草稿</button>
      </form>
      <div class="grid">
        <div class="card">
          <h3 class="card-title">选择场景</h3>
          <p class="card-subtitle">请选择本次创作的故事方向。</p>
        </div>
        <div class="grid">${sceneCards || empty("暂未加载到场景，请稍后重试。")}</div>
      </div>
    </section>
  `;
}

function renderProjects() {
  const selected = state.projects.find((item) => item.projectId === state.selectedProjectId) || state.projects[0];
  const rows = filtered(state.projects).map((project) => `
    <tr>
      <td><strong>${escapeHtml(project.title)}</strong><br><span class="small-note">${escapeHtml(project.projectId)}</span></td>
      <td>${escapeHtml(project.sceneTitle)}</td>
      <td><span class="pill ${project.status === "EXPORTED" ? "green" : ""}">${escapeHtml(project.status)}</span></td>
      <td><div class="progress"><span style="width:${Number(project.progress || 0)}%"></span></div></td>
      <td>${escapeHtml(project.currentStep)}</td>
      <td>
        <button class="btn secondary" data-project="${project.projectId}" data-action="select-project">查看</button>
      </td>
    </tr>
  `).join("");
  return `
    ${renderHeader("/web/projects", `<button class="btn ghost" data-action="refresh">刷新项目</button><button class="btn" data-nav="/web/create">新建项目</button>`)}
    <section class="toolbar">
      <div class="segmented">
        ${["ALL", "DRAFT", "GENERATED", "EXPORTED"].map((item) => `<button class="${state.activeFilter === item ? "active" : ""}" data-filter="${item}">${item}</button>`).join("")}
      </div>
    </section>
    <section class="grid two">
      <div class="card table-card">
        <table>
          <thead><tr><th>项目</th><th>场景</th><th>状态</th><th>进度</th><th>步骤</th><th>操作</th></tr></thead>
          <tbody>${rows || `<tr><td colspan="6">${empty("暂无项目")}</td></tr>`}</tbody>
        </table>
      </div>
      ${selected ? renderProjectDetail(selected) : `<div class="card">${empty("请选择项目")}</div>`}
    </section>
  `;
}

function renderProjectDetail(project) {
  return `
    <aside class="card stack">
      <div>
        <h3 class="card-title">${escapeHtml(project.title)}</h3>
        <p class="card-subtitle">${escapeHtml(project.sceneTitle)} · ${escapeHtml(project.updatedAt || "")}</p>
      </div>
      <div class="progress"><span style="width:${Number(project.progress || 0)}%"></span></div>
      <div class="grid">
        <button class="btn primary" data-project="${project.projectId}" data-action="load-draft">查看草稿</button>
        <button class="btn secondary" data-project="${project.projectId}" data-action="generate-draft">重新生成草稿</button>
        <button class="btn secondary" data-project="${project.projectId}" data-action="generate-storyboard">生成章节/分镜</button>
        <button class="btn secondary" data-project="${project.projectId}" data-action="load-preview">查看预览</button>
        <button class="btn secondary" data-project="${project.projectId}" data-action="export-project">导出作品</button>
      </div>
      ${state.draft?.projectId === project.projectId ? `
        <div class="card" style="box-shadow:none;background:#f8fafc">
          <h4 class="card-title">${escapeHtml(state.draft.title)}</h4>
          <p class="text-block">${escapeHtml(state.draft.opening)}\n\n${escapeHtml(state.draft.body)}\n\n${escapeHtml(state.draft.closing)}</p>
        </div>
      ` : ""}
      ${state.storyboard.length ? `
        <div class="stack">
          ${state.storyboard.map((item) => `
            <div class="card" style="box-shadow:none;background:#f8fafc">
              <strong>${escapeHtml(item.title)}</strong>
              <p class="card-subtitle">${escapeHtml(item.summary)}</p>
            </div>
          `).join("")}
        </div>
      ` : ""}
      ${state.preview?.projectId === project.projectId ? `
        <div class="card" style="box-shadow:none;background:#f8fafc">
          <h4 class="card-title">${escapeHtml(state.preview.title)}</h4>
          <p class="card-subtitle">${escapeHtml(state.preview.subtitleSummary)}</p>
          <p class="small-note">${escapeHtml(state.preview.coverCaption)} · ${escapeHtml(state.preview.musicLabel)}</p>
        </div>
      ` : ""}
    </aside>
  `;
}

function renderWorks() {
  return `
    ${renderHeader("/web/works", `<button class="btn ghost" data-action="refresh">刷新作品</button>`)}
    <section class="grid three">
      ${filtered(state.works).map((work) => `
        <article class="card work-card">
          <div class="work-cover">${escapeHtml(work.outputKind || "WORK")}</div>
          <div class="work-head">
            <div>
              <h3 class="card-title">${escapeHtml(work.title)}</h3>
              <p class="card-subtitle">${escapeHtml(work.sceneLabel)} · ${escapeHtml(work.durationLabel)}</p>
            </div>
            <span class="pill green">${escapeHtml(work.statusLabel)}</span>
          </div>
          <div class="grid">
            <button class="btn secondary" data-work="${work.workId}" data-action="load-work-assets">查看资源</button>
            ${work.videoUrl ? `<a class="btn secondary" href="${escapeAttr(work.videoUrl)}" target="_blank" rel="noreferrer">预览视频</a>` : ""}
            ${work.coverUrl ? `<a class="btn secondary" href="${escapeAttr(work.coverUrl)}" target="_blank" rel="noreferrer">预览封面</a>` : ""}
            <button class="btn danger" data-work="${work.workId}" data-action="delete-work">删除作品</button>
          </div>
        </article>
      `).join("") || empty("暂无导出作品")}
    </section>
  `;
}

function renderOrders() {
  return `
    ${renderHeader("/web/orders", `<button class="btn ghost" data-action="refresh">刷新订单</button>`)}
    <section class="card table-card">
      <table>
        <thead><tr><th>订单</th><th>金额</th><th>导出规格</th><th>状态</th><th>创建时间</th></tr></thead>
        <tbody>
          ${filtered(state.orders).map((order) => `
            <tr>
              <td><strong>${escapeHtml(order.title)}</strong><br><span class="small-note">${escapeHtml(order.orderId)}</span></td>
              <td>${escapeHtml(order.amountLabel)}</td>
              <td>${escapeHtml(order.exportSpec)}</td>
              <td><span class="pill gold">${escapeHtml(order.statusLabel)}</span></td>
              <td>${escapeHtml(order.createdAt)}</td>
            </tr>
          `).join("") || `<tr><td colspan="5">${empty("暂无订单")}</td></tr>`}
        </tbody>
      </table>
    </section>
  `;
}

function renderMember() {
  const planName = displayPlanName(state.member?.currentPlanName || (state.user?.role === "admin" ? "尊享" : "Lite"));
  return `
    ${renderHeader("/web/member", `<button class="btn ghost" data-action="refresh">同步权益</button>`)}
    <section class="grid stats">
      ${statCard("当前级别", planName, "登录账号权益", "◆")}
      ${statCard("导出额度", state.user?.role === "admin" ? "无限" : "按套餐", "尊享账号可直接导出", "✓")}
      ${statCard("账号状态", state.user?.role === "admin" ? "尊享账号" : "普通账号", "当前登录状态", "◇")}
      ${statCard("套餐数", state.plans.length + 1, "含尊享权益", "＋")}
    </section>
    <section class="grid three" style="margin-top:16px">
      ${state.plans.map((plan) => planCard(plan)).join("")}
      <article class="card plan-card" style="border-color:#f5c76b">
        <div class="plan-head">
          <h3 class="card-title">尊享权益</h3>
          <span class="pill gold">全权益</span>
        </div>
        <div class="stat-value">全权益</div>
        <p class="card-subtitle">尊享账号可使用所有导出、预览与高级创作权益。</p>
        <div class="stack">
          ${["全部套餐能力", "导出豁免", "无水印权益", "高级管理入口"].map((item) => `<span class="pill gold">${item}</span>`).join("")}
        </div>
      </article>
    </section>
  `;
}

function renderAi() {
  const status = state.aiStatus || {};
  return `
    ${renderHeader("/web/ai", `<button class="btn ghost" data-action="refresh">重新检测</button>`)}
    <section class="grid two">
      <div class="card">
        <h3 class="card-title">生成能力</h3>
        <div class="stack" style="margin-top:16px">
          ${keyValue("服务", status.provider ? "已连接" : "待确认")}
          ${keyValue("模式", status.mode ? "标准生成" : "待确认")}
          ${keyValue("文本生成", status.models?.text ? "可用" : "待开通")}
          ${keyValue("图片生成", status.models?.image ? "可用" : "待开通")}
          ${keyValue("视频生成", status.models?.video ? "可用" : "待开通")}
          ${keyValue("语音生成", status.models?.tts ? "可用" : "待开通")}
        </div>
      </div>
      <div class="card">
        <h3 class="card-title">服务可用性</h3>
        <div class="stack" style="margin-top:16px">
          ${keyValue("生成服务授权", status.credentials?.dashscopeApiKeyConfigured ? "已开通" : "待开通")}
          ${keyValue("文本能力", status.sdk?.generation ? "可用" : "待开通")}
          ${keyValue("多模态能力", status.sdk?.multimodalConversation ? "可用" : "待开通")}
          ${keyValue("视频能力", status.sdk?.videoSynthesis ? "可用" : "待开通")}
        </div>
      </div>
    </section>
  `;
}

function renderSettings() {
  return `
    ${renderHeader("/web/settings")}
    <section class="grid two">
      <div class="card stack">
        <h3 class="card-title">访问入口</h3>
        ${keyValue("创作中心首页", `${location.origin}/web`)}
        ${keyValue("项目页", `${location.origin}/web/projects`)}
        ${keyValue("作品页", `${location.origin}/web/works`)}
        ${keyValue("管理入口", `${location.origin}/admin`)}
      </div>
      <div class="card stack">
        <h3 class="card-title">登录状态</h3>
        <p class="card-subtitle">当前设备已保存登录状态，退出后将清除。</p>
        <button class="btn danger" id="logout-inline">退出登录</button>
      </div>
    </section>
  `;
}

function statCard(label, value, caption, icon) {
  return `
    <article class="card stat-card">
      <div class="stat-top"><span>${label}</span><span>${icon}</span></div>
      <div class="stat-value">${escapeHtml(String(value))}</div>
      <div class="stat-delta">${escapeHtml(caption)}</div>
    </article>
  `;
}

function projectMini(project) {
  return `
    <button class="card" style="text-align:left;box-shadow:none;background:#f8fafc" data-nav="/web/projects" data-project="${project.projectId}" data-action="select-project">
      <strong>${escapeHtml(project.title)}</strong>
      <p class="card-subtitle">${escapeHtml(project.sceneTitle)} · ${escapeHtml(project.currentStep)}</p>
    </button>
  `;
}

function planCard(plan) {
  return `
    <article class="card plan-card">
      <div class="plan-head">
        <h3 class="card-title">${escapeHtml(plan.name)}</h3>
        ${plan.badge ? `<span class="pill">${escapeHtml(plan.badge)}</span>` : ""}
      </div>
      <div class="stat-value">¥${Number(plan.monthlyPrice || 0)}</div>
      <p class="card-subtitle">${escapeHtml(plan.summary)}</p>
      <div class="stack">${(plan.features || []).map((item) => `<span class="pill green">${escapeHtml(item)}</span>`).join("")}</div>
    </article>
  `;
}

function displayPlanName(value) {
  const text = String(value || "");
  return text.toLowerCase() === "admin" ? "尊享" : text;
}

function keyValue(key, value) {
  return `<div style="display:flex;justify-content:space-between;gap:14px;border-bottom:1px solid var(--border);padding:10px 0"><strong>${escapeHtml(key)}</strong><span class="small-note">${escapeHtml(String(value))}</span></div>`;
}

function renderModal() {
  return `
    <div class="modal-backdrop" id="modal-backdrop">
      <section class="modal">
        <div class="modal-header">
          <h3 class="card-title">${escapeHtml(state.modal.title)}</h3>
          <button class="btn secondary" id="modal-close">关闭</button>
        </div>
        <div class="modal-body">${state.modal.body}</div>
      </section>
    </div>
  `;
}

function bindLogin() {
  document.getElementById("login-form")?.addEventListener("submit", login);
}

function bindGlobal() {
  document.querySelectorAll("[data-nav]").forEach((node) => {
    node.addEventListener("click", () => {
      const projectId = node.getAttribute("data-project");
      if (projectId) state.selectedProjectId = projectId;
      navigate(node.getAttribute("data-nav"));
    });
  });
  document.getElementById("logout-button")?.addEventListener("click", logout);
  document.getElementById("logout-inline")?.addEventListener("click", logout);
  document.getElementById("mobile-nav")?.addEventListener("change", (event) => navigate(event.target.value));
  document.getElementById("global-search")?.addEventListener("input", (event) => {
    state.search = event.target.value;
    render();
  });
  document.getElementById("modal-close")?.addEventListener("click", () => {
    state.modal = null;
    render();
  });
  document.getElementById("modal-backdrop")?.addEventListener("click", (event) => {
    if (event.target.id === "modal-backdrop") {
      state.modal = null;
      render();
    }
  });
}

function bindPage(path) {
  document.querySelectorAll("[data-action='refresh']").forEach((node) => node.addEventListener("click", async () => {
    await refreshAll();
    setToast("数据已同步");
  }));
  document.querySelectorAll("[data-filter]").forEach((node) => node.addEventListener("click", () => {
    state.activeFilter = node.getAttribute("data-filter");
    render();
  }));
  document.querySelectorAll("[data-action='select-project']").forEach((node) => node.addEventListener("click", () => {
    state.selectedProjectId = node.getAttribute("data-project");
    if (currentPath() !== "/web/projects") navigate("/web/projects");
    else render();
  }));
  document.getElementById("create-form")?.addEventListener("submit", createProject);
  document.querySelectorAll("[data-action='generate-draft']").forEach((node) => node.addEventListener("click", () => generateDraft(node.getAttribute("data-project"))));
  document.querySelectorAll("[data-action='load-draft']").forEach((node) => node.addEventListener("click", () => loadDraft(node.getAttribute("data-project"))));
  document.querySelectorAll("[data-action='generate-storyboard']").forEach((node) => node.addEventListener("click", () => generateStoryboard(node.getAttribute("data-project"))));
  document.querySelectorAll("[data-action='load-preview']").forEach((node) => node.addEventListener("click", () => loadPreview(node.getAttribute("data-project"))));
  document.querySelectorAll("[data-action='export-project']").forEach((node) => node.addEventListener("click", () => exportProject(node.getAttribute("data-project"))));
  document.querySelectorAll("[data-action='load-work-assets']").forEach((node) => node.addEventListener("click", () => loadWorkAssets(node.getAttribute("data-work"))));
  document.querySelectorAll("[data-action='delete-work']").forEach((node) => node.addEventListener("click", () => deleteWork(node.getAttribute("data-work"))));
}

async function createProject(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const checkedScene = document.querySelector("input[name='sceneId']:checked");
  const sceneId = checkedScene?.value || state.scenes[0]?.sceneId || "scene_outline_original";
  try {
    const project = await request("/projects", {
      method: "POST",
      body: {
        sceneId,
        mode: String(form.get("mode")),
        title: String(form.get("title") || "新的故事创作"),
      },
    });
    await refreshAll();
    state.selectedProjectId = project.projectId;
    await generateDraft(project.projectId, String(form.get("styleId")), String(form.get("themeLine")));
    navigate("/web/projects");
  } catch (error) {
    setToast(`创建失败：${error.message}`);
  }
}

async function generateDraft(projectId, styleId = "style_classic", themeLine = "一个现代人进入古典小说世界，改写自己和主角的命运。") {
  try {
    const task = await request(`/projects/${projectId}/story-draft/generate`, {
      method: "POST",
      body: { styleId, themeLine },
    });
    await loadDraft(projectId);
    await refreshAll();
    setToast(`草稿已生成：${task.currentStage}`);
  } catch (error) {
    setToast(`草稿生成失败：${error.message}`);
  }
}

async function loadDraft(projectId) {
  try {
    state.draft = await request(`/projects/${projectId}/story-draft`);
    state.selectedProjectId = projectId;
    render();
  } catch (error) {
    setToast(`草稿读取失败：${error.message}`);
  }
}

async function generateStoryboard(projectId) {
  try {
    const submittedTask = await request(`/projects/${projectId}/storyboard/generate`, { method: "POST", body: {} });
    const task = submittedTask.status === "RUNNING" ? await waitTask(submittedTask.taskId) : submittedTask;
    if (task.status !== "SUCCEEDED") {
      throw new Error(taskErrorMessage(task));
    }
    state.storyboard = await request(`/projects/${projectId}/storyboard`);
    await refreshAll();
    setToast(`章节/分镜已生成：${taskStageLabel(task.currentStage)}`);
    render();
  } catch (error) {
    setToast(`章节/分镜失败：${error.message}`);
  }
}

async function loadPreview(projectId) {
  try {
    state.preview = await request(`/projects/${projectId}/preview`);
    render();
  } catch (error) {
    setToast(`预览读取失败：${error.message}`);
  }
}

async function exportProject(projectId) {
  try {
    const isAdmin = state.user?.role === "admin";
    const task = await request(`/exports/${projectId}`, {
      method: "POST",
      body: {
        exportPlanId: isAdmin ? "plan_admin" : "plan_single",
        resolution: "1080P",
        removeWatermark: isAdmin,
      },
    });
    await refreshAll();
    setToast(`导出任务已创建：${task.currentStage}`);
  } catch (error) {
    setToast(`导出失败：${error.message}`);
  }
}

async function loadWorkAssets(workId) {
  try {
    const assets = await request(`/works/${workId}/assets`);
    state.modal = {
      title: "作品资源",
      body: assets.map((asset) => `
        <article class="card" style="box-shadow:none;margin-bottom:12px">
          <span class="pill">${escapeHtml(asset.assetType)}</span>
          <h4 class="card-title" style="margin-top:10px">${escapeHtml(asset.title)}</h4>
          <p class="card-subtitle">${escapeHtml(asset.summary)}</p>
          ${asset.url ? `<p><a class="btn secondary" href="${escapeAttr(asset.url)}" target="_blank" rel="noreferrer">打开资源</a></p>` : ""}
          ${asset.textContent ? `<p class="text-block">${escapeHtml(asset.textContent)}</p>` : ""}
        </article>
      `).join("") || empty("暂无资源"),
    };
    render();
  } catch (error) {
    setToast(`作品资源读取失败：${error.message}`);
  }
}

async function deleteWork(workId) {
  if (!window.confirm("确定删除这个作品吗？")) return;
  try {
    await request(`/works/${workId}`, { method: "DELETE" });
    await refreshAll();
    setToast("作品已删除");
  } catch (error) {
    setToast(`删除失败：${error.message}`);
  }
}

function filtered(items) {
  const query = state.search.trim().toLowerCase();
  return items.filter((item) => {
    if (state.activeFilter !== "ALL" && item.status && item.status !== state.activeFilter) return false;
    if (!query) return true;
    return JSON.stringify(item).toLowerCase().includes(query);
  });
}

function navCount(path) {
  if (path === "/web/projects") return state.projects.length;
  if (path === "/web/works") return state.works.length;
  if (path === "/web/orders") return state.orders.length;
  if (path === "/web/member") return state.user?.role === "admin" ? "A" : "M";
  if (path === "/web/ai") return state.aiStatus?.credentials?.dashscopeApiKeyConfigured ? "✓" : "!";
  return "";
}

function sceneIcon(sceneId) {
  if (sceneId.includes("comic")) return "漫";
  if (sceneId.includes("video")) return "影";
  if (sceneId.includes("honglou")) return "红";
  if (sceneId.includes("xiyou")) return "游";
  return "文";
}

function pipelineCopy(index) {
  return [
    "选择角色穿越、大纲成文、漫画或短视频方向。",
    "创建创作项目，保存标题、场景和输入内容。",
    "生成第一版故事草稿，帮助你确认内容方向。",
    "继续整理章节正文、漫画分格或视频分镜。",
    "短视频走预览任务，非视频直接形成文本/漫画/角色故事包。",
  ][index];
}

function empty(text) {
  return `<div class="empty">${escapeHtml(text)}</div>`;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
  return escapeHtml(value).replaceAll("`", "&#096;");
}

boot();
