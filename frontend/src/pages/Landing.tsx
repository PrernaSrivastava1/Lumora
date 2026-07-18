import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowRight, BrainCircuit, Check, ChevronRight, Layers3, Search, Sparkles } from 'lucide-react'

const capabilities = [
  { icon: Layers3, title: 'A home for your knowledge', text: 'Organize source material into focused, private workspaces.' },
  { icon: BrainCircuit, title: 'Answers you can inspect', text: 'Ask with context, review citations, and stay close to the source.' },
  { icon: Search, title: 'Retrieval you can trust', text: 'Explore semantic matches and compare the path to each result.' },
]

export default function Landing() {
  const navigate = useNavigate()
  const [showWelcome, setShowWelcome] = useState(() => localStorage.getItem('lumora-welcome-seen') !== 'true')
  const [lineIndex, setLineIndex] = useState(0)
  const [characterCount, setCharacterCount] = useState(0)
  const lines = ['Welcome to Lumora.', 'Your knowledge, intelligently connected.', 'Search with meaning. Ask with context.', 'Turn information into understanding.']
  const currentLine = lines[lineIndex]

  useEffect(() => {
    if (!showWelcome || lineIndex >= lines.length) return
    if (characterCount < currentLine.length) {
      const timer = window.setTimeout(() => setCharacterCount(value => value + 1), 23)
      return () => window.clearTimeout(timer)
    }
    const timer = window.setTimeout(() => { setLineIndex(value => value + 1); setCharacterCount(0) }, 460)
    return () => window.clearTimeout(timer)
  }, [characterCount, currentLine, lineIndex, lines.length, showWelcome])

  const startExploring = () => {
    localStorage.setItem('lumora-welcome-seen', 'true')
    setShowWelcome(false)
    window.setTimeout(() => navigate('/home'), 180)
  }

  return (
    <div className="min-h-screen overflow-hidden bg-background text-foreground">
      {showWelcome && (
        <div className="fixed inset-0 z-[100] grid place-items-center bg-background/90 p-6 backdrop-blur-xl transition-opacity duration-500">
          <div className="w-full max-w-2xl text-center">
            <span className="mx-auto grid h-11 w-11 place-items-center rounded-2xl bg-primary text-primary-foreground shadow-sm"><Sparkles className="h-5 w-5" /></span>
            <div className="mt-9 min-h-44 space-y-3">
              {lines.slice(0, lineIndex).map(line => <p key={line} className="text-xl font-medium tracking-[-.03em] text-muted-foreground sm:text-2xl">{line}</p>)}
              {lineIndex < lines.length && <p className="text-3xl font-semibold tracking-[-.05em] sm:text-5xl">{currentLine.slice(0, characterCount)}<span className="ml-1 inline-block h-8 w-px translate-y-1 animate-pulse bg-primary sm:h-11" /></p>}
            </div>
            <div className={`mt-10 flex justify-center gap-3 transition-all duration-500 ${lineIndex >= lines.length ? 'translate-y-0 opacity-100' : 'translate-y-2 opacity-0 pointer-events-none'}`}>
              <button onClick={startExploring} className="inline-flex items-center gap-2 rounded-xl bg-primary px-5 py-3 text-sm font-semibold text-primary-foreground shadow-sm hover:opacity-95">Start exploring <ArrowRight className="h-4 w-4" /></button>
              <button onClick={() => { localStorage.setItem('lumora-welcome-seen', 'true'); setShowWelcome(false) }} className="rounded-xl border border-border bg-card px-5 py-3 text-sm font-semibold hover:bg-secondary">Learn more</button>
            </div>
          </div>
        </div>
      )}
      <header className="mx-auto flex h-20 max-w-7xl items-center justify-between px-5 sm:px-8">
        <Link to="/" className="flex items-center gap-2.5"><span className="grid h-9 w-9 place-items-center rounded-xl bg-primary text-primary-foreground shadow-sm"><Sparkles className="h-4 w-4" /></span><span className="font-semibold tracking-[-.035em]">Lumora</span></Link>
        <nav className="flex items-center gap-2"><Link to="/login" className="hidden rounded-lg px-4 py-2 text-sm font-semibold text-muted-foreground hover:text-foreground sm:inline-flex">Sign in</Link><Link to="/register" className="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground shadow-sm hover:opacity-95">Get started <ArrowRight className="h-4 w-4" /></Link></nav>
      </header>

      <main>
        <section className="relative mx-auto max-w-7xl px-5 pb-24 pt-20 text-center sm:px-8 sm:pt-28 lg:pb-32">
          <div className="pointer-events-none absolute inset-x-0 top-0 -z-10 mx-auto h-[28rem] w-[45rem] rounded-full bg-[radial-gradient(ellipse_at_center,hsl(var(--accent)),transparent_67%)] opacity-80" />
          <p className="eyebrow mx-auto mb-6 flex w-fit items-center gap-2 rounded-full border border-border bg-card/70 px-3 py-1.5 shadow-sm"><span className="h-1.5 w-1.5 rounded-full bg-primary" /> Knowledge, made navigable</p>
          <h1 className="mx-auto max-w-4xl text-5xl font-semibold leading-[1.03] tracking-[-.06em] sm:text-6xl lg:text-7xl">Bring clarity to everything your team knows.</h1>
          <p className="mx-auto mt-7 max-w-2xl text-base leading-7 text-muted-foreground sm:text-lg">Lumora transforms your documents into a calm, searchable intelligence layer—so every answer begins with the knowledge you already trust.</p>
          <div className="mt-9 flex flex-col justify-center gap-3 sm:flex-row"><Link to="/register" className="inline-flex items-center justify-center gap-2 rounded-xl bg-primary px-5 py-3 text-sm font-semibold text-primary-foreground shadow-sm hover:opacity-95">Build your first workspace <ArrowRight className="h-4 w-4" /></Link><Link to="/login" className="inline-flex items-center justify-center gap-2 rounded-xl border border-border bg-card px-5 py-3 text-sm font-semibold hover:bg-secondary">Explore Lumora <ChevronRight className="h-4 w-4" /></Link></div>
          <div className="surface mx-auto mt-16 max-w-5xl overflow-hidden p-3 text-left sm:p-5">
            <div className="rounded-xl border border-border bg-muted/30 p-4 sm:p-6"><div className="flex items-center justify-between border-b border-border pb-4"><div className="flex items-center gap-2"><span className="h-2 w-2 rounded-full bg-primary" /><span className="text-sm font-semibold">Product research</span></div><span className="text-xs text-muted-foreground">84 indexed sources</span></div><div className="mt-6 grid gap-4 md:grid-cols-[.85fr_1.4fr]"><div className="rounded-xl border border-border bg-card p-4"><p className="eyebrow">Workspace health</p><p className="mt-3 text-2xl font-semibold tracking-tight">Ready to ask</p><div className="mt-5 h-1.5 rounded-full bg-secondary"><div className="h-full w-[82%] rounded-full bg-primary" /></div><p className="mt-2 text-xs text-muted-foreground">82% source coverage</p></div><div className="rounded-xl border border-border bg-card p-4"><p className="text-sm font-medium">What changed in customer onboarding this quarter?</p><div className="mt-5 rounded-lg bg-secondary/70 p-4 text-sm leading-6 text-muted-foreground">Lumora found a consistent shift toward guided setup, with citations from research interviews, release notes, and support trends.</div></div></div></div>
          </div>
        </section>

        <section className="border-y border-border bg-card/50"><div className="mx-auto max-w-7xl px-5 py-20 sm:px-8"><div className="max-w-xl"><p className="eyebrow mb-3">One deliberate workflow</p><h2 className="text-3xl font-semibold tracking-[-.045em]">From source material to shared understanding.</h2></div><div className="mt-12 grid gap-5 md:grid-cols-3">{capabilities.map(({ icon: Icon, title, text }) => <article key={title} className="surface p-6"><span className="grid h-9 w-9 place-items-center rounded-xl bg-accent text-accent-foreground"><Icon className="h-4 w-4" /></span><h3 className="mt-7 text-lg font-semibold tracking-[-.025em]">{title}</h3><p className="mt-2 text-sm leading-6 text-muted-foreground">{text}</p></article>)}</div></div></section>

        <section className="mx-auto max-w-7xl px-5 py-24 sm:px-8"><div className="surface grid gap-8 p-8 md:grid-cols-[1fr_auto] md:items-center md:p-12"><div><p className="eyebrow mb-3">Built for thoughtful teams</p><h2 className="max-w-xl text-3xl font-semibold tracking-[-.045em]">Your knowledge should feel useful, not buried.</h2><ul className="mt-6 space-y-3 text-sm text-muted-foreground">{['Create focused workspaces', 'Keep every response connected to sources', 'Measure retrieval quality as you grow'].map(item => <li key={item} className="flex items-center gap-2"><Check className="h-4 w-4 text-primary" />{item}</li>)}</ul></div><Link to="/register" className="inline-flex items-center justify-center gap-2 rounded-xl bg-primary px-5 py-3 text-sm font-semibold text-primary-foreground shadow-sm hover:opacity-95">Get started <ArrowRight className="h-4 w-4" /></Link></div></section>
      </main>
      <footer className="mx-auto flex max-w-7xl flex-col gap-3 border-t border-border px-5 py-8 text-xs text-muted-foreground sm:flex-row sm:items-center sm:justify-between sm:px-8"><span>© {new Date().getFullYear()} Lumora</span><span>Knowledge intelligence for focused teams.</span></footer>
    </div>
  )
}
