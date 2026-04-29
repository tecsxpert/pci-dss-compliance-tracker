import chromadb
from sentence_transformers import SentenceTransformer

# Load embedding model
model = SentenceTransformer('all-MiniLM-L6-v2')

# Create ChromaDB client
client = chromadb.PersistentClient(path="./chroma_db")

# Create collection
collection = client.get_or_create_collection(name="pci_dss")


def add_documents(docs):
    embeddings = model.encode(docs).tolist()

    collection.add(
        documents=docs,
        embeddings=embeddings,
        ids=[str(i) for i in range(len(docs))]
    )


def query_chroma(question):

    embedding = model.encode([question]).tolist()

    results = collection.query(
        query_embeddings=embedding,
        n_results=3
    )

    return results