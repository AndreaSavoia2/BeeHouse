import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  ArrowDownCircle,
  ChevronLeft,
  ChevronRight,
  ArrowUpCircle,
  CalendarDays,
  Check,
  Home,
  KeyRound,
  LogOut,
  Menu,
  Pencil,
  Plus,
  RefreshCw,
  Settings,
  ShieldCheck,
  Tags,
  Trash2,
  UserCog,
  UserPlus,
  Users,
  WalletCards,
  X
} from "lucide-react";
import { AuthPanel } from "./components/AuthPanel";
import {
  AddTransactionPayload,
  api,
  AuthResponse,
  Category,
  Instalment,
  InstalmentPeriod,
  RegistrationPayload,
  Transaction,
  UserAccount
} from "./lib/api";

const STORAGE_KEY = "beehouse.auth";
type AdminSection = "dashboard" | "categories" | "settings";
type UserSection = "dashboard" | "transactions" | "categories" | "profile";
const PASSWORD_PATTERN = /^(?=.*\p{Lu})(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).{8,}$/u;
const PASSWORD_RULE_MESSAGE = "La nuova password deve contenere almeno 8 caratteri, una lettera maiuscola, un numero e un carattere speciale.";

function currency(value: number | undefined) {
  return new Intl.NumberFormat("it-IT", { style: "currency", currency: "EUR" }).format(value ?? 0);
}

function monthName(month: number) {
  return new Date(2026, month - 1, 1).toLocaleDateString("it-IT", { month: "long" });
}

function formatDisplayDate(value: string | number[]) {
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day] = value;
    return new Intl.DateTimeFormat("it-IT").format(new Date(year, month - 1, day));
  }

  const textValue = String(value);
  if (/^\d{4}-\d{2}-\d{2}$/.test(textValue)) {
    const [year, month, day] = textValue.split("-").map(Number);
    return new Intl.DateTimeFormat("it-IT").format(new Date(year, month - 1, day));
  }

  return textValue;
}

