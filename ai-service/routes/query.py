from flask import Blueprint, request, jsonify
from services.chroma_service import ChromaService

# ✅ DEFINE blueprint correctly
query_bp = Blueprint("query", __name__)

# Initialize service
chroma = ChromaService()

@query_bp.route("/query", methods=["POST"])
def query():
    data = request.get_json()

    if not data or "question" not in data:
        return jsonify({"answer": "Invalid request"}), 400

    question = data["question"]

    try:
        docs = chroma.query(question)

        if not docs:
            return jsonify({"answer": "No relevant data found"})

        return jsonify({"answer": docs[0]})

    except Exception as e:
        return jsonify({
            "answer": "Error occurred",
            "error": str(e)
        }), 500