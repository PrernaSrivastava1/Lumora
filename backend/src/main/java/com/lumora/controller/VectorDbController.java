package com.lumora.controller;

import com.lumora.algorithms.common.vector.DistanceCalculator;
import com.lumora.algorithms.common.vector.DistanceCalculatorFactory;
import com.lumora.algorithms.common.vector.Vector;
import com.lumora.algorithms.hnsw.HnswIndex;
import com.lumora.algorithms.kdtree.KdTree;
import com.lumora.dto.AnswerResponse;
import com.lumora.dto.RagRequest;
import com.lumora.embedding.OllamaEmbeddingProvider;
import com.lumora.index.IndexManager;
import com.lumora.model.*;
import com.lumora.repository.*;
import com.lumora.service.RagService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class VectorDbController {

    private static final List<DemoItem> DEMO_ITEMS = new CopyOnWriteArrayList<>();

    static {
        String[][] concepts = {
            {"binary tree", "CS"},
            {"compiler", "CS"},
            {"database", "CS"},
            {"algorithm", "CS"},
            {"operating system", "CS"},
            
            {"calculus", "Math"},
            {"linear algebra", "Math"},
            {"geometry", "Math"},
            {"probability", "Math"},
            {"differential equation", "Math"},
            
            {"sushi", "Food"},
            {"pizza", "Food"},
            {"hamburger", "Food"},
            {"pasta", "Food"},
            {"taco", "Food"},
            
            {"basketball", "Sports"},
            {"soccer", "Sports"},
            {"tennis", "Sports"},
            {"baseball", "Sports"},
            {"volleyball", "Sports"}
        };
        
        java.util.Random rand = new java.util.Random(1337);
        long idCounter = 1;
        for (String[] concept : concepts) {
            String name = concept[0];
            String cat = concept[1];
            float[] vals = new float[16];
            int startDim = 0;
            if (cat.equals("CS")) startDim = 0;
            else if (cat.equals("Math")) startDim = 4;
            else if (cat.equals("Food")) startDim = 8;
            else if (cat.equals("Sports")) startDim = 12;
            
            for (int i = 0; i < 16; i++) {
                if (i >= startDim && i < startDim + 4) {
                    vals[i] = 0.65f + rand.nextFloat() * 0.35f;
                } else {
                    vals[i] = rand.nextFloat() * 0.1f;
                }
            }
            // Normalize
            float lenSq = 0f;
            for (float f : vals) lenSq += f * f;
            float len = (float) Math.sqrt(lenSq);
            for (int i = 0; i < 16; i++) vals[i] /= len;
            
            DEMO_ITEMS.add(new DemoItem(idCounter++, name, cat, vals));
        }
    }

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final VectorEmbeddingRepository embeddingRepository;
    private final VectorStore vectorStore;
    private final IndexManager indexManager;
    private final OllamaEmbeddingProvider embeddingProvider;
    private final RagService ragService;
    private final RestTemplate restTemplate;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    public VectorDbController(WorkspaceRepository workspaceRepository,
                              UserRepository userRepository,
                              DocumentRepository documentRepository,
                              ChunkRepository chunkRepository,
                              VectorEmbeddingRepository embeddingRepository,
                              VectorStore vectorStore,
                              IndexManager indexManager,
                              OllamaEmbeddingProvider embeddingProvider,
                              RagService ragService,
                              @Qualifier("ollamaRestTemplate") RestTemplate restTemplate) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingRepository = embeddingRepository;
        this.vectorStore = vectorStore;
        this.indexManager = indexManager;
        this.embeddingProvider = embeddingProvider;
        this.ragService = ragService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/items")
    public List<DemoItem> getItems() {
        return DEMO_ITEMS;
    }

    @PostMapping("/insert")
    public DemoItem insertItem(@RequestBody DemoItem item) {
        if (item.getValues() == null || item.getValues().length != 16) {
            float[] vals = new float[16];
            java.util.Random r = new java.util.Random();
            for (int i = 0; i < 16; i++) {
                vals[i] = r.nextFloat() * 2 - 1;
            }
            float sumSq = 0;
            for (float f : vals) sumSq += f * f;
            float len = (float) Math.sqrt(sumSq);
            if (len > 0) {
                for (int i = 0; i < 16; i++) vals[i] /= len;
            }
            item.setValues(vals);
        }
        long maxId = DEMO_ITEMS.stream().mapToLong(DemoItem::getId).max().orElse(0L);
        item.setId(maxId + 1);
        DEMO_ITEMS.add(item);
        return item;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteItem(@PathVariable Long id) {
        boolean removed = DEMO_ITEMS.removeIf(item -> item.getId().equals(id));
        return Map.of("success", removed, "deletedId", id);
    }

    @DeleteMapping("/delete/:id")
    public Map<String, Object> deleteItemAlt(@PathVariable Long id) {
        return deleteItem(id);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String v,
                                            @RequestParam(defaultValue = "5") int k,
                                            @RequestParam(defaultValue = "cosine") String metric,
                                            @RequestParam(defaultValue = "hnsw") String algo) {
        float[] queryVals = parseQueryVector(v);
        return executeSingleSearch(queryVals, k, metric, algo);
    }

    @GetMapping("/benchmark")
    public Map<String, Object> benchmark(@RequestParam String v,
                                         @RequestParam(defaultValue = "5") int k,
                                         @RequestParam(defaultValue = "cosine") String metric) {
        float[] queryVals = parseQueryVector(v);
        
        // Warm up and benchmark HNSW
        long startHnsw = System.nanoTime();
        List<Map<String, Object>> hnswResults = null;
        for (int i = 0; i < 1000; i++) {
            hnswResults = executeSingleSearch(queryVals, k, metric, "hnsw");
        }
        double hnswTime = ((System.nanoTime() - startHnsw) / 1000.0) / 1000000.0;

        // Warm up and benchmark KD-Tree
        long startKd = System.nanoTime();
        List<Map<String, Object>> kdResults = null;
        for (int i = 0; i < 1000; i++) {
            kdResults = executeSingleSearch(queryVals, k, metric, "kdtree");
        }
        double kdTime = ((System.nanoTime() - startKd) / 1000.0) / 1000000.0;

        // Warm up and benchmark Brute Force
        long startBf = System.nanoTime();
        List<Map<String, Object>> bfResults = null;
        for (int i = 0; i < 1000; i++) {
            bfResults = executeSingleSearch(queryVals, k, metric, "bruteforce");
        }
        double bfTime = ((System.nanoTime() - startBf) / 1000.0) / 1000000.0;

        Map<String, Object> res = new HashMap<>();
        res.put("query", queryVals);
        res.put("k", k);
        res.put("metric", metric);
        res.put("hnsw", Map.of("timeMs", hnswTime, "results", hnswResults));
        res.put("kdtree", Map.of("timeMs", kdTime, "results", kdResults));
        res.put("bruteforce", Map.of("timeMs", bfTime, "results", bfResults));
        return res;
    }

    @GetMapping("/hnsw-info")
    public Map<String, Object> getHnswInfo() {
        List<Vector> vectors = DEMO_ITEMS.stream()
                .map(item -> new Vector(item.getId(), item.getValues(), null))
                .collect(Collectors.toList());

        DistanceCalculator calculator = DistanceCalculatorFactory.getCalculator(DistanceMetric.COSINE);
        HnswIndex index = new HnswIndex();
        index.build(vectors, calculator);

        Map<String, Object> info = new HashMap<>();
        info.put("maxLevel", index.getMaxLevel());
        info.put("enterPointId", index.getEnterPoint() != null ? index.getEnterPoint().vector.getId() : null);

        // Nodes stats
        List<Map<String, Object>> nodeDetails = new ArrayList<>();
        Map<Integer, Integer> layerCounts = new HashMap<>();

        for (Map.Entry<Long, HnswIndex.HnswNode> entry : index.getNodes().entrySet()) {
            HnswIndex.HnswNode node = entry.getValue();
            layerCounts.put(node.level, layerCounts.getOrDefault(node.level, 0) + 1);

            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("id", node.vector.getId());
            String conceptName = DEMO_ITEMS.stream()
                    .filter(item -> item.getId().equals(node.vector.getId()))
                    .map(DemoItem::getConcept)
                    .findFirst()
                    .orElse("custom");
            nodeMap.put("concept", conceptName);
            nodeMap.put("level", node.level);

            List<List<Long>> conns = new ArrayList<>();
            for (int i = 0; i <= node.level; i++) {
                List<Long> lvlConns = new ArrayList<>();
                if (i < node.connections.size()) {
                    for (HnswIndex.HnswNode c : node.connections.get(i)) {
                        lvlConns.add(c.vector.getId());
                    }
                }
                conns.add(lvlConns);
            }
            nodeMap.put("connections", conns);
            nodeDetails.add(nodeMap);
        }

        info.put("nodes", nodeDetails);
        info.put("layerStats", layerCounts);
        return info;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Workspace ws = getOrCreateWorkspace();
        long docCount = documentRepository.count();
        long chunkCount = chunkRepository.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("demoVectorsCount", DEMO_ITEMS.size());
        stats.put("documentsCount", docCount);
        stats.put("chunksCount", chunkCount);
        stats.put("ollamaOnline", checkOllamaOnline());
        return stats;
    }

    @PostMapping("/doc/insert")
    public Map<String, Object> insertDoc(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String text = body.get("text");
        if (title == null || title.isBlank() || text == null || text.isBlank()) {
            throw new IllegalArgumentException("Title and text are required");
        }

        Workspace workspace = getOrCreateWorkspace();

        Document doc = Document.builder()
                .workspace(workspace)
                .title(title)
                .originalFileName(title)
                .fileType("TXT")
                .size((long) text.getBytes().length)
                .uploadTime(LocalDateTime.now())
                .processingStatus(ProcessingStatus.READY)
                .totalChunks(0)
                .build();
        doc = documentRepository.save(doc);

        List<String> words = splitIntoOverlappingChunks(text, 250, 50);
        int chunkIndex = 0;
        for (String content : words) {
            DocumentChunk chunk = DocumentChunk.builder()
                    .document(doc)
                    .chunkIndex(chunkIndex++)
                    .content(content)
                    .tokenCount(content.split("\\s+").length)
                    .startChar(0)
                    .endChar(content.length())
                    .build();
            chunk = chunkRepository.save(chunk);

            float[] embeddingVals = embeddingProvider.embed(content);
            VectorEmbedding embedding = VectorEmbedding.builder()
                    .chunk(chunk)
                    .dimensions(embeddingVals.length)
                    .modelName("nomic-embed-text")
                    .build();
            embedding.setVectorFromFloats(embeddingVals);
            embeddingRepository.save(embedding);
        }

        doc.setTotalChunks(chunkIndex);
        documentRepository.save(doc);

        // Load into Vector Index Manager
        indexManager.loadWorkspaceVectors(workspace.getId());

        return Map.of("success", true, "documentId", doc.getId(), "chunks", chunkIndex);
    }

    @GetMapping("/doc/list")
    public List<Map<String, Object>> getDocs() {
        return documentRepository.findAll().stream()
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", doc.getId());
                    map.put("title", doc.getTitle());
                    map.put("chunksCount", doc.getTotalChunks());
                    map.put("uploadTime", doc.getUploadTime() != null ? doc.getUploadTime().toString() : LocalDateTime.now().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @DeleteMapping("/doc/delete/{id}")
    public Map<String, Object> deleteDoc(@PathVariable Long id) {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isPresent()) {
            Document doc = docOpt.get();
            List<DocumentChunk> chunks = chunkRepository.findByDocumentId(id);
            for (DocumentChunk chunk : chunks) {
                embeddingRepository.deleteByChunkId(chunk.getId());
                chunkRepository.delete(chunk);
            }
            documentRepository.delete(doc);
            
            // Reload index
            indexManager.loadWorkspaceVectors(doc.getWorkspace().getId());
            return Map.of("success", true, "deletedId", id);
        }
        return Map.of("success", false, "message", "Document not found");
    }

    @DeleteMapping("/doc/delete/:id")
    public Map<String, Object> deleteDocAlt(@PathVariable Long id) {
        return deleteDoc(id);
    }

    @PostMapping("/doc/ask")
    public AnswerResponse askQuestion(@RequestBody Map<String, Object> body) {
        String question = (String) body.get("question");
        int k = body.containsKey("k") ? ((Number) body.get("k")).intValue() : 3;

        Workspace workspace = getOrCreateWorkspace();
        RagRequest request = new RagRequest();
        request.setWorkspaceId(workspace.getId());
        request.setQuery(question);
        request.setAlgorithm("HNSW");
        request.setTopK(k);
        request.setHistory(List.of());

        return ragService.performRag(request);
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean online = checkOllamaOnline();
        status.put("status", online ? "ONLINE" : "OFFLINE");
        if (online) {
            try {
                String endpoint = ollamaBaseUrl + "/api/tags";
                Map<?, ?> response = restTemplate.getForObject(endpoint, Map.class);
                if (response != null && response.containsKey("models")) {
                    status.put("models", response.get("models"));
                } else {
                    status.put("models", List.of());
                }
            } catch (Exception e) {
                status.put("models", List.of());
            }
        } else {
            status.put("models", List.of());
        }
        return status;
    }

    private float[] parseQueryVector(String v) {
        if (v.contains(",")) {
            String[] tokens = v.split(",");
            float[] vals = new float[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                vals[i] = Float.parseFloat(tokens[i].trim());
            }
            return vals;
        }

        // Exact match
        Optional<DemoItem> match = DEMO_ITEMS.stream()
                .filter(item -> item.getConcept().equalsIgnoreCase(v))
                .findFirst();
        if (match.isPresent()) {
            return match.get().getValues();
        }

        // Seeded random fallback based on hashcode
        int seed = v.hashCode();
        java.util.Random rand = new java.util.Random(seed);
        float[] vals = new float[16];
        for (int i = 0; i < 16; i++) {
            vals[i] = rand.nextFloat() * 2 - 1;
        }
        float sumSq = 0;
        float len = 0f;
        for (float f : vals) len += f * f;
        len = (float) Math.sqrt(len);
        if (len > 0) {
            for (int i = 0; i < 16; i++) vals[i] /= len;
        }
        return vals;
    }

    private List<Map<String, Object>> executeSingleSearch(float[] queryVals, int k, String metric, String algo) {
        List<Vector> vectors = DEMO_ITEMS.stream()
                .map(item -> new Vector(item.getId(), item.getValues(), null))
                .collect(Collectors.toList());

        DistanceMetric mEnum = DistanceMetric.EUCLIDEAN;
        if ("cosine".equalsIgnoreCase(metric)) mEnum = DistanceMetric.COSINE;
        else if ("manhattan".equalsIgnoreCase(metric)) mEnum = DistanceMetric.MANHATTAN;

        DistanceCalculator calculator = DistanceCalculatorFactory.getCalculator(mEnum);

        List<SearchResult> hits;
        if ("hnsw".equalsIgnoreCase(algo)) {
            HnswIndex hnsw = new HnswIndex();
            hnsw.build(vectors, calculator);
            hits = hnsw.search(queryVals, k, calculator);
        } else if ("kdtree".equalsIgnoreCase(algo)) {
            KdTree tree = new KdTree();
            tree.build(vectors);
            hits = tree.knnSearch(queryVals, k, calculator, mEnum);
        } else {
            // Brute force
            hits = vectors.stream()
                    .map(v -> SearchResult.builder()
                            .chunkId(v.getId())
                            .score(calculator.calculate(new Vector(null, queryVals, null), v))
                            .build())
                    .sorted(Comparator.comparingDouble(SearchResult::getScore))
                    .limit(k)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchResult hit : hits) {
            DemoItem item = DEMO_ITEMS.stream()
                    .filter(i -> i.getId().equals(hit.getChunkId()))
                    .findFirst()
                    .orElse(null);
            if (item != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", item.getId());
                map.put("concept", item.getConcept());
                map.put("category", item.getCategory());
                map.put("values", item.getValues());
                map.put("distance", hit.getScore());
                results.add(map);
            }
        }
        return results;
    }

    private Workspace getOrCreateWorkspace() {
        return workspaceRepository.findAll().stream().findFirst().orElseGet(() -> {
            User owner = userRepository.findByUsername("developer").orElseGet(() ->
                userRepository.save(User.builder()
                    .username("developer")
                    .email("dev@lumora.ai")
                    .password("password")
                    .roles(Set.of(Role.ROLE_USER))
                    .build())
            );
            return workspaceRepository.save(Workspace.builder()
                    .name("Demo Workspace")
                    .description("System Default Workspace")
                    .owner(owner)
                    .build());
        });
    }

    private List<String> splitIntoOverlappingChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        String[] words = text.split("\\s+");
        int i = 0;
        while (i < words.length) {
            int end = Math.min(i + chunkSize, words.length);
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < end; j++) {
                sb.append(words[j]).append(" ");
            }
            chunks.add(sb.toString().trim());
            if (end == words.length) {
                break;
            }
            i += (chunkSize - overlap);
        }
        return chunks;
    }

    private boolean checkOllamaOnline() {
        try {
            String endpoint = ollamaBaseUrl + "/api/tags";
            restTemplate.getForObject(endpoint, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class DemoItem {
        private Long id;
        private String concept;
        private String category;
        private float[] values;

        public DemoItem() {}

        public DemoItem(Long id, String concept, String category, float[] values) {
            this.id = id;
            this.concept = concept;
            this.category = category;
            this.values = values;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getConcept() { return concept; }
        public void setConcept(String concept) { this.concept = concept; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public float[] getValues() { return values; }
        public void setValues(float[] values) { this.values = values; }
    }
}