function formatDate(year: number, month: number, day: number) {
  return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

function periodDateRange(year: number, month: number) {
  const lastDay = new Date(year, month, 0).getDate();
  return {
    min: formatDate(year, month, 1),
    max: formatDate(year, month, lastDay)
  };
}

function preferredTransactionDate(year: number, month: number) {
  const todayValue = new Date().toISOString().slice(0, 10);
  const range = periodDateRange(year, month);
  return todayValue >= range.min && todayValue <= range.max ? todayValue : range.min;
}

export function App() {
  const [auth, setAuth] = useState<AuthResponse | null>(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  });
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [dashboardTransactions, setDashboardTransactions] = useState<Transaction[]>([]);
  const [dashboardTransactionCount, setDashboardTransactionCount] = useState(0);
  const [transactionPage, setTransactionPage] = useState({ currentPage: 0, totalPages: 0, totalElements: 0 });
  const [categories, setCategories] = useState<Category[]>([]);
  const [users, setUsers] = useState<UserAccount[]>([]);
  const [userPage, setUserPage] = useState({ currentPage: 0, totalPages: 0, totalElements: 0 });
  const [instalment, setInstalment] = useState<Instalment | null>(null);
  const [instalmentPeriods, setInstalmentPeriods] = useState<InstalmentPeriod[]>([]);
  const [arePeriodsLoaded, setArePeriodsLoaded] = useState(false);
  const [message, setMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const [transactionForm, setTransactionForm] = useState<AddTransactionPayload>({
    title: "",
    description: "",
    amount: 0,
    date: now.toISOString().slice(0, 10),
    transactionType: "REDUCE",
    category: ""
  });
  const [categoryName, setCategoryName] = useState("");
  const [categoryEdit, setCategoryEdit] = useState({ id: 0, oldName: "", newName: "" });
  const [registrationForm, setRegistrationForm] = useState<RegistrationPayload>({
    name: "",
    lastname: "",
    email: ""
  });
  const [templateCost, setTemplateCost] = useState("");
  const [profileForm, setProfileForm] = useState({ username: "", oldPassword: "", newPassword: "" });
  const [adminSection, setAdminSection] = useState<AdminSection>("dashboard");
  const [userSection, setUserSection] = useState<UserSection>("dashboard");
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [transactionsPageNumber, setTransactionsPageNumber] = useState(0);
  const [transactionsPageSize, setTransactionsPageSize] = useState(10);
  const [usersPageNumber, setUsersPageNumber] = useState(0);
  const [usersPageSize, setUsersPageSize] = useState(10);
  const isAdmin = auth?.role === "ADMINISTRATOR";

  const periodYears = useMemo(
    () => Array.from(new Set(instalmentPeriods.map((period) => period.year))).sort((a, b) => b - a),
    [instalmentPeriods]
  );
  const selectedYearMonths = useMemo(
    () =>
      instalmentPeriods
        .filter((period) => period.year === year)
        .map((period) => period.month)
        .sort((a, b) => b - a),
    [instalmentPeriods, year]
  );
  const selectedPeriodDateRange = useMemo(() => periodDateRange(year, month), [year, month]);

  const totals = useMemo(() => {
    const source = isAdmin || userSection !== "dashboard" ? transactions : dashboardTransactions;
    return source.reduce(
      (acc, transaction) => {
        const amount = Number(transaction.amount);
        if (transaction.transactionType === "ADD") {
          acc.income += amount;
        } else {
          acc.expense += amount;
        }
        return acc;
      },
      { income: 0, expense: 0 }
    );
  }, [dashboardTransactions, isAdmin, transactions, userSection]);

  const adminTitle =
    adminSection === "dashboard"
      ? "Dashboard amministratore"
      : adminSection === "categories"
        ? "Categorie"
        : "Impostazioni";
  const userTitle =
    userSection === "dashboard"
      ? `Dashboard di ${monthName(month)} ${year}`
      : userSection === "transactions"
        ? "Transazioni"
        : userSection === "categories"
          ? "Categorie"
          : "Profilo";

  async function loadData() {
    if (!auth || !arePeriodsLoaded) return;
    setIsLoading(true);
    setMessage("");

    try {
      if (!instalmentPeriods.length) {
        const categoryList = await api.getCategories(auth.jwt);
        setCategories(Array.isArray(categoryList) ? categoryList : []);
        setInstalment(null);
        setTransactions([]);
        setDashboardTransactions([]);
        setDashboardTransactionCount(0);
        setMessage("Nessuna rata disponibile.");

        if (auth.role === "ADMINISTRATOR") {
          const pagedUsers = await api.getUsers(auth.jwt, usersPageNumber, usersPageSize);
          setUsers(pagedUsers.content ?? []);
          setUserPage({
            currentPage: pagedUsers.currentPage ?? usersPageNumber,
            totalPages: pagedUsers.totalPages ?? 0,
            totalElements: pagedUsers.totalElements ?? 0
          });
        }
        return;
      }

      const [categoryList, currentInstalment] = await Promise.all([
        api.getCategories(auth.jwt),
        api.getInstalment(year, month, auth.jwt)
      ]);
      setCategories(Array.isArray(categoryList) ? categoryList : []);
      setInstalment(currentInstalment);

      if (auth.role === "USER") {
        const [dashboardPage, pagedTransactions] = await Promise.all([
          api.getTransactions(year, month, auth.jwt, 0, 1000),
          api.getTransactions(year, month, auth.jwt, transactionsPageNumber, transactionsPageSize)
        ]);
        setDashboardTransactions(dashboardPage.content ?? dashboardPage.transactions ?? []);
        setDashboardTransactionCount(dashboardPage.totalElements ?? 0);
        setTransactions(pagedTransactions.content ?? pagedTransactions.transactions ?? []);
        setTransactionPage({
          currentPage: pagedTransactions.currentPage ?? transactionsPageNumber,
          totalPages: pagedTransactions.totalPages ?? 0,
          totalElements: pagedTransactions.totalElements ?? 0
        });
        setUsers([]);
      } else {
        const pagedUsers = await api.getUsers(auth.jwt, usersPageNumber, usersPageSize);
        setUsers(pagedUsers.content ?? []);
        setUserPage({
          currentPage: pagedUsers.currentPage ?? usersPageNumber,
          totalPages: pagedUsers.totalPages ?? 0,
          totalElements: pagedUsers.totalElements ?? 0
        });
        setTransactions([]);
        setDashboardTransactions([]);
        setDashboardTransactionCount(0);
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Impossibile caricare i dati");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadData();
  }, [
    auth,
    year,
    month,
    arePeriodsLoaded,
    instalmentPeriods.length,
    transactionsPageNumber,
    transactionsPageSize,
    usersPageNumber,
    usersPageSize
  ]);

  useEffect(() => {
    setTransactionForm((current) => {
      if (current.date >= selectedPeriodDateRange.min && current.date <= selectedPeriodDateRange.max) {
        return current;
      }

      return {
        ...current,
        date: preferredTransactionDate(year, month)
      };
    });
  }, [selectedPeriodDateRange.min, selectedPeriodDateRange.max, year, month]);

  useEffect(() => {
    if (!auth) {
      setInstalmentPeriods([]);
      setArePeriodsLoaded(false);
      return;
    }

    async function loadPeriods() {
      if (!auth) return;
      setArePeriodsLoaded(false);

      try {
        const periods = await api.getInstalmentPeriods(auth.jwt);
        const uniquePeriods = Array.from(
          new Map(
            (Array.isArray(periods) ? periods : [])
              .filter((period) => Number.isInteger(period.year) && Number.isInteger(period.month))
              .map((period) => [`${period.year}-${period.month}`, period])
          ).values()
        ).sort((a, b) => b.year - a.year || b.month - a.month);

        setInstalmentPeriods(uniquePeriods);

        if (uniquePeriods.length && !uniquePeriods.some((period) => period.year === year && period.month === month)) {
          setYear(uniquePeriods[0].year);
          setMonth(uniquePeriods[0].month);
          setTransactionsPageNumber(0);
        }
      } catch (error) {
        setInstalmentPeriods([]);
        setMessage(error instanceof Error ? error.message : "Impossibile caricare i periodi disponibili");
      } finally {
        setArePeriodsLoaded(true);
      }
    }

    void loadPeriods();
  }, [auth]);

  function handleLogin(nextAuth: AuthResponse) {
    setAuth(nextAuth);
    setProfileForm((current) => ({ ...current, username: nextAuth.username }));
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextAuth));
  }

  function logout() {
    setAuth(null);
    setInstalmentPeriods([]);
    setArePeriodsLoaded(false);
    setIsMobileMenuOpen(false);
    localStorage.removeItem(STORAGE_KEY);
  }

  function selectAdminSection(section: AdminSection) {
    setAdminSection(section);
    setIsMobileMenuOpen(false);
  }

  function selectUserSection(section: UserSection) {
    setUserSection(section);
    setIsMobileMenuOpen(false);
  }

  async function handleAddTransaction(event: FormEvent) {
    event.preventDefault();
    if (!auth) return;

    if (transactionForm.date < selectedPeriodDateRange.min || transactionForm.date > selectedPeriodDateRange.max) {
      setMessage(`La data deve essere compresa tra ${selectedPeriodDateRange.min} e ${selectedPeriodDateRange.max}.`);
      return;
    }

    try {
      await api.addTransaction(
        {
          ...transactionForm,
          amount: Number(transactionForm.amount),
          category: transactionForm.category || undefined
        },
        auth.jwt
      );
      setTransactionForm({
        title: "",
        description: "",
        amount: 0,
        date: preferredTransactionDate(year, month),
        transactionType: "REDUCE",
        category: ""
      });
      setMessage("Transazione aggiunta.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Transazione non salvata");
    }
  }

  async function handleDeleteTransaction(id: number) {
    if (!auth) return;
    try {
      await api.deleteTransaction(id, auth.jwt);
      setMessage("Transazione eliminata.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Transazione non eliminata");
    }
  }

  async function handleCreateCategory(event: FormEvent) {
    event.preventDefault();
    if (!auth || !categoryName.trim()) return;
    try {
      await api.createCategory(categoryName.trim(), auth.jwt);
      setCategoryName("");
      setMessage("Categoria creata.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Categoria non salvata");
    }
  }

  async function handleUpdateCategory(event: FormEvent) {
    event.preventDefault();
    if (!auth || !categoryEdit.oldName || !categoryEdit.newName.trim()) return;
    try {
      await api.updateCategory(categoryEdit.oldName, categoryEdit.newName.trim(), auth.jwt);
      setCategoryEdit({ id: 0, oldName: "", newName: "" });
      setMessage("Categoria aggiornata.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Categoria non aggiornata");
    }
  }

  async function handleDeleteCategory(id: number) {
    if (!auth) return;
    try {
      await api.deleteCategory(id, auth.jwt);
      setMessage("Categoria eliminata.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Categoria non eliminata");
    }
  }

  async function handleRegisterUser(event: FormEvent) {
    event.preventDefault();
    if (!auth) return;
    const submittedForm = registrationForm;
    setRegistrationForm({ name: "", lastname: "", email: "" });
    setMessage("Registrazione utente in corso...");

    try {
      await api.registerUser(submittedForm, auth.jwt);
      setUsersPageNumber(0);
      setMessage("Utente registrato. Username e password temporanea sono stati inviati via email.");
      await loadData();
    } catch (error) {
      setRegistrationForm(submittedForm);
      setMessage(error instanceof Error ? error.message : "Utente non registrato");
    }
  }

  async function handleDisableUser(user: UserAccount) {
    if (!auth) return;
    const confirmed = window.confirm(`Vuoi davvero cancellare l'utente ${user.name} ${user.lastname} (${user.username})?`);
    if (!confirmed) return;

    try {
      await api.disableUser(user.id, auth.jwt);
      setMessage("Utente cancellato.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Utente non cancellato");
    }
  }

  async function handleUpdateCost(event: FormEvent) {
    event.preventDefault();
    if (!auth || !templateCost) return;
    try {
      await api.updateInstalmentCost(Number(templateCost), auth.jwt);
      setTemplateCost("");
      setMessage("Costo rata aggiornato.");
      await loadData();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Costo rata non aggiornato");
    }
  }

  async function handleProfile(event: FormEvent) {
    event.preventDefault();
    if (!auth) return;

    try {
      let nextAuth = auth;
      if (profileForm.username && profileForm.username !== auth.username) {
        const jwt = await api.changeUsername(profileForm.username, auth.jwt);
        nextAuth = { ...auth, username: profileForm.username, jwt };
        setAuth(nextAuth);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(nextAuth));
      }
      if (profileForm.oldPassword && profileForm.newPassword) {
        if (!PASSWORD_PATTERN.test(profileForm.newPassword)) {
          setMessage(PASSWORD_RULE_MESSAGE);
          return;
        }
        await api.changePassword(profileForm.oldPassword, profileForm.newPassword, nextAuth.jwt);
      }
      setProfileForm((current) => ({ ...current, oldPassword: "", newPassword: "" }));
      setMessage("Profilo aggiornato.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Profilo non aggiornato");
    }
  }

  if (!auth) {
    return <AuthPanel onLogin={handleLogin} />;
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="mobile-menu-bar">
          <div className="logo-row">
            <span className="logo-icon"><Home size={24} /></span>
            <div>
              <strong>BeeHouse</strong>
              <small>{isAdmin ? "Amministratore" : "Utente"}</small>
            </div>
          </div>

          <button
            className="icon-button menu-toggle"
            onClick={() => setIsMobileMenuOpen((current) => !current)}
            aria-expanded={isMobileMenuOpen}
            aria-label={isMobileMenuOpen ? "Chiudi menu" : "Apri menu"}
          >
            {isMobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>

        <nav className={isMobileMenuOpen ? "open" : ""}>
          {isAdmin ? (
            <>
              <button className={adminSection === "dashboard" ? "active" : ""} onClick={() => selectAdminSection("dashboard")}>
                <WalletCards size={18} /> Dashboard
              </button>
              <button className={adminSection === "categories" ? "active" : ""} onClick={() => selectAdminSection("categories")}>
                <Tags size={18} /> Categorie
              </button>
              <button className={adminSection === "settings" ? "active" : ""} onClick={() => selectAdminSection("settings")}>
                <Settings size={18} /> Impostazioni
              </button>
            </>
          ) : (
            <>
              <button className={userSection === "dashboard" ? "active" : ""} onClick={() => selectUserSection("dashboard")}>
                <WalletCards size={18} /> Dashboard
              </button>
              <button className={userSection === "transactions" ? "active" : ""} onClick={() => selectUserSection("transactions")}>
                <CalendarDays size={18} /> Transazioni
              </button>
              <button className={userSection === "categories" ? "active" : ""} onClick={() => selectUserSection("categories")}>
                <Tags size={18} /> Categorie
              </button>
              <button className={userSection === "profile" ? "active" : ""} onClick={() => selectUserSection("profile")}>
                <UserCog size={18} /> Profilo
              </button>
            </>
          )}
        </nav>

        <button className={`ghost-button logout ${isMobileMenuOpen ? "open" : ""}`} onClick={logout}>
          <LogOut size={18} />
          Esci
        </button>
      </aside>

      <section className="content">
        <header className="topbar">
          <div>
            <p className="eyebrow">Ciao, {auth.name ?? auth.username}</p>
            <h1>{isAdmin ? adminTitle : userTitle}</h1>
          </div>
          <div className="period-controls">
            <select
              className="period-year"
              value={year}
              disabled={!periodYears.length}
              onChange={(event) => {
                const nextYear = Number(event.target.value);
                const nextMonths = instalmentPeriods
                  .filter((period) => period.year === nextYear)
                  .map((period) => period.month)
                  .sort((a, b) => b - a);
                setYear(nextYear);
                setMonth(nextMonths[0] ?? month);
                setTransactionsPageNumber(0);
              }}
            >
              {!periodYears.length && <option value={year}>Nessun anno</option>}
              {periodYears.map((item) => (
                <option key={item} value={item}>{item}</option>
              ))}
            </select>
            <select
              className="period-month"
              value={month}
              disabled={!selectedYearMonths.length}
              onChange={(event) => {
                setMonth(Number(event.target.value));
                setTransactionsPageNumber(0);
              }}
            >
              {!selectedYearMonths.length && <option value={month}>Nessun mese</option>}
              {selectedYearMonths.map((item) => (
                <option key={item} value={item}>{monthName(item)}</option>
              ))}
            </select>
            <button className="icon-button" onClick={() => {
              setTransactionsPageNumber(0);
              void loadData();
            }} title="Aggiorna">
              <RefreshCw size={18} />
            </button>
          </div>
        </header>

        {message && <p className="notice app-notice">{message}</p>}

        {((isAdmin && adminSection === "dashboard") || (!isAdmin && userSection === "dashboard")) && (
          <section id="dashboard" className="metrics-grid">
            <article className="metric primary">
              <span>Saldo rata</span>
              <strong>{currency(Number(instalment?.currentBalance))}</strong>
            </article>
            {isAdmin ? (
              <>
                <article className="metric">
                  <span>Utenti registrati</span>
                  <strong>{userPage.totalElements}</strong>
                </article>
                <article className="metric">
                  <span>Categorie</span>
                  <strong>{categories.length}</strong>
                </article>
                <article className="metric">
                  <span>Importo iniziale</span>
                  <strong>{currency(Number(instalment?.initialAmount))}</strong>
                </article>
              </>
            ) : (
              <>
                <article className="metric">
                  <span>Entrate</span>
                  <strong className="positive">{currency(totals.income)}</strong>
                </article>
                <article className="metric">
                  <span>Uscite</span>
                  <strong className="negative">{currency(totals.expense)}</strong>
                </article>
                <article className="metric">
                <span>Movimenti</span>
                <strong>{dashboardTransactionCount}</strong>
                </article>
              </>
            )}
          </section>
        )}

        {isAdmin ? (
          <AdminArea
            activeSection={adminSection}
            categories={categories}
            users={users}
            userPage={userPage}
            usersPageNumber={usersPageNumber}
            usersPageSize={usersPageSize}
            isLoading={isLoading}
            categoryName={categoryName}
            categoryEdit={categoryEdit}
            registrationForm={registrationForm}
            templateCost={templateCost}
            onCategoryName={setCategoryName}
            onCategoryEdit={setCategoryEdit}
            onRegistrationForm={setRegistrationForm}
            onTemplateCost={setTemplateCost}
            onUsersPageNumber={setUsersPageNumber}
            onUsersPageSize={(value) => {
              setUsersPageSize(value);
              setUsersPageNumber(0);
            }}
            onCreateCategory={handleCreateCategory}
            onUpdateCategory={handleUpdateCategory}
            onDeleteCategory={handleDeleteCategory}
            onDisableUser={handleDisableUser}
            onRegisterUser={handleRegisterUser}
            onUpdateCost={handleUpdateCost}
          />
        ) : (
          <UserArea
            auth={auth}
            categories={categories}
            transactions={transactions}
            transactionForm={transactionForm}
            profileForm={profileForm}
            isLoading={isLoading}
            activeSection={userSection}
            dashboardTransactions={dashboardTransactions.slice(0, 5)}
            transactionPage={transactionPage}
            transactionsPageNumber={transactionsPageNumber}
            transactionsPageSize={transactionsPageSize}
            transactionMinDate={selectedPeriodDateRange.min}
            transactionMaxDate={selectedPeriodDateRange.max}
            onTransactionForm={setTransactionForm}
            onProfileForm={setProfileForm}
            onTransactionsPageNumber={setTransactionsPageNumber}
            onTransactionsPageSize={(value) => {
              setTransactionsPageSize(value);
              setTransactionsPageNumber(0);
            }}
            onAddTransaction={handleAddTransaction}
            onDeleteTransaction={handleDeleteTransaction}
            onProfile={handleProfile}
          />
        )}
      </section>
    </main>
  );
}

