import { useState, useEffect, useRef } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import axios from 'axios'
import {
  Search as SearchIcon,
  Sliders,
  Play,
  Award,
  Zap,
  Loader2,
  AlertCircle,
  Clock,
  Layers,
  Database,
  Trash2,
  FileText,
  MessageSquare,
  Cpu,
  RefreshCw,
  PlusCircle
} from 'lucide-react'

const BASE_URL = 'http://localhost:8080'

// 1. In-browser Principal Component Analysis (PCA) algorithm
function computePCA(data: number[][]): number[][] {
  const numSamples = data.length
  if (numSamples === 0) return []
  const numFeatures = data[0].length

  // Centering the vectors
  const mean = new Array(numFeatures).fill(0)
  for (let i = 0; i < numSamples; i++) {
    for (let j = 0; j < numFeatures; j++) {
      mean[j] += data[i][j]
    }
  }
  for (let j = 0; j < numFeatures; j++) {
    mean[j] /= numSamples
  }

  const centered = data.map(row => row.map((val, j) => val - mean[j]))

  // Covariance matrix
  const cov = Array.from({ length: numFeatures }, () => new Array(numFeatures).fill(0))
  for (let i = 0; i < numFeatures; i++) {
    for (let j = 0; j < numFeatures; j++) {
      let sum = 0
      for (let k = 0; k < numSamples; k++) {
        sum += centered[k][i] * centered[k][j]
      }
      cov[i][j] = sum / (numSamples - 1 || 1)
    }
  }

  // Power iteration helper for eigenvectors
  const getTopEigenvector = (matrix: number[][], numIterations = 100): number[] => {
    let vec = new Array(numFeatures).fill(0).map(() => Math.random() - 0.5)
    let norm = Math.sqrt(vec.reduce((sum, v) => sum + v * v, 0))
    vec = vec.map(v => v / (norm || 1))

    for (let iter = 0; iter < numIterations; iter++) {
      let nextVec = new Array(numFeatures).fill(0)
      for (let i = 0; i < numFeatures; i++) {
        for (let j = 0; j < numFeatures; j++) {
          nextVec[i] += matrix[i][j] * vec[j]
        }
      }
      let nextNorm = Math.sqrt(nextVec.reduce((sum, v) => sum + v * v, 0))
      vec = nextVec.map(v => v / (nextNorm || 1))
    }
    return vec
  }

  const pc1 = getTopEigenvector(cov)

  let lambda1 = 0
  const temp = new Array(numFeatures).fill(0)
  for (let i = 0; i < numFeatures; i++) {
    for (let j = 0; j < numFeatures; j++) {
      temp[i] += cov[i][j] * pc1[j]
    }
  }
  for (let i = 0; i < numFeatures; i++) {
    lambda1 += pc1[i] * temp[i]
  }

  const covDeflated = Array.from({ length: numFeatures }, () => new Array(numFeatures).fill(0))
  for (let i = 0; i < numFeatures; i++) {
    for (let j = 0; j < numFeatures; j++) {
      covDeflated[i][j] = cov[i][j] - lambda1 * pc1[i] * pc1[j]
    }
  }

  const pc2 = getTopEigenvector(covDeflated)

  // Project coordinates onto PC1 and PC2
  return centered.map(row => {
    let x = 0
    let y = 0
    for (let j = 0; j < numFeatures; j++) {
      x += row[j] * pc1[j]
      y += row[j] * pc2[j]
    }
    return [x, y]
  })
}

interface DemoItem {
  id: number
  concept: string
  category: string
  values: number[]
}

interface SearchResult {
  id: number
  concept: string
  category: string
  values: number[]
  distance: number
}

interface DocumentInfo {
  id: number
  title: string
  chunksCount: number
  uploadTime: string
}

