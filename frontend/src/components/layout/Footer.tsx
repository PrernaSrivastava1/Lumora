export default function Footer() {
  return (
    <footer className="border-t border-border bg-card/30 py-4 px-6 text-center text-xs text-muted-foreground">
      &copy; {new Date().getFullYear()} Lumora AI. Built for production-grade vector storage & RAG search.
    </footer>
  )
}
