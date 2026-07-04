import requests
import time
import sys

BASE_URL = "http://localhost:8080/api/v1"

def run_verification():
    print("--- 1. Registering user 'testuser' ---")
    reg_payload = {
        "username": "testuser",
        "email": "testuser@example.com",
        "password": "Password123!"
    }
    try:
        r = requests.post(f"{BASE_URL}/auth/register", json=reg_payload)
        print("Registration status:", r.status_code)
        print("Response:", r.json())
    except Exception as e:
        print("Registration failed or user already exists:", e)

    print("\n--- 2. Logging in 'testuser' ---")
    login_payload = {
        "username": "testuser",
        "password": "Password123!"
    }
    r = requests.post(f"{BASE_URL}/auth/login", json=login_payload)
    if r.status_code != 200:
        print("Login failed:", r.status_code, r.text)
        sys.exit(1)
    
    login_data = r.json()
    token = login_data["data"]["token"]
    print("Login successful! Token acquired.")

    headers = {
        "Authorization": f"Bearer {token}"
    }

    print("\n--- 3. Creating Workspace ---")
    ws_payload = {
        "name": "Lumora Workspace",
        "description": "Workspace for manual verification"
    }
    r = requests.post(f"{BASE_URL}/workspaces", json=ws_payload, headers=headers)
    if r.status_code != 200:
        print("Create workspace failed:", r.status_code, r.text)
        sys.exit(1)
    
    ws_data = r.json()
    workspace_id = ws_data["data"]["id"]
    print(f"Created Workspace ID: {workspace_id}")

    print("\n--- 4. Uploading PDF, DOCX, TXT ---")
    files_to_upload = [
        ("test.txt", "c:/Users/HP/PA-NET-SCOPE/Your-OWN-AI/scratch/test.txt"),
        ("test.docx", "c:/Users/HP/PA-NET-SCOPE/Your-OWN-AI/scratch/test.docx"),
        ("test.pdf", "c:/Users/HP/PA-NET-SCOPE/Your-OWN-AI/scratch/test.pdf")
    ]

    for title, path in files_to_upload:
        print(f"Uploading {title}...")
        with open(path, "rb") as f:
            files = {
                "file": (title, f, "application/octet-stream")
            }
            # workspaceId is passed as a query parameter (RequestParam)
            params = {
                "workspaceId": workspace_id
            }
            r = requests.post(f"{BASE_URL}/documents", files=files, params=params, headers=headers)
            if r.status_code != 200:
                print(f"Upload {title} failed:", r.status_code, r.text)
            else:
                print(f"Uploaded {title} successfully: {r.json()['message']}")

    print("\n--- 5. Polling processing status ---")
    all_processed = False
    attempts = 0
    max_attempts = 20

    while not all_processed and attempts < max_attempts:
        time.sleep(5)
        attempts += 1
        print(f"Attempt {attempts} of {max_attempts}...")
        
        r = requests.get(f"{BASE_URL}/documents", params={"workspaceId": workspace_id}, headers=headers)
        if r.status_code != 200:
            print("Failed to fetch documents:", r.status_code, r.text)
            continue
        
        docs = r.json().get("data", [])
        if not docs:
            print("No documents found in workspace.")
            continue
            
        all_processed = True
        for doc in docs:
            title = doc.get("title")
            status = doc.get("processingStatus")
            chunks = doc.get("totalChunks")
            fail_reason = doc.get("failureReason")
            print(f"  Document: {title} | Status: {status} | Chunks: {chunks} | Reason: {fail_reason}")
            if status != "READY":
                all_processed = False
                
    if all_processed:
        print("\nSUCCESS: All documents successfully processed and indexed!")
        # Let's save the workspace ID and token to a temp text file so we can reuse them in subsequent steps
        with open("c:/Users/HP/PA-NET-SCOPE/Your-OWN-AI/scratch/state.txt", "w") as sf:
            sf.write(f"{workspace_id}\n{token}")
    else:
        print("\nTIMEOUT or FAILURE: One or more documents failed to process.")

if __name__ == "__main__":
    run_verification()
