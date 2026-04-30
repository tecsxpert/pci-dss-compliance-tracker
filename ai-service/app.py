from flask import Flask, jsonify
from routes.query import query_bp

def create_app():
    app = Flask(__name__)

    # Register blueprints
    app.register_blueprint(query_bp)

    # Health check route
    @app.route("/", methods=["GET"])
    def home():
        return jsonify({
            "status": "running",
            "message": "PCI DSS AI Service is up"
        })

    return app


if __name__ == "__main__":
    app = create_app()
    app.run(debug=True)