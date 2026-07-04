import requests
import sys

BASE_URL = "http://localhost:8080/api/v1"

# Read workspace_id and token
try:
    with open("c:/Users/HP/PA-NET-SCOPE/Your-OWN-AI/scratch/state.txt", "r") as sf:
        lines = sf.read().splitlines()
        workspace_id = int(lines[0])
        token = lines[1]
except Exception as e:
    print("Failed to read state.txt:", e)
    sys.exit(1)

headers = {
    "Authorization": f"Bearer {token}"
}

print(f"Loaded Workspace ID: {workspace_id}")

# Step 6: Similarity Search Verification
print("\n--- Step 6: Similarity Search ---")
query_text = "What is Natural Language Processing?"
algorithms = ["BRUTE_FORCE", "KD_TREE", "HNSW", "AUTO"]

for algo in algorithms:
    print(f"\nExecuting search with algorithm: {algo} ...")
    payload = {
        "query": query_text,
        "algorithm": algo,
        "metric": "COSINE",
        "topK": 3,
        "workspaceId": workspace_id
    }
    r = requests.post(f"{BASE_URL}/search", json=payload, headers=headers)
    if r.status_code != 200:
        print(f"Search failed for {algo}:", r.status_code, r.text)
        continue
    
    data = r.json().get("data", {})
    results = data.get("results", [])
    print(f"Results count: {len(results)}")
    for idx, match in enumerate(results):
        score = match.get("score")
        text = match.get("matchedText") or ""
        explanation = match.get("explanation") or ""
        print(f"  Match {idx+1}: Score: {score} | Explanation: {explanation} | Content snippet: {text[:80]}...")

# Step 7: RAG/Chat Verification
print("\n--- Step 7: RAG / Chat ---")
rag_payload = {
    "query": "Explain what Natural Language Processing is, based on the uploaded documents.",
    "workspaceId": workspace_id,
    "algorithm": "AUTO",
    "topK": 3,
    "llmModel": "llama3"
}
print("Executing RAG Chat Prompt ...")
r = requests.post(f"{BASE_URL}/rag/chat", json=rag_payload, headers=headers)
if r.status_code != 200:
    print("RAG/Chat failed:", r.status_code, r.text)
    sys.exit(1)

rag_data = r.json().get("data", {})
answer = rag_data.get("answer") or rag_data.get("response")
sources = rag_data.get("sources") or rag_data.get("citations", [])

print("\nOllama RAG Response:")
print(answer)

print("\nSources/Citations:")
for idx, src in enumerate(sources):
    # Depending on schema, it might be dict or string
    print(f"  Source {idx+1}: {src}")

# Step 8: Analytics Verification
print("\n--- Step 8: Analytics & Benchmarks ---")
# Get analytics summary
print("Retrieving analytics summary...")
r = requests.get(f"{BASE_URL}/analytics/summary", headers=headers)
if r.status_code == 200:
    summary = r.json().get("data", {})
    print("Analytics Summary:")
    print(f"  Total searches: {summary.get('totalSearches')}")
    print(f"  Successful searches: {summary.get('successfulSearches')}")
    print(f"  Average latency: {summary.get('averageLatencyMs')} ms")
    print(f"  Success rate: {summary.get('successRate')}")
else:
    # Let's try base URL /analytics/summary (in case it is not under /api/v1)
    # The controllerRequestMapping was empty so it could be at http://localhost:8080/analytics/summary
    r2 = requests.get("http://localhost:8080/analytics/summary", headers=headers)
    if r2.status_code == 200:
        summary = r2.json().get("data", {})
        print("Analytics Summary (via root):")
        print(f"  Total searches: {summary.get('totalSearches')}")
        print(f"  Successful searches: {summary.get('successfulSearches')}")
        print(f"  Average latency: {summary.get('averageLatencyMs')} ms")
    else:
        print("Failed to load analytics summary:", r.status_code, r.text)

# Run benchmark
print("\nRunning on-demand benchmark comparison...")
r = requests.get(f"http://localhost:8080/benchmark", params={"q": "Natural Language Processing", "workspaceId": workspace_id}, headers=headers)
if r.status_code == 200:
    benchmarks = r.json().get("data", [])
    print(f"Benchmark results count: {len(benchmarks)}")
    for b in benchmarks:
        print(f"  Algorithm: {b.get('algorithm')} | Avg Latency: {b.get('averageLatency')} ms | Executions: {b.get('totalExecutions')} | Success Rate: {b.get('successRate')}")
else:
    print("Failed to run benchmark:", r.status_code, r.text)

