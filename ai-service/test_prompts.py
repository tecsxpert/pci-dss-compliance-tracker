import requests

URL = "http://127.0.0.1:5000/query"

def ask(question):
    response = requests.post(URL, json={"question": question})
    return response.json().get("answer")

tests = [
    "What is PCI DSS requirement 3?",
    "How should cardholder data be stored?",
    "What is encryption in PCI DSS?",
    "Why are firewalls important in PCI DSS?",
    "What is requirement 1 in PCI DSS?",
    "How to secure payment data?",
    "What are PCI DSS compliance steps?",
    "What is sensitive authentication data?",
    "How to prevent data breaches in PCI DSS?",
    "What are best practices for card data protection?"
]

for i, q in enumerate(tests, 1):
    print(f"\n--- Test {i} ---")
    print("Question:", q)
    print("Answer:", ask(q))