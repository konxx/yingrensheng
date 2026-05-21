from html import escape
import secrets

from fastapi import APIRouter, Form, HTTPException
from fastapi.responses import HTMLResponse
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.models import UserAccountModel
from app.db.session import SessionLocal
from app.services.container import service_container


router = APIRouter()

_ADMIN_SESSIONS: dict[str, str] = {}

ADMIN_ALL_ACCESS_RIGHTS = [
    "无限制使用 Lite / Pro / Max 全部会员权益",
    "不受导出次数、生成额度、并发与排队限制约束",
    "可访问管理中心的用户、订单、作品和系统配置能力",
    "可创建新的尊享管理员账号并重置账号密码",
    "可优先使用所有已接入与待接入的生成能力",
]


@router.get("/admin", response_class=HTMLResponse)
def admin_login_page() -> str:
    return _page(
        title="映人生管理中心",
        body=f"""
        <main class="auth-shell">
          <section class="login-card">
            <div class="status-pill"><span></span> 管理中心</div>
            <h1>映人生 <strong>管理中心</strong></h1>
            <p class="muted">用于管理账号、会员层级和尊享权益。</p>
            <form method="post" action="/admin/login" class="stack">
              <label>
                <span>管理员账号</span>
                <input name="username" placeholder="请输入管理员账号" autocomplete="username" />
              </label>
              <label>
                <span>管理员密码</span>
                <input name="password" type="password" placeholder="请输入管理员密码" autocomplete="current-password" />
              </label>
              <button type="submit" class="primary-button">登录管理中心</button>
            </form>
          </section>
        </main>
        """,
    )


@router.post("/admin/login", response_class=HTMLResponse)
def admin_login(username: str = Form(...), password: str = Form(...)) -> str:
    admin = service_container.user_repository.verify_admin(username=username, password=password)
    if admin is None:
        raise HTTPException(status_code=401, detail="INVALID_ADMIN_CREDENTIALS")

    token = secrets.token_urlsafe(32)
    _ADMIN_SESSIONS[token] = admin.username
    return _dashboard(admin_username=admin.username, token=token)


@router.post("/admin/users/create", response_class=HTMLResponse)
def admin_create_user(
    admin_token: str = Form(...),
    username: str = Form(...),
    email: str = Form(...),
    nickname: str = Form(...),
    password: str = Form(...),
) -> str:
    admin_username = _require_admin(admin_token)
    try:
        account = _create_account(
            username=username,
            email=email,
            nickname=nickname,
            password=password,
            role="user",
        )
    except ValueError as exc:
        return _dashboard(admin_username=admin_username, token=admin_token, notice=str(exc), notice_kind="error")
    return _dashboard(
        admin_username=admin_username,
        token=admin_token,
        notice=f"普通用户 {account.username} 已创建。",
    )


@router.post("/admin/admins/create", response_class=HTMLResponse)
def admin_create_system_admin(
    admin_token: str = Form(...),
    username: str = Form(...),
    email: str = Form(...),
    nickname: str = Form(...),
    password: str = Form(...),
) -> str:
    admin_username = _require_admin(admin_token)
    try:
        account = _create_account(
            username=username,
            email=email,
            nickname=nickname,
            password=password,
            role="admin",
        )
    except ValueError as exc:
        return _dashboard(admin_username=admin_username, token=admin_token, notice=str(exc), notice_kind="error")
    return _dashboard(
        admin_username=admin_username,
        token=admin_token,
        notice=f"尊享管理员 {account.username} 已创建，并已开放全部权益。",
    )


@router.post("/admin/users/password", response_class=HTMLResponse)
def admin_update_password(
    admin_token: str = Form(...),
    user_id: str = Form(...),
    new_password: str = Form(...),
) -> str:
    admin_username = _require_admin(admin_token)
    service_container.user_repository.update_password(user_id=user_id, new_password=new_password)
    return _dashboard(admin_username=admin_username, token=admin_token, notice=f"{user_id} 的密码已更新。")