type AdminAreaProps = {
  activeSection: AdminSection;
  categories: Category[];
  users: UserAccount[];
  userPage: { currentPage: number; totalPages: number; totalElements: number };
  usersPageNumber: number;
  usersPageSize: number;
  isLoading: boolean;
  categoryName: string;
  categoryEdit: { id: number; oldName: string; newName: string };
  registrationForm: RegistrationPayload;
  templateCost: string;
  onCategoryName: (value: string) => void;
  onCategoryEdit: (value: { id: number; oldName: string; newName: string }) => void;
  onRegistrationForm: (value: RegistrationPayload) => void;
  onTemplateCost: (value: string) => void;
  onUsersPageNumber: (value: number) => void;
  onUsersPageSize: (value: number) => void;
  onCreateCategory: (event: FormEvent) => void;
  onUpdateCategory: (event: FormEvent) => void;
  onDeleteCategory: (id: number) => void;
  onDisableUser: (user: UserAccount) => void;
  onRegisterUser: (event: FormEvent) => void;
  onUpdateCost: (event: FormEvent) => void;
};

function AdminArea(props: AdminAreaProps) {
  if (props.activeSection === "categories") {
    return (
      <section id="categories" className="panel section-panel">
        <div className="panel-title">
          <div>
            <h2>Categorie</h2>
            <p className="section-copy">Gestisci la lista categorie usata dagli utenti nelle transazioni.</p>
          </div>
          <span>{props.categories.length} categorie</span>
        </div>
        <form className="inline-form" onSubmit={props.onCreateCategory}>
          <input value={props.categoryName} onChange={(event) => props.onCategoryName(event.target.value)} placeholder="Nuova categoria" maxLength={150} />
          <button className="primary-button"><Plus size={18} /> Aggiungi</button>
        </form>
        <div className="category-list">
          {props.categories.map((category) => (
            <article className="category-item" key={category.id}>
              <div className="category-name">
                <span><Tags size={17} /></span>
                <strong>{category.categoryName}</strong>
              </div>
              {props.categoryEdit.id === category.id ? (
                <form className="category-update" onSubmit={props.onUpdateCategory}>
                  <input
                    value={props.categoryEdit.newName}
                    onChange={(event) => props.onCategoryEdit({ ...props.categoryEdit, newName: event.target.value })}
                    placeholder="Nuovo nome"
                    maxLength={150}
                    autoFocus
                  />
                  <button className="icon-button success" title="Conferma aggiornamento">
                    <Check size={17} />
                  </button>
                  <button className="icon-button" type="button" onClick={() => props.onCategoryEdit({ id: 0, oldName: "", newName: "" })} title="Annulla">
                    <X size={17} />
                  </button>
                </form>
              ) : (
                <div className="category-actions">
                  <button
                    className="ghost-button compact"
                    onClick={() => props.onCategoryEdit({ id: category.id, oldName: category.categoryName, newName: category.categoryName })}
                  >
                    <Pencil size={16} />
                    Aggiorna
                  </button>
                  <button className="icon-button danger" onClick={() => props.onDeleteCategory(category.id)} title="Elimina categoria">
                    <Trash2 size={16} />
                  </button>
                </div>
              )}
            </article>
          ))}
          {!props.categories.length && <p className="empty">Nessuna categoria disponibile.</p>}
        </div>
      </section>
    );
  }

  if (props.activeSection === "settings") {
    return (
      <form id="settings" className="panel form-stack section-panel narrow-section" onSubmit={props.onUpdateCost}>
        <div className="panel-title">
          <div>
            <h2>Impostazioni</h2>
            <p className="section-copy">Imposta il costo standard della rata.</p>
          </div>
          <Settings size={20} />
        </div>
        <label>
          Nuovo costo standard
          <input value={props.templateCost} onChange={(event) => props.onTemplateCost(event.target.value)} type="number" min="0.01" step="0.01" required />
        </label>
        <button className="primary-button">
          <ShieldCheck size={18} />
          Aggiorna costo
        </button>
      </form>
    );
  }

  return (
    <section id="users" className="workspace-grid admin-grid">
      <form className="panel form-stack" onSubmit={props.onRegisterUser}>
        <div className="panel-title">
          <h2>Nuovo utente</h2>
          <UserPlus size={20} />
        </div>
        <div className="two-cols">
          <label>
            Nome
            <input value={props.registrationForm.name} onChange={(event) => props.onRegistrationForm({ ...props.registrationForm, name: event.target.value })} maxLength={50} required />
          </label>
          <label>
            Cognome
            <input value={props.registrationForm.lastname} onChange={(event) => props.onRegistrationForm({ ...props.registrationForm, lastname: event.target.value })} maxLength={50} required />
          </label>
        </div>
        <label>
          Email
          <input value={props.registrationForm.email} onChange={(event) => props.onRegistrationForm({ ...props.registrationForm, email: event.target.value })} type="email" required />
        </label>
        <button className="primary-button" disabled={props.isLoading}>
          <UserPlus size={18} />
          Registra utente
        </button>
      </form>

      <section className="panel">
        <div className="panel-title">
          <h2>Lista utenti</h2>
          <span>{props.isLoading ? "Caricamento" : `${props.userPage.totalElements} account`}</span>
        </div>
        <div className="table-list">
          {props.users.map((user) => (
            <article className="user-row" key={user.email}>
              <span className="avatar">{user.name.slice(0, 1)}{user.lastname.slice(0, 1)}</span>
              <div>
                <strong>{user.name} {user.lastname}</strong>
                <span>{user.username} - {user.email}</span>
              </div>
              <button className="ghost-button compact danger" onClick={() => props.onDisableUser(user)}>
                <Trash2 size={16} />
                Cancella
              </button>
            </article>
          ))}
          {!props.users.length && <p className="empty">Nessun utente disponibile.</p>}
        </div>
        <div className="pagination-toolbar pagination-bottom">
          <label>
            Elementi per pagina
            <select value={props.usersPageSize} onChange={(event) => props.onUsersPageSize(Number(event.target.value))}>
              {[5, 10, 25, 50, 100].map((size) => (
                <option key={size} value={size}>{size}</option>
              ))}
            </select>
          </label>
          <div className="pagination-actions">
            <button className="ghost-button compact" disabled={props.usersPageNumber <= 0} onClick={() => props.onUsersPageNumber(props.usersPageNumber - 1)}>
              <ChevronLeft size={16} />
              Precedente
            </button>
            <span className="page-status">Pagina <b>{props.userPage.currentPage + 1}</b> di {Math.max(props.userPage.totalPages, 1)}</span>
            <button className="ghost-button compact" disabled={props.usersPageNumber + 1 >= props.userPage.totalPages} onClick={() => props.onUsersPageNumber(props.usersPageNumber + 1)}>
              Successiva
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      </section>
    </section>
  );
}

