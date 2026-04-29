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

@query_bp.route("/query", methods=["POST"])
def query():
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