export default function Dashboard() {
  const { user } = useAuth()
  const { pathname } = useLocation()
  const [activeTab, setActiveTab] = useState<'overview' | 'search' | 'documents' | 'chat'>(pathname === '/knowledge-map' ? 'search' : 'overview')
  const [ollamaStatus, setOllamaStatus] = useState({ status: 'OFFLINE', models: [] })
  const [dbStats, setDbStats] = useState({ demoVectorsCount: 0, documentsCount: 0, chunksCount: 0 })

  // Tab 1: Search States
  const [demoItems, setDemoItems] = useState<DemoItem[]>([])
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedAlgo, setSelectedAlgo] = useState<'hnsw' | 'kdtree' | 'bruteforce'>('hnsw')
  const [selectedMetric, setSelectedMetric] = useState<'cosine' | 'euclidean' | 'manhattan'>('cosine')
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [searching, setSearching] = useState(false)
  const [benchmarking, setBenchmarking] = useState(false)
  const [benchmarkData, setBenchmarkData] = useState<any | null>(null)
  
  // Custom Demo Insert Form
  const [newConcept, setNewConcept] = useState('')
  const [newCategory, setNewCategory] = useState('CS')
  const [insertingDemo, setInsertingDemo] = useState(false)

  // Tab 2: Document States
  const [docTitle, setDocTitle] = useState('')
  const [docText, setDocText] = useState('')
  const [insertingDoc, setInsertingDoc] = useState(false)
  const [documents, setDocuments] = useState<DocumentInfo[]>([])
  const [loadingDocs, setLoadingDocs] = useState(false)

  // Tab 3: RAG Chat States
  const [chatQuestion, setChatQuestion] = useState('')
  const [chatAnswer, setChatAnswer] = useState('')
  const [chatSources, setChatSources] = useState<any[]>([])
  const [chatting, setChatting] = useState(false)
  const [streamedAnswer, setStreamedAnswer] = useState('')
  const typewriterIntervalRef = useRef<NodeJS.Timeout | null>(null)
  
  // Tooltips & hover inside plot
  const [hoveredPoint, setHoveredPoint] = useState<any | null>(null)

  // PCA coordinates mapping
  const [pcaCoordinates, setPcaCoordinates] = useState<{ id: number; x: number; y: number }[]>([])

  useEffect(() => {
    fetchStats()
    fetchDemoItems()
    fetchDocuments()
  }, [])

  // Recalculate PCA whenever demoItems changes
  useEffect(() => {
    if (demoItems.length > 0) {
      const dataMatrix = demoItems.map(item => item.values)
      const projected = computePCA(dataMatrix)
      const mapped = demoItems.map((item, idx) => ({
        id: item.id,
        x: projected[idx][0],
        y: projected[idx][1]
      }))
      setPcaCoordinates(mapped)
    } else {
      setPcaCoordinates([])
    }
  }, [demoItems])

  const fetchStats = async () => {
    try {
      const statsRes = await axios.get(`${BASE_URL}/stats`)
      setDbStats({
        demoVectorsCount: statsRes.data.demoVectorsCount || 0,
        documentsCount: statsRes.data.documentsCount || 0,
        chunksCount: statsRes.data.chunksCount || 0
      })
      const statusRes = await axios.get(`${BASE_URL}/status`)
      setOllamaStatus(statusRes.data)
    } catch (err) {
      console.error('Failed to retrieve server statistics:', err)
    }
  }

  const fetchDemoItems = async () => {
    try {
      const res = await axios.get(`${BASE_URL}/items`)
      setDemoItems(res.data)
    } catch (err) {
      console.error('Failed to retrieve demo vectors:', err)
    }
  }

  const fetchDocuments = async () => {
    setLoadingDocs(true)
    try {
      const res = await axios.get(`${BASE_URL}/doc/list`)
      setDocuments(res.data)
    } catch (err) {
      console.error('Failed to retrieve document lists:', err)
    } finally {
      setLoadingDocs(false)
    }
  }

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!searchQuery.trim()) return
    setSearching(true)
    setBenchmarkData(null)
    try {
      const res = await axios.get(
        `${BASE_URL}/search?v=${encodeURIComponent(searchQuery)}&k=5&metric=${selectedMetric}&algo=${selectedAlgo}`
      )
      setSearchResults(res.data)
    } catch (err) {
      console.error('Search request failed:', err)
    } finally {
      setSearching(false)
    }
  }

  const runBenchmark = async () => {
    if (!searchQuery.trim()) return
    setBenchmarking(true)
    setSearchResults([])
    try {
      const res = await axios.get(
        `${BASE_URL}/benchmark?v=${encodeURIComponent(searchQuery)}&k=5&metric=${selectedMetric}`
      )
      setBenchmarkData(res.data)
    } catch (err) {
      console.error('Benchmark execution failed:', err)
    } finally {
      setBenchmarking(false)
    }
  }

  const handleInsertDemo = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newConcept.trim()) return
    setInsertingDemo(true)
    try {
      await axios.post(`${BASE_URL}/insert`, {
        concept: newConcept.trim(),
        category: newCategory
      })
      setNewConcept('')
      fetchDemoItems()
      fetchStats()
    } catch (err) {
      console.error('Demo item insertion failed:', err)
    } finally {
      setInsertingDemo(false)
    }
  }

  const handleDeleteDemo = async (id: number) => {
    try {
      await axios.delete(`${BASE_URL}/delete/${id}`)
      fetchDemoItems()
      fetchStats()
      // clear search hits matching deleted
      setSearchResults(prev => prev.filter(r => r.id !== id))
    } catch (err) {
      console.error('Demo item deletion failed:', err)
    }
  }

  const handleEmbedInsert = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!docTitle.trim() || !docText.trim()) return
    setInsertingDoc(true)
    try {
      await axios.post(`${BASE_URL}/doc/insert`, {
        title: docTitle.trim(),
        text: docText.trim()
      })
      setDocTitle('')
      setDocText('')
      fetchDocuments()
      fetchStats()
    } catch (err) {
      console.error('Document embedding creation failed:', err)
    } finally {
      setInsertingDoc(false)
    }
  }

  const handleDeleteDoc = async (id: number) => {
    try {
      await axios.delete(`${BASE_URL}/doc/delete/${id}`)
      fetchDocuments()
      fetchStats()
    } catch (err) {
      console.error('Document deletion failed:', err)
    }
  }

  const handleAskQuestion = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!chatQuestion.trim()) return
    setChatting(true)
    setChatAnswer('')
    setStreamedAnswer('')
    if (typewriterIntervalRef.current) clearInterval(typewriterIntervalRef.current)

    try {
      const res = await axios.post(`${BASE_URL}/doc/ask`, {
        question: chatQuestion.trim(),
        k: 3
      })
      setChatAnswer(res.data.answer)
      setChatSources(res.data.sources || [])
      
      // Simulate Typewriter Effect
      let currentLength = 0
      const fullText = res.data.answer || ''
      typewriterIntervalRef.current = setInterval(() => {
        if (currentLength < fullText.length) {
          currentLength += 2 // chunk sizes of chars
          setStreamedAnswer(fullText.substring(0, currentLength))
        } else {
          setStreamedAnswer(fullText)
          if (typewriterIntervalRef.current) clearInterval(typewriterIntervalRef.current)
        }
      }, 15)
    } catch (err) {
      console.error('Ask AI request failed:', err)
      setChatAnswer('An error occurred during generating answers. Please verify Ollama is serving.')
    } finally {
      setChatting(false)
    }
  }

  // Get color by category
  const getCategoryColor = (cat: string) => {
    switch (cat) {
      case 'CS': return '#818cf8' // Indigo
      case 'Math': return '#f472b6' // Pink
      case 'Food': return '#fb923c' // Amber
      case 'Sports': return '#34d399' // Emerald
      default: return '#94a3b8' // Slate
    }
  }

  // Scaling function for plot
  const scaleCoordinate = (val: number, isX: boolean, width = 500, height = 350) => {
    if (pcaCoordinates.length === 0) return 0
    const vals = pcaCoordinates.map(c => isX ? c.x : c.y)
    const min = Math.min(...vals)
    const max = Math.max(...vals)
    const range = max - min || 1
    
    // add padding
    const padding = 40
    if (isX) {
      return padding + ((val - min) / range) * (width - 2 * padding)
    } else {
      // SVG 0,0 is top left, invert y
      return height - padding - ((val - min) / range) * (height - 2 * padding)
    }
  }

  return (
    <div className="app-page lumora-lab space-y-7 min-h-screen">
      
      {/* Title Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 pb-7 border-b border-border">
        <div>
          <p className="eyebrow mb-2">{activeTab === 'overview' ? `Welcome back, ${user?.username || 'there'}` : 'Workspace overview'}</p>
          <h1 className="text-3xl font-semibold tracking-[-.04em] text-foreground">
            Vector intelligence, clearly.
          </h1>
          <p className="text-sm text-muted-foreground mt-2 max-w-2xl">
            Explore retrieval quality, document coverage, and the shape of your semantic space.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button 
            onClick={fetchStats}
            className="p-2 hover:bg-slate-800 rounded-lg text-slate-400 hover:text-slate-100 transition-colors"
            title="Refresh Server Stats"
          >
            <RefreshCw className="h-4 w-4" />
          </button>
          
          <div className={`flex items-center gap-2 px-3 py-1.5 rounded-lg border text-xs font-semibold ${
            ollamaStatus.status === 'ONLINE' 
              ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' 
              : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
          }`}>
            <span className={`h-2 w-2 rounded-full ${ollamaStatus.status === 'ONLINE' ? 'bg-emerald-400' : 'bg-rose-400 animate-pulse'}`} />
            Ollama: {ollamaStatus.status}
          </div>
        </div>
      </div>

      {/* Quiet system health summary */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-900/60 border border-slate-800 p-4 rounded-xl flex items-center gap-4">
          <div className="p-3 rounded-lg bg-indigo-500/10 text-indigo-400">
            <Database className="h-6 w-6" />
          </div>
          <div>
            <div className="text-2xl font-bold">{dbStats.demoVectorsCount}</div>
            <div className="text-xs text-slate-400 font-medium">Demo 16D Vectors</div>
          </div>
        </div>
        
        <div className="bg-slate-900/60 border border-slate-800 p-4 rounded-xl flex items-center gap-4">
          <div className="p-3 rounded-lg bg-purple-500/10 text-purple-400">
            <FileText className="h-6 w-6" />
          </div>
          <div>
            <div className="text-2xl font-bold">{dbStats.documentsCount}</div>
            <div className="text-xs text-slate-400 font-medium">RAG Documents</div>
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800 p-4 rounded-xl flex items-center gap-4">
          <div className="p-3 rounded-lg bg-pink-500/10 text-pink-400">
            <Layers className="h-6 w-6" />
          </div>
          <div>
            <div className="text-2xl font-bold">{dbStats.chunksCount}</div>
            <div className="text-xs text-slate-400 font-medium">Document Chunks</div>
          </div>
        </div>
      </div>

      {/* Tabs Switcher Navigation */}
      <div className="flex overflow-x-auto border-b border-slate-800">
        <button
          onClick={() => setActiveTab('overview')}
          className={`flex shrink-0 items-center gap-2 px-6 py-3 border-b-2 font-medium text-sm transition-all ${
            activeTab === 'overview'
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Layers className="h-4 w-4" /> Overview
        </button>
        <button
          onClick={() => setActiveTab('search')}
          className={`flex items-center gap-2 px-6 py-3 border-b-2 font-medium text-sm transition-all ${
            activeTab === 'search' 
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5' 
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <SearchIcon className="h-4 w-4" />
          Knowledge map
        </button>
        <button
          onClick={() => setActiveTab('documents')}
          className={`flex items-center gap-2 px-6 py-3 border-b-2 font-medium text-sm transition-all ${
            activeTab === 'documents' 
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5' 
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <FileText className="h-4 w-4" />
          Document library
        </button>
        <button
          onClick={() => setActiveTab('chat')}
          className={`flex items-center gap-2 px-6 py-3 border-b-2 font-medium text-sm transition-all ${
            activeTab === 'chat' 
              ? 'border-indigo-500 text-indigo-400 bg-indigo-500/5' 
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <MessageSquare className="h-4 w-4" />
          Ask Lumora
        </button>
      </div>

      {/* Tab Panels Contents */}
      <div className="mt-4">
        {activeTab === 'overview' && (
          <div className="grid gap-5 lg:grid-cols-[1.45fr_1fr]">
            <section className="surface p-6 md:p-8">
              <p className="eyebrow mb-3">Your next step</p>
              <h2 className="text-2xl font-semibold tracking-[-.035em] text-foreground">Turn your documents into answers.</h2>
              <p className="mt-3 max-w-xl text-sm leading-6 text-muted-foreground">Create a focused workspace, add source material, and ask Lumora questions with citations you can verify.</p>
              <div className="mt-7 flex flex-wrap gap-3">
                <Link to="/workspaces" className="inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground shadow-sm hover:opacity-95"><PlusCircle className="h-4 w-4" /> Manage workspaces</Link>
                <Link to="/documents" className="inline-flex items-center gap-2 rounded-xl border border-border bg-card px-4 py-2.5 text-sm font-semibold text-foreground hover:bg-secondary"><FileText className="h-4 w-4" /> Add documents</Link>
              </div>
            </section>
            <section className="surface p-6">
              <p className="eyebrow">Workspace health</p>
              <div className="mt-5 space-y-4">
                <div className="flex items-center justify-between"><span className="text-sm text-muted-foreground">Document coverage</span><span className="text-sm font-semibold text-foreground">{dbStats.documentsCount} sources</span></div>
                <div className="h-1.5 overflow-hidden rounded-full bg-secondary"><div className="h-full w-3/4 rounded-full bg-primary" /></div>
                <div className="flex items-center justify-between"><span className="text-sm text-muted-foreground">Index readiness</span><span className="text-sm font-semibold text-foreground">{dbStats.chunksCount} chunks</span></div>
                <div className="h-1.5 overflow-hidden rounded-full bg-secondary"><div className="h-full w-2/3 rounded-full bg-primary" /></div>
                <Link to="/analytics" className="mt-2 inline-flex items-center gap-1.5 text-sm font-semibold text-primary hover:underline">View retrieval insights <Play className="h-3.5 w-3.5" /></Link>
              </div>
            </section>
            <section className="surface p-6 lg:col-span-2">
              <div className="flex flex-wrap items-end justify-between gap-4"><div><p className="eyebrow mb-2">Explore</p><h2 className="text-lg font-semibold tracking-[-.02em]">Choose a focused workflow</h2></div><Link to="/search" className="text-sm font-semibold text-primary hover:underline">Open semantic search</Link></div>
              <div className="mt-5 grid gap-3 md:grid-cols-3">
                <Link to="/documents" className="rounded-xl border border-border bg-muted/20 p-4 hover:bg-secondary"><FileText className="h-4 w-4 text-primary" /><h3 className="mt-4 text-sm font-semibold">Organize sources</h3><p className="mt-1 text-xs leading-5 text-muted-foreground">Upload and review material in one place.</p></Link>
                <Link to="/chat" className="rounded-xl border border-border bg-muted/20 p-4 hover:bg-secondary"><MessageSquare className="h-4 w-4 text-primary" /><h3 className="mt-4 text-sm font-semibold">Ask with context</h3><p className="mt-1 text-xs leading-5 text-muted-foreground">Get grounded responses with source references.</p></Link>
                <Link to="/benchmark" className="rounded-xl border border-border bg-muted/20 p-4 hover:bg-secondary"><Award className="h-4 w-4 text-primary" /><h3 className="mt-4 text-sm font-semibold">Evaluate retrieval</h3><p className="mt-1 text-xs leading-5 text-muted-foreground">Compare search strategies with confidence.</p></Link>
              </div>
            </section>
          </div>
        )}
        
        {/* TAB 1: Search (Demo Vectors) */}
        {activeTab === 'search' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            
            {/* Left Control Panel */}
            <div className="lg:col-span-5 space-y-6">
              
              <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-2xl space-y-4">
                <h3 className="text-lg font-bold flex items-center gap-2">
                  <Sliders className="h-5 w-5 text-indigo-400" />
                  Search Parameters
                </h3>
                
                <form onSubmit={handleSearch} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                      Query Concept
                    </label>
                    <input
                      type="text"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      placeholder="Type a concept, e.g. binary tree, sushi"
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition-colors"
                    />
                    
                    {/* Quick suggestion tags */}
                    <div className="flex flex-wrap gap-2 mt-2">
                      {['binary tree', 'calculus', 'sushi', 'basketball'].map((tag) => (
                        <button
                          key={tag}
                          type="button"
                          onClick={() => setSearchQuery(tag)}
                          className="text-[10px] font-semibold bg-slate-800/80 hover:bg-indigo-500/10 hover:text-indigo-400 border border-slate-700/50 hover:border-indigo-500/20 px-2 py-1 rounded-md text-slate-300 transition-colors"
                        >
                          {tag}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                        Algorithm
                      </label>
                      <select
                        value={selectedAlgo}
                        onChange={(e: any) => setSelectedAlgo(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition-colors"
                      >
                        <option value="hnsw">HNSW</option>
                        <option value="kdtree">KD-Tree</option>
                        <option value="bruteforce">Brute Force</option>
                      </select>
                    </div>

                    <div>
                      <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                        Metric
                      </label>
                      <select
                        value={selectedMetric}
                        onChange={(e: any) => setSelectedMetric(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition-colors"
                      >
                        <option value="cosine">Cosine</option>
                        <option value="euclidean">Euclidean</option>
                        <option value="manhattan">Manhattan</option>
                      </select>
                    </div>
                  </div>

                  <div className="flex gap-3 pt-2">
                    <button
                      type="submit"
                      disabled={searching || benchmarking}
                      className="flex-1 bg-indigo-600 hover:bg-indigo-500 disabled:bg-indigo-800 disabled:opacity-50 text-white font-semibold py-2 px-4 rounded-xl text-sm flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/10 transition-colors cursor-pointer"
                    >
                      {searching ? <Loader2 className="h-4 w-4 animate-spin" /> : <Zap className="h-4 w-4" />}
                      Search
                    </button>
                    
                    <button
                      type="button"
                      onClick={runBenchmark}
                      disabled={searching || benchmarking}
                      className="bg-slate-800 hover:bg-slate-700 disabled:bg-slate-900 border border-slate-700 hover:border-slate-600 text-slate-200 font-semibold py-2 px-4 rounded-xl text-sm flex items-center justify-center gap-2 transition-all cursor-pointer"
                    >
                      {benchmarking ? <Loader2 className="h-4 w-4 animate-spin" /> : <Play className="h-4 w-4" />}
                      Compare All Algos
                    </button>
                  </div>
                </form>
              </div>

              {/* Single Search Results List */}
              {searchResults.length > 0 && (
                <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-2xl space-y-4">
                  <h3 className="text-lg font-bold flex items-center gap-2">
                    <Award className="h-5 w-5 text-amber-400" />
                    Nearest Neighbors Results
                  </h3>
                  
                  <div className="space-y-3">
                    {searchResults.map((hit, idx) => (
                      <div key={hit.id} className="bg-slate-950 border border-slate-800/80 p-3.5 rounded-xl flex justify-between items-center hover:border-indigo-500/30 transition-colors">
                        <div>
                          <div className="flex items-center gap-2">
                            <span className="text-xs font-semibold text-slate-500">#{idx + 1}</span>
                            <span className="font-bold text-slate-100">{hit.concept}</span>
                            <span 
                              className="text-[10px] px-2 py-0.5 rounded-full font-bold" 
                              style={{ backgroundColor: `${getCategoryColor(hit.category)}20`, color: getCategoryColor(hit.category) }}
                            >
                              {hit.category}
                            </span>
                          </div>
                          <div className="text-[10px] text-slate-500 font-mono mt-1 mt-0.5 overflow-hidden text-ellipsis whitespace-nowrap max-w-[240px]">
                            v = [{hit.values.map(val => val.toFixed(2)).join(', ')}]
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Distance</div>
                          <div className="text-sm font-mono font-bold text-indigo-400">{hit.distance.toFixed(4)}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Benchmark Latency Comparison Results */}
              {benchmarkData && (
                <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-2xl space-y-4">
                  <h3 className="text-lg font-bold flex items-center gap-2">
                    <Clock className="h-5 w-5 text-indigo-400" />
                    Speed Benchmark Latency
                  </h3>
                  <p className="text-xs text-slate-400">
                    Runs 1,000 queries sequentially for each algorithm. Notice how HNSW zooms through high-dimensional indexes.
                  </p>
                  
                  <div className="space-y-4 pt-2">
                    {['hnsw', 'kdtree', 'bruteforce'].map((algo) => {
                      const lat = benchmarkData[algo]?.timeMs || 0
                      const displayNames: any = { hnsw: 'HNSW Graph', kdtree: 'KD-Tree Partition', bruteforce: 'Brute Force Scan' }
                      // Find max latency for scaling
                      const maxLat = Math.max(benchmarkData.hnsw?.timeMs || 0.1, benchmarkData.kdtree?.timeMs || 0.1, benchmarkData.bruteforce?.timeMs || 0.1)
                      const pct = Math.max(5, (lat / maxLat) * 100)

                      return (
                        <div key={algo} className="space-y-1">
                          <div className="flex justify-between text-xs font-semibold">
                            <span className="text-slate-200">{displayNames[algo]}</span>
                            <span className="text-indigo-400 font-mono font-bold">{lat.toFixed(4)} ms / query</span>
                          </div>
                          <div className="w-full bg-slate-950 h-2.5 rounded-full overflow-hidden border border-slate-800">
                            <div 
                              className={`h-full rounded-full transition-all duration-500 ${
                                algo === 'hnsw' ? 'bg-gradient-to-r from-emerald-500 to-teal-400' :
                                algo === 'kdtree' ? 'bg-gradient-to-r from-indigo-500 to-indigo-400' :
                                'bg-gradient-to-r from-rose-500 to-pink-500'
                              }`} 
                              style={{ width: `${pct}%` }} 
                            />
                          </div>
                        </div>
                      )
                    })}
                  </div>
                </div>
              )}

            </div>

            {/* Right Plot & Demo Custom Forms */}
            <div className="lg:col-span-7 space-y-6">
              
              {/* SVG 2D PCA Scatter Plot */}
              <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-2xl relative">
                <h3 className="text-lg font-bold flex items-center gap-2 mb-2">
                  <Cpu className="h-5 w-5 text-indigo-400" />
                  PCA Dimensionality Reduction Scatter Plot (16D → 2D)
                </h3>
                <p className="text-xs text-slate-400 mb-4">
                  All 16-dimensional vectors are projected into a 2D coordinate plane on the fly using Principal Component Analysis. Watch categories naturally cluster!
                </p>

                <div className="relative border border-slate-800 rounded-xl bg-slate-950/80 p-2 overflow-hidden flex justify-center">
                  <svg width="500" height="350" className="overflow-visible">
                    {/* Grid lines */}
                    <line x1="40" y1="175" x2="460" y2="175" stroke="#1e293b" strokeWidth="1" strokeDasharray="3,3" />
                    <line x1="250" y1="40" x2="250" y2="310" stroke="#1e293b" strokeWidth="1" strokeDasharray="3,3" />

                    {/* Plotted Points */}
                    {pcaCoordinates.map((coord) => {
                      const item = demoItems.find(i => i.id === coord.id)
                      if (!item) return null

                      // Check if point is inside search hit
                      const hitIndex = searchResults.findIndex(r => r.id === coord.id)
                      const isHit = hitIndex !== -1
                      const isTopHit = hitIndex === 0

                      const cx = scaleCoordinate(coord.x, true)
                      const cy = scaleCoordinate(coord.y, false)
                      const color = getCategoryColor(item.category)

                      return (
                        <g 
                          key={coord.id}
                          className="cursor-pointer"
                          onMouseEnter={() => setHoveredPoint({ ...item, cx, cy })}
                          onMouseLeave={() => setHoveredPoint(null)}
                        >
                          {/* Pulse Glow Effect for Hits */}
                          {isHit && (
                            <circle
                              cx={cx}
                              cy={cy}
                              r={isTopHit ? 14 : 10}
                              fill={color}
                              fillOpacity="0.15"
                              className="animate-ping"
                            />
                          )}

                          {/* Outer ring for hit identification */}
                          {isHit && (
                            <circle
                              cx={cx}
                              cy={cy}
                              r={isTopHit ? 9 : 7}
                              fill="none"
                              stroke={color}
                              strokeWidth="2"
                              strokeDasharray={isTopHit ? "0" : "2,2"}
                            />
                          )}

                          {/* Core Vector Node Point */}
                          <circle
                            cx={cx}
                            cy={cy}
                            r={isTopHit ? 6.5 : 5}
                            fill={color}
                            className="transition-all duration-300 hover:scale-150"
                          />
                        </g>
                      )
                    })}
                  </svg>

                  {/* Tooltip Overlay */}
                  {hoveredPoint && (
                    <div 
                      className="absolute bg-slate-900 border border-slate-700 text-slate-100 p-2.5 rounded-lg text-xs font-semibold shadow-xl z-50 pointer-events-none"
                      style={{ 
                        left: `${hoveredPoint.cx + 15}px`, 
                        top: `${hoveredPoint.cy - 10}px` 
                      }}
                    >
                      <div className="font-bold text-indigo-300">{hoveredPoint.concept}</div>
                      <div className="text-[10px] text-slate-400 mt-0.5">Category: {hoveredPoint.category}</div>
                    </div>
                  )}
                </div>

                {/* Plot Legends */}
                <div className="flex flex-wrap gap-4 mt-4 justify-center text-xs">
                  {['CS', 'Math', 'Food', 'Sports'].map(cat => (
                    <div key={cat} className="flex items-center gap-1.5 font-semibold">
                      <span className="h-3 w-3 rounded-full" style={{ backgroundColor: getCategoryColor(cat) }} />
                      <span className="text-slate-300">{cat}</span>
                    </div>
                  ))}
                  <div className="flex items-center gap-1.5 font-semibold border-l border-slate-800 pl-4">
                    <span className="h-3 w-3 rounded-full border border-dashed border-indigo-400" />
                    <span className="text-slate-400">Search Results</span>
                  </div>
                </div>

              </div>

              {/* Insert Demo Form & Table list */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                
                {/* Insert custom Demo Vector Form */}
                <div className="bg-slate-900/60 border border-slate-800 p-4 rounded-2xl">
                  <h4 className="text-sm font-bold flex items-center gap-1.5 text-indigo-300 mb-3">
                    <PlusCircle className="h-4.5 w-4.5" />
                    Insert Demo Vector
                  </h4>
                  
                  <form onSubmit={handleInsertDemo} className="space-y-3">
                    <div>
                      <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Concept Name</label>
                      <input
                        type="text"
                        value={newConcept}
                        onChange={(e) => setNewConcept(e.target.value)}
                        placeholder="e.g. deep learning"
                        className="w-full bg-slate-950 border border-slate-850 rounded-lg px-3 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-indigo-500 transition-colors"
                      />
                    </div>
                    
                    <div>
                      <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Category</label>
                      <select
                        value={newCategory}
                        onChange={(e) => setNewCategory(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-850 rounded-lg px-2.5 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-indigo-500 transition-colors"
                      >
                        <option value="CS">CS (Computer Science)</option>
                        <option value="Math">Math (Mathematics)</option>
                        <option value="Food">Food (Culinary)</option>
                        <option value="Sports">Sports (Athletics)</option>
                      </select>
                    </div>

                    <button
                      type="submit"
                      disabled={insertingDemo}
                      className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-1.5 px-3 rounded-lg text-xs flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                    >
                      {insertingDemo && <Loader2 className="h-3 w-3 animate-spin" />}
                      Add Vector
                    </button>
                  </form>
                </div>

                {/* List of demo vectors with delete */}
                <div className="bg-slate-900/60 border border-slate-800 p-4 rounded-2xl flex flex-col">
                  <h4 className="text-sm font-bold flex items-center gap-1.5 text-indigo-300 mb-3">
                    <Database className="h-4.5 w-4.5" />
                    Manage Items
                  </h4>
                  
                  <div className="flex-1 overflow-y-auto max-h-[160px] space-y-2 pr-1 custom-scrollbar">
                    {demoItems.map(item => (
                      <div key={item.id} className="flex justify-between items-center bg-slate-950 px-2.5 py-1.5 rounded-lg border border-slate-900/60 text-xs">
                        <div className="flex items-center gap-2 overflow-hidden">
                          <span className="h-2 w-2 rounded-full flex-shrink-0" style={{ backgroundColor: getCategoryColor(item.category) }} />
                          <span className="font-bold text-slate-200 overflow-hidden text-ellipsis whitespace-nowrap max-w-[120px]">{item.concept}</span>
                        </div>
                        <button
                          onClick={() => handleDeleteDemo(item.id)}
                          className="text-slate-500 hover:text-rose-400 p-1 hover:bg-rose-500/5 rounded-md transition-all cursor-pointer"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>

              </div>

            </div>

          </div>
        )}

        {/* TAB 2: Documents (Real Embeddings) */}
        {activeTab === 'documents' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
            
            {/* Form Column */}
            <div className="lg:col-span-5 space-y-6">
              
              <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-2xl space-y-4">
                <h3 className="text-lg font-bold flex items-center gap-2">
                  <PlusCircle className="h-5 w-5 text-indigo-400" />
                  Embed & Index Text Document
                </h3>
                <p className="text-xs text-slate-400">
                  Type or paste any textbook content. Lumora will chunk the text, call Ollama to compute 768D embeddings, and populate a separate HNSW index.
                </p>
                
                <form onSubmit={handleEmbedInsert} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                      Document Title
                    </label>
                    <input
                      type="text"
                      required
                      value={docTitle}
                      onChange={(e) => setDocTitle(e.target.value)}
                      placeholder="e.g., Operating Systems Notes"
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition-colors"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                      Document Text Contents
                    </label>
                    <textarea
                      required
                      rows={8}
                      value={docText}
                      onChange={(e) => setDocText(e.target.value)}
                      placeholder="Paste your text study notes, articles, or lecture files here..."
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition-colors font-sans resize-none"
                    />
                  </div>

                  <button
                    type="submit"
                    disabled={insertingDoc}
                    className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:bg-indigo-800 disabled:opacity-50 text-white font-semibold py-2.5 px-4 rounded-xl text-sm flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/10 transition-colors cursor-pointer"
                  >
                    {insertingDoc ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Generating 768D Embeddings...
                      </>
                    ) : (
                      <>
                        <Zap className="h-4 w-4" />
                        ⚡ EMBED & INSERT
                      </>
                    )}
                  </button>
                </form>
              </div>

            </div>

            {/* List Documents Column */}
            <div className="lg:col-span-7">
              
              <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-2xl space-y-4">
                <h3 className="text-lg font-bold flex items-center gap-2">
                  <Database className="h-5 w-5 text-indigo-400" />
                  Indexed Documents
                </h3>
                
                {loadingDocs ? (
                  <div className="flex justify-center items-center py-12">
                    <Loader2 className="h-8 w-8 text-indigo-500 animate-spin" />
                  </div>
                ) : documents.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12 text-slate-500 text-sm">
                    <AlertCircle className="h-8 w-8 mb-2 text-slate-600" />
                    No documents indexed yet. Use the left form to upload contents.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {documents.map((doc) => (
                      <div key={doc.id} className="bg-slate-950 border border-slate-800/80 p-4 rounded-xl flex justify-between items-center hover:border-slate-700 transition-colors">
                        <div className="space-y-1">
                          <div className="font-bold text-slate-100 flex items-center gap-2">
                            <FileText className="h-4 w-4 text-indigo-400" />
                            {doc.title}
                          </div>
                          <div className="flex items-center gap-4 text-xs text-slate-400 font-medium">
                            <span className="flex items-center gap-1">
                              <Layers className="h-3.5 w-3.5 text-slate-500" />
                              {doc.chunksCount} overlapping chunks
                            </span>
                            <span className="flex items-center gap-1">
                              <Clock className="h-3.5 w-3.5 text-slate-500" />
                              {new Date(doc.uploadTime).toLocaleDateString()}
                            </span>
                          </div>
                        </div>
                        
                        <button
                          onClick={() => handleDeleteDoc(doc.id)}
                          className="text-slate-500 hover:text-rose-400 p-2 hover:bg-rose-500/5 rounded-lg transition-all cursor-pointer"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

            </div>

          </div>
        )}

        {/* TAB 3: Ask AI (RAG Pipeline) */}
        {activeTab === 'chat' && (
          <div className="space-y-6">
            
            <div className="bg-slate-900/60 border border-slate-800 p-5 rounded-2xl space-y-4 max-w-3xl mx-auto">
              <h3 className="text-lg font-bold flex items-center gap-2">
                <Cpu className="h-5 w-5 text-indigo-400" />
                Retrieval-Augmented Generation (RAG)
              </h3>
              <p className="text-xs text-slate-400">
                Ask any question about your indexed documents. HNSW will perform semantic search, fetch the closest 3 chunks, and send them as grounding context to the local LLM.
              </p>
              
              <form onSubmit={handleAskQuestion} className="flex gap-3">
                <input
                  type="text"
                  required
                  value={chatQuestion}
                  onChange={(e) => setChatQuestion(e.target.value)}
                  placeholder="Type a question, e.g. What is memory management?"
                  className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition-colors"
                />
                <button
                  type="submit"
                  disabled={chatting || documents.length === 0}
                  className="bg-indigo-600 hover:bg-indigo-500 disabled:bg-indigo-800 disabled:opacity-50 text-white font-semibold py-2 px-5 rounded-xl text-sm flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/10 transition-colors cursor-pointer"
                >
                  {chatting ? <Loader2 className="h-4 w-4 animate-spin" /> : <MessageSquare className="h-4 w-4" />}
                  🤖 ASK AI
                </button>
              </form>
              
              {documents.length === 0 && (
                <div className="flex items-center gap-2 text-xs text-amber-400 bg-amber-500/10 border border-amber-500/20 p-2.5 rounded-lg mt-2">
                  <AlertCircle className="h-4 w-4" />
                  Warning: No documents inserted yet! Insert documents in Tab 2 before asking questions.
                </div>
              )}
            </div>

            {/* Answer & Citations output card */}
            {(chatAnswer || chatting) && (
              <div className="bg-slate-900/60 border border-slate-800 p-6 rounded-2xl space-y-6 max-w-3xl mx-auto">
                
                {/* Chat Answer Block */}
                <div className="space-y-2">
                  <h4 className="text-sm font-bold uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
                    🤖 AI Synthesis Answer
                  </h4>
                  {chatting && !streamedAnswer ? (
                    <div className="flex items-center gap-2 text-slate-400 py-4">
                      <Loader2 className="h-4 w-4 animate-spin text-indigo-500" />
                      Retrieving context & running LLM inference...
                    </div>
                  ) : (
                    <div className="bg-slate-950/80 border border-slate-850 p-4 rounded-xl text-sm leading-relaxed text-slate-200 font-sans whitespace-pre-wrap">
                      {streamedAnswer}
                      {chatting && <span className="inline-block h-3 w-1.5 bg-indigo-400 ml-1 animate-pulse" />}
                    </div>
                  )}
                </div>

                {/* Collapsible sources list */}
                {chatSources.length > 0 && (
                  <div className="space-y-3 border-t border-slate-800/80 pt-4">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                      Grounding Context Chunks (Retrieved from HNSW)
                    </h4>
                    
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                      {chatSources.map((source, index) => (
                        <div 
                          key={index} 
                          className="bg-slate-950 border border-slate-850/80 p-3 rounded-xl space-y-2 flex flex-col justify-between"
                        >
                          <div className="space-y-1">
                            <div className="flex items-center justify-between">
                              <span className="text-[10px] font-bold text-indigo-400 bg-indigo-500/10 px-1.5 py-0.5 rounded">
                                Chip #{index + 1}
                              </span>
                              <span className="text-[10px] font-mono text-slate-500">
                                Sim: {source.similarityScore.toFixed(3)}
                              </span>
                            </div>
                            <div className="text-[11px] text-slate-400 font-medium line-clamp-3 italic">
                              "{source.textPreview}"
                            </div>
                          </div>
                          
                          <div className="text-[10px] text-slate-500 border-t border-slate-900 pt-2 font-bold overflow-hidden text-ellipsis whitespace-nowrap">
                            Doc: {source.documentTitle}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

              </div>
            )}

          </div>
        )}

      </div>

    </div>
  )
}