type UserAreaProps = {
  auth: AuthResponse;
  activeSection: UserSection;
  categories: Category[];
  transactions: Transaction[];
  dashboardTransactions: Transaction[];
  transactionPage: { currentPage: number; totalPages: number; totalElements: number };
  transactionsPageNumber: number;
  transactionsPageSize: number;
  transactionMinDate: string;
  transactionMaxDate: string;
  transactionForm: AddTransactionPayload;
  profileForm: { username: string; oldPassword: string; newPassword: string };
  isLoading: boolean;
  onTransactionForm: (value: AddTransactionPayload) => void;
  onProfileForm: (value: { username: string; oldPassword: string; newPassword: string }) => void;
  onTransactionsPageNumber: (value: number) => void;
  onTransactionsPageSize: (value: number) => void;
  onAddTransaction: (event: FormEvent) => void;
  onDeleteTransaction: (id: number) => void;
  onProfile: (event: FormEvent) => void;
};

function UserArea(props: UserAreaProps) {
  const transactionForm = (
    <form className="panel form-stack" onSubmit={props.onAddTransaction}>
      <div className="panel-title">
        <h2>Nuova transazione</h2>
        <Plus size={20} />
      </div>
      <label>
        Titolo
        <input value={props.transactionForm.title} onChange={(event) => props.onTransactionForm({ ...props.transactionForm, title: event.target.value })} maxLength={150} required />
      </label>
      <div className="two-cols">
        <label>
          Importo
          <input value={props.transactionForm.amount || ""} onChange={(event) => props.onTransactionForm({ ...props.transactionForm, amount: Number(event.target.value) })} type="number" min="0.01" step="0.01" required />
        </label>
        <label>
          Data
          <input
            value={props.transactionForm.date}
            onChange={(event) => props.onTransactionForm({ ...props.transactionForm, date: event.target.value })}
            type="date"
            min={props.transactionMinDate}
            max={props.transactionMaxDate}
            required
          />
        </label>
      </div>
      <div className="type-toggle">
        <button type="button" className={props.transactionForm.transactionType === "ADD" ? "active" : ""} onClick={() => props.onTransactionForm({ ...props.transactionForm, transactionType: "ADD" })}>
          <ArrowUpCircle size={18} /> Entrata
        </button>
        <button type="button" className={props.transactionForm.transactionType === "REDUCE" ? "active" : ""} onClick={() => props.onTransactionForm({ ...props.transactionForm, transactionType: "REDUCE" })}>
          <ArrowDownCircle size={18} /> Uscita
        </button>
      </div>
      <label>
        Categoria
        <select value={props.transactionForm.category} onChange={(event) => props.onTransactionForm({ ...props.transactionForm, category: event.target.value })}>
          <option value="">Nessuna categoria</option>
          {props.categories.map((category) => (
            <option key={category.id} value={category.categoryName}>{category.categoryName}</option>
          ))}
        </select>
      </label>
      <label>
        Descrizione
        <textarea value={props.transactionForm.description} onChange={(event) => props.onTransactionForm({ ...props.transactionForm, description: event.target.value })} rows={3} />
      </label>
      <button className="primary-button" disabled={props.isLoading}>
        <Plus size={18} />
        Salva transazione
      </button>
    </form>
  );

  if (props.activeSection === "dashboard") {
    return (
      <section className="workspace-grid">
        {transactionForm}
        <section className="panel">
          <div className="panel-title">
            <h2>Ultime transazioni</h2>
            <span>Prime 5</span>
          </div>
          <TransactionList transactions={props.dashboardTransactions} currentUsername={props.auth.username} onDelete={props.onDeleteTransaction} />
        </section>
      </section>
    );
  }

  if (props.activeSection === "transactions") {
    return (
      <section id="transactions" className="panel section-panel">
        <div className="panel-title">
          <div>
            <h2>Tutte le transazioni</h2>
            <p className="section-copy">Consulta i movimenti del periodo selezionato.</p>
          </div>
          <span>{props.transactionPage.totalElements} totali</span>
        </div>
        <TransactionList transactions={props.transactions} currentUsername={props.auth.username} onDelete={props.onDeleteTransaction} />
        <div className="pagination-toolbar pagination-bottom">
          <label>
            Elementi per pagina
            <select value={props.transactionsPageSize} onChange={(event) => props.onTransactionsPageSize(Number(event.target.value))}>
              {[5, 10, 25, 50, 100].map((size) => (
                <option key={size} value={size}>{size}</option>
              ))}
            </select>
          </label>
          <div className="pagination-actions">
            <button className="ghost-button compact" disabled={props.transactionsPageNumber <= 0} onClick={() => props.onTransactionsPageNumber(props.transactionsPageNumber - 1)}>
              <ChevronLeft size={16} />
              Precedente
            </button>
            <span className="page-status">Pagina <b>{props.transactionPage.currentPage + 1}</b> di {Math.max(props.transactionPage.totalPages, 1)}</span>
            <button className="ghost-button compact" disabled={props.transactionsPageNumber + 1 >= props.transactionPage.totalPages} onClick={() => props.onTransactionsPageNumber(props.transactionsPageNumber + 1)}>
              Successiva
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      </section>
    );
  }

  if (props.activeSection === "categories") {
    return (
      <section id="categories" className="panel section-panel">
        <div className="panel-title">
          <div>
            <h2>Categorie disponibili</h2>
            <p className="section-copy">Categorie impostate dall'amministratore e disponibili per le tue transazioni.</p>
          </div>
          <Tags size={20} />
        </div>
        <div className="chips">
          {props.categories.map((category) => (
            <span className="chip read-only" key={category.id}>{category.categoryName}</span>
          ))}
          {!props.categories.length && <p className="empty">Nessuna categoria disponibile.</p>}
        </div>
      </section>
    );
  }

  return (
    <section className="panel form-stack narrow-section">
      <div className="panel-title">
        <div>
          <h2>Profilo</h2>
          <p className="section-copy">Gestisci username e password del tuo account.</p>
        </div>
        <UserCog size={20} />
      </div>
      <div className="profile-summary">
        <span className="avatar">{props.auth.name?.slice(0, 1) ?? props.auth.username.slice(0, 1)}</span>
        <div>
          <strong>{props.auth.name ? `${props.auth.name} ${props.auth.lastname ?? ""}` : props.auth.username}</strong>
          <span>{props.auth.username}</span>
        </div>
      </div>
      <form className="form-stack" onSubmit={props.onProfile}>
        <label>
          Username
          <input value={props.profileForm.username || props.auth.username} onChange={(event) => props.onProfileForm({ ...props.profileForm, username: event.target.value })} />
        </label>
        <div className="two-cols password-fields">
          <label className="password-field">
            Password attuale
            <input value={props.profileForm.oldPassword} onChange={(event) => props.onProfileForm({ ...props.profileForm, oldPassword: event.target.value })} type="password" />
            <span className="field-hint" aria-hidden="true">&nbsp;</span>
          </label>
          <label className="password-field">
            Nuova password
            <input
              value={props.profileForm.newPassword}
              onChange={(event) => props.onProfileForm({ ...props.profileForm, newPassword: event.target.value })}
              type="password"
              minLength={8}
              pattern="^(?=.*\p{Lu})(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':&quot;\\|,.<>\/?]).{8,}$"
              title={PASSWORD_RULE_MESSAGE}
            />
            <span className="field-hint">Minimo 8 caratteri, una maiuscola, un numero e un carattere speciale.</span>
          </label>
        </div>
        <button className="ghost-button">
          <KeyRound size={18} />
          Aggiorna profilo
        </button>
      </form>
    </section>
  );
}

