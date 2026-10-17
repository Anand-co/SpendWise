// SpendWise frontend. API base URL can be changed here if your backend uses another port.
const API_BASE = "http://localhost:8080";
const $ = (id) => document.getElementById(id);
const state = { token: localStorage.getItem("spendwise_token") || "", user: null, authMode: "login", page: "dashboard", transactions: [], budgets: [], goals: [], editingTransactionId: null };
const currency = (value) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(Number(value || 0));
const dateToday = () => new Date().toISOString().slice(0,10);
const currentMonth = () => new Date().toISOString().slice(0,7);
const monthStart = (month) => `${month}-01`;
const escapeHtml = (v) => String(v ?? "").replace(/[&<>"']/g, c => ({ "&":"&amp;", "<":"&lt;", ">":"&gt;", '"':"&quot;", "'":"&#39;" }[c]));
let toastTimer;

function toast(message, isError = false) {
  const node = $("toast"); node.textContent = message; node.className = `toast show${isError ? " error" : ""}`;
  clearTimeout(toastTimer); toastTimer = setTimeout(() => node.className = "toast", 3400);
}
async function api(path, options = {}) {
  const headers = { ...(options.body ? { "Content-Type": "application/json" } : {}), ...(state.token ? { Authorization: `Bearer ${state.token}` } : {}), ...(options.headers || {}) };
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const text = await response.text();
  let data = null;
  if (text) { try { data = JSON.parse(text); } catch { data = text; } }
  if (!response.ok) {
    const message = (data && typeof data === "object" && (data.message || data.error || data.detail)) || (typeof data === "string" ? data : `${response.status} ${response.statusText}`);
    throw new Error(message || `Request failed (${response.status})`);
  }
  return data;
}
function setAuthMode(mode) {
  state.authMode = mode;
  const register = mode === "register";
  $("authTitle").textContent = register ? "Create your account" : "Welcome back";
  $("authSubtitle").textContent = register ? "Start building a clearer picture of your money." : "Sign in to see your financial overview.";
  $("authSubmit").textContent = register ? "Create account" : "Sign in";
  $("nameWrap").classList.toggle("hidden", !register);
  $("authName").required = register;
  $("authPassword").autocomplete = register ? "new-password" : "current-password";
  $("authSwitchText").textContent = register ? "Already have an account?" : "New to SpendWise?";
  $("authSwitch").textContent = register ? "Sign in" : "Create account";
}
$("authSwitch").addEventListener("click", () => setAuthMode(state.authMode === "login" ? "register" : "login"));
$("authForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const email = $("authEmail").value.trim(), password = $("authPassword").value;
  const register = state.authMode === "register";
  const body = register ? { name: $("authName").value.trim(), email, password } : { email, password };
  try {
    const result = await api(register ? "/api/auth/register" : "/api/auth/login", { method: "POST", body: JSON.stringify(body) });
    const token = findToken(result);
    if (token) {
      state.token = token; localStorage.setItem("spendwise_token", token);
      state.user = result.user || result;
      showApp(); toast(register ? "Account created. Welcome to SpendWise!" : "Signed in successfully.");
      await loadAll();
    } else if (register) {
      toast("Registration succeeded. Please sign in.", false);
      setAuthMode("login");
    } else {
      toast("Login succeeded but no token was found in the response. Check your auth response format.", true);
    }
  } catch (error) { toast(error.message, true); }
});
function findToken(data) {
  if (!data || typeof data !== "object") return null;
  return data.token || data.accessToken || data.jwt || data.access_token || data?.data?.token || data?.data?.accessToken || null;
}
function showApp() {
  $("authView").classList.add("hidden"); $("appView").classList.remove("hidden");
  const email = state.user?.email || $("authEmail").value || "SpendWise user";
  $("userAvatar").textContent = (state.user?.name || email).trim().charAt(0).toUpperCase();
  $("todayLabel").textContent = new Date().toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
  $("monthName").textContent = new Date().toLocaleDateString("en-IN", { month: "long", year: "numeric" });
  $("txDate").value = dateToday(); $("budgetMonth").value = currentMonth();
  navigate(state.page);
}
function showAuth() { $("appView").classList.add("hidden"); $("authView").classList.remove("hidden"); }
$("logoutBtn").addEventListener("click", () => { state.token = ""; state.user = null; localStorage.removeItem("spendwise_token"); showAuth(); toast("Signed out."); });
function navigate(page) {
  state.page = page;
  document.querySelectorAll(".page").forEach(el => el.classList.add("hidden"));
  $(`${page}Page`).classList.remove("hidden");
  document.querySelectorAll(".nav-item[data-page]").forEach(el => el.classList.toggle("active", el.dataset.page === page));
  $("pageHeading").textContent = ({ dashboard: "Overview", transactions: "Transactions", budgets: "Budgets", goals: "Savings goals" })[page] || "Overview";
  $("sidebar").classList.remove("open");
}
document.querySelectorAll("[data-page]").forEach(btn => btn.addEventListener("click", () => navigate(btn.dataset.page)));
document.querySelectorAll("[data-goto]").forEach(btn => btn.addEventListener("click", () => navigate(btn.dataset.goto)));
$("menuBtn").addEventListener("click", () => $("sidebar").classList.toggle("open"));
$("quickAddBtn").addEventListener("click", openNewTransaction);
$("newTransactionBtn").addEventListener("click", openNewTransaction);
$("newBudgetBtn").addEventListener("click", () => { $("budgetForm").reset(); $("budgetMonth").value = currentMonth(); $("budgetDialog").showModal(); });
$("newGoalBtn").addEventListener("click", () => { $("goalForm").reset(); $("goalCurrent").value = "0"; $("goalDialog").showModal(); });
document.querySelectorAll("[data-close]").forEach(btn => btn.addEventListener("click", () => $(btn.dataset.close).close()));
function openNewTransaction() {
  state.editingTransactionId = null; $("transactionForm").reset(); $("txDate").value = dateToday(); $("txType").value = "EXPENSE"; $("transactionModalTitle").textContent = "Add transaction"; $("transactionDialog").showModal();
}
async function loadAll() {
  try {
    const results = await Promise.allSettled([
      api("/api/transactions"), api(`/api/transactions/summary?month=${currentMonth()}`), api("/api/budgets"), api("/api/savings-goals")
    ]);
    const [txResult, summaryResult, budgetResult, goalResult] = results;
    if (txResult.status === "fulfilled") state.transactions = normalizeList(txResult.value);
    else toast(`Transactions: ${txResult.reason.message}`, true);
    if (budgetResult.status === "fulfilled") state.budgets = normalizeList(budgetResult.value);
    else toast(`Budgets: ${budgetResult.reason.message}`, true);
    if (goalResult.status === "fulfilled") state.goals = normalizeList(goalResult.value);
    else toast(`Savings goals: ${goalResult.reason.message}`, true);
    renderTransactions(); renderBudgets(); renderGoals();
    renderSummary(summaryResult.status === "fulfilled" ? summaryResult.value : null);
    renderRecent(); $("budgetCount").textContent = state.budgets.length; $("goalCount").textContent = state.goals.length;
  } catch (error) { toast(error.message, true); }
}
function normalizeList(value) {
  if (Array.isArray(value)) return value;
  if (Array.isArray(value?.content)) return value.content;
  if (Array.isArray(value?.data)) return value.data;
  return [];
}
function renderSummary(summary) {
  let income = Number(summary?.totalIncome || 0), expenses = Number(summary?.totalExpenses || 0), balance = Number(summary?.balance ?? (income - expenses));
  if (!summary) {
    const month = currentMonth();
    const txs = state.transactions.filter(t => String(t.transactionDate || "").slice(0,7) === month);
    income = txs.filter(t => t.type === "INCOME").reduce((s,t) => s + Number(t.amount || 0), 0);
    expenses = txs.filter(t => t.type === "EXPENSE").reduce((s,t) => s + Number(t.amount || 0), 0);
    balance = income - expenses;
  }
  $("incomeValue").textContent = currency(income); $("expenseValue").textContent = currency(expenses); $("balanceValue").textContent = currency(balance);
  $("snapshotIncome").textContent = currency(income); $("snapshotExpense").textContent = currency(expenses);
  const max = Math.max(income, expenses, 1);
  $("incomeTrack").style.width = `${Math.max(3, income/max*100)}%`; $("expenseTrack").style.width = `${Math.max(3, expenses/max*100)}%`;
  $("snapshotNote").textContent = income + expenses === 0 ? "Add transactions to see your monthly snapshot." : balance >= 0 ? `You have ${currency(balance)} left after this month's expenses.` : `Expenses exceed income by ${currency(Math.abs(balance))}.`;
}
function renderRecent() {
  const items = [...state.transactions].sort((a,b) => String(b.transactionDate || "").localeCompare(String(a.transactionDate || ""))).slice(0,5);
  $("recentTransactions").innerHTML = items.length ? items.map(tx => transactionRow(tx)).join("") : `<div class="empty-state">No transactions yet. Add your first transaction to get started.</div>`;
}
function transactionRow(tx) {
  const income = tx.type === "INCOME";
  return `<div class="tx-row"><div class="tx-symbol ${income ? "income" : ""}">${income ? "↙" : "↗"}</div><div class="tx-details"><div class="tx-title">${escapeHtml(tx.title || "Transaction")}</div><div class="tx-meta">${escapeHtml(tx.category || "Other")} · ${escapeHtml(tx.transactionDate || "")}</div></div><div class="tx-amount ${income ? "positive" : "negative"}">${income ? "+" : "−"}${currency(tx.amount)}</div></div>`;
}
function renderTransactions() {
  const query = ($("transactionSearch").value || "").toLowerCase(), type = $("typeFilter").value;
  const items = [...state.transactions].filter(t => (!type || t.type === type) && `${t.title||""} ${t.category||""} ${t.description||""}`.toLowerCase().includes(query)).sort((a,b) => String(b.transactionDate||"").localeCompare(String(a.transactionDate||"")));
  $("transactionsTable").innerHTML = items.length ? items.map(t => `<tr><td><strong>${escapeHtml(t.title || "Transaction")}</strong><div class="tx-meta">${escapeHtml(t.description || "")}</div></td><td>${escapeHtml(t.category || "Other")}</td><td>${escapeHtml(t.transactionDate || "—")}</td><td><span class="type-pill ${t.type === "INCOME" ? "income" : "expense"}">${escapeHtml(t.type || "EXPENSE")}</span></td><td class="align-right ${t.type === "INCOME" ? "positive" : "negative"}">${t.type === "INCOME" ? "+" : "−"}${currency(t.amount)}</td><td><div class="row-actions"><button class="small-action" data-edit-tx="${t.id}">Edit</button><button class="small-action" data-delete-tx="${t.id}">Delete</button></div></td></tr>`).join("") : `<tr><td colspan="6" class="empty-state">No transactions found.</td></tr>`;
  document.querySelectorAll("[data-edit-tx]").forEach(b => b.addEventListener("click", () => editTransaction(b.dataset.editTx)));
  document.querySelectorAll("[data-delete-tx]").forEach(b => b.addEventListener("click", () => deleteTransaction(b.dataset.deleteTx)));
}
$("transactionSearch").addEventListener("input", renderTransactions); $("typeFilter").addEventListener("change", renderTransactions); $("refreshTransactions").addEventListener("click", loadAll);
async function editTransaction(id) {
  const tx = state.transactions.find(t => String(t.id) === String(id)); if (!tx) return;
  state.editingTransactionId = id;
  $("transactionModalTitle").textContent = "Edit transaction";
  $("txTitle").value = tx.title || ""; $("txAmount").value = tx.amount ?? ""; $("txType").value = tx.type || "EXPENSE"; $("txCategory").value = tx.category || ""; $("txDate").value = tx.transactionDate || dateToday(); $("txDescription").value = tx.description || "";
  $("transactionDialog").showModal();
}
async function deleteTransaction(id) {
  if (!confirm("Delete this transaction? This cannot be undone.")) return;
  try { await api(`/api/transactions/${id}`, { method: "DELETE" }); toast("Transaction deleted."); await loadAll(); }
  catch (e) { toast(e.message, true); }
}
$("transactionForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const body = { title: $("txTitle").value.trim(), amount: Number($("txAmount").value), type: $("txType").value, category: $("txCategory").value.trim(), transactionDate: $("txDate").value, description: $("txDescription").value.trim() || null };
  try {
    const editing = state.editingTransactionId != null;
    await api(editing ? `/api/transactions/${state.editingTransactionId}` : "/api/transactions", { method: editing ? "PUT" : "POST", body: JSON.stringify(body) });
    $("transactionDialog").close(); toast(editing ? "Transaction updated." : "Transaction added."); await loadAll();
  } catch (e) { toast(e.message, true); }
});
function renderBudgets() {
  $("budgetsGrid").innerHTML = state.budgets.length ? state.budgets.map(b => {
    const spent = state.transactions.filter(t => t.type === "EXPENSE" && String(t.category||"").toLowerCase() === String(b.category||"").toLowerCase() && String(t.transactionDate||"").slice(0,7) === String(b.monthStart||"").slice(0,7)).reduce((s,t) => s + Number(t.amount||0),0);
    const limit = Number(b.monthlyLimit||0), percent = limit ? Math.min(100, spent/limit*100) : 0;
    return `<article class="budget-card"><div class="card-top"><div class="category-icon">▤</div><button class="card-menu" title="Delete budget" data-delete-budget="${b.id}">⋯</button></div><div class="card-title">${escapeHtml(b.category || "Budget")}</div><div class="card-sub">${escapeHtml(String(b.monthStart||"").slice(0,7))}</div><div class="budget-amount">${currency(spent)} <span class="card-sub">spent</span></div><div class="card-sub">of ${currency(limit)} monthly limit</div><div class="budget-progress"><div class="progress-track"><span style="width:${percent}%;background:${percent>=100?"#e45b65":percent>=80?"#e9a23b":"#766de5"}"></span></div><div class="progress-meta"><span>${percent.toFixed(0)}% used</span><span>${currency(Math.max(0,limit-spent))} left</span></div></div></article>`;
  }).join("") : `<div class="panel empty-state">No budgets yet. Create a monthly category budget to track spending.</div>`;
  document.querySelectorAll("[data-delete-budget]").forEach(b => b.addEventListener("click", async () => { if (!confirm("Delete this budget?")) return; try { await api(`/api/budgets/${b.dataset.deleteBudget}`, {method:"DELETE"}); toast("Budget deleted."); await loadAll(); } catch(e){toast(e.message,true);} }));
}
$("budgetForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const body = { category: $("budgetCategory").value.trim(), monthlyLimit: Number($("budgetLimit").value), monthStart: monthStart($("budgetMonth").value) };
  try { await api("/api/budgets", {method:"POST",body:JSON.stringify(body)}); $("budgetDialog").close(); toast("Budget saved."); await loadAll(); }
  catch(e){toast(e.message,true);}
});
function renderGoals() {
  $("goalsGrid").innerHTML = state.goals.length ? state.goals.map(g => {
    const target = Number(g.targetAmount||0), saved = Number(g.currentAmount||0), percent = target ? Math.min(100, saved/target*100) : 0;
    return `<article class="goal-card"><div class="card-top"><div class="category-icon">◎</div><button class="card-menu" title="Delete goal" data-delete-goal="${g.id}">⋯</button></div><div class="card-title">${escapeHtml(g.name || "Savings goal")}</div><div class="card-sub">${escapeHtml(g.description || "Keep moving toward your target.")}</div><div class="goal-amounts"><span><strong>${currency(saved)}</strong><br><span class="card-sub">saved</span></span><span class="align-right"><strong>${currency(target)}</strong><br><span class="card-sub">target</span></span></div><div class="progress-track"><span style="width:${percent}%"></span></div><div class="progress-meta"><span>${percent.toFixed(0)}% complete</span><span>${currency(Math.max(0,target-saved))} to go</span></div><div class="goal-date">${g.deadline ? `Target date: ${escapeHtml(g.deadline)}` : "No target date set"}</div><button class="btn btn-light full" style="margin-top:15px" data-update-goal="${g.id}">Update savings</button></article>`;
  }).join("") : `<div class="panel empty-state">No savings goals yet. Create a goal and start tracking your progress.</div>`;
  document.querySelectorAll("[data-delete-goal]").forEach(b => b.addEventListener("click", async () => { if (!confirm("Delete this savings goal?")) return; try { await api(`/api/savings-goals/${b.dataset.deleteGoal}`, {method:"DELETE"}); toast("Goal deleted."); await loadAll(); } catch(e){toast(e.message,true);} }));
  document.querySelectorAll("[data-update-goal]").forEach(b => b.addEventListener("click", () => {
    const goal = state.goals.find(g => String(g.id) === String(b.dataset.updateGoal)); if (!goal) return;
    const value = prompt(`Current saved amount (₹):`, String(goal.currentAmount ?? 0)); if (value === null) return;
    const amount = Number(value); if (!Number.isFinite(amount) || amount < 0) return toast("Enter a valid non-negative amount.", true);
    updateGoal(goal, amount);
  }));
}
async function updateGoal(goal, currentAmount) {
  const body = { name: goal.name, targetAmount: Number(goal.targetAmount), currentAmount, deadline: goal.deadline || null, description: goal.description || null };
  try { await api(`/api/savings-goals/${goal.id}`, {method:"PUT",body:JSON.stringify(body)}); toast("Savings progress updated."); await loadAll(); }
  catch(e){toast(e.message,true);}
}
$("goalForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const body = { name: $("goalName").value.trim(), targetAmount: Number($("goalTarget").value), currentAmount: Number($("goalCurrent").value), deadline: $("goalDeadline").value || null, description: $("goalDescription").value.trim() || null };
  try { await api("/api/savings-goals", {method:"POST",body:JSON.stringify(body)}); $("goalDialog").close(); toast("Savings goal created."); await loadAll(); }
  catch(e){toast(e.message,true);}
});

(async function init() {
  setAuthMode("login");
  if (state.token) {
    try {
      // A stored token is accepted provisionally; API calls below will show an auth error if expired.
      showApp(); await loadAll();
    } catch (e) { state.token = ""; localStorage.removeItem("spendwise_token"); showAuth(); }
  } else showAuth();
})();
