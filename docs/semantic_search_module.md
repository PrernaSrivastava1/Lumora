# Semantic Search Module Documentation

This document describes the architecture, design patterns, search execution flow, and extensibility of the Lumora Semantic Search Module.

## Design Patterns

### Why the Strategy Pattern was Chosen
The **Strategy Pattern** was selected as the primary architectural pattern to decouple the similarity search REST controller and coordinate service from the underlying vector search algorithms.
- **Interchangeability**: It allows the application to switch between `BRUTE_FORCE`, `KD_TREE`, and `HNSW` algorithms dynamically at runtime depending on the client query parameters.
- **Open/Closed Principle**: We can introduce new search strategies (e.g., product quantization, IVF-Flat, SCaNN) without modifying the orchestrating `SearchEngine` or controller classes.
- **Separation of Concerns**: Each algorithm's indexing logic and query routines are isolated inside dedicated classes, facilitating independent unit testing.

## Search Execution Flow

```
   HTTP POST Request (/api/search)
                │
                ▼
      SearchController (Validates payload annotations)
                │
                ▼
       SearchEngine (Captures metrics, counts vectors)
                │
                ▼
   SearchContext (Performs AUTO algorithm resolution)
                │
                ▼
  Selected Strategy (e.g. HNSWSearchStrategy)
   ├─ Converts natural text to embedding if needed
   ├─ Greedily searches HNSW levels/graph nodes
   └─ Joins matching vector IDs with ChunkRepository text
                │
                ▼
   SearchResponse (Populates latency, dimensions, & hits)
```

## Adding New Algorithms

To integrate a new vector search algorithm into Lumora:

1. **Implement the Strategy**:
   Create a new strategy class extending `AbstractSearchStrategy` and override its core hooks:
   ```java
   @Component
   public class IvfFlatSearchStrategy extends AbstractSearchStrategy {
       @Override
       public AlgorithmType getAlgorithmType() {
           return AlgorithmType.IVF_FLAT; // Register in AlgorithmType enum
       }

       @Override
       protected List<SearchResult> doSearch(SearchRequest request) {
           // Implement clustering/index search logic here
       }
   }
   ```

2. **Add to Enum**:
   Append the new algorithm name to the `AlgorithmType` enum in `com.lumora.model.AlgorithmType`.

3. **Automatic Registration**:
   Spring's dependency injection automatically scans the new strategy `@Component` and registers it in `StrategyConfiguration`, injecting it into the `SearchContext` map with zero manual configuration.
