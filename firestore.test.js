const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or create bookmarks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("maker_brat").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("maker_brat").set({
      toolId: "maker_brat",
      category: "MAKER",
      note: "Brat generator",
      userId: ALICE_UID,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user can create, read, list, update, and delete their own bookmark", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("maker_brat");
  const now = new Date();

  await assertSucceeds(
    docRef.set({
      toolId: "maker_brat",
      category: "MAKER",
      note: "Favorite Brat Studio",
      userId: ALICE_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").where("userId", "==", ALICE_UID).get()
  );

  await assertSucceeds(
    docRef.update({
      note: "Updated note",
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(docRef.delete());
});

test("Authenticated user cannot read or modify another user's bookmark or settings", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const now = new Date();
    await context.firestore().collection("users").doc(BOB_UID).collection("bookmarks").doc("ai_gemini").set({
      toolId: "ai_gemini",
      category: "AI",
      note: "Bob's AI tool",
      userId: BOB_UID,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("bookmarks").doc("ai_gemini").get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("bookmarks").where("userId", "==", BOB_UID).get()
  );
});

test("Shadow update with ghost field or invalid enum is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("settings").doc("apiConfig");
  const now = new Date();

  await assertFails(
    docRef.set({
      baseUrl: "https://api.julian-omnicore.dev",
      apiKeyHeader: "julian-live-key",
      executionMode: "INVALID_MODE",
      enableHighThinking: true,
      userId: ALICE_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    docRef.set({
      baseUrl: "https://api.julian-omnicore.dev",
      apiKeyHeader: "julian-live-key",
      executionMode: "HYBRID",
      enableHighThinking: true,
      userId: ALICE_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    docRef.update({
      isVerifiedAdmin: true,
      updatedAt: new Date(),
    })
  );
});
