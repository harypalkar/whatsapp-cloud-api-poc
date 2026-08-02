# MASTER-05 — RAG & Knowledge Base

## Pipeline

```
Upload (PDF/DOC/XLS/TXT) or Crawl URL
  → Extract text
  → Chunk (size + overlap)
  → Embed (AIProvider.embed)
  → Store vectors (pgvector / JSON fallback)
  → Semantic search + citations
  → Grounded answer
```

## Tables

- `knowledge_bases`
- `knowledge_documents`
- `knowledge_chunks` (`embedding vector` or `embedding_json`)
- `knowledge_crawl_jobs`
- `rag_query_logs`

## Citations

Every answer returns `{ answer, citations[{ documentId, chunkId, snippet, score }] }`.