@router.post("/admin/users/role", response_class=HTMLResponse)
def admin_update_user_role(
    admin_token: str = Form(...),
    user_id: str = Form(...),
    role: str = Form(...),
) -> str:
    admin_username = _require_admin(admin_token)
    try:
        account = _update_account_role(user_id=user_id, role=role)
    except ValueError as exc:
        return _dashboard(admin_username=admin_username, token=admin_token, notice=str(exc), notice_kind="error")
    return _dashboard(
        admin_username=admin_username,
        token=admin_token,
        notice=f"{account.user_id} 已调整为{_role_label(account.role)}。",
    )


def _dashboard(
    admin_username: str,
    token: str,
    notice: str | None = None,
    notice_kind: str = "success",
) -> str:
    app_users = _list_accounts(role="user")
    admin_users = _list_accounts(role="admin")
    plans = service_container.member_service.list_plans()
    notice_html = (
        f"""<div class="notice {escape(notice_kind)}">{escape(notice or "")}</div>"""
        if notice
        else ""
    )

    return _page(
        title="映人生管理中心",
        body=f"""
        <main class="dashboard">
          <section class="hero">
            <div>
                <div class="status-pill"><span></span> 管理员会话已验证</div>
              <h1>管理 <strong>控制台</strong></h1>
              <p class="muted">当前登录：<code>{escape(admin_username)}</code>。Lite / Pro / Max 为会员等级；尊享账号开放全部权益。</p>
            </div>
            <div class="orb" aria-hidden="true">
              <div class="ring ring-a"></div>
              <div class="ring ring-b"></div>
              <div class="core">尊享</div>
            </div>
          </section>

          {notice_html}

          <section class="stats-grid" aria-label="operations summary">
            {_stat_card("普通用户", str(len(app_users)), "应用账号")}
            {_stat_card("尊享管理员", str(len(admin_users)), "全部权益")}
            {_stat_card("展示会员等级", str(len(plans)), "Lite / Pro / Max")}
            {_stat_card("尊享权益", "全部", "不受额度限制")}
          </section>

          <section class="section-block">
            <div class="section-heading">
              <p class="eyebrow">会员权益台账</p>
              <h2>会员等级与系统权益</h2>
            </div>
            <div class="plan-grid">
              {_plan_cards(plans)}
              {_admin_plan_card()}
            </div>
          </section>

          <section class="two-column">
            <div class="panel">
              <div class="panel-title">
                <p class="eyebrow">创建账号</p>
                <h2>普通用户</h2>
              </div>
              <form method="post" action="/admin/users/create" class="stack">
                {_token_input(token)}
                {_account_inputs(prefix="user")}
                <button type="submit" class="primary-button">创建普通用户</button>
              </form>
            </div>

            <div class="panel admin-panel">
              <div class="panel-title">
                <p class="eyebrow">全部权益</p>
                <h2>尊享管理员</h2>
              </div>
              <p class="muted compact">尊享账号不受付费等级限制，可享用全部产品权益。</p>
              <form method="post" action="/admin/admins/create" class="stack">
                {_token_input(token)}
                {_account_inputs(prefix="admin")}
                <button type="submit" class="primary-button">创建尊享管理员</button>
              </form>
            </div>
          </section>

          <section class="two-column">
            <div class="panel">
              <div class="panel-title">
                <p class="eyebrow">管理员账号</p>
                <h2>尊享管理员</h2>
              </div>
              {_accounts_table(admin_users, show_access=True)}
            </div>

            <div class="panel">
              <div class="panel-title">
                <p class="eyebrow">账号级别</p>
                <h2>调整用户级别</h2>
              </div>
              <p class="muted compact">输入用户 ID 后可把普通用户提升为尊享账号，也可将管理员降回普通用户。</p>
              <form method="post" action="/admin/users/role" class="stack">
                {_token_input(token)}
                <label>
                  <span>用户 ID</span>
                  <input name="user_id" placeholder="请输入用户 ID" />
                </label>
                <label>
                  <span>目标级别</span>
                  <select name="role">
                    <option value="admin">尊享账号</option>
                    <option value="user">普通用户</option>
                  </select>
                </label>
                <button type="submit" class="primary-button">更新用户级别</button>
              </form>
            </div>
          </section>

          <section class="two-column">
            <div class="panel">
              <div class="panel-title">
                <p class="eyebrow">密码运维</p>
                <h2>重置密码</h2>
              </div>
              <form method="post" action="/admin/users/password" class="stack">
                {_token_input(token)}
                <label>
                  <span>用户 ID</span>
                  <input name="user_id" placeholder="请输入用户 ID" />
                </label>
                <label>
                  <span>新密码</span>
                  <input name="new_password" type="password" placeholder="请输入新密码" />
                </label>
                <button type="submit" class="outline-button">更新密码</button>
              </form>
            </div>

            <div class="panel">
              <div class="panel-title">
                <p class="eyebrow">级别说明</p>
                <h2>尊享权益开放</h2>
              </div>
              <ul>{''.join(f'<li>{escape(right)}</li>' for right in ADMIN_ALL_ACCESS_RIGHTS)}</ul>
            </div>
          </section>

          <section class="section-block">
            <div class="section-heading">
              <p class="eyebrow">用户账号</p>
              <h2>普通用户列表</h2>
            </div>
            <div class="panel table-panel">
              {_accounts_table(app_users)}
            </div>
          </section>
        </main>
        """,
    )


