
# ============================================================================
# PCI-DSS Compliance Tracker — AI Service (Flask Entry Point)
# ============================================================================

from flask import Flask, jsonify
from routes.query import query_bp
from services.chroma_service import ChromaService
import time

from flask import Flask


app = Flask(__name__)

# Register route blueprints
# from routes.describe import describe_bp
# from routes.recommend import recommend_bp
# app.register_blueprint(describe_bp)
# app.register_blueprint(recommend_bp)

    # ✅ Initialize Chroma
chroma = ChromaService()

    # ✅ STORE in app config (VERY IMPORTANT)
app.config["CHROMA"] = chroma
app.config["DOCS"] = chroma.data
app.config["RESPONSE_TIMES"] = []
app.config["START_TIME"] = time.time()

    # ✅ Register blueprint
app.register_blueprint(query_bp)

    # ✅ Health API
@app.route("/health")
def health():
        times = app.config["RESPONSE_TIMES"]
        avg_time = sum(times) / len(times) if times else 0

        return jsonify({
            "status": "ok",
            "model": "simple-keyword-model",
            "doc_count": len(app.config["DOCS"]),
            "avg_response_time_last_10": round(avg_time, 4),
            "uptime_seconds": round(time.time() - app.config["START_TIME"], 2)
        })

        return app

@app.route("/health")
def health():
    return {"status": "ok"}

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
