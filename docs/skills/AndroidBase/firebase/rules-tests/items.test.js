import { before, after, beforeEach, test } from "node:test";
import { readFileSync } from "node:fs";
import { initializeTestEnvironment, assertFails, assertSucceeds } from "@firebase/rules-unit-testing";
import { doc, getDoc, setDoc } from "firebase/firestore";

let environment;
before(async () => {
  environment = await initializeTestEnvironment({
    projectId: "demo-android-base",
    firestore: { rules: readFileSync(new URL("../firestore.rules", import.meta.url), "utf8") },
  });
});
beforeEach(async () => { await environment.clearFirestore(); });
after(async () => { await environment?.cleanup(); });

test("owner can create and read an item", async () => {
  const database = environment.authenticatedContext("alice").firestore();
  const target = doc(database, "users/alice/items/first");
  await assertSucceeds(setDoc(target, { title: "First" }));
  await assertSucceeds(getDoc(target));
});

test("another user and anonymous sessions cannot access the item", async () => {
  for (const context of [environment.authenticatedContext("bob"), environment.unauthenticatedContext()]) {
    const target = doc(context.firestore(), "users/alice/items/first");
    await assertFails(getDoc(target));
    await assertFails(setDoc(target, { title: "Other" }));
  }
});

test("rejects unexpected fields, missing title and invalid values", async () => {
  const database = environment.authenticatedContext("alice").firestore();
  for (const payload of [{}, { title: "" }, { title: 7 }, { title: "x".repeat(121) }, { title: "Valid", admin: true }]) {
    await assertFails(setDoc(doc(database, "users/alice/items/first"), payload));
  }
});