def _require_admin(token: str) -> str:
    admin_username = _ADMIN_SESSIONS.get(token)
    if not admin_username:
        raise HTTPException(status_code=401, detail="ADMIN_SESSION_EXPIRED")
    return admin_username


def _create_account(
    username: str,
    email: str,
    nickname: str,
    password: str,
    role: str,
) -> UserAccountModel:
    normalized_role = role.strip().lower()
    if normalized_role not in {"user", "admin"}:
        raise ValueError("Unsupported account role.")
    if not username.strip() or not email.strip() or not nickname.strip() or not password:
        raise ValueError("All account fields are required.")

    with SessionLocal() as session:
        existing = session.execute(
            select(UserAccountModel).where(
                (UserAccountModel.username == username.strip()) | (UserAccountModel.email == email.strip()),
            ),
        ).scalar_one_or_none()
        if existing is not None:
            raise ValueError("账号或邮箱已存在。")

        account = UserAccountModel(
            user_id=_next_account_id(session=session, role=normalized_role),
            username=username.strip(),
            email=email.strip(),
            nickname=nickname.strip(),
            password=password,
            avatar_url="",
            role=normalized_role,
        )
        session.add(account)
        session.commit()
        session.refresh(account)
        session.expunge(account)
        return account


def _update_account_role(user_id: str, role: str) -> UserAccountModel:
    normalized_role = role.strip().lower()
    if normalized_role not in {"user", "admin"}:
        raise ValueError("目标级别只能是普通用户或尊享账号。")
    if not user_id.strip():
        raise ValueError("用户 ID 不能为空。")

    with SessionLocal() as session:
        account = session.execute(
            select(UserAccountModel).where(UserAccountModel.user_id == user_id.strip()),
        ).scalar_one_or_none()
        if account is None:
            raise ValueError("未找到该用户。")
        account.role = normalized_role
        session.commit()
        session.refresh(account)
        session.expunge(account)
        return account


def _next_account_id(session: Session, role: str) -> str:
    prefix = "admin" if role == "admin" else "user"
    next_number = session.query(UserAccountModel).filter(UserAccountModel.user_id.like(f"{prefix}_%")).count() + 1
    while session.execute(
        select(UserAccountModel).where(UserAccountModel.user_id == f"{prefix}_{next_number:03d}"),
    ).scalar_one_or_none():
        next_number += 1
    return f"{prefix}_{next_number:03d}"


