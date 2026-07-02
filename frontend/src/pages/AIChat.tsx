import { MessageSquare, Send, Sparkles, Plus, AlertCircle } from 'lucide-react'

export default function AIChat() {
  return (
    <div className="flex h-[calc(100vh-8.5rem)] rounded-xl border border-border bg-card overflow-hidden">
      {/* Sessions Left Drawer */}
      <div className="hidden md:flex w-64 border-r border-border bg-muted/20 flex-col h-full shrink-0">
        <div className="p-4 border-b border-border">
          <button
            disabled
            className="w-full inline-flex items-center justify-center gap-2 rounded-lg border border-border bg-background px-3 py-2 text-sm font-semibold hover:bg-muted/30 transition-all cursor-not-allowed"
          >
            <Plus className="h-4 w-4" />
            New Session
          </button>
        </div>
        <div className="flex-1 p-4 overflow-y-auto space-y-1">
          <div className="flex items-center gap-2.5 px-3 py-2 rounded-lg bg-secondary text-foreground text-sm font-medium">
            <MessageSquare className="h-4 w-4" />
            <span className="truncate">RAG Query 1</span>
          </div>
        </div>
      </div>

      {/* Main Chat Feed Area */}
      <div className="flex-1 flex flex-col h-full bg-background relative">
        {/* Header */}
        <div className="flex h-14 items-center justify-between border-b border-border px-6">
          <div className="flex items-center gap-2">
            <Sparkles className="h-4 w-4 text-primary" />
            <span className="font-bold text-sm">Local Assistant (llama3.2)</span>
          </div>
          <div className="text-xs text-muted-foreground">Connected: nomic-embed-text</div>
        </div>

        {/* Messages list */}
        <div className="flex-1 p-6 overflow-y-auto space-y-6">
          {/* Assistant Welcome */}
          <div className="flex items-start gap-4 max-w-2xl">
            <div className="rounded-lg p-2 bg-primary/10 text-primary shrink-0">
              <Sparkles className="h-4 w-4" />
            </div>
            <div className="space-y-3">
              <div className="rounded-2xl bg-muted/40 p-4 border border-border text-sm leading-relaxed">
                Hello! I am your local AI assistant. Once you index your documents, I will query the HNSW graphs to retrieve relevant text chunks and answer your questions directly from your own data.
              </div>
              <div className="flex flex-wrap gap-2">
                <span className="rounded-full bg-secondary/80 border border-border px-3 py-1 text-xs cursor-not-allowed hover:bg-secondary transition-colors">
                  "Explain dynamic programming..."
                </span>
                <span className="rounded-full bg-secondary/80 border border-border px-3 py-1 text-xs cursor-not-allowed hover:bg-secondary transition-colors">
                  "What is binary search?"
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Input Form Bar */}
        <div className="p-4 border-t border-border bg-card">
          <div className="max-w-3xl mx-auto flex gap-2 relative items-center">
            <input
              type="text"
              placeholder="Ask a question about your indexed files..."
              disabled
              className="flex-1 border border-input rounded-lg pl-4 pr-12 py-2 text-sm bg-muted/10 focus:outline-none cursor-not-allowed"
            />
            <button
              disabled
              className="absolute right-2 p-1.5 rounded-md bg-primary text-primary-foreground hover:bg-primary/95 cursor-not-allowed"
            >
              <Send className="h-4 w-4" />
            </button>
          </div>
          <div className="text-[10px] text-center text-muted-foreground mt-2 flex items-center justify-center gap-1">
            <AlertCircle className="h-3 w-3" />
            Answers are synthesized locally via your local Ollama instance.
          </div>
        </div>
      </div>
    </div>
  )
}
