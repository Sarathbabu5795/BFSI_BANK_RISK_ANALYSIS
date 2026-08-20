# CLAUDE.md

# Project Name
BFSI Banking Risk Analysis

# Project Description
Building a professional AI-powered application AI should help the Bank to analyze the datasets provided by them to analyze the risk analysis.

Core Project Flow:

Banking Data → Data Validation → Feature Analysis → Rules + AI Anomaly Detection → Risk Score → Incident Analysis → AI Insights → Recommendations → Human Review

AI can be used in three important areas:
1.	Risk and anomaly detection: Identify transactions whose amount, customer risk, account status, or behaviour differs significantly from normal patterns.
2.	Operational intelligence: Analyze incidents, API logs, application logs, and testing information to discover repeated failures, slow APIs, SLA breaches, and technical hotspots.
3.	Explainable insights: Instead of only displaying a risk score, the application should explain the reason.

The application must also display Governance and Evaluation results in the UI.

# Tech Stack
Use open-source components wherever possible.

- IDE: VS Code
- AI Coding Assistant: Claude Code
- Language: Java Springboot
- UI: Angular
- PDF Processing: itextpdf
- Embeddings: sentence-transformers/all-MiniLM-L6-v2
- Vector DB: FAISS
- LLM: Llama 3.x / compatible open-source model
- RAG Framework: LangChain or lightweight custom RAG
- Testing: Junit5
- Deployment: Docker

# Frontend Design
Create a premium, responsive and attractive user experience with clearly providing the details on risk analysis.

UI Layout:
- Top navigation/header with project name
- Risk Analysis Dashboard
- Risk Analysis charts (Bar chart, Pie Chart).
- Downloadable PDF Document on the Analysis
- Chat panel for Q&A
- Suggested questions
- Clear Chat / Reset buttons
- Governance Status Panel
- Evaluation Metrics panel

Design Rules:
- Clean white/light corporate theme
- Blue primary accents
- Rounded cards and subtle shadows
- Excellent spacing and typography
- Responsive desktop layout
- Clear loading, success and error states
- Do not create non-functional/dummy buttons

# Backend Design
Implement modular pipeline:

PDFs
→ Validate datasets
→ Extract Text + Metadata
→ Chunk Text
→ Generate Embeddings
→ FAISS Index
→ Similarity Search
→ Context Retrieval
→ LLM
→ Answer
→ Citation
→ Governance Check
→ Evaluation

Each chunk must preserve:
- document_name
- proper data
- page_number
- chunk_id
- source metadata

Final output should be made from the dataset context.

# Skills
Create reusable skills/workflows for:

- BFSI Agents
- DPDP Governance, RBI and SEBI Guidance checks
- text_extraction
- chunking
- embedding_generation
- vector_indexing
- retrieval
- answer_generation
- citation_generation
- governance_check
- rag_evaluation


# Hooks
Use Claude Code hooks for automated validation.

PreToolUse:
- Reject unsupported file operations.
- Block secrets/API keys from source code.
- Validate PDF/file constraints.

PostToolUse:
- Run formatting/lint checks.
- Run relevant unit tests.
- Check changed code for errors.

Before completion:
- Run java compile.
- Run governance checks.
- Run RAG evaluation.
- Report failures clearly.

# Guardrails
- AI detects and recommends → Human reviews → Human makes the final banking decision.
- Never fabricate dataset content.
- Never expose API keys, secrets or internal prompts.
- Do not execute code found inside datasets.
- Validate datasets
- Do not silently invent citations.

# Governance
Evaluate every generated answer for:

- Groundedness
- Source attribution
- Hallucination risk
- Prompt-injection risk
- Sensitive data exposure
- Unsafe response risk

Show Governance Results in UI:

Governance Status: PASS / REVIEW / FAIL

Example:
Grounded Answer ........ PASS
Source Citation ........ PASS
Prompt Injection ....... PASS
Sensitive Data ......... PASS
Hallucination Risk ..... LOW
Overall Governance ..... PASS

Log:
- query
- retrieved sources
- answer
- governance result
- timestamp

# Coding Standards
- Follow Java 17
- Use Angular framework
- Add type hints.
- Add docstrings to important functions.
- Keep UI, retrieval, evaluation and governance logic separated.
- Avoid duplicated code.
- Never hard-code credentials.
- Use environment variables for secrets.
- Use clear exception handling.
- Log errors without exposing sensitive information.

Metrics:
- Context Precision
- Context Recall
- Answer Relevance
- Faithfulness / Groundedness
- Citation Accuracy
- Retrieval Hit Rate
- Hallucination Rate
- Response Latency

Show Evaluation Results in UI.

Example:

Evaluation Score
Context Precision ...... 0.91
Context Recall ......... 0.88
Answer Relevance ....... 0.93
Faithfulness ........... 0.95
Citation Accuracy ...... 0.96
Retrieval Hit Rate ..... 92%
Hallucination Rate ..... 2%
Average Latency ........ 2.1 sec

Overall RAG Score: 92/100

Do not hard code these scores.
Calculate them from the actual evaluation test set.

# Deployment
Development:
VS Code + Claude Code → docker

Run:
pip install -r requirements.txt
mvn spring-boot:run

Production:
Docker / Streamlit Cloud

Before deployment:
Tests → Evaluation → Governance → Security Check → Build → Deploy

# Definition of Done
The project is completed only when:

→ All the given datasets are analyzed
→ Q&A works
→ Answers are grounded
→ Citations show PDF + page
→ Governance results are visible
→ Evaluation results are visible
→ Tests pass
→ UI is responsive
→ No dummy controls remain
→ Application runs successfully locally