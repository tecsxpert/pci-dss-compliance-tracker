import os
from groq import Groq

class GroqClient:
    def __init__(self):
        api_key = os.getenv("GROQ_API_KEY")

        if not api_key:
            raise ValueError("GROQ_API_KEY is not set")

        self.client = Groq(api_key=api_key)

    def generate(self, question, context):
        prompt = f"""
You are a PCI DSS expert.

Context:
{context}

Question:
{question}

Answer clearly:
"""

        response = self.client.chat.completions.create(
    model="llama-3.1-8b-instant",   # ✅ UPDATED MODEL
    messages=[
        {"role": "user", "content": prompt}
    ]
)

        return response.choices[0].message.content