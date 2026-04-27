from flask import Flask
from routes.categorize import categorize_bp
from routes.query import query_bp

app = Flask(__name__)

app.register_blueprint(categorize_bp)
app.register_blueprint(query_bp)

@app.route("/")
def home():
    return "PCI DSS AI API running"

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=True)