def _list_accounts(role: str) -> list[UserAccountModel]:
    with SessionLocal() as session:
        accounts = session.execute(
            select(UserAccountModel).where(UserAccountModel.role == role).order_by(UserAccountModel.id.desc()),
        ).scalars().all()
        for account in accounts:
            session.expunge(account)
        return list(accounts)


def _plan_cards(plans: list) -> str:
    if not plans:
        return '<div class="plan-card"><h3>暂无展示会员等级</h3><p class="muted">请先初始化数据库以写入 Lite / Pro / Max。</p></div>'
    return "".join(
        f"""
        <article class="plan-card">
          <div class="plan-topline">
            <span class="tier-badge readonly">仅展示</span>
            {_badge(plan.badge)}
          </div>
          <h3>{escape(plan.name)}</h3>
          <p class="price">¥{plan.monthly_price}<small>/月</small></p>
          <p class="muted">{escape(plan.summary)}</p>
          <ul>{''.join(f'<li>{escape(feature)}</li>' for feature in plan.features)}</ul>
        </article>
        """
        for plan in plans
    )


def _admin_plan_card() -> str:
    return f"""
    <article class="plan-card plan-card-admin">
      <div class="plan-topline">
        <span class="tier-badge live">全部权益</span>
        <span class="tier-badge gold">尊享</span>
      </div>
      <h3>尊享权益</h3>
      <p class="price">全部<small>开放</small></p>
      <p class="muted">尊享账号专属权益，可覆盖全部创作与导出能力。</p>
      <ul>{''.join(f'<li>{escape(right)}</li>' for right in ADMIN_ALL_ACCESS_RIGHTS)}</ul>
    </article>
    """


def _accounts_table(accounts: list[UserAccountModel], show_access: bool = False) -> str:
    if not accounts:
        return '<p class="empty">暂无账号。</p>'
    rows = "".join(
        f"""
        <tr>
          <td><code>{escape(account.user_id)}</code></td>
          <td>{escape(account.username)}</td>
          <td>{escape(account.email)}</td>
          <td>{escape(account.nickname)}</td>
          <td><span class="role-chip {escape(account.role)}">{escape(_role_label(account.role))}</span></td>
          {f'<td><span class="access-chip">全部权益</span></td>' if show_access else ''}
        </tr>
        """
        for account in accounts
    )
    access_header = "<th>权益</th>" if show_access else ""
    return f"""
    <div class="table-wrap">
      <table>
        <thead>
          <tr><th>用户 ID</th><th>账号</th><th>邮箱</th><th>昵称</th><th>级别</th>{access_header}</tr>
        </thead>
        <tbody>{rows}</tbody>
      </table>
    </div>
    """


def _account_inputs(prefix: str) -> str:
    safe_prefix = escape(prefix)
    is_admin = prefix == "admin"
    username_placeholder = "请输入账号"
    email_placeholder = "请输入邮箱"
    nickname_placeholder = "尊享管理员" if is_admin else "普通用户"
    return f"""
    <label>
      <span>账号</span>
      <input name="username" placeholder="{escape(username_placeholder)}" autocomplete="off" />
    </label>
    <label>
      <span>邮箱</span>
      <input name="email" placeholder="{escape(email_placeholder)}" autocomplete="off" />
    </label>
    <label>
      <span>昵称</span>
      <input name="nickname" placeholder="{escape(nickname_placeholder)}" autocomplete="off" />
    </label>
    <label>
      <span>密码</span>
      <input name="password" type="password" placeholder="请输入密码" autocomplete="new-password" />
    </label>
    """


def _token_input(token: str) -> str:
    return f'<input type="hidden" name="admin_token" value="{escape(token)}" />'