type TransactionListProps = {
  transactions: Transaction[];
  currentUsername: string;
  onDelete: (id: number) => void;
};

function normalizeUsername(username: string | undefined) {
  return (username ?? "").trim().toLowerCase();
}

function TransactionList({ transactions, currentUsername, onDelete }: TransactionListProps) {
  const normalizedCurrentUsername = normalizeUsername(currentUsername);

  return (
    <div className="table-list">
      {transactions.map((transaction) => {
        const ownerUsername = normalizeUsername(transaction.user?.username);
        const canDelete = !ownerUsername || ownerUsername === normalizedCurrentUsername;

        return (
          <article className="transaction-row" key={transaction.id}>
            <div>
              <strong>{transaction.title}</strong>
              <span>{transaction.category || "Senza categoria"} - {formatDisplayDate(transaction.date)}</span>
            </div>
            <b className={transaction.transactionType === "ADD" ? "positive" : "negative"}>
              {transaction.transactionType === "ADD" ? "+" : "-"} {currency(Number(transaction.amount))}
            </b>
            <button
              className="icon-button danger"
              disabled={!canDelete}
              onClick={() => canDelete && onDelete(transaction.id)}
              title={canDelete ? "Elimina transazione" : "Transazione di un altro utente"}
            >
              <Trash2 size={17} />
            </button>
          </article>
        );
      })}
      {!transactions.length && <p className="empty">Nessuna transazione per questo periodo.</p>}
    </div>
  );
}
