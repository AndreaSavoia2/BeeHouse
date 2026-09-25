import { FormEvent, useState } from "react";
import { Home, KeyRound, Mail, ShieldCheck } from "lucide-react";
import { api, AuthResponse } from "../lib/api";

type AuthPanelProps = {
  onLogin: (auth: AuthResponse) => void;
};

export function AuthPanel({ onLogin }: AuthPanelProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [mode, setMode] = useState<"login" | "help">("login");
  const [message, setMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  async function handleLogin(event: FormEvent) {
    event.preventDefault();
    setIsLoading(true);
    setMessage("");

    try {
      const auth = await api.login(username, password);
      onLogin(auth);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Accesso non riuscito");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleHelp(action: "username" | "password") {
    if (!email) {
      setMessage("Inserisci la tua email.");
      return;
    }

    setIsLoading(true);
    setMessage("");

    try {
      const result =
        action === "username"
          ? await api.remindUsername(email)
          : await api.resetPassword(email);
      setMessage(result || "Richiesta inviata.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Operazione non riuscita");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <main className="auth-shell">
      <section className="brand-side">
        <div className="brand-mark">
          <Home size={34} />
        </div>
        <p className="eyebrow">BeeHouse</p>
        <h1>Gestione casa, utenti e movimenti.</h1>
        <p className="hero-copy">
          Una console operativa collegata al backend Spring Boot: area Utente per i movimenti
          personali e area Amministratore per gestione utenti, categorie e impostazioni.
        </p>
        <div className="hero-stats">
          <span>Login JWT</span>
          <span>Utente</span>
          <span>Amministratore</span>
        </div>
      </section>

      <section className="auth-card" aria-label="Accesso">
        <div className="segmented">
          <button className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>
            Login
          </button>
          <button className={mode === "help" ? "active" : ""} onClick={() => setMode("help")}>
            Recupero
          </button>
        </div>

        {mode === "login" ? (
          <form onSubmit={handleLogin} className="form-stack">
            <div className="auth-intro">
              <h2>Accedi</h2>
              <p>La registrazione viene gestita dall'amministratore.</p>
            </div>
            <label>
              Username
              <input value={username} onChange={(event) => setUsername(event.target.value)} minLength={2} required />
            </label>
            <label>
              Password
              <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
            </label>
            <button className="primary-button" disabled={isLoading}>
              <ShieldCheck size={18} />
              {isLoading ? "Accesso..." : "Entra"}
            </button>
          </form>
        ) : (
          <div className="form-stack">
            <div className="auth-intro">
              <h2>Recupero account</h2>
              <p>Ricevi via email il tuo username o una nuova password temporanea.</p>
            </div>
            <label>
              Email
              <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
            </label>
            <button className="primary-button" onClick={() => handleHelp("username")} disabled={isLoading}>
              <Mail size={18} />
              Ricorda username
            </button>
            <button className="ghost-button" onClick={() => handleHelp("password")} disabled={isLoading}>
              <KeyRound size={18} />
              Reset password
            </button>
          </div>
        )}

        {message && <p className="notice">{message}</p>}
      </section>
    </main>
  );
}