def _stat_card(label: str, value: str, caption: str) -> str:
    return f"""
    <article class="stat-card">
      <span>{escape(label)}</span>
      <strong>{escape(value)}</strong>
      <small>{escape(caption)}</small>
    </article>
    """


def _badge(value: str | None) -> str:
    return f'<span class="tier-badge">{escape(value)}</span>' if value else ""


def _role_label(role: str) -> str:
    return {
        "admin": "尊享账号",
        "user": "普通用户",
    }.get(role, role)


def _page(title: str, body: str) -> str:
    return f"""
    <!doctype html>
    <html lang="zh-CN">
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>{escape(title)}</title>
        <style>{ADMIN_CSS}</style>
      </head>
      <body>
        <div class="void-grid" aria-hidden="true"></div>
        <div class="ambient ambient-a" aria-hidden="true"></div>
        <div class="ambient ambient-b" aria-hidden="true"></div>
        {body}
      </body>
    </html>
    """


ADMIN_CSS = """
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&family=Space+Grotesk:wght@500;600;700&display=swap');

:root {
  color-scheme: dark;
  --bg: #030304;
  --surface: rgba(15, 17, 21, 0.86);
  --surface-strong: #0f1115;
  --text: #ffffff;
  --muted: #94a3b8;
  --border: rgba(255, 255, 255, 0.1);
  --orange: #f7931a;
  --burnt: #ea580c;
  --gold: #ffd600;
  --danger: #fb7185;
}

* {
  box-sizing: border-box;
}

html {
  min-height: 100%;
  background: var(--bg);
}

body {
  min-height: 100vh;
  margin: 0;
  color: var(--text);
  background:
    radial-gradient(circle at 12% 8%, rgba(247, 147, 26, 0.16), transparent 26rem),
    radial-gradient(circle at 88% 12%, rgba(255, 214, 0, 0.1), transparent 24rem),
    var(--bg);
  font-family: Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

.void-grid {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background-size: 52px 52px;
  background-image:
    linear-gradient(to right, rgba(30, 41, 59, 0.42) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(30, 41, 59, 0.42) 1px, transparent 1px);
  mask-image: radial-gradient(circle at center, black 35%, transparent 76%);
}

.ambient {
  position: fixed;
  z-index: 0;
  width: 28rem;
  height: 28rem;
  border-radius: 999px;
  filter: blur(120px);
  opacity: 0.14;
  pointer-events: none;
}

.ambient-a {
  left: -7rem;
  top: 12rem;
  background: var(--orange);
}

.ambient-b {
  right: -8rem;
  bottom: 4rem;
  background: var(--gold);
}

a {
  color: var(--orange);
}

code {
  color: var(--gold);
  font-family: "JetBrains Mono", Consolas, monospace;
}

.auth-shell,
.dashboard {
  position: relative;
  z-index: 1;
}

.auth-shell {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 28px;
}

.login-card,
.panel,
.plan-card,
.stat-card {
  border: 1px solid var(--border);
  background: var(--surface);
  box-shadow: 0 0 50px -18px rgba(247, 147, 26, 0.32);
  backdrop-filter: blur(18px);
}

.login-card {
  width: min(100%, 460px);
  padding: 34px;
  border-radius: 24px;
}

.dashboard {
  width: min(1440px, calc(100% - 32px));
  margin: 0 auto;
  padding: 32px 0 64px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  align-items: center;
  gap: 28px;
  min-height: 260px;
  padding: 42px;
  border: 1px solid var(--border);
  border-radius: 28px;
  background: linear-gradient(145deg, rgba(15, 17, 21, 0.94), rgba(3, 3, 4, 0.74));
  overflow: hidden;
}

h1,
h2,
h3 {
  margin: 0;
  font-family: "Space Grotesk", Inter, sans-serif;
  letter-spacing: 0;
}

h1 {
  max-width: 760px;
  font-size: clamp(42px, 8vw, 86px);
  line-height: 0.94;
}

h1 strong,
h2 strong {
  color: transparent;
  background: linear-gradient(90deg, var(--orange), var(--gold));
  background-clip: text;
  -webkit-background-clip: text;
}

h2 {
  font-size: clamp(24px, 3vw, 38px);
}

h3 {
  font-size: 24px;
}

.muted {
  color: var(--muted);
  line-height: 1.7;
}

.compact {
  margin-top: 0;
}

.hint {
  margin: 18px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.status-pill,
.tier-badge,
.role-chip,
.access-chip {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  min-height: 28px;
  border: 1px solid rgba(247, 147, 26, 0.35);
  border-radius: 999px;
  padding: 6px 10px;
  color: var(--orange);
  background: rgba(247, 147, 26, 0.1);
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.status-pill {
  margin-bottom: 18px;
}

.status-pill span {
  position: relative;
  width: 8px;
  height: 8px;
  margin-right: 8px;
  border-radius: 999px;
  background: var(--gold);
  box-shadow: 0 0 18px rgba(255, 214, 0, 0.8);
}

.stack {
  display: grid;
  gap: 16px;
}

label span {
  display: block;
  margin-bottom: 7px;
  color: var(--muted);
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 12px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

input,
select {
  width: 100%;
  height: 48px;
  border: 0;
  border-bottom: 2px solid rgba(255, 255, 255, 0.16);
  border-radius: 10px 10px 0 0;
  outline: none;
  padding: 0 14px;
  color: var(--text);
  background: rgba(0, 0, 0, 0.45);
  font: 500 14px Inter, sans-serif;
  transition: border-color 160ms ease, box-shadow 160ms ease, background 160ms ease;
}

input:focus,
select:focus {
  border-color: var(--orange);
  background: rgba(0, 0, 0, 0.62);
  box-shadow: 0 12px 26px -16px rgba(247, 147, 26, 0.8);
}

select {
  appearance: none;
}

.primary-button,
.outline-button {
  min-height: 46px;
  border-radius: 999px;
  padding: 0 18px;
  cursor: pointer;
  font: 700 12px "JetBrains Mono", Consolas, monospace;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  transition: transform 200ms ease, box-shadow 200ms ease, border-color 200ms ease;
}

.primary-button {
  border: 0;
  color: #fff;
  background: linear-gradient(90deg, var(--burnt), var(--orange));
  box-shadow: 0 0 26px -8px rgba(247, 147, 26, 0.75);
}

.outline-button {
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #fff;
  background: transparent;
}

.primary-button:hover,
.outline-button:hover {
  transform: translateY(-1px);
  border-color: rgba(247, 147, 26, 0.65);
  box-shadow: 0 0 32px -10px rgba(247, 147, 26, 0.75);
}

.orb {
  position: relative;
  display: grid;
  place-items: center;
  width: 190px;
  height: 190px;
  margin-left: auto;
}

.ring {
  position: absolute;
  border-radius: 999px;
  border: 1px solid rgba(247, 147, 26, 0.42);
}

.ring-a {
  inset: 4px 22px;
  transform: rotate(26deg);
  box-shadow: 0 0 32px -12px rgba(247, 147, 26, 0.8);
}

.ring-b {
  inset: 26px 0;
  transform: rotate(-38deg);
  border-color: rgba(255, 214, 0, 0.38);
}

.core {
  display: grid;
  place-items: center;
  width: 106px;
  height: 106px;
  border: 1px solid rgba(255, 214, 0, 0.35);
  border-radius: 999px;
  color: var(--gold);
  background: radial-gradient(circle, rgba(255, 214, 0, 0.22), rgba(247, 147, 26, 0.08) 62%, rgba(0, 0, 0, 0.32));
  box-shadow: 0 0 44px -10px rgba(255, 214, 0, 0.52);
  font: 700 22px "JetBrains Mono", Consolas, monospace;
}

.notice {
  margin: 20px 0;
  padding: 14px 18px;
  border: 1px solid rgba(247, 147, 26, 0.35);
  border-radius: 18px;
  color: #fff;
  background: rgba(247, 147, 26, 0.12);
}

.notice.error {
  border-color: rgba(251, 113, 133, 0.45);
  background: rgba(251, 113, 133, 0.12);
}

.stats-grid,
.plan-grid,
.two-column {
  display: grid;
  gap: 18px;
}

.stats-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 20px;
}

.stat-card {
  padding: 22px;
  border-radius: 20px;
}

.stat-card span,
.stat-card small,
.eyebrow {
  color: var(--muted);
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.stat-card strong {
  display: block;
  margin: 10px 0 4px;
  font: 700 34px "Space Grotesk", Inter, sans-serif;
}

.section-block {
  margin-top: 36px;
}

.section-heading,
.panel-title {
  margin-bottom: 16px;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--orange);
}

.plan-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.plan-card {
  position: relative;
  min-height: 360px;
  padding: 24px;
  border-radius: 22px;
  overflow: hidden;
}

.plan-card::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background: linear-gradient(135deg, rgba(247, 147, 26, 0.15), transparent 32%);
  opacity: 0;
  transition: opacity 200ms ease;
}

.plan-card:hover::before,
.plan-card-admin::before {
  opacity: 1;
}

.plan-card-admin {
  border-color: rgba(247, 147, 26, 0.78);
  transform: translateY(-6px);
  box-shadow: 0 0 44px -12px rgba(247, 147, 26, 0.58);
}

.plan-topline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 34px;
  margin-bottom: 14px;
}

.tier-badge.readonly {
  color: var(--muted);
  border-color: rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
}

.tier-badge.live,
.role-chip.admin,
.access-chip {
  color: var(--gold);
  border-color: rgba(255, 214, 0, 0.42);
  background: rgba(255, 214, 0, 0.1);
}

.tier-badge.gold {
  color: #fff;
}

.price {
  margin: 16px 0 6px;
  color: #fff;
  font: 700 34px "Space Grotesk", Inter, sans-serif;
}

.price small {
  margin-left: 6px;
  color: var(--muted);
  font: 500 13px Inter, sans-serif;
}

ul {
  display: grid;
  gap: 10px;
  margin: 20px 0 0;
  padding: 0;
  list-style: none;
}

li {
  position: relative;
  padding-left: 18px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.45;
}

li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0.68em;
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--orange), var(--gold));
  box-shadow: 0 0 12px rgba(247, 147, 26, 0.7);
}

.two-column {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 36px;
}

.panel {
  padding: 24px;
  border-radius: 22px;
}

.admin-panel {
  border-color: rgba(247, 147, 26, 0.42);
}

.table-panel {
  padding: 0;
  overflow: hidden;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 720px;
}

th,
td {
  padding: 16px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  text-align: left;
  vertical-align: top;
}

th {
  color: var(--muted);
  background: rgba(255, 255, 255, 0.035);
  font: 600 11px "JetBrains Mono", Consolas, monospace;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

td {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.role-chip.user {
  color: var(--orange);
}

.empty {
  margin: 0;
  color: var(--muted);
}

@media (max-width: 1080px) {
  .hero,
  .two-column {
    grid-template-columns: 1fr;
  }

  .orb {
    margin: 0;
  }

  .stats-grid,
  .plan-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .plan-card-admin {
    transform: none;
  }
}

@media (max-width: 680px) {
  .dashboard {
    width: min(100% - 20px, 1440px);
    padding-top: 10px;
  }

  .hero,
  .login-card,
  .panel,
  .plan-card {
    padding: 22px;
    border-radius: 18px;
  }

  .stats-grid,
  .plan-grid {
    grid-template-columns: 1fr;
  }
}
"""
