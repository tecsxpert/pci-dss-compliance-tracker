from flask import Blueprint, request, jsonify
from services.chroma_service import ChromaService
from services.groq_client import GroqClient

query_bp = Blueprint("query", __name__)

# ✅ Initialize services
chroma = ChromaService()
groq = GroqClient()

# ✅ ADD SEED DATA HERE (runs when route loads)
chroma.add_data("Cardholder data must be encrypted using strong cryptography", "1")
chroma.add_data("Use firewalls to protect cardholder data", "2")

from flask import Blueprint, request, jsonify, current_app
import time

query_bp = Blueprint("query", __name__)

@query_bp.route("/query", methods=["POST"])
def query():
    start = time.time()   # ⏱ start timer

    data = request.get_json()
    question = data.get("question")

    if not question:
        return jsonify({"error": "Question is required"}), 400

    # 🔍 Retrieve documents
    docs = chroma.query(question)

    # ✅ Handle empty DB case
    if not docs:
        return jsonify({
            "answer": "No relevant data found",
            "sources": []
        })

    # 🧠 Build context
    context = "\n".join(docs)

    # 🤖 Generate answer
    answer = groq.generate(question, context)

    return jsonify({
        "answer": answer,
        "sources": docs
    })
    question = data.get("question", "")

    chroma = current_app.config.get("CHROMA")

    docs = chroma.query(question)

    if not docs:
        answer = "No relevant data found"
    else:
        answer = docs[0]

    end = time.time()   # ⏱ end timer
    duration = end - start

    # ✅ Store response times
    times = current_app.config.get("RESPONSE_TIMES")
    times.append(duration)

    # Keep only last 10
    if len(times) > 10:
        times.pop(0)

    return jsonify({"answer": answer})

