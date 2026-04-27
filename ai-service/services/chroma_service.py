import chromadb
from chromadb.utils import embedding_functions

class ChromaService:
    def __init__(self):
        print("Initializing simple in-memory DB...")

        self.data = []

        print("DB ready!")

    def add_data(self, text, doc_id):
        print(f"Adding: {doc_id}")
        self.data.append(text)
        print(f"Added: {doc_id}")

    def query(self, question):
        # Simple keyword match (temporary)
        results = []

        for doc in self.data:
            if any(word.lower() in doc.lower() for word in question.split()):
                results.append(doc)

        return results[:2]
