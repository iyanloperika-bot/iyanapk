# JULIAN // OMNI-CORE v9.0 — Firestore Security Specification

## 1. Data Invariants
1. **Authentication Mandate**: All reads and writes to `/users/{userId}/bookmarks/{toolId}` and `/users/{userId}/settings/{configId}` require an authenticated Google Sign-In session (`request.auth != null`).
2. **Strict Tenant Isolation**: A user can only read, list, create, update, or delete documents under their own `/users/{userId}` path (`request.auth.uid == userId`), and the document payload's `userId` field must equal `request.auth.uid`.
3. **Immutable Metadata**: On updates, `userId`, `toolId` (for bookmarks), and `createdAt` must remain strictly equal to `resource.data`.
4. **Temporal Bounds**: `createdAt` and `updatedAt` must be valid Firestore `timestamp` objects and `<= request.time`.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Read**: `GET /users/alice/bookmarks/ai_chat` with `auth == null` -> REJECT.
2. **Cross-User Read**: `GET /users/bob/bookmarks/ai_chat` as `alice` -> REJECT.
3. **Cross-User List**: `LIST /users/bob/bookmarks` as `alice` -> REJECT.
4. **Identity Spoofing on Create**: `CREATE /users/alice/bookmarks/ai_chat` with `{ userId: "bob", ... }` as `alice` -> REJECT.
5. **Shadow Ghost Field Injection**: `CREATE /users/alice/bookmarks/ai_chat` with `{ ..., isAdmin: true }` -> REJECT via `.keys().hasOnly(...)`.
6. **Oversized String DoS**: `CREATE /users/alice/bookmarks/ai_chat` with `note` of 5,000 characters -> REJECT via `note.size() <= 300`.
7. **Invalid Category Enum**: `CREATE /users/alice/bookmarks/ai_chat` with `category: "HACKED"` -> REJECT via enum check.
8. **Invalid Execution Mode Enum**: `CREATE /users/alice/settings/apiConfig` with `executionMode: "ROOT"` -> REJECT via enum check.
9. **Immutable `createdAt` Tampering**: `UPDATE /users/alice/bookmarks/ai_chat` changing `createdAt` -> REJECT.
10. **Immutable `userId` Hijack**: `UPDATE /users/alice/settings/apiConfig` changing `userId` to `"bob"` -> REJECT.
11. **Malformed Document ID Poisoning**: Document ID exceeding 128 chars or containing invalid characters -> REJECT via `isValidId()`.
12. **Type Confusion on `enableHighThinking`**: Passing string `"true"` instead of boolean -> REJECT via `is bool`.
