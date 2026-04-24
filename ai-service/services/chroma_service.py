import chromadb
from chromadb.config import Settings

class ChromaService:
    def __init__(self):
        self.client = chromadb.Client(
            Settings(persist_directory="./chroma_db")
        )

        self.collection = self.client.get_or_create_collection(
            name="pci_compliance_new",
            embedding_function=None  # 🔥 disables ONNX completely
        )

    def add_data(self, text, doc_id):
        self.collection.add(
            documents=[text],
            ids=[doc_id],
            embeddings=[[0.1, 0.2, 0.3]]  # manual embedding
        )

    def query(self):
        return self.collection.query(
            query_embeddings=[[0.1, 0.2, 0.3]],
            n_results=1
        )