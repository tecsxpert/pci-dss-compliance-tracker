import requests
import json

BASE_URL = "http://localhost:5000"

DEMO_RECORDS = [
    "Weak firewall rules allow unauthorized access",
    "Default passwords are still enabled",
    "Cardholder data is stored without encryption",
    "Logs are not monitored regularly",
    "Antivirus software is outdated",
    "Multi-factor authentication is missing",
    "Physical server room is unlocked",
    "Employees share admin accounts",
    "Security patches are delayed",
    "No incident response plan exists",
    "Sensitive data exposed in logs",
    "Improper access control configuration",
    "Firewall rules are overly permissive",
    "Database backups are not encrypted",
    "Unused ports remain open",
    "No vulnerability scans are performed",
    "Weak password policy implemented",
    "Audit logs are deleted too early",
    "Third-party vendors lack compliance checks",
    "Remote access is unsecured",
    "Network segmentation is missing",
    "Expired SSL certificates detected",
    "No employee security training",
    "Critical systems lack monitoring",
    "Sensitive files shared publicly",
    "Admin privileges granted unnecessarily",
    "Unsecured APIs expose customer data",
    "No disaster recovery testing",
    "PCI audit documentation incomplete",
    "Wireless networks use weak encryption"
]

ENDPOINTS = [
    ("/describe", "input"),
    ("/categorise", "input"),
    ("/recommend", "input"),
    ("/generate-report", "input"),
]


def test_endpoint(endpoint, field_name):
    print(f"\n===== TESTING {endpoint} =====\n")

    passed = 0
    failed = 0

    for i, record in enumerate(DEMO_RECORDS, start=1):
        payload = {field_name: record}

        try:
            response = requests.post(
                BASE_URL + endpoint,
                json=payload,
                timeout=20
            )

            data = response.json()

            print(f"{i}. Status: {response.status_code}")

            if response.status_code in [200, 202]:
                passed += 1
            else:
                failed += 1

            print(json.dumps(data, indent=2)[:500])
            print("-" * 60)

        except Exception as e:
            failed += 1
            print(f"{i}. ERROR: {str(e)}")

    print(f"\nRESULTS FOR {endpoint}")
    print(f"Passed: {passed}")
    print(f"Failed: {failed}")


def main():
    for endpoint, field_name in ENDPOINTS:
        test_endpoint(endpoint, field_name)


if __name__ == "__main__":
    main()