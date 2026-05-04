from services.chroma_service import ChromaService

print("Starting seed...")

db = ChromaService()

print("Adding data...")

db.add_data("PCI DSS Requirement 1 install firewall to protect cardholder data", "req1")

db.add_data("PCI DSS Requirement 3 protect stored cardholder data using encryption", "req3")

db.add_data("PCI DSS compliance steps include firewall encryption access control and monitoring", "steps1")

db.add_data("Sensitive authentication data includes CVV PIN and track data and must not be stored", "sad1")

db.add_data("Cardholder data storage must use encryption and secure access control", "storage1")

print("✅ Data inserted successfully!")