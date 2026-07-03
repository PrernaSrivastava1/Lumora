import React, { useState, useEffect } from 'react'
import { MessageSquare, Send, Sparkles, Trash2, Copy, AlertCircle, RefreshCw } from 'lucide-react'
import apiClient from '@/services/api'

interface SourceReference {
  documentTitle: string
  textPreview: string
  similarityScore: number
}

interface Message {
  role: 'user' | 'assistant'
  content: string
  sources?: SourceReference[]
  algorithmUsed?: string
  responseTimeMs?: number
}

interface Workspace {
  id: number
  name: string
  totalDocuments: number
}

export default function AIChat() {
  const [workspaces, setWorkspaces] = useState<Workspace[]>([])
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState<number | ''>('')
  const [algorithm, setAlgorithm] = useState<string>('AUTO')
  const [topK, setTopK] = useState<number>(5)

  const [messages, setMessages] = useState<Message[]>([
    {
      role: 'assistant',
      content: 'Hello! I am your local Lumora RAG assistant. Select a workspace from the dropdown above to start asking questions about your indexed documents.',
    },
  ])
  const [inputMsg, setInputMsg] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  // Load user workspaces list
  useEffect(() => {
    const fetchWorkspaces = async () => {
      try {
        const res = await apiClient.get('/workspaces')
        if (res.data) {
          // If response wrapped in standard ApiResponse
          const list = res.data.data || res.data
          setWorkspaces(list)
          if (list.length > 0) {
            setSelectedWorkspaceId(list[0].id)
          }
        }
      } catch (err) {
        console.error('Failed to load workspaces:', err)
      }
    }
    fetchWorkspaces()
  }, [])

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!inputMsg.trim() || isSubmitting) return
    if (!selectedWorkspaceId) {
      setErrorMsg('Please select a workspace first.')
      return
    }

    const userMsg: Message = { role: 'user', content: inputMsg }
    setMessages((prev) => [...prev, userMsg])
    setInputMsg('')
    setIsSubmitting(true)
    setErrorMsg('')

    try {
      const res = await apiClient.post('/rag/chat', {
        query: userMsg.content,
        workspaceId: selectedWorkspaceId,
        algorithm: algorithm,
        topK: topK,
      })

      if (res.data.success) {
        const { answer, sources, algorithmUsed, responseTimeMs } = res.data.data
        setMessages((prev) => [
          ...prev,
          {
            role: 'assistant',
            content: answer,
            sources,
            algorithmUsed,
            responseTimeMs,
          },
        ])
      } else {
        setErrorMsg(res.data.message || 'Failed to synthesize answer.')
      }
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Error occurred while calling generation endpoint.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleClear = () => {
    setMessages([
      {
        role: 'assistant',
        content: 'Conversation history cleared. Select a workspace and ask a new question.',
      },
    ])
    setErrorMsg('')
  }

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text)
  }

  return (
    <div className="flex flex-col h-[calc(100vh-8.5rem)] rounded-xl border border-border bg-card overflow-hidden">
      {/* Top Configuration Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 p-4 border-b border-border bg-muted/10">
        <div className="flex flex-wrap items-center gap-3">
          {/* Workspace Selector */}
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-muted-foreground">Workspace:</span>
            <select
              value={selectedWorkspaceId}
              onChange={(e) => setSelectedWorkspaceId(Number(e.target.value))}
              className="px-2.5 py-1.5 rounded-lg border border-input bg-background text-xs focus:outline-none"
            >
              {workspaces.map((ws) => (
                <option key={ws.id} value={ws.id}>
                  {ws.name} ({ws.totalDocuments} Docs)
                </option>
              ))}
            </select>
          </div>

          {/* Algorithm Selector */}
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-muted-foreground">Search strategy:</span>
            <select
              value={algorithm}
              onChange={(e) => setAlgorithm(e.target.value)}
              className="px-2.5 py-1.5 rounded-lg border border-input bg-background text-xs focus:outline-none"
            >
              <option value="AUTO">AUTO (Dynamic)</option>
              <option value="BRUTE_FORCE">Brute Force (Exact)</option>
              <option value="KD_TREE">KD-Tree (Axes Split)</option>
              <option value="HNSW">HNSW (Graph Routing)</option>
            </select>
          </div>

          {/* Top-K Selector */}
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-muted-foreground">Top K:</span>
            <input
              type="number"
              min={1}
              max={10}
              value={topK}
              onChange={(e) => setTopK(Number(e.target.value))}
              className="w-14 px-2 py-1 rounded-lg border border-input bg-background text-xs text-center focus:outline-none"
            />
          </div>
        </div>

        <button
          onClick={handleClear}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium border border-border hover:bg-red-500/5 hover:text-red-400 hover:border-red-500/20 transition-all"
        >
          <Trash2 className="h-3.5 w-3.5" />
          Clear Chat
        </button>
      </div>

      {/* Main Chat Feed Area */}
      <div className="flex-1 flex flex-col h-full bg-background relative overflow-hidden">
        {/* Messages list */}
        <div className="flex-1 p-6 overflow-y-auto space-y-6">
          {messages.map((msg, index) => (
            <div
              key={index}
              className={`flex items-start gap-4 max-w-3xl ${
                msg.role === 'user' ? 'ml-auto flex-row-reverse' : ''
              }`}
            >
              <div
                className={`rounded-lg p-2 shrink-0 ${
                  msg.role === 'user' ? 'bg-primary text-primary-foreground' : 'bg-primary/10 text-primary'
                }`}
              >
                {msg.role === 'user' ? (
                  <MessageSquare className="h-4 w-4" />
                ) : (
                  <Sparkles className="h-4 w-4" />
                )}
              </div>
              <div className="space-y-3">
                <div
                  className={`rounded-2xl p-4 border text-sm leading-relaxed whitespace-pre-wrap ${
                    msg.role === 'user'
                      ? 'bg-muted/10 border-border text-foreground'
                      : 'bg-card border-border text-foreground shadow-sm'
                  }`}
                >
                  {msg.content}

                  {/* Metadata display */}
                  {msg.role === 'assistant' && msg.responseTimeMs !== undefined && (
                    <div className="text-[10px] text-muted-foreground mt-3 pt-2 border-t border-border flex items-center gap-3">
                      <span>Strategy: <strong className="text-violet-400">{msg.algorithmUsed}</strong></span>
                      <span>Resolved in: <strong>{msg.responseTimeMs} ms</strong></span>
                      <button
                        onClick={() => handleCopy(msg.content)}
                        className="inline-flex items-center gap-0.5 hover:text-primary ml-auto transition-colors"
                        title="Copy answer"
                      >
                        <Copy className="h-3 w-3" /> Copy
                      </button>
                    </div>
                  )}
                </div>

                {/* Retrieved Sources preview */}
                {msg.role === 'assistant' && msg.sources && msg.sources.length > 0 && (
                  <div className="space-y-2">
                    <span className="text-[10px] font-bold text-muted-foreground tracking-wider uppercase">
                      Retrieved References ({msg.sources.length}):
                    </span>
                    <div className="grid gap-2 sm:grid-cols-2">
                      {msg.sources.map((src, sIdx) => (
                        <div
                          key={sIdx}
                          className="rounded-lg border border-border bg-card/50 p-2.5 text-xs hover:border-violet-500/20 transition-all"
                        >
                          <div className="flex items-center justify-between mb-1">
                            <span className="font-semibold text-foreground truncate max-w-[150px]">
                              {src.documentTitle}
                            </span>
                            <span className="text-[10px] bg-violet-500/10 text-violet-400 px-1.5 py-0.5 rounded-full">
                              Score: {Math.round(src.similarityScore * 100)}%
                            </span>
                          </div>
                          <p className="text-muted-foreground line-clamp-3 text-[11px] italic leading-relaxed">
                            "{src.textPreview.trim()}"
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}

          {isSubmitting && (
            <div className="flex items-start gap-4 max-w-2xl">
              <div className="rounded-lg p-2 bg-primary/10 text-primary shrink-0">
                <Sparkles className="h-4 w-4" />
              </div>
              <div className="flex items-center gap-2 text-sm text-muted-foreground bg-card border border-border p-4 rounded-2xl shadow-sm">
                <RefreshCw className="h-4 w-4 animate-spin text-primary" />
                Thinking and retrieving chunks...
              </div>
            </div>
          )}

          {errorMsg && (
            <div className="rounded-lg border border-red-500/20 bg-red-500/5 p-4 flex items-start gap-2.5 text-sm text-red-400">
              <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
              <span>{errorMsg}</span>
            </div>
          )}
        </div>

        {/* Input Form Bar */}
        <form onSubmit={handleSend} className="p-4 border-t border-border bg-card">
          <div className="max-w-3xl mx-auto flex gap-2 relative items-center">
            <input
              type="text"
              value={inputMsg}
              onChange={(e) => setInputMsg(e.target.value)}
              placeholder={
                selectedWorkspaceId
                  ? 'Ask a question about your indexed files...'
                  : 'Please create a workspace and upload documents to begin...'
              }
              disabled={isSubmitting || !selectedWorkspaceId}
              className="flex-1 border border-input rounded-lg pl-4 pr-12 py-2.5 text-sm bg-muted/10 focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all disabled:opacity-50"
            />
            <button
              type="submit"
              disabled={isSubmitting || !inputMsg.trim() || !selectedWorkspaceId}
              className="absolute right-2 p-1.5 rounded-md bg-primary text-primary-foreground hover:bg-primary/95 transition-all disabled:opacity-50"
            >
              <Send className="h-4 w-4" />
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
