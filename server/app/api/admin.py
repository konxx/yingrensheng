from fastapi import APIRouter, Form, HTTPException
from fastapi.responses import HTMLResponse

from app.services.container import service_container


router = APIRouter()


@router.get("/admin", response_class=HTMLResponse)
def admin_login_page() -> str:
    return """
    <html>
      <head><title>YingRenSheng Admin</title></head>
      <body style="font-family: Arial; margin: 40px;">
        <h1>YingRenSheng Admin</h1>
        <p>Default credentials: admin / admin</p>
        <form method="post" action="/admin/login">
          <div><input name="username" placeholder="username" value="admin" /></div>
          <div style="margin-top: 8px;"><input name="password" type="password" placeholder="password" value="admin" /></div>
          <div style="margin-top: 12px;"><button type="submit">Login</button></div>
        </form>
      </body>
    </html>
    """


@router.post("/admin/login", response_class=HTMLResponse)
def admin_login(username: str = Form(...), password: str = Form(...)) -> str:
    admin = service_container.user_repository.verify_admin(username=username, password=password)
    if admin is None:
        raise HTTPException(status_code=401, detail="INVALID_ADMIN_CREDENTIALS")
    users = service_container.user_repository.list_users()
    rows = "".join(
        f"<tr><td>{user.user_id}</td><td>{user.phone}</td><td>{user.nickname}</td></tr>"
        for user in users
    )
    return f"""
    <html>
      <head><title>YingRenSheng Admin Dashboard</title></head>
      <body style="font-family: Arial; margin: 40px;">
        <h1>Admin Dashboard</h1>
        <p>Logged in as {username}</p>
        <h2>User Accounts</h2>
        <table border="1" cellpadding="8" cellspacing="0">
          <tr><th>User ID</th><th>Phone</th><th>Nickname</th></tr>
          {rows}
        </table>
      </body>
    </html>
    """

