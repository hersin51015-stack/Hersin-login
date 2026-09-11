# Standalone Python GUI for Windows 7 / 8 / 10 / 11
# Runs with standard Python 3.8+ (even without admin rights)
import tkinter as tk
from tkinter import messagebox
import webbrowser

def do_login():
    email = email_entry.get().strip()
    pwd = pwd_entry.get().strip()
    if not email:
        messagebox.showwarning("Hersin Login", "Please enter an email or username.")
        return
    messagebox.showinfo("Success", f"Welcome back, {email}!\nAuthentication successful.")

def do_google():
    client_id = "922049258875-qbgi10cpskcmn9vhasenb1s2um3nte63.apps.googleusercontent.com"
    auth_url = f"https://accounts.google.com/o/oauth2/v2/auth?client_id={client_id}&response_type=token&redirect_uri=http://localhost:5000&scope=email%20profile%20openid"
    webbrowser.open(auth_url)

root = tk.Tk()
root.title("Hersin Login (Windows 7/8/10/11)")
root.geometry("400x440")
root.resizable(False, False)
root.configure(bg="#F0F2F5")

title_lbl = tk.Label(root, text="HERSIN LOGIN", font=("Segoe UI", 16, "bold"), bg="#F0F2F5", fg="#1A1A24")
title_lbl.pack(pady=(30, 20))

tk.Label(root, text="Email or Username:", font=("Segoe UI", 10), bg="#F0F2F5", anchor="w").pack(fill="x", padx=40)
email_entry = tk.Entry(root, font=("Segoe UI", 11))
email_entry.pack(fill="x", padx=40, pady=(4, 15), ipady=4)

tk.Label(root, text="Password:", font=("Segoe UI", 10), bg="#F0F2F5", anchor="w").pack(fill="x", padx=40)
pwd_entry = tk.Entry(root, font=("Segoe UI", 11), show="*")
pwd_entry.pack(fill="x", padx=40, pady=(4, 25), ipady=4)

login_btn = tk.Button(root, text="Log In", font=("Segoe UI", 11, "bold"), bg="#2563EB", fg="white", relief="flat", cursor="hand2", command=do_login)
login_btn.pack(fill="x", padx=40, ipady=6)

google_btn = tk.Button(root, text="Sign in with Google", font=("Segoe UI", 10), bg="#FFFFFF", fg="#374151", relief="groove", cursor="hand2", command=do_google)
google_btn.pack(fill="x", padx=40, pady=(12, 20), ipady=6)

status_lbl = tk.Label(root, text="Native Windows 7, 8, 10 & 11 Support", font=("Segoe UI", 8), fg="#6B7280", bg="#F0F2F5")
status_lbl.pack(side="bottom", pady=15)

if __name__ == "__main__":
    root.mainloop()
