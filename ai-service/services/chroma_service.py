class ChromaService:
    def __init__(self):
        self.data = {}

        # ✅ ADD DATA HERE
        self.add_data("PCI DSS Requirement 3 protects stored cardholder data using encryption", "req3")
        self.add_data("PCI DSS Requirement 1 is about firewalls and network security", "req1")
        self.add_data("Sensitive authentication data includes CVV, PIN, and full track data", "sad1")
        self.add_data("PCI DSS compliance steps include securing network, encrypting data, and monitoring access", "steps1")
        self.add_data("Cardholder data should be stored securely using encryption and access control", "storage1")

    def add_data(self, text, doc_id):
        self.data[doc_id] = text.lower()

    def query(self, question):
        question = question.lower()

    # ✅ Direct intent mapping (VERY IMPORTANT)
        if "requirement 1" in question:
            return [self.data["req1"]]

        if "requirement 3" in question:
            return [self.data["req3"]]
 
        if "firewall" in question:
            return [self.data["req1"]]

        if "authentication" in question or "cvv" in question or "pin" in question:
            return [self.data["sad1"]]

        if "steps" in question or "compliance" in question:
            return [self.data["steps1"]]

        if "store" in question or "storage" in question:
            return [self.data["storage1"]]

        if "secure payment" in question or "prevent breach" in question:
            return [self.data["steps1"]]

        if "encryption" in question:
            return [self.data["req3"]]

    # ✅ fallback scoring (if nothing matches)
        stopwords = {
            "what", "is", "the", "in", "of", "how", "to",
            "are", "why", "a", "an", "dss", "pci"
        }

        question_words = [w for w in question.split() if w not in stopwords]

        best_score = 0
        best_doc = None

        for doc_id, text in self.data.items():
            score = sum(1 for word in question_words if word in text)

            if score > best_score:
                best_score = score
                best_doc = text

        if best_score == 0:
            return []

        return [best_doc]