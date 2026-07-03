import React, { useState, useEffect, useRef } from 'react'
import {
  MessageSquare,
  Send,
  Sparkles,
  Trash2,
  Copy,
  AlertCircle,
  RefreshCw,
  Info,
  ChevronDown,
  ChevronUp,
  Terminal,
  StopCircle
} from 'lucide-react'
import apiClient from '@/services/api'

interface SourceReference {
  documentTitle: string
  textPreview: string
  similarityScore: number
}

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  sources?: SourceReference[]
  algorithmUsed?: string
  responseTimeMs?: number
  promptTokens?: number
  answerTokens?: number
  contextSizeChars?: number
  finalPromptSent?: String
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
      id: 'welcome',
      role: 'assistant',
      content: 'Hello! I am your local Lumora RAG assistant. Select a workspace from the dropdown above to start asking questions about your indexed documents.',
    },
  ])
  const [inputMsg, setInputMsg] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')
  
  // AbortController to support stopping generation
  const abortControllerRef = useRef<AbortController | null>(null)

  // Accordion state for collapsed references
  const [expandedSources, setExpandedSources] = useState<Record<string, boolean>>({})

  // Modal prompt inspector state
  const [inspectingPrompt, setInspectingPrompt] = useState<string | null>(null)

  // Load user workspaces list
  useEffect(() => {
    const fetchWorkspaces = async () => {
      try {
        const res = await apiClient.get('/workspaces')
        if (res.data) {
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

  const toggleSource = (msgId: string) => {
    setExpandedSources((prev) => ({ ...prev, [msgId]: !prev[msgId] }))
  }

  const handleSend = async (e: React.FormEvent, customPrompt?: string) => {
    if (e) e.preventDefault()
    const promptToSend = customPrompt || inputMsg
    if (!promptToSend.trim() || isSubmitting) return
    if (!selectedWorkspaceId) {
      setErrorMsg('Please select a workspace first.')
      return
    }

    const userMsgId = 'user-' + Date.now()
    const userMsg: Message = { id: userMsgId, role: 'user', content: promptToSend }
    setMessages((prev) => [...prev, userMsg])
    setInputMsg('')
    setIsSubmitting(true)
    setErrorMsg('')

    // Create AbortController to support Stop Generation
    const controller = new AbortController()
    abortControllerRef.current = controller

    try {
      const res = await apiClient.post('/rag/chat', {
        query: promptToSend,
        workspaceId: selectedWorkspaceId,
        algorithm: algorithm,
        topK: topK,
      }, { signal: controller.signal })

      if (res.data.success) {
        const { answer, sources, algorithmUsed, responseTimeMs, promptTokens, answerTokens, contextSizeChars, finalPromptSent } = res.data.data
        const assistantMsgId = 'assistant-' + Date.now()
        
        // Simulated streaming/typing animation
        let displayedAnswer = ''
        const fullAnswer = answer
        setMessages((prev) => [
          ...prev,
          {
            id: assistantMsgId,
            role: 'assistant',
            content: '',
            sources,
            algorithmUsed,
            responseTimeMs,
            promptTokens,
            answerTokens,
            contextSizeChars,
            finalPromptSent
          }
        ])

        const words = fullAnswer.split(' ')
        let currentWordIndex = 0
        
        const typingInterval = setInterval(() => {
          if (currentWordIndex < words.length) {
            displayedAnswer += (currentWordIndex === 0 ? '' : ' ') + words[currentWordIndex]
            setMessages((prev) =>
              prev.map((msg) =>
                msg.id === assistantMsgId ? { ...msg, content: displayedAnswer } : msg
              )
            )
            currentWordIndex++
          } else {
            clearInterval(typingInterval)
            setIsSubmitting(false)
          }
        }, 30) // Fast natural typing flow

      } else {
        setErrorMsg(res.data.message || 'Failed to synthesize answer.')
        setIsSubmitting(false)
      }
    } catch (err: any) {
      if (err.name === 'CanceledError') {
        setErrorMsg('Generation stopped by user.')
      } else {
        setErrorMsg(err.response?.data?.message || 'Error occurred while calling generation endpoint.')
      }
      setIsSubmitting(false)
    }
  }

  const handleStop = () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort()
      setIsSubmitting(false)
    }
  }

  const handleRegenerate = (msgIndex: number) => {
    // Find the last user message before this assistant message
    for (let i = msgIndex - 1; i >= 0; i--) {
      if (messages[i].role === 'user') {
        handleSend(null as any, messages[i].content)
        break
      }
    }
  }

  const handleClear = () => {
    setMessages([
      {
        id: 'welcome',
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
              key={msg.id}
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
              <div className="space-y-3 max-w-full">
                {/* Text Bubble */}
                <div
                  className={`rounded-2xl p-4 border text-sm leading-relaxed whitespace-pre-wrap relative group ${
                    msg.role === 'user'
                      ? 'bg-muted/10 border-border text-foreground'
                      : 'bg-card border-border text-foreground shadow-sm'
                  }`}
                >
                  {msg.content}

                  {/* Actions & Metrics display */}
                  {msg.role === 'assistant' && msg.id !== 'welcome' && (
                    <div className="text-[10px] text-muted-foreground mt-4 pt-2 border-t border-border flex flex-wrap items-center gap-3">
                      <span>Strategy: <strong className="text-violet-400">{msg.algorithmUsed}</strong></span>
                      <span>Time: <strong>{msg.responseTimeMs} ms</strong></span>
                      {msg.promptTokens !== undefined && (
                        <span>Tokens: <strong>{msg.promptTokens} in / {msg.answerTokens} out</strong></span>
                      )}
                      {msg.contextSizeChars !== undefined && (
                        <span>Context: <strong>{msg.contextSizeChars} chars</strong></span>
                      )}

                      <div className="flex items-center gap-2 ml-auto">
                        {msg.finalPromptSent && (
                          <button
                            onClick={() => setInspectingPrompt(msg.finalPromptSent as string)}
                            className="inline-flex items-center gap-0.5 hover:text-primary transition-colors"
                            title="Inspect LLM Prompt"
                          >
                            <Terminal className="h-3 w-3" /> Inspect Prompt
                          </button>
                        )}
                        <button
                          onClick={() => handleCopy(msg.content)}
                          className="inline-flex items-center gap-0.5 hover:text-primary transition-colors"
                          title="Copy Answer"
                        >
                          <Copy className="h-3 w-3" /> Copy
                        </button>
                        <button
                          onClick={() => handleRegenerate(index)}
                          className="inline-flex items-center gap-0.5 hover:text-primary transition-colors"
                          title="Regenerate response"
                        >
                          <RefreshCw className="h-3 w-3" /> Retry
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                {/* Collapsible references panel */}
                {msg.role === 'assistant' && msg.sources && msg.sources.length > 0 && (
                  <div className="border border-border rounded-lg bg-card overflow-hidden">
                    <button
                      onClick={() => toggleSource(msg.id)}
                      className="w-full flex items-center justify-between px-3 py-2 text-xs font-semibold text-muted-foreground hover:bg-muted/10 transition-colors"
                    >
                      <span className="flex items-center gap-1.5">
                        <Info className="h-3.5 w-3.5 text-violet-400" />
                        Source Citations ({msg.sources.length})
                      </span>
                      {expandedSources[msg.id] ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />}
                    </button>

                    {expandedSources[msg.id] && (
                      <div className="p-3 border-t border-border bg-muted/5 divide-y divide-border space-y-2">
                        {msg.sources.map((src, sIdx) => (
                          <div key={sIdx} className="pt-2 first:pt-0 text-xs">
                            <div className="flex items-center justify-between mb-1.5">
                              <span className="font-semibold text-foreground">{src.documentTitle}</span>
                              <span className="text-[10px] bg-violet-500/10 text-violet-400 px-1.5 py-0.5 rounded-full">
                                Relevance: {Math.round(src.similarityScore * 100)}%
                              </span>
                            </div>
                            <p className="text-muted-foreground italic leading-relaxed text-[11px] bg-card p-2 rounded border border-border">
                              "{src.textPreview.trim()}"
                            </p>
                          </div>
                        ))}
                      </div>
                    )}
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
              <div className="flex items-center gap-3 text-sm text-muted-foreground bg-card border border-border p-4 rounded-2xl shadow-sm">
                <RefreshCw className="h-4 w-4 animate-spin text-primary" />
                <span>Thinking and retrieving chunks...</span>
                <button
                  onClick={handleStop}
                  className="ml-3 inline-flex items-center gap-1 text-xs text-red-400 hover:text-red-300 font-semibold transition-colors border border-red-500/20 px-2 py-1 rounded bg-red-500/5"
                >
                  <StopCircle className="h-3.5 w-3.5" /> Stop
                </button>
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
        <form onSubmit={(e) => handleSend(e)} className="p-4 border-t border-border bg-card">
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

      {/* Inspect Prompt Modal */}
      {inspectingPrompt !== null && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="relative w-full max-w-2xl rounded-xl border border-border bg-card shadow-2xl overflow-hidden flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between border-b border-border px-4 py-3 bg-muted/20">
              <h3 className="text-sm font-bold flex items-center gap-1.5">
                <Terminal className="h-4 w-4 text-violet-400" />
                Inspecting LLM Prompt Payload
              </h3>
              <button
                onClick={() => setInspectingPrompt(null)}
                className="text-xs font-semibold text-muted-foreground hover:text-foreground border border-border rounded px-2 py-1 bg-background hover:bg-muted/10 transition-colors"
              >
                Close
              </button>
            </div>
            <div className="p-4 overflow-y-auto flex-1 font-mono text-xs bg-muted/10 whitespace-pre-wrap leading-relaxed select-text">
              {inspectingPrompt}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
