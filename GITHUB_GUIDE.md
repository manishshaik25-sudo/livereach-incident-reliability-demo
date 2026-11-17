# Publish to GitHub (Windows PowerShell)

1. Extract the ZIP and open the folder that contains `pom.xml` in VS Code.
2. Run the tests using the README instructions; **only claim passing tests if you see BUILD SUCCESS.**
3. Review `.gitignore`, `compose.yaml`, and all files before pushing. Never upload real credentials, tokens or customer data.
4. Create a **public empty** repository at https://github.com/new named `livereach-incident-reliability-demo` (do not auto-create README).
5. In VS Code terminal:

```powershell
git init
git add .
git status
git commit -m "Build retry-safe camera incident API"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/livereach-incident-reliability-demo.git
git push -u origin main
```

If `origin` already exists use `git remote set-url origin <your repo URL>` instead of `git remote add`.

6. Open the GitHub repository, verify README, source, tests and engineering note are visible; pin the repo in your GitHub profile.
7. Email only the verified repo URL. Describe it as **an independent LiveReach-inspired backend reliability demonstration**, not as a fix to LiveReach's code.

To make later edits:
```powershell
git add .
git commit -m "Improve incident reliability demo"
git push
```
